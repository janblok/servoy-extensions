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
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Pair;

/**
 * @author Jan Blok
 */
public class Poster implements IScriptObject
{
	private HttpClient client;
	private PostMethod post;
	private Map params;
	private Map files;
	private Map<String, String> headers;
	private String charset = null;

	public Poster()
	{
	}//only used by script engine

	public Poster(String a_url, HttpClient hc)
	{
		if (hc == null)
		{
			client = new HttpClient();
		}
		else
		{
			client = hc;
		}
		post = new PostMethod(a_url);
		params = new HashMap();
		files = new HashMap();
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
		if (value != null)
		{
			params.put(name, value);
			return true;
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
				files.put(new Pair(parameterName, fileName), f);
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
				Iterator it = params.keySet().iterator();
				while (it.hasNext())
				{
					String name = (String)it.next();
					String value = (String)params.get(name);
					if (name != null)
					{
						post.addParameter(name, value);
					}
					else
					{
						post.setRequestEntity(new StringRequestEntity(value));
					}
				}
			}
			else
			{
				Part[] parts = new Part[files.size() + params.size()];
				int i = 0;
				boolean multiPartRequest = true;

				// add the files
				for (Iterator it = files.keySet().iterator(); it.hasNext(); i++)
				{
					Pair p = (Pair)it.next();
					File f = (File)files.get(p);
					String fname = (String)p.getRight();
					if (p.getLeft() != null)
					{
						if (fname != null)
						{
							parts[i] = new FilePart((String)p.getLeft(), fname, f);
						}
						else
						{
							parts[i] = new FilePart((String)p.getLeft(), f);
						}
					}
					else
					{
						multiPartRequest = false;
						post.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(f), -1));
					}
				}

				// add the parameters
				for (Iterator it = params.keySet().iterator(); it.hasNext(); i++)
				{
					String name = (String)it.next();
					String value = (String)params.get(name);
					if (name != null)
					{
						parts[i] = new StringPart(name, value);
					}
					else
					{
						multiPartRequest = false;
						post.setRequestEntity(new StringRequestEntity(value));
					}
				}

				if (multiPartRequest) post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
			}

			// choose encoding to use
			String charsetToUse = charset;
			if (charsetToUse == null)
			{
				charsetToUse = "UTF-8";
			}
			post.getParams().setContentCharset(charsetToUse);
			post.getParams().setCredentialCharset(charsetToUse);
			post.getParams().setHttpElementCharset(charsetToUse);

			Iterator<String> it = headers.keySet().iterator();
			while (it.hasNext())
			{
				String name = it.next();
				String value = headers.get(name);
				post.addRequestHeader(name, value);
			}

			// post
			if (args.length == 2) client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			int status = client.executeMethod(post);
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
			return post.getResponseBodyAsString();
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		finally
		{
			post.releaseConnection();
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
