/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2011 Servoy BV

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

package com.servoy.extensions.plugins.spellcheck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import com.servoy.j2db.util.Debug;

public class ServiceHandler
{
	private final String strUrlServiceProvider;

	public ServiceHandler(String urlServiceProvider)
	{
		this.strUrlServiceProvider = urlServiceProvider;
	}

	public String handleTextSpellChecking(String text, String selectedLang)
	{
		StringBuffer responseBody = new StringBuffer();

		// here create an XML file and send a request to google  or to RapidSpellChecker with POST
		String xmlString = createXmlStringRequest(text, selectedLang);

		try
		{
			URL url = new URL(strUrlServiceProvider);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.addRequestProperty("Content-type", "text/xml; charset=UTF-8");
			OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), Charset.forName("UTF-8"));
			osw.write(xmlString);
			osw.flush();

			// Get the response
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = br.readLine()) != null)
			{
				responseBody.append(line);
				responseBody.append('\n');
			}
			osw.close();
			br.close();

		}
		catch (IOException e)
		{
			Debug.error(e);
		}
		return responseBody.toString();
	}

	private String createXmlStringRequest(String text, String selectedLang)
	{
		StringBuffer requestXML = new StringBuffer();
		requestXML.append("<spellrequest textalreadyclipped=\"0\"" + " ignoredups=\"1\"" + " ignoredigits=\"1\" ignoreallcaps=\"0\">");
		requestXML.append("<rslang>" + selectedLang + "</rslang>");
		requestXML.append("<text>");
		requestXML.append(text);
		requestXML.append("</text></spellrequest>");
		return requestXML.toString();
	}
}
