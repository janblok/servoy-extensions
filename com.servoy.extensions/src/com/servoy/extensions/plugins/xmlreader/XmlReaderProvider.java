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
package com.servoy.extensions.plugins.xmlreader;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.servoy.extensions.plugins.file.JSFile;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;

/**
 * @author jcompagner
 */
@SuppressWarnings("nls")
public class XmlReaderProvider implements IScriptObject
{

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getSample(java.lang.String)
	 */
	public String getSample(String methodName)
	{
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(java.lang.String)
	 */
	public String getToolTip(String methodName)
	{
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getParameterNames(java.lang.String)
	 */
	public String[] getParameterNames(String methodName)
	{
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#isDeprecated(java.lang.String)
	 */
	public boolean isDeprecated(String methodName)
	{
		return "readXmlDocument".equals(methodName);
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class[] getAllReturnedTypes()
	{
		return new Class[] { XmlNode.class };
	}

	public XmlNode[] js_readXmlDocumentFromFile(Object argument)
	{
		return js_readXmlDocument(argument);
	}

	public XmlNode[] js_readXmlDocumentFromString(String argument)
	{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new InputSource(new StringReader(argument)));
			return readDoc(doc);
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return null;
	}

	public XmlNode[] js_readXmlDocument(Object argument)
	{
		File file = null;
		if (argument instanceof File)
		{
			file = (File)argument;
		}
		else if (argument instanceof JSFile)
		{
			file = ((JSFile)argument).getFile();
		}
		else if (argument instanceof String)
		{
			file = new File((String)argument);
		}

		if (file == null)
		{
			return null;
		}

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			return readDoc(doc);
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return null;
	}

	/**
	 * @param doc
	 */
	private XmlNode[] readDoc(Document doc)
	{
		if (doc != null)
		{
			NodeList nl = doc.getChildNodes();
			List<XmlNode> al = new ArrayList<XmlNode>();
			for (int i = 0; i < nl.getLength(); i++)
			{
				Node item = nl.item(i);
				if (item.getNodeType() == Node.TEXT_NODE)
				{
					String value = item.getNodeValue();
					if (value == null || "".equals(value.trim()))
					{
						continue;
					}
				}
				al.add(new XmlNode(item));
			}
			XmlNode[] nodes = al.toArray(new XmlNode[al.size()]);
			return nodes;
		}
		return null;
	}
}
