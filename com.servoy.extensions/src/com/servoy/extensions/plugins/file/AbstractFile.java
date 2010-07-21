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
import java.io.IOException;

import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.FileChooserUtils;
import com.servoy.j2db.util.ImageLoader;

/**
 * Defines the basic implementation of the {@link IAbstractFile} interface
 * 
 * @author jcompagner
 * @author Servoy Stuff
 * @since Servoy 5.2
 */
public abstract class AbstractFile implements IAbstractFile
{

	public File getFile()
	{
		return null;
	}

	public String getContentType()
	{
		return null;
	}

	public String getParent()
	{
		return null;
	}

	public IAbstractFile getParentFile()
	{
		return null;
	}

	public String getPath()
	{
		return null;
	}

	public boolean isAbsolute()
	{
		return true;
	}

	public String getAbsolutePath()
	{
		return null;
	}

	public IAbstractFile getAbsoluteFile()
	{
		return null;
	}

	public boolean canRead()
	{
		return true;
	}

	public boolean canWrite()
	{
		return false;
	}

	public boolean exists()
	{
		return true;
	}

	public boolean isDirectory()
	{
		return false;
	}

	public boolean isFile()
	{
		return false;
	}

	public boolean isHidden()
	{
		return false;
	}

	public long lastModified()
	{
		return -1;
	}

	public long size()
	{
		return -1;
	}

	public boolean createNewFile() throws IOException
	{
		return false;
	}

	public boolean delete()
	{
		return false;
	}

	public String[] list()
	{
		return new String[0];
	}

	public IAbstractFile[] listFiles()
	{
		return null;
	}

	public boolean mkdir()
	{
		return false;
	}

	public boolean mkdirs()
	{
		return false;
	}

	public boolean renameTo(IAbstractFile upload)
	{
		return false;
	}

	public boolean setLastModified(long time)
	{
		return false;
	}

	public boolean setReadOnly()
	{
		return false;
	}

	/**
	 * Returns the mime-type of a file, using byte reading and/or file extension recognition
	 * 
	 * @param file the File to find the contentType of
	 * @retun the mime-type or null if not recognized
	 */
	public static String getContentType(final File file)
	{
		if (file.exists() && file.canRead() && file.isFile() && file.length() > 0)
		{
			try
			{
				return ImageLoader.getContentType(FileChooserUtils.readFile(file, 32), file.getName());
			}
			catch (Exception e)
			{
				Debug.error("Error reading the file " + file.getName() + "for getting the content type", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return null;
	}
}
