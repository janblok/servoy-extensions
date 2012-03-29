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

import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Utils;

public class HttpClient implements IScriptObject, IJavaScriptType
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

	public void js_setTimeout(int timeout)
	{
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
	}

	public boolean js_setCookie(String name, String value)
	{
		return js_setCookie(name, value, ""); //$NON-NLS-1$
	}

	public boolean js_setCookie(String name, String value, String domain)
	{
		return js_setCookie(name, value, domain, ""); //$NON-NLS-1$
	}

	public boolean js_setCookie(String name, String value, String domain, String path)
	{
		return js_setCookie(name, value, domain, path, -1);
	}

	public boolean js_setCookie(String name, String value, String domain, String path, int maxAge)
	{
		return js_setCookie(name, value, domain, path, maxAge, false);
	}

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

	public Cookie js_getCookie(String cookieName)
	{
		List<org.apache.http.cookie.Cookie> cookies = client.getCookieStore().getCookies();
		for (org.apache.http.cookie.Cookie element : cookies)
		{
			if (element.getName().equals(cookieName)) return new com.servoy.extensions.plugins.http.Cookie(element);
		}
		return null;
	}

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

	public PostRequest js_createPostRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new PostRequest(url, client);
	}

	public GetRequest js_createGetRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new GetRequest(url, client);
	}

	public DeleteRequest js_createDeleteRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new DeleteRequest(url, client);
	}

	public PutRequest js_createPutRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new PutRequest(url, client);
	}

	public OptionsRequest js_createOptionsRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new OptionsRequest(url, client);
	}

	public HeadRequest js_createHeadRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new HeadRequest(url, client);
	}

	public TraceRequest js_createTraceRequest(String url)
	{
		HttpProvider.setHttpClientProxy(client, url, proxyUser, proxyPassword);
		return new TraceRequest(url, client);
	}

	public void js_setClientProxyCredentials(String userName, String password)
	{
		if (!Utils.stringIsEmpty(userName))
		{
			this.proxyUser = userName;
			this.proxyPassword = password;
		}
	}

	public String[] getParameterNames(String methodName)
	{
		if ("setClientProxyCredentials".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "userName", "password" };
		}
		else if ("getCookie".equals(methodName))
		{
			return new String[] { "cookieName" };
		}
		else if ("getCookies".equals(methodName))
		{
			return new String[] { };
		}
		else if ("setCookie".equals(methodName))
		{
			return new String[] { "cookieName", "cookieValue", "[domain]", "[path]", "[maxAge]", "[secure]" };
		}
		else if ("setTimeout".equals(methodName))
		{
			return new String[] { "msTimeout" };
		}
		else if ("createPostRequest".equals(methodName) || "createGetRequest".equals(methodName) || "createPutRequest".equals(methodName) ||
			"createDeleteRequest".equals(methodName) || "createTraceRequest".equals(methodName) || "createHeadRequest".equals(methodName) ||
			"createOptionsRequest".equals(methodName))
		{
			return new String[] { "url" };
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("setTimeout".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("client.setTimeout(1000)\n");
			return retval.toString();
		}
		else if ("getCookie".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var cookie = client.getCookie('JSESSIONID');\n"); //$NON-NLS-1$
			retval.append("if (cookie != null)\n"); //$NON-NLS-1$
			retval.append("{\n"); //$NON-NLS-1$
			retval.append("\t// do something\n"); //$NON-NLS-1$
			retval.append("}\n"); //$NON-NLS-1$
			retval.append("else\n\t"); //$NON-NLS-1$
			retval.append("client.setCookie('JSESSIONID', 'abc', 'localhost', '/', -1, false)\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("setCookie".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var cookieSet = client.setCookie('JSESSIONID', 'abc', 'localhost', '/', -1, false)\n");
			retval.append("if (cookieSet)\n");
			retval.append("{\n"); //$NON-NLS-1$
			retval.append("\t//do something\n");
			retval.append("}\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("getCookies".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var cookies = client.getHttpClientCookies()\n");
			return retval.toString();
		}
		else if ("setClientProxyCredentials".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nclient.setClientProxyCredentials('my_proxy_username','my_proxy_password');"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("createPostRequest".equals(methodName))
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
		else if ("createGetRequest".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nvar client = plugins.http.createNewHttpClient();");//$NON-NLS-1$
			retval.append("\nvar request = client.createGetRequest('http://www.servoy.com');"); //$NON-NLS-1$
			retval.append("\nvar response = request.executeRequest();"); //$NON-NLS-1$
			retval.append("\nvar httpCode = response.getStatusCode(); // httpCode 200 is ok"); //$NON-NLS-1$
			retval.append("\nvar content = response.getResponseBody();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("createPutRequest".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nvar client = plugins.http.createNewHttpClient();");
			retval.append("\nvar putRequest = client.createPutRequest('http://jakarta.apache.org');");
			retval.append("\nputRequest.setFile('UploadMe.gif');");
			retval.append("\nvar httpCode = putRequest.executeRequest().getStatusCode() // httpCode 200 is ok\n");
			return retval.toString();
		}
		else if ("createDeleteRequest".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nvar client = plugins.http.createNewHttpClient();");
			retval.append("\nvar deleteRequest = client.createDeleteRequest('http://jakarta.apache.org/delete.me');");
			retval.append("\nvar response = deleteRequest.executeRequest();"); //$NON-NLS-1$
			retval.append("\nvar httpCode = response.getStatusCode() // httpCode 200 is ok\n");
			retval.append("\nvar content = response.getResponseBody();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("createTraceRequest".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nvar client = plugins.http.createNewHttpClient();");
			retval.append("\nvar traceRequest = client.createTraceRequest('http://www.servoy.com');");
			retval.append("\nvar response = traceRequest.executeRequest();"); //$NON-NLS-1$
			retval.append("\nvar httpCode = response.getStatusCode() // httpCode 200 is ok");
			retval.append("\nvar content = response.getResponseBody();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("createHeadRequest".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nvar client = plugins.http.createNewHttpClient();");//$NON-NLS-1$
			retval.append("\nvar request = client.createHeadRequest('http://www.servoy.com');"); //$NON-NLS-1$
			retval.append("\nvar response = request.executeRequest();"); //$NON-NLS-1$
			retval.append("\nvar httpCode = response.getStatusCode(); // httpCode 200 is ok"); //$NON-NLS-1$
			retval.append("\nvar header = response.getResponseHeaders('last-modified');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("createOptionsRequest".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nvar client = plugins.http.createNewHttpClient();");//$NON-NLS-1$
			retval.append("\nvar request = client.createOptionsRequest('http://www.servoy.com');"); //$NON-NLS-1$
			retval.append("\nvar response = request.getAllowedMethods(request.executeRequest());\n"); //$NON-NLS-1$
			return retval.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("setClientProxyCredentials".equals(methodName)) //$NON-NLS-1$
		{
			return "Set proxy credentials.";
		}
		else if ("getCookie".equals(methodName))
		{
			return "Get a cookie by name.";
		}
		else if ("getCookies".equals(methodName))
		{
			return "Get all cookies from this client.";
		}
		else if ("setCookie".equals(methodName))
		{
			return "Add cookie to the this client.";
		}
		else if ("setTimeout".equals(methodName))
		{
			return "Sets a timeout in milliseconds for retrieving of data (when 0 there is no timeout).";
		}
		else if ("createPostRequest".equals(methodName))
		{
			return "Create a new post request ( Origin server should accept/process the submitted data.).";
		}
		else if ("createHeadRequest".equals(methodName))
		{
			return "Creates a new head request (similar to get request, must not contain body content).";
		}
		else if ("createGetRequest".equals(methodName))
		{
			return "Creates a new get request (retrieves whatever information is stored on specified url).";
		}
		else if ("createPutRequest".equals(methodName))
		{
			return "Creates a new put request (similar to post request, contains information to be submitted).";
		}
		else if ("createDeleteRequest".equals(methodName))
		{
			return "Creates a new delete request (a request to delete a resource on server).";
		}
		else if ("createTraceRequest".equals(methodName))
		{
			return "Creates a new trace request (debug request, server will just echo back).";
		}
		else if ("createOptionsRequest".equals(methodName))
		{
			return "Creates a new options request (a request for information about communication options).";
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
