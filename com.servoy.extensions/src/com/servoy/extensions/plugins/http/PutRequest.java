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

/**
 * @author pbakker
 *
 */
public class PutRequest extends BaseEntityEnclosingRequest
{
	//only used by script engine
	public PutRequest()
	{
		super();
	}

	public PutRequest(String url, DefaultHttpClient hc)
	{
		super(url, hc);
		method = new HttpPut(url);
	}

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

	@Override
	public String getSample(String methodName)
	{
		if ("setFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("putRequest.setFile('c:/temp/manual_01a.doc')"); //$NON-NLS-1$
			return retval.toString();
		}
		return super.getSample(methodName);
	}

	@Override
	public String getToolTip(String methodName)
	{
		if ("setFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Set a file to put."; //$NON-NLS-1$
		}
		return super.getToolTip(methodName);
	}

	@Override
	public String[] getParameterNames(String methodName)
	{
		if (methodName.equals("setFile")) //$NON-NLS-1$
		{
			return new String[] { "filePath" }; //$NON-NLS-1$
		}
		return super.getParameterNames(methodName);
	}


}
