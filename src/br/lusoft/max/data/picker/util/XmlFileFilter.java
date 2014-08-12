package br.lusoft.max.data.picker.util;

import java.io.File;
import java.io.FileFilter;

final class XmlFileFilter implements FileFilter
{
	@Override
	public boolean accept(File pathname)
	{
		return (pathname.isFile() && pathname.getName().toLowerCase().endsWith(".xml"));
	}
}
