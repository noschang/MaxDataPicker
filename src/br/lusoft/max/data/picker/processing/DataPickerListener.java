package br.lusoft.max.data.picker.processing;

public interface DataPickerListener extends DataDownloaderListener, DataMinerListener
{
	public void pickingStarted();

	public void pickingFinished();

	public void savingDataStarted();

	public void savingDataFinished();

	public void pickingCanceled();
}
