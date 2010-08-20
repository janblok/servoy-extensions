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
package com.servoy.extensions.plugins.window.menu.swing;

import javax.swing.JCheckBoxMenuItem;

import com.servoy.extensions.plugins.window.menu.AbstractMenuItem;
import com.servoy.extensions.plugins.window.menu.ICheckboxMenuItem;
import com.servoy.extensions.plugins.window.menu.IMenu;
import com.servoy.extensions.plugins.window.util.Utilities;

/**
 * Checkbox menu item in smart client.
 * 
 * @author jblok
 */

public class ScriptableJCheckBoxMenuItem extends JCheckBoxMenuItem implements ICheckboxMenuItem
{
	private AbstractMenuItem scriptObjectWrapper;

	public ScriptableJCheckBoxMenuItem()
	{
	}

	public IMenu getParentMenu()
	{
		return ScriptableJMenu.getParentMenu(this);
	}

	public Object getMenuComponent()
	{
		return this;
	}

	public void setIconURL(String iconURL)
	{
		setIcon(Utilities.getImageIcon(iconURL));
	}

	public AbstractMenuItem getScriptObjectWrapper()
	{
		return scriptObjectWrapper;
	}

	public void setScriptObjectWrapper(AbstractMenuItem abstractMenuItem)
	{
		this.scriptObjectWrapper = abstractMenuItem;
	}

	public void setBackgroundColor(String bgColor)
	{
		setBackground(Utilities.createColor(bgColor));
	}
}
