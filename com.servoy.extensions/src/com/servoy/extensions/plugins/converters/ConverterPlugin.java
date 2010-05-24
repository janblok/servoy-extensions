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
package com.servoy.extensions.plugins.converters;

import java.beans.PropertyChangeEvent;
import java.util.Properties;

import javax.swing.Icon;

import com.servoy.j2db.dataprocessing.IColumnConverter;
import com.servoy.j2db.plugins.IClientPlugin;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IColumnConverterProvider;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.preference.PreferencePanel;
import com.servoy.j2db.scripting.IScriptObject;

public class ConverterPlugin implements IClientPlugin, IColumnConverterProvider
{
	private IColumnConverter[] converters;
	private IClientPluginAccess application;

	public Icon getImage()
	{
		return null;
	}

	public String getName()
	{
		return "ConverterPlugin";
	}

	public PreferencePanel[] getPreferencePanels()
	{
		return null;
	}

	public IScriptObject getScriptObject()
	{
		return null;
	}

	public void initialize(IClientPluginAccess app) throws PluginException
	{
		application = app;
		// the init is executed later than getColumnConvertors so must be sure plugin access is not null
		if (converters != null)
		{
			for (IColumnConverter converter : converters)
			{
				if (converter instanceof GlobalMethodConverter)
				{
					((GlobalMethodConverter)converter).setClientPluginAccess(app);
				}
			}
		}
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

	public void propertyChange(PropertyChangeEvent evt)
	{
	}

	public IColumnConverter[] getColumnConverters()
	{
		if (converters == null)
		{
			converters = new IColumnConverter[] { new GlobalMethodConverter(application) };
		}
		return converters;
	}
}
