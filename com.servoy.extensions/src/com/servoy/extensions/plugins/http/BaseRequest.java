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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.mozilla.javascript.Function;

import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * @author pbakker
 *
 */
public abstract class BaseRequest implements IScriptable, IJavaScriptType
{
	protected DefaultHttpClient client;
	protected final HttpRequestBase method;
	protected String url;
	protected HttpContext context;
	protected Map<String, String[]> headers;
	private IClientPluginAccess plugin;

	public BaseRequest()
	{
		method = null;
	}//only used by script engine

	public BaseRequest(String url, DefaultHttpClient hc, HttpRequestBase method, IClientPluginAccess plugin)
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
		this.method = method;
		this.plugin = plugin;
	}

	/**
	 * Add a header to the request.
	 *
	 * @sample
	 * method.addHeader('Content-type','text/xml; charset=ISO-8859-1')
	 *
	 * @param headerName 
	 * @param value 
	 */
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

	/**
	 * Execute the request method.
	 *
	 * @sample
	 * var response = method.executeRequest()
	 *
	 */
	public Response js_executeRequest()
	{
		return js_executeRequest(null, null);
	}

	/**
	 * @clonedesc js_executeRequest()
	 * @sampleas js_executeRequest()
	 *
	 * @param userName the user name
	 * @param password the password
	 */
	public Response js_executeRequest(String userName, String password)
	{
		try
		{
			return executeRequest(userName, password);
		}
		catch (Exception ex)
		{
			Debug.error(ex);
			return null;
		}
	}

	private Response executeRequest(String userName, String password) throws ClientProtocolException, IOException
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

	/**
	 * Execute the request method asynchronous. Success callback method will be called when response is received. Response is sent as parameter in callback. If no response is received (request errors out), the errorCallbackMethod is called with exception message as parameter.
	 *
	 * @sample
	 * var response = method.executeAsyncRequest(globals.successCallback,globals.errorCallback)
	 * 
	 * @param successCallbackMethod callbackMethod to be called after response is received
	 * @param errorCallbackMethod callbackMethod to be called if request errors out
	 *
	 */
	public void js_executeAsyncRequest(Function successCallbackMethod, Function errorCallbackMethod)
	{
		js_executeAsyncRequest(null, null, successCallbackMethod, errorCallbackMethod);
	}

	/**
	 * @clonedesc js_executeAsyncRequest(Function,Function)
	 * @sampleas js_executeAsyncRequest(Function,Function)
	 *
	 * @param userName the user name
	 * @param password the password
	 * @param successCallbackMethod callbackMethod to be called after response is received
	 * @param errorCallbackMethod callbackMethod to be called if request errors out
	 */

	public void js_executeAsyncRequest(final String username, final String password, Function successCallbackMethod, Function errorCallbackMethod)
	{
		final FunctionDefinition successFunctionDef = successCallbackMethod != null ? new FunctionDefinition(successCallbackMethod) : null;
		final FunctionDefinition errorFunctionDef = errorCallbackMethod != null ? new FunctionDefinition(errorCallbackMethod) : null;

		Runnable runnable = new Runnable()
		{
			public void run()
			{
				try
				{
					final Response response = executeRequest(username, password);

					if (successFunctionDef != null)
					{
						successFunctionDef.executeAsync(plugin, new Object[] { response });
					}
				}
				catch (final Exception ex)
				{
					if (errorFunctionDef != null)
					{
						errorFunctionDef.executeAsync(plugin, new Object[] { ex.getMessage() });
					}
				}
			}
		};
		plugin.getExecutor().execute(runnable);
	}
}
