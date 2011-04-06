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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * @author pbakker
 *
 */
public class Response implements IScriptObject, IJavaScriptType
{
	private HttpResponse res;

	public Response()
	{

	}

	public Response(HttpResponse response)
	{
		res = response;
	}

	public String[] getAllowedMethods()
	{
		HeaderIterator it = res.headerIterator(OptionsRequest.OPTIONS_HEADER);
		Set<String> methods = new HashSet<String>();
		while (it.hasNext())
		{
			Header header = it.nextHeader();
			HeaderElement[] elements = header.getElements();
			for (HeaderElement element : elements)
			{
				methods.add(element.getName());
			}
		}
		return methods.toArray(new String[0]);
	}

	public int js_getStatusCode()
	{
		if (res != null)
		{
			return res.getStatusLine().getStatusCode();
		}
		return 0;
	}

	public String js_getResponseBody()
	{
		try
		{
			return EntityUtils.toString(res.getEntity());
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return "";
	}

	public byte[] js_getMediaData()
	{
		try
		{
			ByteArrayOutputStream sb = new ByteArrayOutputStream();
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
			return sb.toByteArray();
		}
		catch (IOException e)
		{
			Debug.error(e);
		}
		return null;
	}

	public HashMap<String, String> js_getResponseHeaders()
	{
		return js_getResponseHeaders(null);
	}

	public HashMap<String, String> js_getResponseHeaders(String name)
	{
		try
		{
			Header[] ha;
			if (name == null)
			{
				ha = res.getAllHeaders();
			}
			else
			{
				ha = res.getHeaders(name);
			}
			HashMap<String, String> sa = new HashMap<String, String>();
			for (Header element : ha)
			{
				sa.put(element.getName(), element.getValue());
			}
			return sa;
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return null;
	}

	public String js_getCharset()
	{
		return EntityUtils.getContentCharSet(res.getEntity());
	}

	public String[] getParameterNames(String methodName)
	{
		if ("getResponseHeaders".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[headerName]" }; //$NON-NLS-1$
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("getResponseBody".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var pageData = response.getResponseBody();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("getCharset".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var charset = response.getCharset();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("getMediaData".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var mediaData = response.getMediaData();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("getResponseHeaders".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var allHeaders = response.getResponseHeaders(null);\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("getStatusCode".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var status = response.getStatusCode();// compare with HTTP_STATUS constants \n"); //$NON-NLS-1$
			return retval.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("getResponseBody".equals(methodName)) //$NON-NLS-1$
		{
			return "Get the content of the response as String."; //$NON-NLS-1$
		}
		else if ("getCharset".equals(methodName)) //$NON-NLS-1$
		{
			return "Get the charset of the response body."; //$NON-NLS-1$
		}
		else if ("getMediaData".equals(methodName)) //$NON-NLS-1$
		{
			return "Get the content of response as binary data. It also supports gzip-ed content."; //$NON-NLS-1$
		}
		else if ("getResponseHeaders".equals(methodName)) //$NON-NLS-1$
		{
			return "Gets the headers of the response as name/value arrays."; //$NON-NLS-1$
		}
		else if ("getStatusCode".equals(methodName)) //$NON-NLS-1$
		{
			return "Gets the status code of the response, the list of the possible values is in HTTP_STATUS constants."; //$NON-NLS-1$
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
