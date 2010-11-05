package com.servoy.extensions.plugins.spellcheck;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
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
import com.servoy.j2db.util.Settings;

//TODO Remove unnecessary prints
public class SpellCheckServerPlugin implements IServerPlugin
{
	public static final String WEBSERVICE_NAME = "spellchecker"; //$NON-NLS-1$

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
		return null;
	}

	public void load() throws PluginException
	{
	}

	public void unload() throws PluginException
	{
	}

	public String check(String text)
	{
		StringBuffer responseBody = new StringBuffer();
		RequestSAXParser parser = new RequestSAXParser(text);
		String textToBeChecked = parser.parseXMLString();
		responseBody.append(createXmlStringResponse(textToBeChecked));
		return responseBody.toString();

	}

	private String createXmlStringResponse(String text)
	{


		String xmlString = null;
		SpellResult spellResponse = createResponseFromRapidSpell(text);
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
			ArrayList<SpellCorrection> corrections = spellResponse.getSpellCorrections();
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
		catch (ParserConfigurationException pce)
		{
			System.out.println("Error while trying to instantiate DocumentBuilder " + pce);
			System.exit(1);
		}
		catch (TransformerConfigurationException e)
		{
			System.out.println("Error in transforming XML!" + e);
			e.printStackTrace();
		}
		catch (TransformerFactoryConfigurationError e)
		{
			System.out.println("Error in transforming XML!" + e);
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			System.out.println("Error in transforming XML!" + e);
			e.printStackTrace();
		}

		return xmlString;
	}

	public SpellResult createResponseFromRapidSpell(String textToBeChecked)
	{
		SpellResult spellResult = new SpellResult();
		RapidSpellChecker checker = new RapidSpellChecker();
		BadWord badWord;
		Enumeration suggestions;

		//check some text.
		spellResult.setCharsCheckednumber(textToBeChecked.length());
		checker.check(textToBeChecked);

		//setting the desired user dictionary
		String lang = SpellCheckerPreferencePanel.getDesiredLanguage();
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

		checker.setLanguageParser(LanguageType.getLanguageTypeFromString(lang));

		if ((LanguageType.getLanguageTypeFromString(lang) == LanguageType.DUTCH) || (LanguageType.getLanguageTypeFromString(lang) == LanguageType.GERMAN)) checker.setCheckCompoundWords(true);
		if (LanguageType.getLanguageTypeFromString(lang) == LanguageType.ITALIAN) checker.setCheckCompoundWords(false);


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
				StringBuffer textCorrection = new StringBuffer();

				//display all suggestions.
				while (suggestions.hasMoreElements())
				{
					textCorrection.append((String)suggestions.nextElement() + " ");
				}
				c.setTextCorrection(textCorrection.toString());

				//Add correction to the spelling result
				spellResult.addCorrection(c);
			}
			catch (NoCurrentBadWordException e)
			{
				spellResult.setErrorNumber(1);
				System.err.println(e);
			}

		}
		return spellResult;
	}
}
