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
import java.util.Date;

import com.servoy.j2db.plugins.IUploadData;
import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptObject;

/**
 * The {@link IScriptObject} representation of a file, either local, remote or web.
 * 
 * @author jcompagner
 * @author Servoy Stuff
 */
public class JSFile implements IScriptObject, IJavaScriptType
{
	private final IAbstractFile file;
	private JSFile[] EMPTY;

	public JSFile()
	{
		//for developer scripting introspection only
		this((File)null);
	}

	public JSFile(File file)
	{
		this.file = new LocalFile(file);
	}

	public JSFile(IUploadData upload)
	{
		if (upload.getFile() != null)
		{
			this.file = new LocalFile(upload.getFile());
		}
		else if (upload instanceof IAbstractFile)
		{
			this.file = (IAbstractFile)upload;
		}
		else
		{
			this.file = new UploadData(upload);
		}
	}

	public IAbstractFile getAbstractFile()
	{
		return this.file;
	}

	/**
	 * 
	 *
	 * @sample //null
	 */
	public String js_getName()
	{
		return file.getName();
	}

	/**
	 * Returns the parent filename
	 *
	 * @sample 
	 */
	public String js_getParent()
	{
		return file.getParent();
	}

	/**
	 * Returns the parent file
	 *
	 * @sample 
	 */
	public JSFile js_getParentFile()
	{
		return new JSFile(file.getParentFile());
	}

	/**
	 * Returns the path to the file (the parents complete name)
	 *
	 * @sample 
	 */
	public String js_getPath()
	{
		return file.getPath();
	}

	/**
	 * Returns true if the path is absolute. (Starts with / on unix or has a driver letter on windows)
	 *
	 * @sample 
	 */
	public boolean js_isAbsolute()
	{
		return file.isAbsolute();
	}

	/**
	 * Returns the absolute form of this abstract pathname.
	 *
	 * @sample 
	 */
	public String js_getAbsolutePath()
	{
		return file.getAbsolutePath();
	}


	/**
	 * Returns the absolute form of this abstract pathname.
	 *
	 * @sample 
	 */
	public JSFile js_getAbsoluteFile()
	{
		return new JSFile(file.getAbsoluteFile());
	}

	/**
	 * returns true if the file exists and is readable (has access to it)
	 *
	 * @sample 
	 */
	public boolean js_canRead()
	{
		return file.canRead();
	}

	/**
	 * returns true if the file exists and can be modified
	 *
	 * @sample 
	 */
	public boolean js_canWrite()
	{
		return file.canWrite();
	}

	/**
	 * returns true if the file/directory exists on the filesystem
	 *
	 * @sample 
	 */
	public boolean js_exists()
	{
		return file.exists();
	}

	/**
	 * Returns true the jsfile is a directory (not a file)
	 *
	 * @sample 
	 */
	public boolean js_isDirectory()
	{
		return file.isDirectory();
	}

	public byte[] js_getBytes()
	{
		return file.getBytes();
	}

	/**
	 * Returns true the jsfile is a file (not directory)
	 *
	 * @sample 
	 */
	public boolean js_isFile()
	{
		return file.isFile();
	}

	/**
	 * Returns true the jsfile is a hidden (a filesystem attribute)
	 *
	 * @sample 
	 */
	public boolean js_isHidden()
	{
		return file.isHidden();
	}

	/**
	 * Returns the last modified time/date of the file
	 *
	 * @sample 
	 */
	public Date js_lastModified()
	{
		return new Date(file.lastModified());
	}

	/**
	 * returns the size in bytes of the file (0 if the file didn't exists)
	 *
	 * @sample 
	 */
	public long js_size()
	{
		return file.size();
	}

	/**
	 * returns true if the file(name) did not already exists and could be created
	 *
	 * @sample 
	 */
	public boolean js_createNewFile() throws IOException
	{
		return file.createNewFile();
	}

	/**
	 * 
	 *
	 * @sample //null
	 */
	public boolean js_delete()
	{
		return file.delete();
	}

	/**
	 * 
	 *
	 * @sample //null
	 */
	public boolean js_deleteFile()
	{
		return file.delete();
	}

	/**
	 * Returns an array of strings naming the files and directories of the file (if the file is directory)
	 *
	 * @sample 
	 */
	public String[] js_list()
	{
		return file.list();
	}

