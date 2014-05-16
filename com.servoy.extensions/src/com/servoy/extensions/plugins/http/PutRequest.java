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

package com.servoy.extensions.plugins.http;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.servoy.extensions.plugins.file.JSFile;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.util.Debug;

/**
 * @author pbakker
 *
 */
@ServoyDocumented
public class PutRequest extends BaseEntityEnclosingRequest
{
	//only used by script engine
	public PutRequest()
	{
		super();
	}

	public PutRequest(String url, DefaultHttpClient hc, IClientPluginAccess plugin)
	{
		super(url, hc, new HttpPut(url), plugin);
	}

	/**
	 * Set a file to put.
	 *
	 * @sample
	 * putRequest.setFile('c:/temp/manual_01a.doc')
	 *
	 * @param filePath 
	 */
	public boolean js_setFile(String filePath)
	{
		if (filePath != null)
		{
			File file = new File(filePath);
			if (file.exists())
			{
				((HttpPut)method).setEntity(new FileEntity(file, ContentType.create("binary/octet-stream")));
				return true;
			}
		}
		return false;
	}

	/**
	 * Set a file to put.
	 *
	 * @sample
	 * putRequest.setFile(jsFileInstance)
	 *
	 * @param file 
	 */
	public boolean js_setFile(JSFile file)
	{
		if (file != null && file.js_exists())
		{
			try
			{
				((HttpPut)method).setEntity(new InputStreamEntity(file.getAbstractFile().getInputStream(), file.js_size(),
					ContentType.create("binary/octet-stream")));
				return true;
			}
			catch (IOException e)
			{
				Debug.error(e);
			}
		}
		return false;
	}

}
