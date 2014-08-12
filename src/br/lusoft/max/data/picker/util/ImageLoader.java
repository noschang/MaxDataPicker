package br.lusoft.max.data.picker.util;

import java.awt.Image;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public final class ImageLoader
{
	private static final Logger LOGGER = Logger.getLogger(ImageLoader.class.getName());
	private static final String path = "br/lusoft/max/data/picker/view/images/";

	public static Image load(final String fileName)
	{
		try
		{
			return ImageIO.read(ClassLoader.getSystemResourceAsStream(path.concat(fileName)));
		}
		catch (IOException exception)
		{
			LOGGER.log(Level.INFO, exception.getMessage(), exception);
		}

		return null;
	}
}
