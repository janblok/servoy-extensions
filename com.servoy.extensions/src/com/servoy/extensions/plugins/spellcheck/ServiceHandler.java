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

	public String handleTextSpellChecking(String text)
	{
		StringBuffer responseBody = new StringBuffer();

		// here create an XML file and send a request to google  or to RapidSpellChecker with POST
		String xmlString = createXmlStringRequest(text);

		try
		{
			URL url = new URL(strUrlServiceProvider);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.addRequestProperty("Content-type", "text/xml; charset=UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(), Charset.forName("UTF-8")); //$NON-NLS-1$
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

	private String createXmlStringRequest(String text)
	{
		StringBuffer requestXML = new StringBuffer();
		requestXML.append("<spellrequest textalreadyclipped=\"0\"" + " ignoredups=\"1\"" + " ignoredigits=\"1\" ignoreallcaps=\"0\"><text>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		requestXML.append(text);
		requestXML.append("</text></spellrequest>"); //$NON-NLS-1$
		return requestXML.toString();
	}
}
