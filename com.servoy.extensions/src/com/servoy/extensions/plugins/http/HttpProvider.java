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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.servoy.j2db.MediaURLStreamHandler;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * @author jblok
 */
public class HttpProvider implements IScriptObject
{
	private String proxyUser = null;
	private String proxyPassword = null;
	private int timeout = -1;

	@Deprecated
	private String lastPageEncoding = null;
	private final IClientPluginAccess access;

	public HttpProvider(IClientPluginAccess access)
	{
		this.access = access;
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
			DefaultHttpClient client = getOrCreateHTTPclient(clientname, a_url);
			HttpGet get = new HttpGet(a_url);
			URL _url = new URL(a_url);
			if (vargs.length == 4)
			{
				BasicCredentialsProvider bcp = new BasicCredentialsProvider();
				bcp.setCredentials(new AuthScope(_url.getHost(), _url.getPort()), new UsernamePasswordCredentials(username, password));
				client.setCredentialsProvider(bcp);
			}
			HttpResponse res = client.execute(get); // you can get the status from here... (return code)
			lastPageEncoding = EntityUtils.getContentCharSet(res.getEntity());
			return EntityUtils.toString(res.getEntity());
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

	private DefaultHttpClient getOrCreateHTTPclient(String name, String url)
	{
		DefaultHttpClient client = httpClients.get(name);
		if (client == null)
		{
			client = new DefaultHttpClient();
			client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

			if (name != null)
			{
				httpClients.put(name, client);
			}
			if (url != null)
			{
				setHttpClientProxy(client, url, proxyUser, proxyPassword);
			}
		}
		return client;
	}

	public static void setHttpClientProxy(DefaultHttpClient client, String url, String proxyUser, String proxyPassword)
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
						HttpHost host = new HttpHost(address.getHostName(), address.getPort());
						client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
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
			HttpHost host = new HttpHost(proxyHost, proxyPort);
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
		}

