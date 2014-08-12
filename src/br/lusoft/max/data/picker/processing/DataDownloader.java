package br.lusoft.max.data.picker.processing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBException;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import br.lusoft.max.data.picker.model.Client;
import br.lusoft.max.data.picker.model.Realtor;
import br.lusoft.max.data.picker.util.FileManager;

final class DataDownloader
{
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36";
	private static final String DEAL_FILE_NAME_FORMAT = "deal-%d.html";
	private static final String DEALS_LIST_FILE_NAME = "deals.html";

	private final List<DataDownloaderListener> listeners;
	private final List<Integer> alreadyProcessedIds;

	private final File dealsListFile;
	private final HttpClient client;

	private final Realtor realtor;
	private final File workDirectory;

	private boolean running = false;
	private boolean canceled = false;

	public DataDownloader(final Realtor realtor, final File workDirectory)
	{
		this.realtor = realtor;
		this.workDirectory = workDirectory;
		this.dealsListFile = new File(workDirectory, DEALS_LIST_FILE_NAME);
		this.listeners = new ArrayList<>();
		this.client = HttpClientBuilder.create().build();
		this.alreadyProcessedIds = loadAlreadyProcessedIds();
	}

	public void download() throws IOException, URISyntaxException, ServerLoginException
	{
		if (!isRunning())
		{
			setCanceled(false);
			setRunning(true);

			fireDownloadStarted();
			prepareWorkDirectory();
			loginIntoServer();
			downloadDealsList();
			downloadDealsFiles();
			fireDownloadFinished();

			setRunning(false);
		}
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

	private void downloadDealsFiles() throws IOException, URISyntaxException
	{
		if (!isCanceled())
		{
			final List<Integer> invalidIds = new DataMiner(workDirectory).loadInvalidIds();
			final List<Integer> dealsIds = extractIdsFromDealsListFile();

			dealsIds.removeAll(alreadyProcessedIds);
			dealsIds.removeAll(invalidIds);

			int current = 1;
			int total = dealsIds.size();

			for (int dealId : dealsIds)
			{
				if (!isCanceled())
				{
					fireFileDownloadStarted(current, total);

					downloadDealFile(dealId);
					current = current + 1;

					fireFileDownloadFinished(current, total);
				}
			}
		}
	}

	private void downloadDealFile(final int dealId) throws IOException, URISyntaxException
	{
		final File dealFile = new File(workDirectory, String.format(DEAL_FILE_NAME_FORMAT, dealId));
		final URI uri = new URI(String.format("https://ssl1358.websiteseguro.com/maximobiliaria1/intranet/negociacoes/view.asp?id=%d", dealId));
		final HttpGet get = new HttpGet(uri);

		get.addHeader("User-Agent", USER_AGENT);

		final HttpResponse response = client.execute(get);
		final InputStream data = response.getEntity().getContent();

		FileManager.saveDataToFile(data, dealFile);
	}

	private List<Integer> extractIdsFromDealsListFile() throws IOException
	{
		final List<Integer> dealsIds = new ArrayList<>();
		final String fileContent = FileManager.loadFile(dealsListFile);
		final Matcher matcher = Pattern.compile("view.asp\\?id=([0-9]+)").matcher(fileContent);

		while (matcher.find())
		{
			Integer id = Integer.parseInt(matcher.group(1));

			if (id != 0 && !dealsIds.contains(id))
			{
				dealsIds.add(id);
			}
		}

		dealsListFile.delete();

		return dealsIds;
	}

	private void downloadDealsList() throws IOException, URISyntaxException
	{
		if (!isCanceled())
		{
			final URI uri = new URI("https://ssl1358.websiteseguro.com/maximobiliaria1/intranet/negociacoes/default.asp");
			final HttpPost post = new HttpPost(uri);
			final List<NameValuePair> parameters = new ArrayList<>();

			parameters.add(new BasicNameValuePair("nm", ""));
			parameters.add(new BasicNameValuePair("fone", ""));
			parameters.add(new BasicNameValuePair("mail", ""));
			parameters.add(new BasicNameValuePair("cor", "100"));
			parameters.add(new BasicNameValuePair("imovel", ""));
			parameters.add(new BasicNameValuePair("ativo", "T"));
			parameters.add(new BasicNameValuePair("ordem", "N"));
			parameters.add(new BasicNameValuePair("pesquisar", "Pesquisar"));

			post.setEntity(new UrlEncodedFormEntity(parameters, Consts.UTF_8));
			post.addHeader("User-Agent", USER_AGENT);

			final HttpResponse response = client.execute(post);
			final InputStream data = response.getEntity().getContent();

			FileManager.saveDataToFile(data, dealsListFile);
		}
	}

	private void loginIntoServer() throws IOException, URISyntaxException, ServerLoginException
	{
		if (!isCanceled())
		{
			final URI uri = new URI("https://ssl1358.websiteseguro.com/maximobiliaria1/intranet/");
			final HttpPost post = new HttpPost(uri);
			final List<NameValuePair> parameters = new ArrayList<>();

			parameters.add(new BasicNameValuePair("cd", realtor.getLogin()));
			parameters.add(new BasicNameValuePair("pwd", realtor.getPassword()));

			post.setEntity(new UrlEncodedFormEntity(parameters, Consts.UTF_8));
			post.addHeader("User-Agent", USER_AGENT);

			final HttpEntity entity = client.execute(post).getEntity();
			final String content = EntityUtils.toString(entity);

			if (content.contains("Usuário não autorizado"))
			{
				throw new ServerLoginException();
			}
		}
	}

	private List<Integer> loadAlreadyProcessedIds()
	{
		final List<Integer> ids = new ArrayList<>();

		try
		{
			final DataManager dataManager = new DataManager(workDirectory);
			final List<Client> data = dataManager.loadFromLastDataFile();

			for (Client client : data)
			{
				if (!ids.contains(client.getDealId()))
				{
					ids.add(client.getDealId());
				}
			}
		}
		catch (JAXBException | ParseException exception)
		{
			System.err.println("Ignoring exception: ");
			System.err.println();
			exception.printStackTrace(System.err);
		}

		return ids;
	}

	public static boolean isDealListFile(final File file)
	{
		return file.getName().toLowerCase().equals(DEAL_FILE_NAME_FORMAT.toLowerCase());
	}

	private void prepareWorkDirectory()
	{
		if (!isCanceled())
		{
			if (!workDirectory.exists())
			{
				workDirectory.mkdirs();
			}
		}
	}

	public void addListener(final DataDownloaderListener listener)
	{
		if (!listeners.contains(listener))
		{
			listeners.add(listener);
		}
	}

	public void removeListener(final DataDownloaderListener listener)
	{
		if (listeners.contains(listener))
		{
			listeners.remove(listener);
		}
	}

	private void fireDownloadStarted()
	{
		for (DataDownloaderListener listener : listeners)
		{
			listener.downloadStarted();
		}
	}

	private void fireDownloadFinished()
	{
		for (DataDownloaderListener listener : listeners)
		{
			listener.downloadFinished();
		}
	}

	private void fireFileDownloadStarted(final int current, final int total)
	{
		for (DataDownloaderListener listener : listeners)
		{
			listener.fileDownloadStarted(current, total);
		}
	}

	private void fireFileDownloadFinished(final int current, final int total)
	{
		for (DataDownloaderListener listener : listeners)
		{
			listener.fileDownloadFinished(current, total);
		}
	}
}
