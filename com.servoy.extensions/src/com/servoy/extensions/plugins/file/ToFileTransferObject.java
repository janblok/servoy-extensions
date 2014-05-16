/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */
package com.servoy.extensions.plugins.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Implementation of an ITransferObject, writing the bytes received by a client to a {@link File}<br/>
 * The file is given in the constructor by the caller
 * 
 * @author Servoy Stuff
 * @since Servoy 5.2
 */
public class ToFileTransferObject implements ITransferObject
{

	/**
	 * The file that will be used to write the bytes received
	 */
	private final File file;

	private final RemoteFileData fileData;

	/**
	 * The (File)OutputStream that will be used to write the bytes received
	 */
	private FileOutputStream output;

	private FileInputStream input;


	/**
	 * Constructor
	 * 
	 * @param file The {@link File} to use for writing the bytes received
	 * 
	 * @throws IOException if a {@link FileOutputStream} cannot be created from the {@link File} given
	 * @throws IllegalArgumentException if the {@link File} given is null
	 */
	@SuppressWarnings("nls")
	public ToFileTransferObject(final File file, RemoteFileData fileData) throws IOException
	{
		if (file == null)
		{
			throw new IllegalArgumentException("ToFileTransferObject file parameter cannot be null!");
		}
		if (file.isDirectory())
		{
			throw new IllegalArgumentException("ToFileTransferObject file parameter cannot be a directory!");
		}
		try
		{
			if (!file.exists())
			{
				File dir = file.getParentFile();
				if (!((dir.exists() || dir.mkdirs()) && file.createNewFile()))
				{
					throw new IOException("File " + file.getName() + " cannot be created!");
				}
			}
		}
		catch (IOException ex)
		{
			throw ex;
		}
		this.file = file;
		this.fileData = fileData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.stuff.servoy.plugin.velocityreport.ITransferObject#write(byte[], int, int)
	 */
	public void write(final byte[] bytes, final long start, final long length) throws IOException
	{
		if (output == null) output = new FileOutputStream(file);
		output.write(bytes, (int)start, (int)length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.stuff.servoy.plugin.velocityreport.ITransferObject#read(int)
	 */
	public byte[] read(long length) throws IOException
	{
		if (input == null) input = new FileInputStream(file);
		byte[] bytes = new byte[(int)length];
		int read = input.read(bytes);
		if (read == -1) return null;
		if (read != length)
		{
			byte[] tmp = new byte[read];
			System.arraycopy(bytes, 0, tmp, 0, read);
			return tmp;
		}
		return bytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.stuff.servoy.plugin.velocityreport.ITransferObject#close()
	 */
	public Object close()
	{
		try
		{
			if (output != null) output.close();
			if (input != null) input.close();
		}
		catch (IOException ignore)
		{
		}
		fileData.refreshSize();
		return fileData;
	}

}
