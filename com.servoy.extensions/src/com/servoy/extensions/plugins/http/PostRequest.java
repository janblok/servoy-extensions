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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
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
import org.apache.http.util.EntityUtils;

import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Pair;

/**
 * @author jblok
 */
public class PostRequest extends BaseEntityEnclosingRequest
{
	private Map<Pair<String, String>, File> files;
	private List<NameValuePair> params;
	protected String charset = HTTP.UTF_8;

	public PostRequest()
	{
		super();
	}//only used by script engine

	public PostRequest(String url, DefaultHttpClient hc)
	{
		super(url, hc);
		method = new HttpPost(url);
		files = new HashMap<Pair<String, String>, File>();
	}

	@Deprecated
	public void js_setEncoding(String encoding)
	{
		this.charset = encoding;
	}

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

	@Deprecated
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
				String[] values = headers.get(name);
				for (String value : values)
				{
					post.addHeader(name, value);
				}
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

	@Override
	public Response js_executeRequest(String userName, String password)
	{
		try
		{
			HttpEntity entity;
			if (files.size() == 0)
			{
				entity = new StringEntity(content);
				content = null;
			}
			else if (files.size() == 1)
			{
				File f = files.values().iterator().next();
				entity = new FileEntity(f, "binary/octet-stream");
			}
			else
			{
				entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				// For File parameters
				Iterator<Pair<String, String>> itf = files.keySet().iterator();
				while (itf.hasNext())
				{
					Pair<String, String> p = itf.next();
					File f = files.get(p);
					String paramName = p.getLeft();
					((MultipartEntity)entity).addPart(paramName, new FileBody(f));
				}
			}
			if (entity != null)
			{
				((HttpEntityEnclosingRequestBase)method).setEntity(entity);
			}
			return super.js_executeRequest(userName, password);
		}
		catch (Exception ex)
		{
			Debug.error(ex);
			return null;
		}
	}

	@Deprecated
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

	public void js_setCharset(String charset)
	{
		this.charset = charset;
	}

	@Override
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
		return super.getSample(methodName);
	}

	@Override
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
		return super.getToolTip(methodName);
	}

	@Override
	public String[] getParameterNames(String methodName)
	{
		if (methodName.equals("addFile")) //$NON-NLS-1$
		{
			return new String[] { "parameterName", "fileName", "fileLocation" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else if ("doPost".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[username", "password]" }; //$NON-NLS-1$//$NON-NLS-2$
		}
		else if ("setEncoding".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "encoding" }; //$NON-NLS-1$
		}
		return super.getParameterNames(methodName);
	}

	@Override
	public boolean isDeprecated(String methodName)
	{
		if ("setEncoding".equals(methodName))
		{
			return true;
		}
		if ("getPageData".equals(methodName))
		{
			return true;
		}
		if ("doPost".equals(methodName))
		{
			return true;
		}
		return super.isDeprecated(methodName);
	}

	@Override
	public Class[] getAllReturnedTypes()
	{
		return null;
	}
}
