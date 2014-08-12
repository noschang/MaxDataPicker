package br.lusoft.max.data.picker.processing;

interface DataMinerListener
{
	public void miningStarted();
	
	public void fileMiningStarted(final int current, final int total);
	
	public void fileMiningFinished(final int current, final int total);
	
	public void miningFinished(); 
}
