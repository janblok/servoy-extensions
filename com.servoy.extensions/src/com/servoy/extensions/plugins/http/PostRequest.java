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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.ExecutionContext;
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
		js_setCharset(encoding);
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
			if (params == null)
			{
				params = new ArrayList<NameValuePair>();
			}
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

		String username = null;
		String password = null;
		if (args.length == 2)
		{
			username = "" + args[0]; //$NON-NLS-1$
			password = "" + args[1]; //$NON-NLS-1$
		}
		try
		{
			Response res = js_executeRequest(username, password);
			return res.js_getStatusCode();
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
				if (params != null)
				{
					entity = new UrlEncodedFormEntity(params, charset);
				}
				else
				{
					entity = new StringEntity(content, charset);
				}
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

				// add the parameters
				Iterator<NameValuePair> it = params.iterator();
				while (it.hasNext())
				{
					NameValuePair nvp = it.next();
					// For usual String parameters
					((MultipartEntity)entity).addPart(nvp.getName(), new StringBody(nvp.getValue(), "text/plain", Charset.forName(charset)));
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
		if ("addParameter".equals(methodName)) //$NON-NLS-1$
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
		if ("doPost".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var httpCode = poster.doPost()"); //$NON-NLS-1$
			return retval.toString();
		}
		if ("getPageData".equals(methodName))
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
		if ("addParameter".equals(methodName)) //$NON-NLS-1$
		{
			return "Add a parameter to the post."; //$NON-NLS-1$
		}
		if ("getPageData".equals(methodName)) //$NON-NLS-1$
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
		if ("addParameter".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "name", "value" }; //$NON-NLS-1$//$NON-NLS-2$
		}
		if ("doPost".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[username]", "[password]" }; //$NON-NLS-1$//$NON-NLS-2$
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

}
