package br.lusoft.max.data.picker.processing;

import java.io.File;
import java.io.FileFilter;

final class ExtensionFileFilter implements FileFilter
{
	private String extension;

	public ExtensionFileFilter(final String extension)
	{
		setExtension(extension);
	}

	@Override
	public boolean accept(File pathname)
	{
		return (pathname.isFile() && pathname.getName().toLowerCase().endsWith(extension.toLowerCase()));
	}

	private void setExtension(String extension)
	{
		if (!extension.startsWith("."))
		{
			this.extension = String.format(".%s", extension);
		}
		else
		{
			this.extension = extension;
		}
	}
}
