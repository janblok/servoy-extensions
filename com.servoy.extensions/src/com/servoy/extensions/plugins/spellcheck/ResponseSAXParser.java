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
