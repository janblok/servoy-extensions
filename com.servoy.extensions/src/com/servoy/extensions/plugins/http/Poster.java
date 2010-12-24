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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Pair;

/**
 * @author jblok
 */
public class Poster implements IScriptObject
{
	private DefaultHttpClient client;
	private String url;
	private HttpContext context;
	private List<NameValuePair> params;
	private Map<Pair<String, String>, File> files;
	private Map<String, String> headers;
	private String charset = HTTP.UTF_8;
	private String content;

	public Poster()
	{
	}//only used by script engine

	public Poster(String a_url, DefaultHttpClient hc)
	{
		url = a_url;
		if (hc == null)
		{
			client = new DefaultHttpClient();
		}
		else
		{
			client = hc;
		}
		params = new ArrayList<NameValuePair>();
		files = new HashMap<Pair<String, String>, File>();
		headers = new HashMap<String, String>();
	}

	/**
	 * add a parameter to the post
	 *
	 * @sample 
	 * poster.addParameter('name','value')
	 * poster.addParameter(null,'value') //sets the content to post
	 * 
	 * @param name 
	 *
	 * @param value 
	 */
	public boolean js_addParameter(String name, String value)
	{
		if (name != null)
		{
			params.add(new BasicNameValuePair(name, value));
			return true;
		}
		else if (value != null)
		{
			content = value;
		}
		return false;
	}

