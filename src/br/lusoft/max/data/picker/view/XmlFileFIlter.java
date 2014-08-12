package br.lusoft.max.data.picker.view;

import java.io.File;
import javax.swing.filechooser.FileFilter;

final class XmlFileFIlter extends FileFilter
{
	@Override
	public boolean accept(File file)
	{
		return (file.isDirectory() || file.getName().toLowerCase().endsWith(".xml"));
	}
	
	@Override
	public String getDescription()
	{
		return "Arquivos XML (Extended Markup Language) (*.xml)";
	}
}
