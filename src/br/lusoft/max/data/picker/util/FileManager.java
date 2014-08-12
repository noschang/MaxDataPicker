package br.lusoft.max.data.picker.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FileManager
{
	private static final int BUFFER_LENGTH = 131072; // 128 KB
	
	public static String loadFile(final File file) throws IOException
	{
		final StringBuilder stringBuilder = new StringBuilder();

		String line = null;
		BufferedReader reader = null;

		try
		{
			reader = new BufferedReader(new FileReader(file));

			while ((line = reader.readLine()) != null)
			{
				stringBuilder.append(line);
			}
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (IOException exception)
			{
			}
		}

		return stringBuilder.toString();
	}

	public static void saveDataToFile(final InputStream inputStream, final File file) throws IOException
	{
		final byte[] buffer = new byte[BUFFER_LENGTH]; 

		int bytesRead = 0;
		OutputStream fileOutputStream = null;

		try
		{
			fileOutputStream = new FileOutputStream(file);

			while ((bytesRead = inputStream.read(buffer, 0, BUFFER_LENGTH)) > 0)
			{
				fileOutputStream.write(buffer, 0, bytesRead);
			}

			fileOutputStream.flush();
		}
		finally
		{
			try
			{
				fileOutputStream.close();
			}
			catch (IOException exception)
			{
			}
			try
			{
				inputStream.close();
			}
			catch (IOException exception)
			{
			}
		}
	}
}
