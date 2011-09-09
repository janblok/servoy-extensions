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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.servoy.j2db.util.Debug;


public class RequestSAXParser
{
	private static SpellRequest spellRequest;

	private final String xmlString;

	public RequestSAXParser(String xmlString)
	{
		this.xmlString = xmlString;
	}

	public String parseXMLString()
	{

		SAXParserFactory spf = SAXParserFactory.newInstance();
		try
		{
			// get a new instance of parser
			SAXParser parser = spf.newSAXParser();

			// parse the file and also register this class for call backs

			SAXHandler handler = new SAXHandler();

			InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes("UTF-8")); //$NON-NLS-1$

			parser.parse(inputStream, handler);

		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return spellRequest.getText();
	}

	private static final class SAXHandler extends DefaultHandler
	{
		// invoked when document-parsing is started:
		private String tempVal;

		@Override
		public void startDocument() throws SAXException
		{
		}

		// notifies about finish of parsing:
		@Override
		public void endDocument() throws SAXException
		{
		}

		// we enter to element 'qName':
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException
		{
			tempVal = ""; //$NON-NLS-1$
			if (qName.equals("spellrequest")) //$NON-NLS-1$
			{
				spellRequest = new SpellRequest();
				spellRequest.setIgnoreallcaps(Integer.parseInt(attrs.getValue("ignoreallcaps"))); //$NON-NLS-1$
				spellRequest.setIgnoredigits(Integer.parseInt(attrs.getValue("ignoredigits"))); //$NON-NLS-1$
				spellRequest.setIgnoredups(Integer.parseInt(attrs.getValue("ignoredups"))); //$NON-NLS-1$
				spellRequest.setTextalreadyclipped(Integer.parseInt(attrs.getValue("textalreadyclipped"))); //$NON-NLS-1$
			}
			else if (qName.equals("text")) //$NON-NLS-1$
			{
				spellRequest.setText(attrs.getValue("text")); //$NON-NLS-1$
			}
			else
			{
				throw new IllegalArgumentException("Element '" + qName + "' is not allowed here"); //$NON-NLS-1$//$NON-NLS-2$
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			tempVal = new String(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			if (qName.equalsIgnoreCase("text")) //$NON-NLS-1$
			{
				spellRequest.setText(tempVal);
			}
		}
	}
}
