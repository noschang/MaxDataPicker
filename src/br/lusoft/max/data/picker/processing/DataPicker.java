package br.lusoft.max.data.picker.processing;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import br.lusoft.max.data.picker.model.Client;
import br.lusoft.max.data.picker.model.Realtor;

public final class DataPicker
{
	private final Realtor realtor;
	private final File downloadPath;
	private final List<DataPickerListener> listeners;

	private List<Client> data;

	private DataDownloader dataDownloader;
	private DataMiner dataMiner;
	private DataManager dataManager;

	private boolean running = false;
	private boolean canceled = false;

	public DataPicker(final File downloadPath, final Realtor realtor)
	{
		this.realtor = realtor;
		this.downloadPath = downloadPath;
		this.listeners = new ArrayList<>();
	}

	public void pick() throws IOException, URISyntaxException, JAXBException, ServerLoginException
	{
		if (!isRunning())
		{
			setCanceled(false);
			setRunning(true);

			firePickingStarted();

			dataDownloader = new DataDownloader(realtor, downloadPath);
			dataMiner = new DataMiner(downloadPath);
			dataManager = new DataManager(downloadPath);

			addDownloaderListeners(dataDownloader);
			addMinerListeners(dataMiner);

			downloadData();
			mineData();
			saveData();

			if (!isCanceled())
			{
				firePickingFinished();
			}
			else
			{
				firePickingCanceled();
			}

			setRunning(false);
		}
	}

	private void saveData() throws JAXBException, IOException
	{
		if (!isCanceled())
		{
			dataManager.saveToNewDataFile(this.data);
		}
	}

	private void mineData() throws IOException
	{
		if (!isCanceled())
		{
			dataMiner.mine();
			this.data = dataMiner.getData();
		}
	}

	private void downloadData() throws IOException, URISyntaxException, ServerLoginException
	{
		if (!isCanceled())
		{
			dataDownloader.download();
		}
	}

	public void cancel()
	{
		if (!isCanceled())
		{
			while (isRunning())
			{
				setCanceled(true);
				this.dataDownloader.cancel();
				this.dataMiner.cancel();
			}
		}
	}

	public int getUpdatesCount()
	{
		if (dataMiner != null)
		{
			return dataMiner.getUpdatesCount();
		}

		return 0;
	}

	public synchronized boolean isRunning()
	{
		return this.running;
	}

	private synchronized void setRunning(final boolean running)
	{
		this.running = running;
	}

	public synchronized boolean isCanceled()
	{
		return this.canceled;
	}

	private synchronized void setCanceled(final boolean canceled)
	{
		this.canceled = canceled;
	}

	public List<Client> getData()
	{
		return data;
	}

	public void addListener(final DataPickerListener listener)
	{
		if (!listeners.contains(listener))
		{
			listeners.add(listener);
		}
	}

	public void removeListener(final DataPickerListener listener)
	{
		if (listeners.contains(listener))
		{
			listeners.remove(listener);
		}
	}

	public void firePickingStarted()
	{
		for (DataPickerListener listener : listeners)
		{
			listener.pickingStarted();
		}
	}

	public void firePickingFinished()
	{
		for (DataPickerListener listener : listeners)
		{
			listener.pickingFinished();
		}
	}

	public void firePickingCanceled()
	{
		for (DataPickerListener listener : listeners)
		{
			listener.pickingCanceled();
		}
	}

	private void addMinerListeners(final DataMiner dataMiner)
	{
		for (DataPickerListener listener : listeners)
		{
			dataMiner.addListener(listener);
		}
	}

	private void addDownloaderListeners(final DataDownloader dataDownloader)
	{
		for (DataPickerListener listener : listeners)
		{
			dataDownloader.addListener(listener);
		}
	}
}
