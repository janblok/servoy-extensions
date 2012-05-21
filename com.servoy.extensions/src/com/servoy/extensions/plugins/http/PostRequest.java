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
import java.util.Map.Entry;

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

import com.servoy.extensions.plugins.file.JSFile;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Pair;

/**
 * @author jblok
 */
@ServoyDocumented
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
		super(url, hc, new HttpPost(url));
		files = new HashMap<Pair<String, String>, File>();
	}

	/**
	 * @deprecated Replaced by {@link #setCharset(String)}
	 */
	@Deprecated
	public void js_setEncoding(String encoding)
	{
		js_setCharset(encoding);
	}

	/**
	 * Add a file to the post.
	 *
	 * @sample
	 * poster.addFile('myFileParamName','manual.doc','c:/temp/manual_01a.doc')
	 * poster.addFile(null,'postXml.xml','c:/temp/postXml.xml') // sets the xml to post
	 * 
	 * var f = plugins.file.convertToJSFile('./somefile02.txt')
	 * if (f && f.exists()) poster.addFile('myTxtFileParamName','somefile.txt', f)
	 * 
	 * f = plugins.file.convertToJSFile('./anotherfile_v2b.txt')
	 * if (f && f.exists()) poster.addFile('myOtherTxtFileParamName', f)
	 *
	 * @param parameterName 
	 * @param fileName 
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
	 * Add a file to the post.
	 * 
	 * @sample
	 * poster.addFile('myFileParamName','manual.doc','c:/temp/manual_01a.doc')
	 * poster.addFile(null,'postXml.xml','c:/temp/postXml.xml') // sets the xml to post
	 * 
	 * var f = plugins.file.convertToJSFile('./somefile02.txt')
	 * if (f && f.exists()) poster.addFile('myTxtFileParamName','somefile.txt', f)
	 * 
	 * f = plugins.file.convertToJSFile('./anotherfile_v2b.txt')
	 * if (f && f.exists()) poster.addFile('myOtherTxtFileParamName', f)
	 * 
	 * @param parameterName
	 * @param jsFile
	 */
	public boolean js_addFile(String parameterName, Object jsFile)
	{
		if (jsFile instanceof JSFile)
		{
			File f = ((JSFile)jsFile).getFile();
			if (f.exists())
			{
				files.put(new Pair<String, String>(parameterName, f.getName()), f);
				return true;
			}
		}
		return false;
	}

	/**
	 * Add a file to the post.
	 * 
	 * @sample
	 * poster.addFile('myFileParamName','manual.doc','c:/temp/manual_01a.doc')
	 * poster.addFile(null,'postXml.xml','c:/temp/postXml.xml') // sets the xml to post
	 * 
	 * var f = plugins.file.convertToJSFile('./somefile02.txt')
	 * if (f && f.exists()) poster.addFile('myTxtFileParamName','somefile.txt', f)
	 * 
	 * f = plugins.file.convertToJSFile('./anotherfile_v2b.txt')
	 * if (f && f.exists()) poster.addFile('myOtherTxtFileParamName', f)
	 * 
	 * @param parameterName
	 * @param fileName
	 * @param jsFile
	 */
	public boolean js_addFile(String parameterName, String fileName, Object jsFile)
	{
		if (jsFile instanceof JSFile)
		{
			File f = ((JSFile)jsFile).getFile();
			if (f.exists())
			{
				files.put(new Pair<String, String>(parameterName, fileName), f);
				return true;
			}
		}
		return false;
	}

	/**
	 * Add a parameter to the post.
	 *
	 * @sample
	 * poster.addParameter('name','value')
	 * poster.addParameter(null,'value') //sets the content to post
	 *
	 * @param name 
	 * @param value 
	 */
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

	/**
	 * @deprecated Replaced by {@link #executeRequest(String,String)}
	 *
	 * @sample
	 * //null
	 * var httpCode = poster.doPost()
	 *
	 * @param username optional
	 * @param password optional
	 */
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
			else if (files.size() == 1 && (params == null || params.size() == 0))
			{
				File f = files.values().iterator().next();
				entity = new FileEntity(f, "binary/octet-stream"); //$NON-NLS-1$
			}
			else
			{
				entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				// For File parameters
				for (Entry<Pair<String, String>, File> e : files.entrySet())
				{
					((MultipartEntity)entity).addPart(e.getKey().getLeft(), new FileBody(e.getValue()));
				}

				// add the parameters
				if (params != null)
				{
					Iterator<NameValuePair> it = params.iterator();
					while (it.hasNext())
					{
						NameValuePair nvp = it.next();
						// For usual String parameters
						((MultipartEntity)entity).addPart(nvp.getName(), new StringBody(nvp.getValue(), "text/plain", Charset.forName(charset)));
					}
				}
			}
			((HttpEntityEnclosingRequestBase)method).setEntity(entity);
			return super.js_executeRequest(userName, password);
		}
		catch (Exception ex)
		{
			Debug.error(ex);
			return null;
		}
	}

	/**
	 * Get the result page data after a post.
	 * 
	 * @deprecated Replaced by {@link #executeRequest(String,String)}
	 * 
	 * @sample
	 * var pageData = poster.getPageData()
	 */
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

}
