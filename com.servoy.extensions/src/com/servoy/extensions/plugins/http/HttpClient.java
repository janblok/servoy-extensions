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

import java.sql.Date;
import java.util.List;

import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.NegotiateSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Utils;

@ServoyDocumented
public class HttpClient implements IScriptable, IJavaScriptType
{
	DefaultHttpClient client;

	private String proxyUser = null;
	private String proxyPassword = null;

	public HttpClient()
	{
		client = new DefaultHttpClient();
		client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, Boolean.TRUE);
		client.getAuthSchemes().register(AuthPolicy.NTLM, new NTLMSchemeFactory());
		client.getAuthSchemes().register(AuthPolicy.SPNEGO, new NegotiateSchemeFactory());
	}

	/**
	 * Sets a timeout in milliseconds for retrieving of data (when 0 there is no timeout).
	 *
	 * @sample
	 * client.setTimeout(1000)
	 *
	 * @param msTimeout 
	 */
	public void js_setTimeout(int timeout)
	{
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
	}

	/**
	 * Add cookie to the this client.
	 *
	 * @sample
	 * var cookieSet = client.setCookie('JSESSIONID', 'abc', 'localhost', '/', -1, false)
	 * if (cookieSet)
	 * {
	 * 	//do something
	 * }
	 *
	 * @param cookieName 
	 * @param cookieValue 
	 */
	public boolean js_setCookie(String name, String value)
	{
		return js_setCookie(name, value, ""); //$NON-NLS-1$
	}

	/**
	 * @clonedesc js_setCookie(String, String)
	 * @sampleas js_setCookie(String, String)
	 *
	 * @param cookieName 
	 * @param cookieValue 
	 * @param domain
	 */
	public boolean js_setCookie(String name, String value, String domain)
	{
		return js_setCookie(name, value, domain, ""); //$NON-NLS-1$
	}

	/**
	 * @clonedesc js_setCookie(String, String)
	 * @sampleas js_setCookie(String, String)
	 *
	 * @param cookieName 
	 * @param cookieValue 
	 * @param domain
	 * @param path
	 */
	public boolean js_setCookie(String name, String value, String domain, String path)
	{
		return js_setCookie(name, value, domain, path, -1);
	}

	/**
	 * @clonedesc js_setCookie(String, String)
	 * @sampleas js_setCookie(String, String)
	 *
	 * @param cookieName 
	 * @param cookieValue 
	 * @param domain
	 * @param path
	 * @param maxAge
	 */
	public boolean js_setCookie(String name, String value, String domain, String path, int maxAge)
	{
		return js_setCookie(name, value, domain, path, maxAge, false);
	}

	/**
	 * @clonedesc js_setCookie(String, String)
	 * @sampleas js_setCookie(String, String)
	 *
	 * @param cookieName 
	 * @param cookieValue 
	 * @param domain
	 * @param path
	 * @param maxAge
	 * @param secure
	 */
	public boolean js_setCookie(String name, String value, String domain, String path, int maxAge, boolean secure)
	{
		//Correct to disallow empty Cookie values? how to clear a Cookie then?
		if (Utils.stringIsEmpty(name) || Utils.stringIsEmpty(value))
		{
			return false;
		}
		int age = maxAge;
		if (maxAge == 0)
		{
			age = -1;
		}

		BasicClientCookie cookie;
		cookie = new BasicClientCookie(name, value);
		if (!Utils.stringIsEmpty(path))
		{
			cookie.setPath(path);
			cookie.setExpiryDate(new Date(System.currentTimeMillis() + age));
			cookie.setSecure(secure);
		}
		cookie.setDomain(domain);
		client.getCookieStore().addCookie(cookie);
		return true;
	}

	/**
	 * Get a cookie by name.
	 *
	 * @sample
	 * var cookie = client.getCookie('JSESSIONID');
	 * if (cookie != null)
	 * {
	 * 	// do something
	 * }
	 * else
	 * 	client.setCookie('JSESSIONID', 'abc', 'localhost', '/', -1, false)
	 *
	 * @param cookieName 
	 */
	public Cookie js_getCookie(String cookieName)
	{
		List<org.apache.http.cookie.Cookie> cookies = client.getCookieStore().getCookies();
		for (org.apache.http.cookie.Cookie element : cookies)
		{
			if (element.getName().equals(cookieName)) return new com.servoy.extensions.plugins.http.Cookie(element);
		}
		return null;
	}

	/**
	 * Get all cookies from this client.
	 *
	 * @sample
	 * var cookies = client.getHttpClientCookies()
	 */
	public Cookie[] js_getCookies()
	{
		List<org.apache.http.cookie.Cookie> cookies = client.getCookieStore().getCookies();
		Cookie[] cookieObjects = new Cookie[cookies.size()];
		for (int i = 0; i < cookies.size(); i++)
		{
			cookieObjects[i] = new Cookie(cookies.get(i));
		}
		return cookieObjects;
	}

	/**
	 * Create a new request of specified type.
	 *
	 * @sample
	 * var request = client.createGetRequest('http://www.servoy.com');
	 *
	 * @param url 
	 */
	public PostRequest js_createPostRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new PostRequest(url, client);
	}

	/**
	 * Create a new request of specified type.
	 *
	 * @sample
	 * var request = client.createGetRequest('http://www.servoy.com');
	 *
	 * @param url 
	 */
	public GetRequest js_createGetRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new GetRequest(url, client);
	}

	/**
	 * Create a new request of specified type.
	 *
	 * @sample
	 * var request = client.createGetRequest('http://www.servoy.com');
	 *
	 * @param url 
	 */
	public DeleteRequest js_createDeleteRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new DeleteRequest(url, client);
	}

	/**
	 * Create a new request of specified type.
	 *
	 * @sample
	 * var request = client.createGetRequest('http://www.servoy.com');
	 *
	 * @param url 
	 */
	public PutRequest js_createPutRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new PutRequest(url, client);
	}

	/**
	 * Create a new request of specified type.
	 *
	 * @sample
	 * var request = client.createGetRequest('http://www.servoy.com');
	 *
	 * @param url 
	 */
	public OptionsRequest js_createOptionsRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new OptionsRequest(url, client);
	}

	/**
	 * Create a new request of specified type.
	 *
	 * @sample
	 * var request = client.createGetRequest('http://www.servoy.com');
	 *
	 * @param url 
	 */
	public HeadRequest js_createHeadRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new HeadRequest(url, client);
	}

	/**
	 * Create a new request of specified type.
	 *
	 * @sample
	 * var request = client.createGetRequest('http://www.servoy.com');
	 *
	 * @param url 
	 */
	public TraceRequest js_createTraceRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new TraceRequest(url, client);
	}

	/**
	 * Set proxy credentials.
	 *
	 * @sample
	 * client.setClientProxyCredentials('my_proxy_username','my_proxy_password');
	 *
	 * @param userName 
	 * @param password 
	 */
	public void js_setClientProxyCredentials(String userName, String password)
	{
		if (!Utils.stringIsEmpty(userName))
		{
			this.proxyUser = userName;
			this.proxyPassword = password;
		}
	}

}
