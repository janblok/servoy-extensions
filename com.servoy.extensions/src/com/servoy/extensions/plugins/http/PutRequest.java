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

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.servoy.j2db.documentation.ServoyDocumented;

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

	public PutRequest(String url, DefaultHttpClient hc)
	{
		super(url, hc, new HttpPut(url));
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
				((HttpPut)method).setEntity(new FileEntity(file, "binary/octet-stream")); //$NON-NLS-1$
			}
		}
		return false;
	}

}
