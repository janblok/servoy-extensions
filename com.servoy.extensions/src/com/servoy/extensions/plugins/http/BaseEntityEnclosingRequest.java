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

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import com.servoy.base.scripting.annotations.ServoyClientSupport;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.util.Utils;

/**
 * @author pbakker
 */
@ServoyClientSupport(ng = false, wc = true, sc = true)
public class BaseEntityEnclosingRequest extends BaseRequest
{
	private String content;
	private String mimeType;
	protected String charset = HTTP.UTF_8;

	public BaseEntityEnclosingRequest()
	{
	}

	public BaseEntityEnclosingRequest(String url, DefaultHttpClient hc, HttpRequestBase method, IClientPluginAccess plugin)
	{
		super(url, hc, method, plugin);
	}

	/**
	 * Set the body of the request.
	 *
	 * @sample
	 * method.setBodyContent(content)
	 *
	 * @param content 
	 */
	public void js_setBodyContent(String content)
	{
		this.content = content;
	}

	/**
	 * Set the body of the request and content mime type.
	 *
	 * @sample
	 * method.setBodyContent(content, 'text/xml')
	 *
	 * @param content
	 * @param mimeType 
	 */
	public void js_setBodyContent(String content, String mimeType)
	{
		this.content = content;
		this.mimeType = mimeType;
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
	protected HttpEntity buildEntity() throws Exception
	{
		if (!Utils.stringIsEmpty(content))
		{
			StringEntity se = new StringEntity(content, mimeType, charset);
			content = null;
			return se;
		}
		return null;
	}
}