	/**
	 * Returns an array of JSFiles naming the files and directories of the file (if the file is directory)
	 *
	 * @sample 
	 */
	public JSFile[] js_listFiles()
	{
		IAbstractFile[] files = file.listFiles();
		if (files == null) return EMPTY;
		JSFile[] retArray = new JSFile[files.length];
		for (int i = 0; i < files.length; i++)
		{
			retArray[i] = new JSFile(files[i]);
		}
		return retArray;
	}

	/**
	 * Returns true if a new directory was made
	 *
	 * @sample 
	 */
	public boolean js_mkdir()
	{
		return file.mkdir();
	}

	/**
	 * Returns true if a new directory (and all its parents if neccesary) was made
	 *
	 * @sample 
	 */
	public boolean js_mkdirs()
	{
		return file.mkdirs();
	}

	/**
	 * Returns true if the file could be renamed to the JSFile or String given
	 *
	 * @sample 
	 *
	 * @param JSFile/String_destination 
	 */
	public boolean js_renameTo(Object dest)
	{
		if (dest instanceof JSFile)
		{
			return file.renameTo(((JSFile)dest).file);
		}
		else if (dest instanceof String)
		{
			if (file instanceof RemoteFile)
			{
				return ((RemoteFile)file).renameTo((String)dest);
			}
			else
			{
				return file.renameTo(new LocalFile(new File((String)dest)));
			}
		}
		return false;
	}

	/**
	 * Sets the last modified date of the file
	 *
	 * @sample 
	 *
	 * @param Date/Long_date 
	 */
	public boolean js_setLastModified(Object object)
	{
		long time = -1;
		if (object instanceof Date)
		{
			time = ((Date)object).getTime();
		}
		else if (object instanceof Number)
		{
			time = ((Number)object).longValue();
		}
		if (time != -1)
		{
			return file.setLastModified(time);
		}
		return false;
	}

	/**
	 * Sets the readonly attribute of the directory, returns true if success
	 *
	 * @sample 
	 */
	public boolean js_setReadOnly()
	{
		return file.setReadOnly();
	}

	/**
	 * get the contenttype of this file like 'application/pdf'
	 *
	 * @sample 
	 */
	public String js_getContentType()
	{
		return file.getContentType();
	}

	/**
	 * Set the content of the file (local or remote) to the bytes provided<br/>
	 * Will not create a new file if one doesn't exist
	 * 
	 * @param bytes the data
	 * 
	 * @return true if the operation worked
	 * @since 5.2.5
	 */
	public boolean js_setBytes(byte[] bytes)
	{
		return js_setBytes(bytes, false);
	}

	/**
	 * Set the content of the file (local or remote) to the bytes provided
	 * 
	 * @param bytes the data
	 * @param createFile true to create a file if it doesn't exist
	 * 
	 * @return true if the operation worked
	 * @since 5.2.5
	 */
	public boolean js_setBytes(byte[] bytes, boolean createFile)
	{
		return file.setBytes(bytes, createFile);
	}

