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
import java.io.FileFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.filechooser.FileSystemView;

import com.servoy.j2db.plugins.IServerAccess;
import com.servoy.j2db.plugins.IServerPlugin;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.util.Debug;

/**
 * The server plugin, also {@link IFileService} implementation
 * 
 * @author Servoy Stuff
 * @since Servoy 5.2
 */
public class FileServerPlugin implements IServerPlugin, IFileService
{
	/**
	 * Contains the ITransferObject used per client/file to transfer the byte chunks
	 */
	private final Map<UUID, ITransferObject> transferMap = new ConcurrentHashMap<UUID, ITransferObject>();

	/**
	 * The default location where files will be saved (or related to it)
	 */
	private volatile File defaultFolder;

	private IServerAccess application;


	@SuppressWarnings("nls")
	public Map<String, String> getRequiredPropertyNames()
	{
		final Map<String, String> req = new HashMap<String, String>();
		req.put(IFileService.DEFAULT_FOLDER_PROPERTY, "Set the default folder path to save files sent by client (will default to /uploads/)");
		return req;
	}

	/**
	 * Reads the default folder property and register the {@link IFileService}
	 */
	public void initialize(IServerAccess app) throws PluginException
	{
		this.application = app;
		setDefaultFolder(app.getSettings().getProperty(IFileService.DEFAULT_FOLDER_PROPERTY));
		try
		{
			app.registerRMIService(IFileService.SERVICE_NAME, this);
		}
		catch (RemoteException ex)
		{
			throw new PluginException(ex);
		}
	}

	/**
	 * Initializes the default folder (where the files will be saved)<br/>
	 * First tries to use the folder received in parameter (if not null)<br/>
	 * Then tries to find the Tomcat ROOT context and an /uploads/ directory (create it if needed)<br/>
	 * If all fails will use the home Directory<br/>
	 * Finally, logs the default folder location
	 */
	@SuppressWarnings("nls")
	private void setDefaultFolder(final String folder)
	{
		if (folder != null)
		{
			// try to use the supplied path:
			defaultFolder = new File(folder.trim());
			if (!defaultFolder.exists())
			{
				if (!defaultFolder.mkdirs())
				{
					throw new RuntimeException("Cant set the default folder for the File plugin to '" + folder + "' can't create the directory");
				}
			}
			if (defaultFolder.canWrite())
			{
				throw new RuntimeException("Cant set the default folder for the File plugin to '" + folder + "' can't write into the directory");
			}
		}
		else
		{
			// try to find the Tomcat ROOT context:
			File parent = new File("../application_server/server/webapps/ROOT");
			if (!parent.exists() || !parent.isDirectory())
			{
				// fall back to home directory
				parent = FileSystemView.getFileSystemView().getHomeDirectory();
			}
			// then tries to locate (or create) an 'uploads" folder.
			if (parent != null && parent.exists() && parent.isDirectory())
			{
				defaultFolder = new File(parent.getAbsolutePath() + File.separator + "uploads");
				if (!defaultFolder.exists())
				{
					if (!defaultFolder.mkdirs())
					{
						throw new RuntimeException("Cant set the default folder for the File plugin to '" + folder + "' can't create the directory");
					}
				}
			}
		}
		if (defaultFolder == null)
		{
			throw new RuntimeException("Default folder couldnt be resolved");
		}
		try
		{
			// ensures that we have a canonical representation of the default folder (to help security checks):
			defaultFolder = defaultFolder.getCanonicalFile();
			Debug.log("Default upload folder location was set to " + defaultFolder.getCanonicalPath());
		}
		catch (IOException ex)
		{
		}
	}

	public File getDefaultFolder()
	{
		return defaultFolder;
	}

	@SuppressWarnings("nls")
	public Properties getProperties()
	{
		final Properties props = new Properties();
		props.put(DISPLAY_NAME, "File Plugin");
		return props;
	}

	public void load() throws PluginException
	{
		// ignore
	}

