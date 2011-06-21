/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2011 Servoy BV

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

package com.servoy.extensions.plugins.spellcheck.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.servoy.extensions.plugins.spellcheck.SpellCheckServerPlugin;

public class SpellCheckXMLServlet extends HttpServlet
{
	private final SpellCheckServerPlugin spellCheckServerPlugin;
	private final String CHARSET_DEFAULT = "UTF-8"; //$NON-NLS-1$

	public SpellCheckXMLServlet(String webServiceName, SpellCheckServerPlugin spellCheckServer)
	{
		this.spellCheckServerPlugin = spellCheckServer;

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/xml"); //$NON-NLS-1$

		String content = spellCheckServerPlugin.check(getBody(request));

		response.setHeader("Content-Type", "text/xml" + ";charset=" + CHARSET_DEFAULT); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$


		byte[] bytes = content.getBytes(CHARSET_DEFAULT);

		ServletOutputStream outputStream = null;
		try
		{
			outputStream = response.getOutputStream();
			outputStream.write(bytes);
		}
		finally
		{
			if (outputStream != null)
			{
				outputStream.flush();
				outputStream.close();
			}
		}
		response.setContentLength(bytes.length);

	}

	protected String getBody(HttpServletRequest request) throws IOException
	{
		InputStream is = null;
		try
		{
			is = request.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] buffer = new byte[128];
			int length;
			while ((length = is.read(buffer)) >= 0)
			{
				baos.write(buffer, 0, length);
			}

			return new String(baos.toByteArray(), CHARSET_DEFAULT);
		}
		finally
		{
			if (is != null)
			{
				is.close();
			}
		}
	}
}
