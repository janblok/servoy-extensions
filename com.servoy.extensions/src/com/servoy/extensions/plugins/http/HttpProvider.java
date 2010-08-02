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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * @author jblok
 */
public class HttpProvider implements IScriptObject
{
	private String lastPageEncoding = null;
	private String proxyUser = null;
	private String proxyPassword = null;
	private int timeout = -1;

	public HttpProvider(HttpPlugin plugin)
	{
	}

	/**
	 * Get all page html in a variable (authentication only works with http client usage). If name is provided a http client will be created/used.
	 *
	 * @sample
	 * // get data using a default connection
	 * var pageData = plugins.http.getPageData('http://www.cnn.com');
	 * // create an http client and use it to get the data
	 * var pageData = plugins.http.getPageData('http://www.cnn.com','myclient');
	 *
	 * @param url 
	 *
	 * @param [http_clientname] optional 
	 *
	 * @param [username optional 
	 *
	 * @param password] 
	 */
	public String js_getPageData(Object[] vargs)
	{
		String a_url = null;
		String clientname = null;
		String username = null;
		String password = null;
		if (vargs.length >= 1) a_url = (String)vargs[0];
		if (a_url == null || a_url.trim().length() == 0) return "";
		if (vargs.length == 1)
		{
			return getPageDataOldImplementation(a_url);
		}
		if (vargs.length >= 2) clientname = (String)vargs[1];
		if (vargs.length >= 3) username = "" + vargs[2];
		if (vargs.length >= 4) password = "" + vargs[3];
		try
		{
			HttpClient client = getOrCreateHTTPclient(clientname, a_url);
			client.getParams().setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
			GetMethod get = new GetMethod(a_url);
			get.setFollowRedirects(true);
			if (vargs.length == 4) client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			client.executeMethod(get); // you can get the status from here... (return code)
			lastPageEncoding = get.getResponseCharSet();
			return get.getResponseBodyAsString();
		}
		catch (Exception e)
		{
			Debug.error(e);
			return "";//$NON-NLS-1$
		}
	}

