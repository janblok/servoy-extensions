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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.mozilla.javascript.Function;

import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.ImageLoader;

/**
 * Web plugin provider implementation
 * @author jcompagner
 * @author Servoy Stuff
 */
public class WebFileProvider extends FileProvider
{

	/**
	 * @param plugin
	 */
	public WebFileProvider(FilePlugin plugin)
	{
		super(plugin);
	}

	@SuppressWarnings("nls")
	@Override
	public boolean js_writeFile(Object f, byte[] data, String mimeType)
	{
		if (data == null) return false;
		File file = getFileFromArg(f, false);
		if (file == null || f == null)
		{
			String name = "file.bin";
			if (f instanceof JSFile) name = ((JSFile)f).js_getName();
			else if (f != null) name = f.toString();
			IClientPluginAccess access = plugin.getClientPluginAccess();
			String type = (mimeType == null) ? ImageLoader.getContentType(data, name) : mimeType.trim();
			String url = ((IWebClientPluginAccess)access).serveResource(name, data, type);
			((IWebClientPluginAccess)access).showURL(url, "_self", null, 0);
			return true;
		}
		else
		{
			return super.js_writeFile(file, data, mimeType);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public String js_readTXTFile(Object[] args)
	{
		if (args != null && args.length > 0 && args[0] instanceof JSFile)
		{
			byte[] bytes = ((JSFile)args[0]).js_getBytes();
			if (bytes != null)
			{
				try
				{
					return readTXTFile(args, new ByteArrayInputStream(bytes));
				}
				catch (Exception e)
				{
					Debug.error(e);
					return null;
				}
			}
			return "";
		}
		else
		{
			return super.js_readTXTFile(args);
		}
	}

	@Override
	public byte[] js_readFile(Object[] args)
	{
		if (args != null && args.length > 0 && args[0] instanceof JSFile)
		{
			return ((JSFile)args[0]).js_getBytes();
		}
		return super.js_readFile(args);
	}

	/**
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @see com.servoy.extensions.plugins.file.FileProvider#writeTXT(java.lang.Object, java.lang.String, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("nls")
	@Override
	protected boolean writeTXT(Object f, String data, String encoding, String contentType)
	{
		File file = getFileFromArg(f, false);
		if (file == null || f == null)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length());
			try
			{
				if (writeToOutputStream(baos, data, encoding))
				{
					String mimeType = contentType;
					if (encoding != null)
					{
						mimeType += "; charset=\"" + encoding + "\"";
					}
					String name = f == null ? "file.txt" : f instanceof JSFile ? ((JSFile)f).js_getName() : f.toString();
					IClientPluginAccess access = plugin.getClientPluginAccess();
					String url = ((IWebClientPluginAccess)access).serveResource(name, baos.toByteArray(), mimeType);
					((IWebClientPluginAccess)access).showURL(url, "_self", null, 0);
					return true;
				}
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
			return false;
		}
		return super.writeTXT(f, data, encoding, contentType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.file.FileProvider#js_streamFilesToServer(java.lang.Object, java.lang.Object, org.mozilla.javascript.Function)
	 */
	@SuppressWarnings("nls")
	@Override
	public JSProgressMonitor js_streamFilesToServer(final Object f, final Object s, final Function callback)
	{
		if (f != null)
		{
			// first get the default upload location (canonical path on the server):
			final String defaultLocation = getDefaultUploadLocation();
			if (defaultLocation != null)
			{
				final File serverFolder = new File(defaultLocation);
				final FunctionDefinition function = (callback == null) ? null : new FunctionDefinition(callback);

				final Object[] fileObjects = unwrap(f);
				final Object[] serverFiles = unwrap(s);

				if (fileObjects != null)
				{
					for (int i = 0; i < fileObjects.length; i++)
					{

						InputStream is = null;
						OutputStream os = null;
						Exception ex = null;
						File dest = null;
						try
						{
							// tries to retrieve an inputStream from the IUploadData:
							is = ((JSFile)fileObjects[i]).getAbstractFile().getInputStream();
							if (is != null)
							{
								String serverFileName = null;
								if (serverFiles != null && i < serverFiles.length)
								{
									if (serverFiles[i] instanceof JSFile)
									{
										JSFile jsFile = (JSFile)serverFiles[i];
										IAbstractFile abstractFile = jsFile.getAbstractFile();
										if (abstractFile instanceof RemoteFile)
										{
											serverFileName = ((RemoteFile)abstractFile).getAbsolutePath();
										}
										else
										{
											serverFileName = abstractFile.getName();
										}
									}
									else
									{
										serverFileName = serverFiles[i].toString();
										FilePluginUtils.filePathCheck(serverFileName);
									}
								}
								else
								{
									// no server file or server file name was provided, so create a default one:
									serverFileName = "/" + ((JSFile)fileObjects[i]).getAbstractFile().getName();
								}

								dest = new File(serverFolder, serverFileName);
								if (!FilePluginUtils.checkParentFile(dest, serverFolder))
								{
									// prevents case where serverFileName contains "../" in its path
									throw new SecurityException("Browsing on the server out of the defaultFolder is not allowed");
								}
								if ((dest.exists() && dest.canWrite()) || dest.createNewFile())
								{
									os = new BufferedOutputStream(new FileOutputStream(dest));

									final byte[] buffer = new byte[CHUNK_BUFFER_SIZE];
									int read;
									while ((read = is.read(buffer)) != -1)
									{
										os.write(buffer, 0, read);
									}
									os.flush();
								}
							}
						}
						catch (final IOException e)
						{
							Debug.error(e);
							ex = e;
						}
						finally
						{
							if (is != null)
							{
								try
								{
									is.close();
								}
								catch (final IOException e)
								{
								}
							}
							if (os != null)
							{
								try
								{
									os.close();
								}
								catch (final IOException e)
								{
								}
							}
							if (function != null)
							{
								try
								{
									IFileService service = getFileService();
									RemoteFileData remoteFile = new RemoteFileData(dest, FilePluginUtils.constructHierarchy(dest, serverFolder));
									final JSFile returnedFile = (dest == null) ? null : new JSFile(new RemoteFile(remoteFile, service,
										plugin.getClientPluginAccess().getClientID()));
									function.execute(plugin.getClientPluginAccess(), new Object[] { returnedFile, ex }, true);
								}
								catch (final Exception e)
								{
									Debug.error(e);
								}
							}
						}
					}
				}
			}
		}
		return null; // no JSProgressMonitor since there will be no Thread
	}

}
