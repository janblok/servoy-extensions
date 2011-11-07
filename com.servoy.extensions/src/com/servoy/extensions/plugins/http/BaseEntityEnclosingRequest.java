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

	/**
	 * Set the body of the request.
	 *
	 * @sample
	 * method.setBodyContent(content)
	 *
	 * @param s 
	 */
	public void js_setBodyContent(String s)
	{
		this.content = s;
	}

	/**
	 * Set the charset used when posting. If this is null or not called it will use the default charset (UTF-8).
	 *
	 * @sample
	 * var client = plugins.http.createNewHttpClient();
	 * var poster = client.createPostRequest('https://twitter.com/statuses/update.json');
	 * poster.addParameter('status',scopes.globals.textToPost);
	 * poster.addParameter('source','Test Source');
	 * poster.setCharset('UTF-8');
	 * var httpCode = poster.executeRequest(scopes.globals.twitterUserName, scopes.globals.twitterPassword).getStatusCode() // httpCode 200 is ok
	 *
	 * @param charset 
	 */
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

}
