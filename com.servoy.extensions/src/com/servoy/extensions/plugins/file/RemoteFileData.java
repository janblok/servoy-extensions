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
import java.io.Serializable;

/**
 * Class that holds the data representation of a {@link RemoteFile}
 * 
 * @author jcompagner
 * @author Servoy Stuff
 * @since Servoy 5.2
 */
public class RemoteFileData implements Serializable
{

	private final String name;
	private final RemoteFileData parent;
	private final long lastModified;
	private long length;
	private final boolean directory;
	private final boolean file;
	private final boolean exists;
	private final boolean canRead;
	private final boolean canWrite;
	private final boolean hidden;
	private final int hash;

	private transient File fileObj;

	/**
	 * Main Constructor, set up all the final properties
	 * 
	 * @param file the File this class represents
	 * @param name the name to use for this class
	 * @param parent the parent {@link RemoteFileData} (a folder)
	 */
	public RemoteFileData(File file, String name, RemoteFileData parent)
	{
		this.fileObj = file;
		this.name = name;
		this.parent = parent;
		this.lastModified = file.lastModified();
		this.length = file.length();
		this.directory = file.isDirectory();
		this.file = file.isFile();
		this.canRead = file.canRead();
		this.canWrite = file.canWrite();
		this.hidden = file.isHidden();
		this.hash = file.hashCode();
		this.exists = file.exists();
	}

	/**
	 * @return the fileObj
	 */
	File getFile()
	{
		return fileObj;
	}


	/**
	 * Overloaded Constructor, with no name provided (deduced from the file provided)
	 * 
	 * @param file the File this class represents
	 * @param parent the parent {@link RemoteFileData} (a folder)
	 */
	public RemoteFileData(File file, RemoteFileData parent)
	{
		this(file, file.getName(), parent);
	}

	/**
	 * Overloaded Constructor, with no parent
	 * 
	 * @param file the File this class represents
	 * @param name the name to use for this class
	 */
	public RemoteFileData(File file, String name)
	{
		this(file, name, null);
	}

	/**
	 * @return the name of the file this class represents (can be "/" for defaultFolder)
	 */
	public String getName()
	{
		return (name == null) ? "/" : name;
	}

	/**
	 * @return the parent of this class, can be a folder or null if this class represents the defaultFolder
	 */
	public RemoteFileData getParent()
	{
		return parent;
	}

	/**
	 * @return the absolute path of this {@link RemoteFileData}, constructed recursively from the parent hierarchy
	 */
	public String getAbsolutePath()
	{
		if (parent == null)
		{
			return getName();
		}
		else
		{
			final String parentPath = parent.getAbsolutePath();
			if (parentPath.endsWith("/"))
			{
				return parentPath + getName();
			}
			else
			{
				return parentPath + '/' + getName();
			}
		}
	}

	public long size()
	{
		return length;
	}

	public void refreshSize()
	{
		if (fileObj != null) length = fileObj.length();
	}

	public long lastModified()
	{
		return lastModified;
	}

	public boolean isDirectory()
	{
		return directory;
	}

	public boolean isFile()
	{
		return file;
	}

	public boolean canRead()
	{
		return canRead;
	}

	public boolean canWrite()
	{
		return canWrite;
	}

	public boolean isHidden()
	{
		return hidden;
	}

	public boolean exists()
	{
		return exists;
	}

	/**
	 * @returns true if the RemoteFileData provided is not null and has the same absolutePath
	 */
	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof RemoteFileData && this.getAbsolutePath().equals(((RemoteFileData)obj).getAbsolutePath()));
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	@Override
	public String toString()
	{
		return getAbsolutePath();
	}


}