	@Override
	public String toString()
	{
		return file.toString();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.IScriptObject#getSample(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String getSample(String methodName)
	{
		if ("canRead".equals(methodName) || "canWrite".equals(methodName) || "exists".equals(methodName) || "getAbsolutePath".equals(methodName) ||
			"getContentType".equals(methodName) || "getName".equals(methodName) || "getPath".equals(methodName) || "isAbsolute".equals(methodName) ||
			"isDirectory".equals(methodName) || "isFile".equals(methodName) || "isHidden".equals(methodName) || "lastModified".equals(methodName) ||
			"size".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile('./big.jpg');\n");
			sb.append("// or for a remote file:\n");
			sb.append("// var f = plugins.convertToRemoteJSFile('/images/big.jpg');\n");
			sb.append("if (f && f.exists()) {\n");
			sb.append("\tapplication.output('is absolute: ' + f.isAbsolute());\n");
			sb.append("\tapplication.output('is dir: ' + f.isDirectory());\n");
			sb.append("\tapplication.output('is file: ' + f.isFile());\n");
			sb.append("\tapplication.output('is hidden: ' + f.isHidden());\n");
			sb.append("\tapplication.output('can read: ' + f.canRead());\n");
			sb.append("\tapplication.output('can write: ' + f.canWrite());\n");
			sb.append("\tapplication.output('last modified: ' + f.lastModified());\n");
			sb.append("\tapplication.output('name: ' + f.getName());\n");
			sb.append("\tapplication.output('path: ' + f.getPath());\n");
			sb.append("\tapplication.output('absolute path: ' + f.getAbsolutePath());\n");
			sb.append("\tapplication.output('content type: ' + f.getContentType());\n");
			sb.append("\tapplication.output('size: ' + f.size());\n");
			sb.append("}\n");
			sb.append("else {\n");
			sb.append("\tapplication.output('File/folder not found.');\n");
			sb.append("}\n");
			return sb.toString();
		}
		else if ("createNewFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile('story.txt');\n");
			sb.append("if (!f.exists())\n");
			sb.append("\tf.createNewFile();\n");
			return sb.toString();
		}
		else if ("deleteFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile('story.txt');\n");
			sb.append("// or for a remote file:\n");
			sb.append("// var f = plugins.convertToRemoteJSFile('/story.txt');\n");
			sb.append("if (f && f.exists())\n");
			sb.append("\tf.deleteFile();\n");
			return sb.toString();
		}
		else if ("setReadOnly".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile('invoice.txt');\n");
			sb.append("%%elementName%%.writeTXTFile(f, 'important data that should not be changed');\n");
			sb.append("f.setReadOnly();\n");
			return sb.toString();
		}
		else if ("setLastModified".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile('story.txt');\n");
			sb.append("f.createNewFile();\n");
			sb.append("// Make the file look old.\n");
			sb.append("f.setLastModified(new Date(1999, 5, 21));\n");
			return sb.toString();
		}
		else if ("renameTo".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile('story.txt');\n");
			sb.append("f.renameTo('otherstory.txt');\n");
			sb.append("// or for a remote file:\n");
			sb.append("// var f = plugins.convertToRemoteJSFile('/story.txt');\n");
			sb.append("// f.renameTo('/otherstory.txt');\n");
			return sb.toString();
		}
		else if ("mkdir".equals(methodName) || "mkdirs".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile('one/two/three/four');\n");
			sb.append("f.mkdirs(); // Create all four levels of folders in one step.\n");
			sb.append("var g = %%elementName%%.convertToJSFile('one/two/three/four/five');\n");
			sb.append("g.mkdir(); // This will work because all parent folders are already created.\n");
			return sb.toString();
		}
		else if ("list".equals(methodName) || "listFiles".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var d = %%elementName%%.convertToJSFile('plugins');\n");
			sb.append("// or for a remote file:\n");
			sb.append("// var d = plugins.convertToRemoteJSFile('/plugins');\n");
			sb.append("var names = d.list();\n");
			sb.append("application.output('Names:');\n");
			sb.append("for (var i=0; i<names.length; i++)\n");
			sb.append("\tapplication.output(names[i]);\n");
			sb.append("var files = d.listFiles();\n");
			sb.append("application.output('Absolute paths:');\n");
			sb.append("for (var i=0; i<files.length; i++)\n");
			sb.append("\tapplication.output(files[i].getAbsolutePath());\n");
			return sb.toString();
		}
		else if ("getAbsoluteFile".equals(methodName) || "getParent".equals(methodName) || "getParentFile".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var f = %%elementName%%.convertToJSFile('story.txt');\n");
			sb.append("// or for a remote file:\n");
			sb.append("// var f = %%elementName%%.convertToRemoteJSFile('/story.txt');\n");
			sb.append("application.output('parent folder: ' + f.getAbsoluteFile().getParent());\n");
			sb.append("application.output('parent folder has ' + f.getAbsoluteFile().getParentFile().listFiles().length + ' entries');\n");
			return sb.toString();
		}
		else if ("setBytes".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var file = %%elementName%%.convertToJSFile('/pathTo/file.jpg');\n");
			sb.append("// or for a remote file:\n");
			sb.append("// var file = %%elementName%%.convertToRemoteJSFile('/remotePathTo/file.jpg');\n");
			sb.append("var success = file.setBytes(blobDataProvider, true);");
			return sb.toString();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String getToolTip(String methodName)
	{
		if ("canRead".equals(methodName))
		{
			return "Returns true if the file exists and is readable (has access to it) - works on remote files too.";
		}
		else if ("canWrite".equals(methodName))
		{
			return "Returns true if the file exists and can be modified - works on remote files too.";
		}
		else if ("createNewFile".equals(methodName))
		{
			return "Creates the file on disk if needed. Returns true if the file (name) did not already exists and had to be created - for remote, use the streamFilesToServer to stream a file.";
		}
		else if ("deleteFile".equals(methodName))
		{
			return "Deletes the file from the disk if possible. Returns true if the file could be deleted. If the file is a directory, then it must be empty in order to be deleted - works on remote files too.";
		}
		else if ("exists".equals(methodName))
		{
			return "Returns true if the file/directory exists on the filesystem - works on remote files too.";
		}
		else if ("getAbsoluteFile".equals(methodName))
		{
			return "Returns a JSFile instance that corresponds to the absolute form of this pathname - works on remote files too.";
		}
		else if ("getAbsolutePath".equals(methodName))
		{
			return "Returns a String representation of the absolute form of this pathname - works on remote files too.";
		}
		else if ("getContentType".equals(methodName))
		{
			return "Returns the contenttype of this file, like for example 'application/pdf' - works on remote files too.";
		}
		else if ("getName".equals(methodName))
		{
			return "Returns the name of the file. The name consists in the last part of the file path - works on remote files too.";
		}
		else if ("getParent".equals(methodName))
		{
			return "Returns the String representation of the path of the parent of this file - works on remote files too.";
		}
		else if ("getParentFile".equals(methodName))
		{
			return "Returns a JSFile instance that corresponds to the parent of this file - works on remote files too.";
		}
		else if ("getPath".equals(methodName))
		{
			return "Returns a String holding the path to the file - works on remote files too.";
		}
		else if ("isAbsolute".equals(methodName))
		{
			return "Returns true if the path is absolute. The path is absolute if it starts with '/' on Unix/Linux/MacOS or has a driver letter on Windows - works on remote files too.";
		}
		else if ("isDirectory".equals(methodName))
		{
			return "Returns true if the file is a directory - works on remote files too.";
		}
		else if ("isFile".equals(methodName))
		{
			return "Returns true if the file is a file and not a regular file - works on remote files too.";
		}
		else if ("isHidden".equals(methodName))
		{
			return "Returns true if the file is hidden (a file system attribute) - works on remote files too.";
		}
		else if ("lastModified".equals(methodName))
		{
			return "Returns the time/date of the last modification on the file - works on remote files too.";
		}
		else if ("list".equals(methodName))
		{
			return "Returns an array of strings naming the files and directories located inside the file, if the file is a directory - works on remote files too.";
		}
		else if ("listFiles".equals(methodName))
		{
			return "Returns an array of JSFiles naming the files and directories located inside the file, if the file is a directory - works on remote files too.";
		}
		else if ("mkdir".equals(methodName))
		{
			return "Creates a directory on disk if possible. Returns true if a new directory was created - for remote, use the streamFilesToServer to create the directory instead.";
		}
		else if ("mkdirs".equals(methodName))
		{
			return "Creates a directory on disk, together with all its parent directories, if possible. Returns true if the hierarchy of directories is created - for remote, use the streamFilesToServer to create the directories instead.";
		}
		else if ("renameTo".equals(methodName))
		{
			return "Renames the file to a different name. Returns true if the file could be renamed - works on remote files too.";
		}
		else if ("setLastModified".equals(methodName))
		{
			return "Sets the date/time of the last modification on the file.";
		}
		else if ("setReadOnly".equals(methodName))
		{
			return "Sets the readonly attribute of the file/directory. Returns true on success.";
		}
		else if ("size".equals(methodName))
		{
			return "Returns the size in bytes of the file. Returns 0 if the file does not exist on disk - works on remote files too.";
		}
		else if ("setBytes".equals(methodName))
		{
			return "Sets the byte[] content of a JSFile to the byte array provided, creating a file if none exists when the createFile parameter is true (default = false). Returns true if the content was set - works on local and remote files.";
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.IScriptObject#getParameterNames(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String[] getParameterNames(String methodName)
	{
		if (methodName.equals("renameTo"))
		{
			return new String[] { "destination" };
		}
		else if (methodName.equals("setLastModified"))
		{
			return new String[] { "date" };
		}
		else if (methodName.equals("setBytes"))
		{
			return new String[] { "bytes", "[createFile]" };
		}
		return null;
	}

	@SuppressWarnings("nls")
	public boolean isDeprecated(String methodName)
	{
		if ("js_delete".equals(methodName) || "delete".equals(methodName))
		{
			return true;
		}
		return false;
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}

	/**
	 * @return
	 */
	public File getFile()
	{
		return file.getFile();
	}

}
