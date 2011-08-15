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

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * @author pbakker
 *
 */
public class BaseEntityEnclosingRequest extends BaseRequest
{
	protected String content;
	protected String charset = HTTP.UTF_8;

	public BaseEntityEnclosingRequest()
	{
	}

	public BaseEntityEnclosingRequest(String url, DefaultHttpClient hc, HttpRequestBase method)
	{
		super(url, hc, method);
	}

	public void js_setBodyContent(String s)
	{
		this.content = s;
	}

	public void js_setCharset(String s)
	{
		this.charset = s;
	}

	@Override
	public Response js_executeRequest(String userName, String password)
	{
		try
		{
			if (!Utils.stringIsEmpty(content))
			{
				((HttpEntityEnclosingRequestBase)method).setEntity(new StringEntity(content, charset));
			}
		}
		catch (UnsupportedEncodingException e)
		{
			Debug.error(e);
		}
		return super.js_executeRequest(userName, password);
	}

	@Override
	public String getSample(String methodName)
	{
		if ("setBodyContent".equals(methodName)) //$NON-NLS-1$
		{
			StringBuilder retval = new StringBuilder();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("method.setBodyContent(content)"); //$NON-NLS-1$
			return retval.toString();
		}
		if ("setCharset".equals(methodName))
		{
			StringBuilder retval = new StringBuilder();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nvar client = plugins.http.createNewHttpClient();");
			retval.append("\nvar poster = client.createPostRequest('https://twitter.com/statuses/update.json');");
			retval.append("\nposter.addParameter('status',globals.textToPost);");
			retval.append("\nposter.addParameter('source','Test Source');");
			retval.append("\nposter.setCharset('UTF-8');");
			retval.append("\nvar httpCode = poster.executeRequest(globals.twitterUserName, globals.twitterPassword).getStatusCode() // httpCode 200 is ok\n");
			return retval.toString();
		}
		return super.getSample(methodName);
	}

	@Override
	public String getToolTip(String methodName)
	{
		if ("setBodyContent".equals(methodName)) //$NON-NLS-1$
		{
			return "Set the body of the request."; //$NON-NLS-1$
		}
		if ("setCharset".equals(methodName)) //$NON-NLS-1$
		{
			return "Set the charset used when posting. If this is null or not called it will use the default charset (UTF-8)."; //$NON-NLS-1$
		}
		return super.getToolTip(methodName);
	}

	@Override
	public String[] getParameterNames(String methodName)
	{
		if ("setBodyContent".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "content" }; //$NON-NLS-1$
		}
		if ("setCharset".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "charset" }; //$NON-NLS-1$
		}

		return super.getParameterNames(methodName);
	}
}
