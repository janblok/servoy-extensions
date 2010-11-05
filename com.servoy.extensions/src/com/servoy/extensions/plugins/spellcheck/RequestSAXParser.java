package com.servoy.extensions.plugins.spellcheck;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.servoy.extensions.plugins.spellcheck2.SpellRequest;


//TODO Remove unnecessary prints
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
		catch (SAXException se)
		{
			se.printStackTrace();
		}
		catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		}
		catch (IOException ie)
		{
			ie.printStackTrace();
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