	/**
	 * Takes care of releasing resources, especially in case transfers are still on-going,<br/>
	 * by first closing any opened OutputStream with the help of the ITransferObject<br/>
	 * then deleting the reference to the {@link ConcurrentHashMap}
	 */
	public void unload() throws PluginException
	{
		defaultFolder = null;
		for (final UUID key : transferMap.keySet())
		{
			ITransferObject trans = transferMap.remove(key);
			trans.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.file.IFileService#openTransfer(String,String)
	 */
	@SuppressWarnings("nls")
	public UUID openTransfer(final String clientId, final String filePath) throws IOException, SecurityException
	{
		securityCheck(clientId, filePath);
		final File f = new File(defaultFolder, filePath);
		if (!checkParentFile(f.getCanonicalFile()))
		{
			throw new SecurityException("Saving  on the server out of the defaultFolder is not allowed");
		}
		final RemoteFileData fileData = new RemoteFileData(f, filePath);
		final ITransferObject to = new ToFileTransferObject(f, fileData);
		final UUID id = UUID.randomUUID();
		transferMap.put(id, to);
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.file.IFileService#getRemoteFolderContent(String,String,String[],int,int,int)
	 */
	@SuppressWarnings("nls")
	public RemoteFileData[] getRemoteFolderContent(final String clientId, final String path, final String[] fileFilter, final int filesOption,
		final int visibleOption, final int lockedOption) throws RemoteException, IOException, SecurityException
	{
		securityCheck(clientId, path);
		final File f = new File(defaultFolder, path);
		if (!checkParentFile(f.getCanonicalFile()))
		{
			throw new SecurityException("Browsing on the server out of the defaultFolder is not allowed");
		}
		final RemoteFileData parent = constructHierarchy(f);
		if (f.isDirectory())
		{
			final List<RemoteFileData> list = new ArrayList<RemoteFileData>();
			final FileFilter ff = new FileFilter()
			{
				public boolean accept(File pathname)
				{
					boolean retVal = true;
					if (fileFilter != null)
					{
						String name = pathname.getName().toLowerCase();
						for (String element : fileFilter)
						{
							retVal = name.endsWith(element);
							if (retVal) break;
						}
					}
					if (!retVal) return retVal;

					// file or folder
					if (filesOption == AbstractFile.FILES)
					{
						retVal = pathname.isFile();
					}
					else if (filesOption == AbstractFile.FOLDERS)
					{
						retVal = pathname.isDirectory();
					}
					if (!retVal) return false;

					boolean hidden = pathname.isHidden();
					if (visibleOption == AbstractFile.VISIBLE) retVal = !hidden;
					else if (visibleOption == AbstractFile.NON_VISIBLE) retVal = hidden;
					if (!retVal) return false;

					boolean canWrite = pathname.canWrite();
					if (lockedOption == AbstractFile.LOCKED) retVal = !canWrite;
					else if (lockedOption == AbstractFile.NON_LOCKED) retVal = canWrite;
					return retVal;
				}
			};
			final File[] files = f.listFiles(ff);
			for (final File file : files)
			{
				list.add(new RemoteFileData(file, parent));
			}
			return list.toArray(new RemoteFileData[0]);
		}
		else
		{
			if (filesOption == AbstractFile.ALL || filesOption == AbstractFile.FILES)
			{
				if (visibleOption == AbstractFile.ALL || (visibleOption == AbstractFile.VISIBLE && !f.isHidden()) ||
					(visibleOption == AbstractFile.NON_VISIBLE && f.isHidden()))
				{
					if (lockedOption == AbstractFile.ALL || (lockedOption == AbstractFile.LOCKED && !f.canWrite()) ||
						(lockedOption == AbstractFile.NON_LOCKED && f.canWrite()))
					{
						return new RemoteFileData[] { new RemoteFileData(f, parent) };
					}
				}
			}
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.file.IFileService#getRemoteFileData(String,String)
	 */
	@SuppressWarnings("nls")
	public RemoteFileData getRemoteFileData(String clientId, String path) throws RemoteException, IOException, SecurityException
	{
		securityCheck(clientId, path);
		final File f = new File(defaultFolder, path);
		if (!checkParentFile(f.getCanonicalFile()))
		{
			throw new SecurityException("Browsing on the server out of the defaultFolder is not allowed");
		}
		final RemoteFileData parent = constructHierarchy(f);
		if (f.isDirectory())
		{
			return parent;
		}
		else
		{
			return new RemoteFileData(f, parent);
		}
	}

	/**
	 * Create a hierarchy of RemoteFileData recursively
	 * 
	 * @param f The file to construct the hierarchy for
	 * @return the parent of the hierarchy
	 */
	private RemoteFileData constructHierarchy(File f)
	{
		if (f == null) return null;
		if (defaultFolder.equals(f))
		{
			return new RemoteFileData(defaultFolder, null, null);
		}
		else
		{
			RemoteFileData parent = constructHierarchy(f.getParentFile());
			if (f.isDirectory())
			{
				RemoteFileData data = new RemoteFileData(f, parent);
				return data;
			}
			else
			{
				return parent;
			}
		}
	}

	/**
	 * Checks that the parent of the file provided is the defaultFolder<br/>
	 * Added for security check (to prevent traversing the hierarchy of the server up from the defaultFolder).
	 * 
	 * @param f the file to check for parents
	 * 
	 * @return true if parent is defaultFolder
	 */
	private boolean checkParentFile(File f)
	{
		if (f == null) return false;
		if (defaultFolder.equals(f) || defaultFolder.equals(f.getParentFile()))
		{
			return true;
		}
		else
		{
			return checkParentFile(f.getParentFile());
		}
	}

	/**
	 * Security check to avoid hacking of the transfer capabilities of the plugin
	 * 
	 * @param clientId the id of a client
	 * @param filePath the file path to check - must start with '/'
	 * 
	 * @throws IOException if the plugin is unloaded
	 * @throws SecurityException if the client is not authenticated
	 */
	@SuppressWarnings("nls")
	private void securityCheck(final String clientId, final String filePath) throws IOException, SecurityException
	{
		if (defaultFolder == null)
		{
			throw new IOException("File Plugin is unloaded");
		}
		if (!application.isAuthenticated(clientId))
		{
			throw new SecurityException("Rejected unauthenticated access");
		}
		filePathCheck(filePath);
	}

	/**
	 * Check that the path is 'absolute' on the server
	 * 
	 * @param filePath the file path to check - must start with '/'
	 * @throws IllegalArgumentException if the path doesn't start with '/'
	 */
	private void filePathCheck(final String filePath)
	{
		if (filePath.charAt(0) != '/')
		{
			throw new IllegalArgumentException("Remote path should start with '/'"); //$NON-NLS-1$
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.file.IFileService#writeBytes(UUID,byte[],int,int)
	 */
	@SuppressWarnings("nls")
	public void writeBytes(final UUID uuid, final byte[] bytes, final long start, final long length) throws IOException
	{
		if (defaultFolder == null) throw new IOException("File Plugin is unloaded");
		final ITransferObject to = transferMap.get(uuid);
		if (to == null) throw new IOException("Unkown uuid for writeBytes");
		to.write(bytes, start, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.file.IFileService#readBytes(UUID,int)
	 */
	@SuppressWarnings("nls")
	public byte[] readBytes(UUID uuid, long length) throws RemoteException, IOException
	{
		if (defaultFolder == null) throw new IOException("File Plugin is unloaded");
		final ITransferObject to = transferMap.get(uuid);
		if (to == null) throw new IOException("Unkown uuid for writeBytes");
		return to.read(length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.file.IFileService#closeTransfer(UUID)
	 */
	public Object closeTransfer(final UUID uuid)
	{
		final ITransferObject to = transferMap.remove(uuid);
		return (to == null) ? null : to.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.file.IFileService#delete(String,String)
	 */
	@SuppressWarnings("nls")
	public boolean delete(final String clientId, final String filePath) throws RemoteException, IOException
	{
		securityCheck(clientId, filePath);
		final File f = new File(getDefaultFolder(), filePath);
		if (!checkParentFile(f.getCanonicalFile()))
		{
			throw new SecurityException("Delete  on the server out of the defaultFolder is not allowed");
		}
		return f.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.file.IFileService#getContentType(String,String)
	 */
	@SuppressWarnings("nls")
	public String getContentType(String clientId, String filePath) throws RemoteException, IOException
	{
		securityCheck(clientId, filePath);
		final File f = new File(getDefaultFolder(), filePath);
		if (!checkParentFile(f.getCanonicalFile()))
		{
			throw new SecurityException("Cannot get the contentType of a file on the server out of the defaultFolder");
		}
		return AbstractFile.getContentType(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.file.IFileService#renameTo(String,String,String)
	 */
	@SuppressWarnings("nls")
	public RemoteFileData renameTo(String clientId, String srcPath, String destPath) throws RemoteException, IOException
	{
		securityCheck(clientId, srcPath);
		filePathCheck(destPath);
		// extra check: we don't want the destPath to be '/'
		if (destPath.equals("/"))
		{
			throw new SecurityException("Cannot rename a file/folder to the defaultFolder path");
		}
		final File src = new File(getDefaultFolder(), srcPath);
		final File dest = new File(getDefaultFolder(), destPath);
		if (!checkParentFile(src.getCanonicalFile()) || !checkParentFile(dest.getCanonicalFile()))
		{
			throw new SecurityException("Cannot rename a file/folder out of the defaultFolder");
		}
		boolean result = false;
		// the next two lines might throw SecurityException/IOException:
		if (!dest.exists() || dest.delete())
		{
			result = src.renameTo(dest);
		}
		if (result)
		{
			final RemoteFileData parent = constructHierarchy(dest.getParentFile());
			return new RemoteFileData(dest, parent);
		}
		return null;
	}

}
