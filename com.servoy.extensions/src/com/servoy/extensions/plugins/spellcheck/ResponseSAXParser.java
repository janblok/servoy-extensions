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

public class ResponseSAXParser
{
	private static SpellResult spellResponse;
	private static ResponseSAXParser instance = null;

	private ResponseSAXParser()
	{
	}

	public void parseXMLString(String xmlString)
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
	}

	public static ResponseSAXParser getInstance()
	{
		if (instance == null) instance = new ResponseSAXParser();
		return instance;
	}

	public SpellResult getResponse()
	{
		return spellResponse;
	}

	private static final class SAXHandler extends DefaultHandler
	{
		// invoked when document-parsing is started:
		private String tempVal;
		private SpellCorrection tempCorrection = null;

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
			tempCorrection = new SpellCorrection();
			if (qName.equals("spellresult")) //$NON-NLS-1$
			{
				// create a new instance of employee
				spellResponse = new SpellResult();
				spellResponse.setErrorNumber(Integer.parseInt(attrs.getValue("error"))); //$NON-NLS-1$
				spellResponse.setClippedNumber(Integer.parseInt(attrs.getValue("clipped"))); //$NON-NLS-1$
				spellResponse.setCharsCheckednumber(Integer.parseInt(attrs.getValue("charschecked"))); //$NON-NLS-1$
			}
			else if (qName.equals("c")) //$NON-NLS-1$
			{
				tempCorrection.setOffset(Integer.parseInt(attrs.getValue("o"))); //$NON-NLS-1$
				tempCorrection.setLength(Integer.parseInt(attrs.getValue("l"))); //$NON-NLS-1$
				tempCorrection.setConfidence(Integer.parseInt(attrs.getValue("s"))); //$NON-NLS-1$
				tempCorrection.setTextCorrection(tempVal);
				spellResponse.addCorrection(tempCorrection);
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
			if (qName.equalsIgnoreCase("c")) //$NON-NLS-1$
			{
				tempCorrection.setTextCorrection(tempVal);
			}

		}
	}
}
