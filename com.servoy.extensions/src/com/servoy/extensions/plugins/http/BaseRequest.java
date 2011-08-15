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

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * @author pbakker
 *
 */
public abstract class BaseRequest implements IScriptObject, IJavaScriptType
{
	protected DefaultHttpClient client;
	protected HttpRequestBase method;
	protected String url;
	protected HttpContext context;
	protected Map<String, String[]> headers;

	public BaseRequest()
	{
	}//only used by script engine

	public BaseRequest(String url, DefaultHttpClient hc)
	{
		this.url = url;
		if (hc == null)
		{
			client = new DefaultHttpClient();
		}
		else
		{
			client = hc;
		}
		headers = new HashMap<String, String[]>();
	}

	public boolean js_addHeader(String headerName, String value)
	{
		if (headerName != null)
		{
			if (headers.containsKey(headerName))
			{
				String[] values = headers.get(headerName);
				String[] newValues = new String[values.length + 1];
				System.arraycopy(values, 0, newValues, 0, values.length);
				newValues[values.length] = value;
				headers.put(headerName, newValues);
			}
			else
			{
				headers.put(headerName, new String[] { value });
			}
			return true;
		}
		return false;
	}

	public Response js_executeRequest()
	{
		return js_executeRequest(null, null);
	}

	public Response js_executeRequest(String userName, String password)
	{
		try
		{
			Iterator<String> it = headers.keySet().iterator();
			while (it.hasNext())
			{
				String name = it.next();
				String[] values = headers.get(name);
				for (String value : values)
				{
					method.addHeader(name, value);
				}
			}

			if (!Utils.stringIsEmpty(userName))
			{
				BasicCredentialsProvider bcp = new BasicCredentialsProvider();
				URL _url = new URL(url);
				bcp.setCredentials(new AuthScope(_url.getHost(), _url.getPort()), new UsernamePasswordCredentials(userName, password));
				client.setCredentialsProvider(bcp);
			}

			return new Response(client.execute(method, context));
		}
		catch (Exception ex)
		{
			Debug.error(ex);
			return null;
		}
	}

	public String getSample(String methodName)
	{
		if ("executeRequest".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var response = method.executeRequest()"); //$NON-NLS-1$
			return retval.toString();
		}
		if ("addHeader".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("method.addHeader('Content-type','text/xml; charset=ISO-8859-1')");
			return retval.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("addHeader".equals(methodName)) //$NON-NLS-1$
		{
			return "Add a header to the request."; //$NON-NLS-1$
		}
		if ("executeRequest".equals(methodName)) //$NON-NLS-1$
		{
			return "Execute the request method."; //$NON-NLS-1$
		}
		return null;
	}

	public String[] getParameterNames(String methodName)
	{
		if ("executeRequest".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[username]", "[password]" }; //$NON-NLS-1$//$NON-NLS-2$
		}
		if ("addHeader".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "headerName", "value" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}
}
