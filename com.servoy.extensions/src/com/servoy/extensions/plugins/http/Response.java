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
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.servoy.base.scripting.annotations.ServoyClientSupport;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.scripting.JSMap;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * @author pbakker
 *
 */
@ServoyDocumented
@ServoyClientSupport(ng = false, wc = true, sc = true)
public class Response implements IScriptable, IJavaScriptType
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

	/**
	 * Gets the status code of the response, the list of the possible values is in HTTP_STATUS constants.
	 *
	 * @sample
	 * var status = response.getStatusCode();// compare with HTTP_STATUS constants
	 */
	public int js_getStatusCode()
	{
		if (res != null)
		{
			return res.getStatusLine().getStatusCode();
		}
		return 0;
	}

	/**
	 * Get the content of the response as String.
	 *
	 * @sample
	 * var pageData = response.getResponseBody();
	 */
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

	/**
	 * Get the content of response as binary data. It also supports gzip-ed content.
	 *
	 * @sample
	 * var mediaData = response.getMediaData();
	 */
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

	/**
	 * Gets the headers of the response as name/value arrays.
	 *
	 * @sample
	 * var allHeaders = response.getResponseHeaders();
	 * var header;
	 * 
	 * for (header in allHeaders) application.output(header + ': ' + allHeaders[header]);
	 */
	public JSMap js_getResponseHeaders()
	{
		return js_getResponseHeaders(null);
	}

	/**
	 * @clonedesc js_getResponseHeaders()
	 * @sample
	 * var contentLength = response.getResponseHeaders("Content-Length");
	 *
	 * @param headerName 
	 */
	public JSMap js_getResponseHeaders(String headerName)
	{
		try
		{
			Header[] ha;
			if (headerName == null)
			{
				ha = res.getAllHeaders();
			}
			else
			{
				ha = res.getHeaders(headerName);
			}
			JSMap sa = new JSMap();
			for (Header element : ha)
			{
				if (sa.containsKey(element.getName()))
				{
					sa.put(element.getName(), Utils.arrayAdd((String[])sa.get(element.getName()), element.getValue(), true));
				}
				else
				{
					sa.put(element.getName(), new String[] { element.getValue() });
				}
			}
			return sa;
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return null;
	}

	/**
	 * Get the charset of the response body.
	 *
	 * @sample
	 * var charset = response.getCharset();
	 */
	public String js_getCharset()
	{
		return EntityUtils.getContentCharSet(res.getEntity());
	}

}
