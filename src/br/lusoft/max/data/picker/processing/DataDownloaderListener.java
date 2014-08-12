package br.lusoft.max.data.picker.processing;

interface DataDownloaderListener
{
	public void downloadStarted();

	public void fileDownloadStarted(final int current, final int total);

	public void fileDownloadFinished(final int current, final int total);

	public void downloadFinished();
}
