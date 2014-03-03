/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2014 Servoy BV

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.servoy.j2db.plugins.IServerAccess;
import com.servoy.j2db.util.Utils;

/**
 * @author jcompagner
 *
 */
public class FileServlet extends HttpServlet
{
	private final FileServerPlugin fileServerPlugin;
	private final IServerAccess app;

	/**
	 * @param fileServerPlugin
	 * @param app 
	 */
	public FileServlet(FileServerPlugin fileServerPlugin, IServerAccess app)
	{
		this.fileServerPlugin = fileServerPlugin;
		this.app = app;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String pathInfo = req.getPathInfo();
		if (pathInfo.startsWith("/file/"))
		{

			RemoteFileData remoteFileData = fileServerPlugin.getRemoteFileData(app.getServerLocalClientID(), pathInfo.substring(5));
			File file = remoteFileData.getFile();
			if (file != null && file.exists() && file.isFile())
			{
				String contentType = AbstractFile.getContentType(file);
				if (contentType != null) resp.setContentType(contentType);
				resp.setContentLength((int)file.length());
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
				try
				{
					Utils.streamCopy(bis, resp.getOutputStream());
				}
				finally
				{
					bis.close();
				}
			}
			else
			{
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		}
		else
		{
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}

	}
}
