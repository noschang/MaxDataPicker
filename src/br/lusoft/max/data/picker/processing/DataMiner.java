package br.lusoft.max.data.picker.processing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import br.lusoft.max.data.picker.model.Client;
import br.lusoft.max.data.picker.model.PhoneNumber;
import br.lusoft.max.data.picker.model.Property;
import br.lusoft.max.data.picker.model.PropertyType;
import br.lusoft.max.data.picker.util.FileManager;

final class DataMiner
{
	private final File workDirectory;
	private final List<DataMinerListener> listeners;
	private final List<Integer> invalidIds;

	private List<Client> data;
	private int updatesCount = 0;
	private boolean running = false;
	private boolean canceled = false;

	public DataMiner(final File workDirectory)
	{
		this.workDirectory = workDirectory;
		this.listeners = new ArrayList<>();
		this.invalidIds = loadInvalidIds();
	}

	private List<Client> loadExistingData()
	{
		try
		{
			final DataManager dataManager = new DataManager(workDirectory);
			final List<Client> data = dataManager.loadFromLastDataFile();

			return data;
		}
		catch (JAXBException | ParseException exception)
		{
			System.err.println("Ignoring exception: ");
			System.err.println();
			exception.printStackTrace(System.err);

			return new ArrayList<>();
		}
	}

	public void mine() throws IOException
	{
		if (!isRunning())
		{
			setCanceled(false);
			setRunning(true);
			fireMiningStarted();

			this.data = mineFiles();

			setRunning(false);
			fireMiningFinished();
		}
	}

	private List<Client> mineFiles() throws IOException
	{
		final List<Client> clients = loadExistingData();
		final int previousSize = clients.size();

		final FileFilter htmlFilter = new ExtensionFileFilter(".html");
		final File[] dealFiles = workDirectory.listFiles(htmlFilter);

		int current = 1;

		for (File dealFile : dealFiles)
		{
			if (!isCanceled())
			{
				fireFileMiningStarted(current, dealFiles.length);

				final String fileContent = FileManager.loadFile(dealFile);
				final Document document = Jsoup.parse(fileContent);

				if (isValidDeal(document))
				{
					Client client = mineDeal(getDealId(dealFile), document);
					clients.add(client);
				}
				else
				{
					invalidIds.add(extractIdFromFileName(dealFile));
				}

				fireFileMiningFinished(current, dealFiles.length);
				current = current + 1;
			}
		}

		this.updatesCount = clients.size() - previousSize;

		saveInvalidIds();
		cleanUpWorkingDirectory();

		return clients;
	}

	public int getUpdatesCount()
	{
		return updatesCount;
	}

	private Integer extractIdFromFileName(final File dealFile)
	{
		final Matcher matcher = Pattern.compile("[0-9]+").matcher(dealFile.getName());

		if (matcher.find())
		{
			return Integer.parseInt(matcher.group());
		}

		return 0;
	}

	private void cleanUpWorkingDirectory()
	{
		final FileFilter htmlFilter = new ExtensionFileFilter(".html");

		for (File file : workDirectory.listFiles(htmlFilter))
		{
			if (!DataDownloader.isDealListFile(file) && !DataManager.isDataFile(file))
			{
				file.delete();
			}
		}
	}

	public List<Integer> loadInvalidIds()
	{
		final List<Integer> ids = new ArrayList<>();
		final File invalidIdsFile = new File(workDirectory, "invalid.ids");

		if (invalidIdsFile.exists())
		{
			String line;
			BufferedReader reader;

			try
			{
				reader = new BufferedReader(new FileReader(invalidIdsFile));

				while ((line = reader.readLine()) != null)
				{
					if (line.trim().length() > 0)
					{
						ids.add(Integer.parseInt(line));
					}
				}
			}
			catch (IOException exception)
			{
				System.err.println("Ignoring exception: ");
				System.err.println();
				exception.printStackTrace(System.err);
			}
		}

		return ids;
	}