		if (proxyUser != null)
		{
			BasicCredentialsProvider bcp = new BasicCredentialsProvider();
			bcp.setCredentials(new AuthScope(proxyHost, proxyPort), new UsernamePasswordCredentials(proxyUser, proxyPassword));
			client.setCredentialsProvider(bcp);
		}
	}

	@Deprecated
	public void js_setClientProxyUserNamePassword(String userName, String password)
	{
		js_setClientProxyCredentials(userName, password);
	}

	/**
	 * Set the proxy username and password. Used for named http clients (else use implicit java data).
	 *
	 * @sample plugins.http.setClientProxyCredentials('my_proxy_username','my_proxy_password');
	 *
	 * @param username 
	 *
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

	@Deprecated
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
	@Deprecated
	public String js_getLastPageCharset()
	{
		return lastPageEncoding;
	}

	/**
	 * Get media(binary data) such as images in a variable; it also supports gzip-ed content
	 *
	 * @sample 
	 * var image_byte_array = plugins.http.getMediaData('http://www.cnn.com/cnn.gif');
	 *
	 * @param url 
	 *
	 */
	public byte[] js_getMediaData(String url)
	{
		if (url == null) return null;
		ByteArrayOutputStream sb = new ByteArrayOutputStream();
		try
		{
			URLConnection connection;
			if (url.startsWith(MediaURLStreamHandler.MEDIA_URL_DEF))
			{
				connection = new URL(null, url, access.getMediaURLStreamHandler()).openConnection();
			}
			else
			{
				connection = new URL(url).openConnection();
			}
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
		return sb.toByteArray();
	}

	/**
	 * Get media(binary data) such as images in a variable; it also supports gzip-ed content
	 *
	 * @sample 
	 * var image_byte_array2 = plugins.http.getMediaData('http://www.cnn.com/cnn.gif', 'clientName');
	 *
	 * @param url 
	 * @param clientName 
	 */
	@Deprecated
	public byte[] js_getMediaData(String url, String clientName)
	{
		if (url == null || clientName == null) return null;
		DefaultHttpClient client = httpClients.get(clientName);
		if (client == null)
		{
			return null;
		}
		ByteArrayOutputStream sb = new ByteArrayOutputStream();
		try
		{
			setHttpClientProxy(client, url, proxyUser, proxyPassword);

			HttpGet method = new HttpGet(url);

			//for some proxies
			method.addHeader("Cache-control", "no-cache");
			method.addHeader("Pragma", "no-cache");
			//maybe mod_deflate set and wrong configured
			method.addHeader("Accept-Encoding", "gzip");
			HttpResponse res = client.execute(method);
			int statusCode = res.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK)
			{
				return null;
			}

			InputStream is = null;
			Header contentEncoding = res.getFirstHeader("Content-Encoding");
			boolean gziped = contentEncoding == null ? false : "gzip".equalsIgnoreCase(contentEncoding.getValue());
			is = res.getEntity().getContent();
			if (gziped)
			{
				is = new GZIPInputStream(is);
			}
			BufferedInputStream bis = new BufferedInputStream(is);
			Utils.streamCopy(bis, sb);
			bis.close();
			is.close();
		}
		catch (IOException e)
		{
			Debug.error(e);
		}
		return sb.toByteArray();
	}

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

		int status = 0;
		try
		{
			URL _url = new URL(url);
			HttpPut method = new HttpPut(url + "/" + fileName);
			DefaultHttpClient client = getOrCreateHTTPclient(clientName, url);
			File file = new File(path);

			if (!"".equals(username.trim()))
			{
				BasicCredentialsProvider bcp = new BasicCredentialsProvider();
				bcp.setCredentials(new AuthScope(_url.getHost(), _url.getPort()), new UsernamePasswordCredentials(username, password));
				client.setCredentialsProvider(bcp);
			}
			int statusCode = 0, attempt = 0;

			if (file == null || !file.exists())
			{
				return false;
			}
			method.setEntity(new FileEntity(file, "binary/octet-stream"));
			HttpResponse res = client.execute(method);
			status = res.getStatusLine().getStatusCode();
		}
		catch (IOException e)
		{
			return false;
		}

		return (status == HttpStatus.SC_OK);
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
	@Deprecated
	public boolean js_setHttpClientCookie(Object[] vargs)
	{
		String clientName = "", cookieName = "", cookieValue = "", domain = "", path = "";
		int maxAge = -1;
		boolean secure = false;

		if (vargs.length >= 1) clientName = (String)vargs[0];
		if (clientName == null || "".equals(clientName.trim())) return false;
		DefaultHttpClient client = httpClients.get(clientName);
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

		BasicClientCookie cookie;
		cookie = new BasicClientCookie(cookieName, cookieValue);
		if (vargs.length > 3)
		{
			cookie.setPath(path);
			cookie.setExpiryDate(new Date(System.currentTimeMillis() + maxAge));
			cookie.setSecure(secure);
		}
		cookie.setDomain(domain);
		client.getCookieStore().addCookie(cookie);
		return true;
	}

	/**
	 * returns a Cookie array with all the cookies set on the specified client
	 *
	 * @sample var cookies = plugins.http.getHttpClientCookies('clientName')
	 *
	 * @param clientName 
	 */
	@Deprecated
	public Cookie[] js_getHttpClientCookies(String httpClientName)
	{
		if (httpClientName == null || "".equals(httpClientName.trim()))
		{
			return new Cookie[] { };
		}
		DefaultHttpClient httpClient = httpClients.get(httpClientName);
		if (httpClient == null)
		{
			return new Cookie[] { };
		}
		List<org.apache.http.cookie.Cookie> cookies = httpClient.getCookieStore().getCookies();
		Cookie[] cookieObjects = new Cookie[cookies.size()];
		for (int i = 0; i < cookies.size(); i++)
		{
			cookieObjects[i] = new Cookie(cookies.get(i));
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
	@Deprecated
	public Cookie js_getHttpClientCookie(String clientName, String cookieName)
	{
		DefaultHttpClient client = httpClients.get(clientName);
		if (client == null) return null;
		List<org.apache.http.cookie.Cookie> cookies = client.getCookieStore().getCookies();
		for (org.apache.http.cookie.Cookie element : cookies)
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
	@Deprecated
	public PostRequest js_getPoster(Object[] vargs)//http://jakarta.apache.org/commons/httpclient/apidocs/index.html
	{
		String a_url = null;
		String clientname = null;
		if (vargs.length >= 1) a_url = (String)vargs[0];
		if (vargs.length >= 2) clientname = (String)vargs[1];
		DefaultHttpClient client = null;
		if (clientname != null)
		{
			client = getOrCreateHTTPclient(clientname, a_url);
		}
		return new PostRequest(a_url, client);
	}

	@Deprecated
	private final Map<String, DefaultHttpClient> httpClients = new HashMap<String, DefaultHttpClient>();

	@Deprecated
	public void js_createHttpClient(String name)
	{
		getOrCreateHTTPclient(name, null);
	}

	public HttpClient js_createNewHttpClient()
	{
		return new HttpClient();
	}

	/**
	 * Delete a named http client
	 *
	 * @sample plugins.http.deleteHttpClient('mybrowser');
	 *
	 * @param http_clientname 
	 */
	@Deprecated
	public void js_deleteHttpClient(String name)
	{
		httpClients.remove(name);
	}

	@Deprecated
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
			DefaultHttpClient client = getOrCreateHTTPclient(name, null);
			if (client != null)
			{
				HttpParams params = client.getParams();
				HttpConnectionParams.setConnectionTimeout(params, timeout);
				HttpConnectionParams.setSoTimeout(params, timeout);
			}
		}
	}

	public boolean isDeprecated(String methodName)
	{
		String[] sa = new String[] { "setClientProxyUserNamePassword", "setClientProxyCredentials", "getPoster", "getLastPageEncoding", "getLastPageCharset", "deleteHttpClient", "setHttpClientCookie", "getHttpClientCookie", "getHttpClientCookies", "setTimeout", "put", "createHttpClient" };
		List<String> hs = Arrays.asList(sa);
		if (hs.contains(methodName))
		{
			return true;
		}
		return false;
	}

	@SuppressWarnings("nls")
	public String[] getParameterNames(String methodName)
	{
		if ("getPageData".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "url", "[http_clientname]", "[username]" };
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
			return new String[] { "name" };
		}
		else if ("createNewHttpClient".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { };
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
			retval.append("var client = plugins.http.createHttpClient();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("createNewHttpClient".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var client = plugins.http.createNewHttpClient();\n"); //$NON-NLS-1$
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
			return "Get all page html in a variable."; //$NON-NLS-1$
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
			return "Get or create an http client."; //$NON-NLS-1$
		}
		else if ("createNewHttpClient".equals(methodName)) //$NON-NLS-1$
		{
			return "Create an http client (like a web browser with session binding) usable todo multiple request/posts in same server session."; //$NON-NLS-1$
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
	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { PostRequest.class, PutRequest.class, GetRequest.class, DeleteRequest.class, OptionsRequest.class, HeadRequest.class, TraceRequest.class, Cookie.class, Response.class, HttpClient.class, HTTP_STATUS.class };
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

			DefaultHttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
			HttpGet main = new HttpGet(args[0]);
			HttpResponse res = client.execute(main);
			;
			int main_rs = res.getStatusLine().getStatusCode();
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
						HttpGet get = new HttpGet(surl.toString());

						HttpResponse response = client.execute(get);
						int result = response.getStatusLine().getStatusCode();
						System.out.println("page http result " + result); //$NON-NLS-1$
						if (result == 200)
						{
							InputStream is = response.getEntity().getContent();
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
