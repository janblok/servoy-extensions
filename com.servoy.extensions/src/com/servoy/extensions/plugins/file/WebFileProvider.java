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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.ImageLoader;

/**
 * @author jcompagner
 *
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
	public boolean js_writeFile(Object f, byte[] data)
	{
		if (data == null) return false;
		File file = getFileFromArg(f, false);
		if (file == null || f == null)
		{
			String name = "file.bin";
			if (f instanceof JSFile) name = ((JSFile)f).js_getName();
			else if (f != null) name = f.toString();
			IClientPluginAccess access = plugin.getClientPluginAccess();
			String url = ((IWebClientPluginAccess)access).serveResource(name, data, ImageLoader.getContentType(data, name));
			((IWebClientPluginAccess)access).showURL(url, "_self", null, 0);
			return true;
		}
		else
		{
			return super.js_writeFile(file, data);
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

}