	private void saveInvalidIds() throws IOException
	{
		final File invalidIdsFile = new File(workDirectory, "invalid.ids");
		final BufferedWriter writer = new BufferedWriter(new FileWriter(invalidIdsFile));

		for (Integer id : invalidIds)
		{
			writer.write(id.toString());
			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}

	public synchronized boolean isRunning()
	{
		return this.running;
	}

	private synchronized void setRunning(final boolean running)
	{
		this.running = running;
	}

	private synchronized boolean isCanceled()
	{
		return this.canceled;
	}

	private synchronized void setCanceled(final boolean canceled)
	{
		this.canceled = canceled;
	}

	public void cancel()
	{
		if (!isCanceled())
		{
			while (isRunning())
			{
				setCanceled(true);
			}
		}
	}

	public List<Client> getData()
	{
		return data;
	}

	private int getDealId(final File dealFile)
	{
		return Integer.parseInt(dealFile.getName().replace("deal-", "").replace(".html", ""));
	}

	private Client mineDeal(final int dealId, final Document document) throws IOException
	{
		final Client client = new Client();
		final Property interestProperty = new Property();
		final List<PhoneNumber> phoneNumbers = new ArrayList<>();

		Elements elements = document.select("td[class=cad]");
		ListIterator<Element> iterator = elements.listIterator();

		client.setDealId(dealId);
		client.setName(iterator.next().text().trim());
		client.setPartnerName(iterator.next().text().trim());

		interestProperty.setType(PropertyType.byName(iterator.next().text().trim()));

		iterator.next(); // Consume unwanted data
		iterator.next(); // Consume unwanted data

		phoneNumbers.addAll(extractPhoneNumbers(iterator.next().text().trim(), PhoneNumber.Type.HOME));
		phoneNumbers.addAll(extractPhoneNumbers(iterator.next().text().trim(), PhoneNumber.Type.BUSINESS));
		phoneNumbers.addAll(extractPhoneNumbers(iterator.next().text().trim(), PhoneNumber.Type.CELL));
		phoneNumbers.addAll(extractPhoneNumbers(iterator.next().text().trim(), PhoneNumber.Type.OTHER));

		client.setEmail(iterator.next().text().trim());
		client.setEnterprise(iterator.next().text().trim());
		client.setJobFunction(iterator.next().text().trim());

		iterator.next(); // Consume unwanted data

		interestProperty.setValue(extractValue(iterator.next().text().trim()));
		client.setRemark(iterator.next().text().trim());

		iterator.next(); // Consume unwanted data

		interestProperty.setDescription(iterator.next().text().trim());
		client.setInterestProperty(interestProperty);
		client.setPhoneNumbers(phoneNumbers);

		return client;
	}

	private boolean isValidDeal(final Document document) throws IOException
	{
		final Elements elements = document.select("p[class=erro]");
		final ListIterator<Element> iterator = elements.listIterator();

		return !iterator.hasNext();
	}

	private double extractValue(String text)
	{
		if (text.length() > 0)
		{
			text = text.replace("R$", "");
			text = text.replace(".", "");
			text = text.replace(",", ".");

			return Double.parseDouble(text);
		}

		return 0.0;
	}

	private List<PhoneNumber> extractPhoneNumbers(String text, final PhoneNumber.Type type)
	{
		final List<PhoneNumber> phoneNumbers = new ArrayList<>();

		text = text.replace("(", "");
		text = text.replace(")", "");
		text = text.replace("-", "");
		text = text.replace(" ", "");
		text = text.replace(".", "");
		text = text.trim();

		final Matcher matcher = Pattern.compile("[0-9]{7,20}").matcher(text);

		while (matcher.find())
		{
			final PhoneNumber phoneNumber = new PhoneNumber();

			phoneNumber.setNumber(matcher.group());
			phoneNumber.setType(type);

			phoneNumbers.add(phoneNumber);
		}

		return phoneNumbers;
	}

	public void addListener(final DataMinerListener listener)
	{
		if (!listeners.contains(listener))
		{
			listeners.add(listener);
		}
	}

	public void removeListener(final DataMinerListener listener)
	{
		if (listeners.contains(listener))
		{
			listeners.remove(listener);
		}
	}

	private void fireMiningStarted()
	{
		for (DataMinerListener listener : listeners)
		{
			listener.miningStarted();
		}
	}

	private void fireMiningFinished()
	{
		for (DataMinerListener listener : listeners)
		{
			listener.miningFinished();
		}
	}

	private void fireFileMiningStarted(final int current, final int total)
	{
		for (DataMinerListener listener : listeners)
		{
			listener.fileMiningStarted(current, total);
		}
	}

	private void fireFileMiningFinished(final int current, final int total)
	{
		for (DataMinerListener listener : listeners)
		{
			listener.fileMiningFinished(current, total);
		}
	}
}