	/**
	 * add a file to the post
	 *
	 * @sample 
	 * poster.addFile('myFileParamName','manual.doc','c:/temp/manual_01a.doc')
	 * poster.addFile(null,'postXml.xml','c:/temp/postXml.xml') // sets the xml to post
	 *
	 * @param parameterName 
	 *
	 * @param fileName 
	 *
	 * @param fileLocation 
	 */
	public boolean js_addFile(String parameterName, String fileName, String fileLocation)
	{
		if (fileLocation != null)
		{
			File f = new File(fileLocation);
			if (f.exists())
			{
				files.put(new Pair<String, String>(parameterName, fileName), f);
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a header to the request.
	 * 
	 * @sample 
	 * poster.addFile(null,'postXml.xml','c:/temp/postXml.xml') // sets the xml to post
	 * poster.addHeader('Content-type','text/xml; charset=ISO-8859-1')
	 * 
	 * @param headerName
	 * 
	 * @param value
	 * 
	 */
	public boolean js_addHeader(String headerName, String value)
	{
		if (headerName != null)
		{
			headers.put(headerName, value);
			return true;
		}
		return false;
	}

	/**
	 * set the encoding used when posting - if this is null or not called it will use the default encoding (UTF-8)
	 *
	 * @sample
	 * var poster = plugins.http.getPoster('https://twitter.com/statuses/update.json');
	 * poster.addParameter('status',globals.textToPost);
	 * poster.addParameter('source','Test Source');
	 * poster.setEncoding('UTF-8');
	 * var httpCode = poster.doPost(globals.twitterUserName, globals.twitterPassword); //httpCode 200 is ok
	 *
	 * @param encoding 
	 */
	public void js_setEncoding(String encoding)
	{
		this.charset = encoding;
	}

	/**
	 * set the charset used when posting - if this is null or not called it will use the default charset (UTF-8)
	 *
	 * @sample
	 * var poster = plugins.http.getPoster('https://twitter.com/statuses/update.json');
	 * poster.addParameter('status',globals.textToPost);
	 * poster.addParameter('source','Test Source');
	 * poster.setCharset('UTF-8');
	 * var httpCode = poster.doPost(globals.twitterUserName, globals.twitterPassword); //httpCode 200 is ok
	 *
	 * @param charset 
	 */
	public void js_setCharset(String charset)
	{
		this.charset = charset;
	}

	/**
	 * do the actual post
	 *
	 * @sample var httpCode = poster.doPost()
	 *
	 * @param [username optional 
	 *
	 * @param password] 
	 */
	public int js_doPost(Object[] args)
	{
		HttpPost post = new HttpPost(url);

		String username = null;
		String password = null;
		if (args.length == 2)
		{
			username = "" + args[0]; //$NON-NLS-1$
			password = "" + args[1]; //$NON-NLS-1$
		}
		try
		{
			if (files.size() == 0)
			{
				if (params.size() > 0)
				{
					post.setEntity(new UrlEncodedFormEntity(params, charset));
				}
				else
				{
					post.setEntity(new StringEntity(content));
					content = null;
				}
			}
			else if (files.size() == 1 && params.size() == 0)
			{
				File f = files.values().iterator().next();
				post.setEntity(new FileEntity(f, "binary/octet-stream"));
			}
			else
			{
				MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				// For File parameters
				Iterator<Pair<String, String>> itf = files.keySet().iterator();
				while (itf.hasNext())
				{
					Pair<String, String> p = itf.next();
					File f = files.get(p);
					String paramName = p.getLeft();
					String fname = p.getRight();
					entity.addPart(paramName, new FileBody(f));
				}

				// add the parameters
				Iterator<NameValuePair> it = params.iterator();
				while (it.hasNext())
				{
					NameValuePair nvp = it.next();
					// For usual String parameters
					entity.addPart(nvp.getName(), new StringBody(nvp.getValue(), "text/plain", Charset.forName(charset)));
				}

				post.setEntity(entity);
			}

			Iterator<String> it = headers.keySet().iterator();
			while (it.hasNext())
			{
				String name = it.next();
				String value = headers.get(name);
				post.addHeader(name, value);
			}

			// post
			if (args.length == 2)
			{
				BasicCredentialsProvider bcp = new BasicCredentialsProvider();
				URL _url = new URL(url);
				bcp.setCredentials(new AuthScope(_url.getHost(), _url.getPort()), new UsernamePasswordCredentials(username, password));
				client.setCredentialsProvider(bcp);
			}
			context = new BasicHttpContext();
			HttpResponse res = client.execute(post, context);
			int status = res.getStatusLine().getStatusCode();
			return status;
		}
		catch (Exception ex)
		{
			Debug.error(ex);
			return 0;
		}
	}

	/**
	 * get the result page data after a post
	 *
	 * @sample var pageData = poster.getPageData()
	 */
	public String js_getPageData()
	{
		try
		{
			if (context != null)
			{
				HttpResponse response = (HttpResponse)context.getAttribute(ExecutionContext.HTTP_RESPONSE);
				context = null;
				return EntityUtils.toString(response.getEntity());
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return ""; //$NON-NLS-1$
	}

	public String getSample(String methodName)
	{
		if ("addFile".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("poster.addFile('myFileParamName','manual.doc','c:/temp/manual_01a.doc')"); //$NON-NLS-1$
			retval.append("\n"); //$NON-NLS-1$
			retval.append("poster.addFile(null,'postXml.xml','c:/temp/postXml.xml') // sets the xml to post");//$NON-NLS-1$
			return retval.toString();
		}
		else if ("addParameter".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("poster.addParameter('name','value')"); //$NON-NLS-1$
			retval.append("\n"); //$NON-NLS-1$
			retval.append("poster.addParameter(null,'value') //sets the content to post"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("doPost".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var httpCode = poster.doPost()"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("setEncoding".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nvar poster = plugins.http.getPoster('https://twitter.com/statuses/update.json');\nposter.addParameter('status',globals.textToPost);\nposter.addParameter('source','Test Source');\nposter.setEncoding('UTF-8');\nvar httpCode = poster.doPost(globals.twitterUserName, globals.twitterPassword); //httpCode 200 is ok\n");
			return retval.toString();
		}
		else if ("setCharset".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\nvar poster = plugins.http.getPoster('https://twitter.com/statuses/update.json');\nposter.addParameter('status',globals.textToPost);\nposter.addParameter('source','Test Source');\nposter.setCharset('UTF-8');\nvar httpCode = poster.doPost(globals.twitterUserName, globals.twitterPassword); //httpCode 200 is ok\n");
			return retval.toString();
		}
		else if ("getPageData".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("var pageData = poster.getPageData()");
			return retval.toString();
		}
		else if ("addHeader".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("poster.addFile(null,'postXml.xml','c:/temp/postXml.xml') // sets the xml to post");
			retval.append("\n");
			retval.append("poster.addHeader('Content-type','text/xml; charset=ISO-8859-1')");
			return retval.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("addFile".equals(methodName)) //$NON-NLS-1$
		{
			return "Add a file to the post."; //$NON-NLS-1$
		}
		else if ("addParameter".equals(methodName)) //$NON-NLS-1$
		{
			return "Add a parameter to the post."; //$NON-NLS-1$
		}
		else if ("doPost".equals(methodName)) //$NON-NLS-1$
		{
			return "Do the actual post."; //$NON-NLS-1$
		}
		else if ("setEncoding".equals(methodName)) //$NON-NLS-1$
		{
			return "Set the encoding used when posting. If this is null or not called it will use the default encoding (UTF-8)."; //$NON-NLS-1$
		}
		else if ("setCharset".equals(methodName)) //$NON-NLS-1$
		{
			return "Set the charset used when posting. If this is null or not called it will use the default charset (UTF-8)."; //$NON-NLS-1$
		}
		else if ("getPageData".equals(methodName)) //$NON-NLS-1$
		{
			return "Get the result page data after a post."; //$NON-NLS-1$
		}
		else if ("addHeader".equals(methodName)) //$NON-NLS-1$
		{
			return "Adds a header to the request."; //$NON-NLS-1$
		}
		return null;
	}

	public String[] getParameterNames(String methodName)
	{
		if (methodName.equals("addFile")) //$NON-NLS-1$
		{
			return new String[] { "parameterName", "fileName", "fileLocation" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else if (methodName.equals("addParameter")) //$NON-NLS-1$
		{
			return new String[] { "name", "value" }; //$NON-NLS-1$//$NON-NLS-2$
		}
		else if ("doPost".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[username", "password]" }; //$NON-NLS-1$//$NON-NLS-2$
		}
		else if ("setEncoding".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "encoding" }; //$NON-NLS-1$
		}
		else if ("setCharset".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "charset" }; //$NON-NLS-1$
		}
		else if ("addHeader".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "headerName", "value" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		if ("setEncoding".equals(methodName))
		{
			return true;
		}
		return false;
	}

	public Class[] getAllReturnedTypes()
	{
		return null;
	}
}
