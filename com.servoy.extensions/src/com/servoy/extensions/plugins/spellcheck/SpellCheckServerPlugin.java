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

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.keyoti.rapidSpell.BadWord;
import com.keyoti.rapidSpell.LanguageType;
import com.keyoti.rapidSpell.NoCurrentBadWordException;
import com.keyoti.rapidSpell.RapidSpellChecker;
import com.servoy.extensions.plugins.spellcheck.servlets.SpellCheckXMLServlet;
import com.servoy.j2db.J2DBGlobals;
import com.servoy.j2db.plugins.IServerAccess;
import com.servoy.j2db.plugins.IServerPlugin;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.preference.PreferencePanel;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Settings;

public class SpellCheckServerPlugin implements IServerPlugin
{
	public static final String WEBSERVICE_NAME = "spellchecker"; //$NON-NLS-1$

	public SpellCheckServerPlugin()
	{
	}

	public void initialize(IServerAccess app) throws PluginException
	{
		app.registerWebService(WEBSERVICE_NAME, new SpellCheckXMLServlet(WEBSERVICE_NAME, this));
	}

	public PreferencePanel[] getPreferencePanels()
	{
		return null;
	}

	public Map<String, String> getRequiredPropertyNames()
	{
		return null;
	}

	public Properties getProperties()
	{
		Properties props = new Properties();
		props.put(DISPLAY_NAME, "Spellchecker Plugin"); //$NON-NLS-1$
		return props;
	}

	public void load() throws PluginException
	{
	}

	public void unload() throws PluginException
	{
	}

	public String check(String text)
	{
		StringBuilder responseBody = new StringBuilder();
		String theLanguage = parseForLanguage(text);
		String goodText = removeLanguageFromRequest(text);
		RequestSAXParser parser = new RequestSAXParser(goodText);
		String textToBeChecked = parser.parseXMLString();
		responseBody.append(createXmlStringResponse(textToBeChecked, theLanguage));
		return responseBody.toString();
	}

	/**
	 * @param text
	 * @return
	 */
	private String removeLanguageFromRequest(String text)
	{
		int startIndex = text.lastIndexOf("<rslang>"); //$NON-NLS-1$
		int l = "</rslang>".length();//$NON-NLS-1$
		int endIndex = text.indexOf("</rslang>"); //$NON-NLS-1$
		return text.substring(0, startIndex) + text.substring(endIndex + l, text.length());
	}

	private String createXmlStringResponse(String text, String theLanguage)
	{
		String xmlString = null;
		SpellResult spellResponse = createResponseFromRapidSpell(text, theLanguage);
		Document dom;
		//get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try
		{
			//get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//create an instance of DOM
			dom = db.newDocument();

			//Create root element spell response
			Element rootEle = dom.createElement("spellresult");
			dom.appendChild(rootEle);

			rootEle.setAttribute("charschecked", spellResponse.getCharsCheckednumber());
			rootEle.setAttribute("clipped", spellResponse.getClippedNumber());
			rootEle.setAttribute("error", spellResponse.getErrorNumber());

			//create the 'c' (correction) elements and attach them to the spell response
			// for each correction element
			List<SpellCorrection> corrections = spellResponse.getSpellCorrections();
			for (SpellCorrection c : corrections)
			{
				Element cEle = dom.createElement("c");
				cEle.setAttribute("s", c.getConfidence());
				cEle.setAttribute("l", c.getLength());
				cEle.setAttribute("o", c.getOffset());
				Text textContent = dom.createTextNode(c.getTextCorrection());
				cEle.appendChild(textContent);
				rootEle.appendChild(cEle);
			}
			// Transform 
			Transformer transformer;
			transformer = TransformerFactory.newInstance().newTransformer();

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(dom);
			transformer.transform(source, result);

			xmlString = result.getWriter().toString();

		}
		catch (Exception e)
		{
			Debug.error("Error in transforming XML!", e);
		}

		return xmlString;
	}

	private String parseForLanguage(String xmlString)
	{
		int startIndex = xmlString.lastIndexOf("<rslang>"); //$NON-NLS-1$
		int l = "<rslang>".length();//$NON-NLS-1$
		int endIndex = xmlString.indexOf("</rslang>"); //$NON-NLS-1$
		if (startIndex == -1 || endIndex == -1) return SpellCheckerUtils.DEFAULT;
		else return xmlString.substring(startIndex + l, endIndex);
	}

	public SpellResult createResponseFromRapidSpell(String textToBeChecked, String lang)
	{
		SpellResult spellResult = new SpellResult();
		RapidSpellChecker checker = new RapidSpellChecker();
		BadWord badWord;
		Enumeration suggestions;

		//making sure we set a language
		if (lang == null) checker.setLanguageParser(LanguageType.getLanguageTypeFromString(SpellCheckerUtils.ENGLISH));
		else checker.setLanguageParser(LanguageType.getLanguageTypeFromString(lang));

		//setting the desired user dictionary
		String mainDict = RapidSpellUtils.getDictionaryForLanguage(lang);
		if (mainDict != null)
		{
			String path2dictionary = (String)Settings.getInstance().get(J2DBGlobals.SERVOY_APPLICATION_SERVER_DIRECTORY_KEY) + "/plugins/spellcheck/"; //$NON-NLS-1$
			checker.setDictFilePath(path2dictionary + mainDict);
		}
		else
		{
			checker.setDictFilePath(null);
		}

		//compound words
		if ((LanguageType.getLanguageTypeFromString(lang) == LanguageType.DUTCH) || (LanguageType.getLanguageTypeFromString(lang) == LanguageType.GERMAN)) checker.setCheckCompoundWords(true);
		if (LanguageType.getLanguageTypeFromString(lang) == LanguageType.ITALIAN) checker.setCheckCompoundWords(false);

		//check text
		spellResult.setCharsCheckednumber(textToBeChecked.length());
		checker.check(textToBeChecked);

		//iterate through all bad words in the text.
		while ((badWord = checker.nextBadWord()) != null)
		{
			SpellCorrection c = new SpellCorrection();
			c.setOffset(textToBeChecked.indexOf(badWord.getWord()));
			c.setLength(badWord.getWord().length());
			c.setConfidence(1);

			try
			{
				//get suggestions for the current bad word.
				suggestions = checker.findSuggestions().elements();
				StringBuilder textCorrection = new StringBuilder();

				//display all suggestions.
				while (suggestions.hasMoreElements())
				{
					textCorrection.append((String)suggestions.nextElement() + ' ');
				}
				c.setTextCorrection(textCorrection.toString());

				//Add correction to the spelling result
				spellResult.addCorrection(c);
			}
			catch (NoCurrentBadWordException e)
			{
				spellResult.setErrorNumber(1);
			}

		}
		return spellResult;
	}
}
