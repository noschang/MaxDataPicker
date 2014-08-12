package br.lusoft.max.data.picker.processing;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import br.lusoft.max.data.picker.model.Client;

final class DataManager
{
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("'data_'MM_dd_yyyy'.xml'");
	private final File workDirectory;

	public DataManager(final File workDirectory)
	{
		this.workDirectory = workDirectory;
	}

	public void saveToNewDataFile(final List<Client> data) throws JAXBException, IOException
	{
		final JAXBContext context = JAXBContext.newInstance(XmlData.class);
		final Marshaller marshaller = context.createMarshaller();

		final File dataFile = new File(workDirectory, dateFormat.format(new Date()));
		final XmlData xmlData = new XmlData();

		xmlData.setData(data);

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(xmlData, dataFile);
	}

	public List<Client> loadFromLastDataFile() throws JAXBException, ParseException
	{
		final File dataFile = getLastDataFile();

		if (dataFile != null)
		{
			final JAXBContext context = JAXBContext.newInstance(XmlData.class);
			final Unmarshaller unmarshaller = context.createUnmarshaller();

			return ((XmlData) unmarshaller.unmarshal(dataFile)).getData();
		}

		return new ArrayList<>();
	}

	private File getLastDataFile() throws ParseException
	{
		final FileFilter xmlFilter = new ExtensionFileFilter(".xml");
		final File[] files = workDirectory.listFiles(xmlFilter);

		File lastFile = null;
		Calendar lastDate = Calendar.getInstance();

		lastDate.set(Calendar.DAY_OF_MONTH, 1);
		lastDate.set(Calendar.MONTH, 0);
		lastDate.set(Calendar.YEAR, 1900);

		if (files != null && files.length > 0)
		{
			for (File file : files)
			{
				if (isDataFile(file))
				{
					Calendar fileDate = Calendar.getInstance();
					fileDate.setTime(dateFormat.parse(file.getName()));

					if (fileDate.after(lastDate))
					{
						lastDate = fileDate;
						lastFile = file;
					}
				}
			}
		}

		return lastFile;
	}

	public static boolean isDataFile(final File file)
	{
		return file.getName().toLowerCase().contains("data") && file.getName().toLowerCase().endsWith(".xml");
	}
}