	public String getPageDataOldImplementation(String input)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			URL url = new URL(input);
			URLConnection connection = url.openConnection();
			if (timeout >= 0) connection.setConnectTimeout(timeout);
			InputStream is = connection.getInputStream();
			final String type = connection.getContentType();
			String charset = null;
			if (type != null)
			{
				final String[] parts = type.split(";");
				for (int i = 1; i < parts.length && charset == null; i++)
				{
					final String t = parts[i].trim();
					final int index = t.toLowerCase().indexOf("charset=");
					if (index != -1) charset = t.substring(index + 8);
				}
			}
			lastPageEncoding = charset;
			InputStreamReader isr = null;
			if (charset != null) isr = new InputStreamReader(is, charset);
			else isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			int read = 0;
			while ((read = br.read()) != -1)
			{
				sb.append((char)read);
			}
			br.close();
			isr.close();
			is.close();
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return sb.toString();
	}

	private HttpClient getOrCreateHTTPclient(String name, String url)
	{
		HttpClient client = httpClients.get(name);
		if (client == null)
		{
			client = new HttpClient();
		}
		if (name != null) httpClients.put(name, client);
		if (url != null) setHttpClientProxy(client, url);
		return client;
	}

	private void setHttpClientProxy(HttpClient client, String url)
	{
		String proxyHost = null;
		int proxyPort = 8080;
		try
		{
			System.setProperty("java.net.useSystemProxies", "true");
			URI uri = new URI(url);
			List<Proxy> proxies = ProxySelector.getDefault().select(uri);
			if (proxies != null && client != null)
			{
				for (Proxy proxy : proxies)
				{
					if (proxy.address() != null && proxy.address() instanceof InetSocketAddress)
					{
						InetSocketAddress address = (InetSocketAddress)proxy.address();
						proxyHost = address.getHostName();
						client.getHostConfiguration().setProxy(address.getHostName(), address.getPort());
						// no proxy credentials ? seems not
						break;
					}
				}
			}
		}
		catch (Exception ex)
		{
			Debug.log(ex);
		}
		if (proxyHost == null && System.getProperty("http.proxyHost") != null && !"".equals(System.getProperty("http.proxyHost")))
		{
			proxyHost = System.getProperty("http.proxyHost");
			try
			{
				proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
			}
			catch (Exception ex)
			{
				//ignore
			}
			client.getHostConfiguration().setProxy(proxyHost, proxyPort);

		}
		if (proxyUser != null) client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyUser, proxyPassword));

	}

	/**
	 * Set the proxy username and password. Used for named http clients ( else use implicit java data).
	 *
	 * @sample plugins.http.setClientProxyUserNamePassword('my_proxy_username','my_proxy_password');
	 *
	 * @param username 
	 *
	 * @param password 
	 */
	public void js_setClientProxyUserNamePassword(Object[] args)
	{
		if (args == null || args.length != 2) return;
		this.proxyUser = (String)args[0];
		this.proxyPassword = (String)args[1];
	}

	public String js_getLastPageEncoding()
	{
		return lastPageEncoding;
	}

	/**
	 * Get the charset of the last page received with getPageData(...)
	 *
	 * @sample
	 * var a = plugins.http.getPageData('http://www.google.com.hk');
	 * var charset = plugins.http.getLastPageCharset();
	 * var success = plugins.file.writeTXTFile('someFilePath', a, charset);
	 * if (!success) plugins.dialogs.showWarningDialog('Warning', 'Could not write file', 'OK');
	 */
	public String js_getLastPageCharset()
	{
		return lastPageEncoding;
	}

	/**
	 * Get media(binary data) such as images in a variable; it also supports gzip-ed content
	 *
	 * @sample 
	 * var image_byte_array = plugins.http.getMediaData('http://www.cnn.com/cnn.gif');
	 * var image_byte_array2 = plugins.http.getMediaData('http://www.cnn.com/cnn.gif', 'clientName');
	 *
	 * @param url 
	 *
	 * @param [http_clientname] optional 
	 */
	public byte[] js_getMediaData(Object[] args)
	{
		if (args.length == 0) return null;
		String input = (String)args[0];
		ByteArrayOutputStream sb = new ByteArrayOutputStream();
		if (args.length == 1)
		{
			try
			{
				URL url = new URL(input);
				URLConnection connection = url.openConnection();
				if (timeout >= 0) connection.setConnectTimeout(timeout);
				InputStream is = connection.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				Utils.streamCopy(bis, sb);
				bis.close();
				is.close();
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
		}
		else
		{
			String clientName = "" + args[1]; //$NON-NLS-1$
			HttpClient client = httpClients.get(clientName);
			if (client == null)
			{
				return null;
			}
			try
			{
				setHttpClientProxy(client, input);
				URL url = new URL(input);
				GetMethod method = new GetMethod(input);

				//for some proxies
				method.addRequestHeader("Cache-control", "no-cache");
				method.addRequestHeader("Pragma", "no-cache");
				//maybe mod_deflate set and wrong configured
				method.addRequestHeader("Accept-Encoding", "gzip");
				int statusCode = client.executeMethod(method);
				if (statusCode != HttpStatus.SC_OK)
				{
					String statusText = HttpStatus.getStatusText(statusCode);
					return null;
				}

				InputStream is = null;
				Header contentEncoding = method.getResponseHeader("Content-Encoding");
				boolean gziped = contentEncoding == null ? false : "gzip".equalsIgnoreCase(contentEncoding.getValue());
				is = method.getResponseBodyAsStream();
				if (gziped)
				{
					is = new GZIPInputStream(is);
				}
				BufferedInputStream bis = new BufferedInputStream(is);
				Utils.streamCopy(bis, sb);
				bis.close();
				is.close();
			}
			catch (HttpException e)
			{
				Debug.error(e);
			}
			catch (IOException e)
			{
				Debug.error(e);
			}
		}
		return sb.toByteArray();
	}

	/**
	 * put a file at the specified URL
	 *
	 * @sample var fileAdded = plugins.http.put('clientName', 'http://www.abc.com/put_stuff.jsp', 'manual.doc', 'c:/temp/manual_01a.doc', 'user', 'password')
	 *
	 * @param clientName 
	 *
	 * @param url 
	 *
	 * @param fileName 
	 *
	 * @param filePath 
	 *
	 * @param [username] optional 
	 *
	 * @param [password] optional 
	 */
	public boolean js_put(Object[] vargs)
	{
		String clientName = "", url = "", fileName = "", path = "", username = "", password = "";
		if (vargs.length >= 1) clientName = (String)vargs[0];
		if (vargs.length >= 2) url = (String)vargs[1];
		if (vargs.length >= 3) fileName = (String)vargs[2];
		if (vargs.length >= 4) path = (String)vargs[3];
		if (vargs.length >= 5) username = (String)vargs[4];
		if (vargs.length >= 6) password = (String)vargs[5];

		if ("".equals(clientName.trim())) return false;

		try
		{
			URL _url = new URL(url);
		}
		catch (MalformedURLException e)
		{
			return false;
		}
		PutMethod method = new PutMethod(url + "/" + fileName);
		HttpClient client = getOrCreateHTTPclient(clientName, url);
		File file = new File(path);

		if (!"".equals(username.trim()))
		{
			client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
		}
		int statusCode = 0, attempt = 0;

		if (file == null)
		{
			return false;
		}
		InputStream content = null;
		try
		{
			content = new FileInputStream(file);
		}
		catch (FileNotFoundException e)
		{
			Debug.error(e);
		}
		if (content == null)
		{
			return false;
		}
		method.setRequestEntity(new InputStreamRequestEntity(content));

		while (statusCode != HttpStatus.SC_OK && attempt++ < 10)
		{
			try
			{
				statusCode = client.executeMethod(method);
			}
			catch (Exception e)
			{
				if (attempt == 10)
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * add cookie to the specified client
	 *
	 * @sample
	 * var cookieSet = plugins.http.setHttpClientCookie('clientName', 'JSESSIONID', 'abc', 'localhost', '/', -1, false)
	 * if (cookieSet)
	 * 	//do something
	 *
	 * @param clientName 
	 *
	 * @param cookieName 
	 *
	 * @param cookieValue 
	 *
	 * @param [domain] optional 
	 *
	 * @param [path] optional 
	 *
	 * @param [maxAge] optional 
	 *
	 * @param [secure] optional 
	 */
	public boolean js_setHttpClientCookie(Object[] vargs)
	{
		String clientName = "", cookieName = "", cookieValue = "", domain = "", path = "";
		int maxAge = -1;
		boolean secure = false;

		if (vargs.length >= 1) clientName = (String)vargs[0];
		if (clientName == null || "".equals(clientName.trim())) return false;
		HttpClient client = httpClients.get(clientName);
		if (client == null) return false;

		if (vargs.length >= 2) cookieName = (String)vargs[1];
		if (vargs.length >= 3) cookieValue = (String)vargs[2];
		if (vargs.length >= 4) domain = (String)vargs[3];
		if (vargs.length >= 5) path = (String)vargs[4];
		if (vargs.length >= 6)
		{
			maxAge = Utils.getAsInteger(vargs[5]);
			//if something is wrong, 0 is returned
			if (maxAge == 0) maxAge = -1;
		}
		if (vargs.length >= 7) secure = Utils.getAsBoolean(vargs[6]);

		if (cookieName == null || "".equals(cookieName.trim())) return false;
		if (cookieValue == null || "".equals(cookieValue.trim())) return false;

		org.apache.commons.httpclient.Cookie cookie;
		if (vargs.length == 3) cookie = new org.apache.commons.httpclient.Cookie(domain, cookieName, cookieValue);
		else cookie = new org.apache.commons.httpclient.Cookie(domain, cookieName, cookieValue, path, maxAge, secure);
		HttpState state = client.getState();
		state.addCookie(cookie);
		return true;
	}

	/**
	 * returns a Cookie array with all the cookies set on the specified client
	 *
	 * @sample var cookies = plugins.http.getHttpClientCookies('clientName')
	 *
	 * @param clientName 
	 */
	public com.servoy.extensions.plugins.http.Cookie[] js_getHttpClientCookies(String httpClientName)
	{
		if (httpClientName == null || "".equals(httpClientName.trim()))
		{
			return new Cookie[] { };
		}
		HttpClient httpClient = httpClients.get(httpClientName);
		if (httpClient == null)
		{
			return new Cookie[] { };
		}
		org.apache.commons.httpclient.Cookie[] cookies = httpClient.getState().getCookies();
		com.servoy.extensions.plugins.http.Cookie[] cookieObjects = new com.servoy.extensions.plugins.http.Cookie[cookies.length];
		for (int i = 0; i < cookies.length; i++)
		{
			cookieObjects[i] = new com.servoy.extensions.plugins.http.Cookie(cookies[i]);
		}
		return cookieObjects;
	}

	/**
	 * get cookie object from the specified client
	 *
	 * @sample
	 * var cookie = plugins.http.getHttpClientCookie('clientName', 'JSESSIONID');
	 * if (cookie != null) 
	 * {
	 * 	// do something
	 * }
	 * else
	 * {
	 * 	plugins.http.setHttpClientCookie('clientName', 'JSESSIONID', 'abc', 'localhost', '/', -1, false)
	 * }
	 *
	 * @param clientName 
	 *
	 * @param cookieName 
	 */
	public com.servoy.extensions.plugins.http.Cookie js_getHttpClientCookie(String clientName, String cookieName)
	{
		HttpClient client = httpClients.get(clientName);
		if (client == null) return null;
		HttpState state = client.getState();
		org.apache.commons.httpclient.Cookie[] cookies = state.getCookies();
		for (org.apache.commons.httpclient.Cookie element : cookies)
		{
			if (element.getName().equals(cookieName)) return new com.servoy.extensions.plugins.http.Cookie(element);
		}
		return null;
	}

	/**
	 * Get poster object to do http (file) posts (if posting files it will post multipart!)
	 *
	 * @sample
	 * var poster = plugins.http.getPoster('http://www.abc.com/apply_form.jsp');
	 * var didAddParam = poster.addParameter('myParamName','myValue');
	 * var didAddFile = poster.addFile('myFileParamName','manual.doc','c:/temp/manual_01a.doc');
	 * var httpCode = poster.doPost('username','mypassword'); //httpCode 200 is ok
	 * //var httpCode = poster.doPost('username','mypassword'); //use if authentication is needed
	 * var pageData = poster.getPageData()
	 *
	 * @param url 
	 *
	 * @param [http_clientname] optional 
	 */
	public Poster js_getPoster(Object[] vargs)//http://jakarta.apache.org/commons/httpclient/apidocs/index.html
	{
		String a_url = null;
		String clientname = null;
		if (vargs.length >= 1) a_url = (String)vargs[0];
		if (vargs.length >= 2) clientname = (String)vargs[1];
		HttpClient client = null;
		if (clientname != null)
		{
			client = getOrCreateHTTPclient(clientname, a_url);
		}
		return new Poster(a_url, client);
	}

	private final Map<String, HttpClient> httpClients = new HashMap<String, HttpClient>();

	/**
	 * Create a named http client (like a web browser with session binding) usable todo multiple request/posts in same server session
	 *
	 * @sample
	 * plugins.http.createHttpClient('mybrowser');
	 * var pageData = plugins.http.getPageData('http://www.cnn.com','mybrowser','username','mypassword');
	 *
	 * @param http_clientname 
	 */
	public void js_createHttpClient(String name)
	{
		HttpClient client = getOrCreateHTTPclient(name, null);
		client.getParams().setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
		httpClients.put(name, client);
	}

	/**
	 * Delete a named http client
	 *
	 * @sample plugins.http.deleteHttpClient('mybrowser');
	 *
	 * @param http_clientname 
	 */
	public void js_deleteHttpClient(String name)
	{
		httpClients.remove(name);
	}

	public void js_setTimeout(Object[] args)
	{
		if (args == null || args.length == 0 || args.length > 2) return;
		int timeout = Utils.getAsInteger(args[0], true);
		String name = args.length == 2 ? (String)args[1] : null;
		if (name == null)
		{
			this.timeout = timeout;
		}
		else
		{
			HttpClient client = getOrCreateHTTPclient(name, null);
			if (client != null)
			{
				client.setTimeout(timeout);
			}
		}
	}

	public boolean isDeprecated(String methodName)
	{
		if ("getLastPageEncoding".equals(methodName))
		{
			return true;
		}
		return false;
	}

	public String[] getParameterNames(String methodName)
	{
		if ("getPageData".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "url", "[http_clientname]", "[username", "password]" };
		}
		else if ("getLastPageCharset".equals(methodName)) //$NON-NLS-1$
		{
			return new String[0];
		}
		else if ("setClientProxyUserNamePassword".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "username", "password" };
		}
		else if ("getMediaData".equals(methodName))
		{
			return new String[] { "url", "[http_clientname]" };
		}
		else if ("getPoster".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "url", "[http_clientname]" };
		}
		else if ("createHttpClient".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "http_clientname" };
		}
		else if ("deleteHttpClient".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "http_clientname" };
		}
		else if ("getHttpClientCookie".equals(methodName))
		{
			return new String[] { "clientName", "cookieName" };
		}
		else if ("getHttpClientCookies".equals(methodName))
		{
			return new String[] { "clientName" };
		}
		else if ("setHttpClientCookie".equals(methodName))
		{
			return new String[] { "clientName", "cookieName", "cookieValue", "[domain]", "[path]", "[maxAge]", "[secure]" };
		}
		else if ("put".equals(methodName))
		{
			return new String[] { "clientName", "url", "fileName", "filePath", "[username]", "[password]" };
		}
		else if ("setTimeout".equals(methodName))
		{
			return new String[] { "msTimeout", "[http_clientname]" };
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("getPageData".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("// get data using a default connection");
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var pageData = plugins.http.getPageData('http://www.cnn.com');\n"); //$NON-NLS-1$
			retval.append("// create an http client and use it to get the data");
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var pageData = plugins.http.getPageData('http://www.cnn.com','myclient');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("setClientProxyUserNamePassword".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nplugins.http.setClientProxyUserNamePassword('my_proxy_username','my_proxy_password');"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("getLastPageCharset".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nvar a = plugins.http.getPageData('http://www.google.com.hk');\nvar charset = plugins.http.getLastPageCharset();\nvar success = plugins.file.writeTXTFile('someFilePath', a, charset);\nif (!success) plugins.dialogs.showWarningDialog('Warning', 'Could not write file', 'OK');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("createHttpClient".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.http.createHttpClient('mybrowser');\n"); //$NON-NLS-1$
			retval.append("var pageData = plugins.http.getPageData('http://www.cnn.com','mybrowser','username','mypassword');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("deleteHttpClient".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.http.deleteHttpClient('mybrowser');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("getMediaData".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var image_byte_array = plugins.http.getMediaData('http://www.cnn.com/cnn.gif');\n"); //$NON-NLS-1$
			retval.append("var image_byte_array2 = plugins.http.getMediaData('http://www.cnn.com/cnn.gif', 'clientName');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("getPoster".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var poster = plugins.http.getPoster('http://www.abc.com/apply_form.jsp');\n"); //$NON-NLS-1$
			retval.append("var didAddParam = poster.addParameter('myParamName','myValue');\n"); //$NON-NLS-1$
			retval.append("var didAddFile = poster.addFile('myFileParamName','manual.doc','c:/temp/manual_01a.doc');\n"); //$NON-NLS-1$
			retval.append("var httpCode = poster.doPost('username','mypassword'); //httpCode 200 is ok\n"); //$NON-NLS-1$
			retval.append("//var httpCode = poster.doPost('username','mypassword'); //use if authentication is needed\n"); //$NON-NLS-1$
			retval.append("var pageData = poster.getPageData()\n");
			return retval.toString();
		}
		else if ("getHttpClientCookie".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var cookie = plugins.http.getHttpClientCookie('clientName', 'JSESSIONID');\n"); //$NON-NLS-1$
			retval.append("if (cookie != null)\n"); //$NON-NLS-1$
			retval.append("{\n"); //$NON-NLS-1$
			retval.append("\t// do something\n"); //$NON-NLS-1$
			retval.append("}\n"); //$NON-NLS-1$
			retval.append("else\n\t"); //$NON-NLS-1$
			retval.append("plugins.http.setHttpClientCookie('clientName', 'JSESSIONID', 'abc', 'localhost', '/', -1, false)\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("setHttpClientCookie".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var cookieSet = plugins.http.setHttpClientCookie('clientName', 'JSESSIONID', 'abc', 'localhost', '/', -1, false)\n");
			retval.append("if (cookieSet)\n");
			retval.append("{\n"); //$NON-NLS-1$
			retval.append("\t//do something\n");
			retval.append("}\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("getHttpClientCookies".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var cookies = plugins.http.getHttpClientCookies('clientName')\n");
			return retval.toString();
		}
		else if ("put".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var fileAdded = plugins.http.put('clientName', 'http://www.abc.com/put_stuff.jsp', 'manual.doc', 'c:/temp/manual_01a.doc', 'user', 'password')\n");
			return retval.toString();
		}
		else if ("setTimeout".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.http.setTimeout(1000,'client_name')\n");
			return retval.toString();
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(String)
	 */
	public String getToolTip(String methodName)
	{
		if ("getPageData".equals(methodName)) //$NON-NLS-1$
		{
			return "Get all page html in a variable (authentication only works with http client usage). If name is provided a http client will be created/used."; //$NON-NLS-1$
		}
		else if ("setClientProxyUserNamePassword".equals(methodName)) //$NON-NLS-1$
		{
			return "Set the proxy username and password. Used for named http clients ( else use implicit java data)."; //$NON-NLS-1$
		}
		else if ("getLastPageCharset".equals(methodName)) //$NON-NLS-1$
		{
			return "Get the charset of the last page received with getPageData(...)"; //$NON-NLS-1$
		}
		else if ("createHttpClient".equals(methodName)) //$NON-NLS-1$
		{
			return "Create a named http client (like a web browser with session binding) usable todo multiple request/posts in same server session."; //$NON-NLS-1$
		}
		else if ("deleteHttpClient".equals(methodName)) //$NON-NLS-1$
		{
			return "Delete a named http client."; //$NON-NLS-1$
		}
		else if ("getMediaData".equals(methodName)) //$NON-NLS-1$
		{
			return "Get media (binary data) such as images in a variable. It also supports gzip-ed content."; //$NON-NLS-1$
		}
		else if ("getPoster".equals(methodName)) //$NON-NLS-1$
		{
			return "Get poster object to do http (file) posts. If posting files, it will post multipart!"; //$NON-NLS-1$
		}
		else if ("getHttpClientCookie".equals(methodName))
		{
			return "Get cookie object from the specified client.";
		}
		else if ("setHttpClientCookie".equals(methodName))
		{
			return "Add cookie to the specified client.";
		}
		else if ("getHttpClientCookies".equals(methodName))
		{
			return "Returns a Cookie array with all the cookies set on the specified client.";
		}
		else if ("put".equals(methodName))
		{
			return "Put a file at the specified URL.";
		}
		else if ("setTimeout".equals(methodName))
		{
			return "Sets a timeout in milliseconds for retrieving of data (when 0 there is no timeout).";
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class[] getAllReturnedTypes()
	{
		return new Class[] { Poster.class, Cookie.class };
	}

	//old test method
	public static void main(String[] args) throws Exception
	{
		if (args.length != 3)
		{
			System.out.println("Use: ContentFetcher mainurl contenturl destdir"); //$NON-NLS-1$
			System.out.println("Example: ContentFetcher http://site.com http://site.com/dir[0-2]/image_A[001-040].jpg c:/temp"); //$NON-NLS-1$
			System.out.println("Result: accessing http://site.com for cookie, reading http://site.com/dir1/image_A004.jpg writing c:/temp/dir_1_image_A004.jpg"); //$NON-NLS-1$
		}
		else
		{
			String url = args[1];
			String destdir = args[2];

			List parts = new ArrayList();
			int dir_from = 0;
			int dir_to = 0;
			int dir_fill = 0;
			int from = 0;
			int to = 0;
			int fill = 0;

			StringTokenizer tk = new StringTokenizer(url, "[]", true); //$NON-NLS-1$
			boolean hasDir = (tk.countTokens() > 5);
			boolean inDir = hasDir;
			System.out.println("hasDir " + hasDir); //$NON-NLS-1$
			boolean inTag = false;
			while (tk.hasMoreTokens())
			{
				String token = tk.nextToken();
				if (token.equals("[")) //$NON-NLS-1$
				{
					inTag = true;
					continue;
				}
				if (token.equals("]")) //$NON-NLS-1$
				{
					inTag = false;
					if (inDir) inDir = false;
					continue;
				}
				if (inTag)
				{
					int idx = token.indexOf('-');
					String s_from = token.substring(0, idx);
					int a_from = new Integer(s_from).intValue();
					int a_fill = s_from.length();
					int a_to = new Integer(token.substring(idx + 1)).intValue();
					if (inDir)
					{
						dir_from = a_from;
						dir_to = a_to;
						dir_fill = a_fill;
					}
					else
					{
						from = a_from;
						to = a_to;
						fill = a_fill;
					}
				}
				else
				{
					parts.add(token);
				}
			}

			HttpClient client = new HttpClient();
			client.getParams().setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
			GetMethod main = new GetMethod(args[0]);
			main.setFollowRedirects(true);
			int main_rs = client.executeMethod(main);
			if (main_rs != 200)
			{
				System.out.println("main page retrieval failed " + main_rs); //$NON-NLS-1$
				return;
			}

			for (int d = dir_from; d <= dir_to; d++)
			{
				String dir_number = "" + d; //$NON-NLS-1$
				if (dir_fill > 1)
				{
					dir_number = "000000" + d; //$NON-NLS-1$
					int dir_digits = (int)(Math.log(fill) / Math.log(10));
					System.out.println("dir_digits " + dir_digits); //$NON-NLS-1$
					dir_number = dir_number.substring(dir_number.length() - (dir_fill - dir_digits), dir_number.length());
				}
				for (int i = from; i <= to; i++)
				{
					try
					{
						String number = "" + i; //$NON-NLS-1$
						if (fill > 1)
						{
							number = "000000" + i; //$NON-NLS-1$
							int digits = (int)(Math.log(fill) / Math.log(10));
							System.out.println("digits " + digits); //$NON-NLS-1$
							number = number.substring(number.length() - (fill - digits), number.length());
						}
						int part = 0;
						StringBuffer surl = new StringBuffer((String)parts.get(part++));
						if (hasDir)
						{
							surl.append(dir_number);
							surl.append(parts.get(part++));
						}
						surl.append(number);
						surl.append(parts.get(part++));
						System.out.println("reading url " + surl); //$NON-NLS-1$

						int indx = surl.toString().lastIndexOf('/');
						StringBuffer sfile = new StringBuffer(destdir);
						sfile.append("\\"); //$NON-NLS-1$
						if (hasDir)
						{
							sfile.append("dir_"); //$NON-NLS-1$
							sfile.append(dir_number);
							sfile.append("_"); //$NON-NLS-1$
						}
						sfile.append(surl.toString().substring(indx + 1));
						File file = new File(sfile.toString());
						if (file.exists())
						{
							file = new File("" + System.currentTimeMillis() + sfile.toString());
						}
						System.out.println("write file " + file.getAbsolutePath()); //$NON-NLS-1$

//						URL iurl = new URL(surl.toString());
						GetMethod get = new GetMethod(surl.toString());
						get.setFollowRedirects(true);

						int result = client.executeMethod(get);
						System.out.println("page http result " + result); //$NON-NLS-1$
						if (result == 200)
						{
							InputStream is = get.getResponseBodyAsStream();//iurl.openStream();
							FileOutputStream fos = new FileOutputStream(file);
							Utils.streamCopy(is, fos);
							fos.close();
						}
					}
					catch (Exception e)
					{
						System.err.println(e);
					}
				}
			}
		}
	}

}
