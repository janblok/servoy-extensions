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
package com.servoy.extensions.plugins.window.menu;


import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Window;

import javax.swing.SwingUtilities;

import com.servoy.extensions.plugins.window.WindowProvider;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.ui.IComponent;

/**
 * Popup menu scriptable.
 * 
 */

public class Popup extends AbstractMenu
{
	public Popup()
	{
		// used for scripting
	}

	public Popup(IClientPluginAccess pluginAccess, IMenuHandler menuHandler, IPopupMenu popupMenu)
	{
		super(pluginAccess, menuHandler, popupMenu);
	}

	@Override
	public IPopupMenu getMenu()
	{
		return (IPopupMenu)super.getMenu();
	}

	public void js_show(IComponent component) throws PluginException
	{
		if (component != null)
		{
			getMenuHandler().showPopup(getMenu(), component, 0, component.getSize().height);
		}
	}

	public void js_show(IComponent component, int x, int y) throws PluginException
	{
		getMenuHandler().showPopup(getMenu(), component, x, y);
	}

	public void js_show() throws PluginException
	{
		IRuntimeWindow runtimeWindow = getPluginAccess().getCurrentRuntimeWindow();
		if (runtimeWindow instanceof ISmartRuntimeWindow)
		{
			Window window = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
			if (window != null)
			{
				Point loc = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(loc, window);
				getMenu().showPopup(window, loc.x, loc.y);
				return;
			}
		}
		// default to (0, 0) when not in smart client
		js_show(0, 0);
	}

	public void js_show(int x, int y) throws PluginException
	{
		Point loc = new Point(x, y);
		Object comp = getMenuHandler().findComponentAt(loc);
		if (comp != null)
		{
			loc = getMenuHandler().makeLocationWindowRelative(comp, loc);
		}
		getMenu().showPopup(comp, loc.x, loc.y);
	}

	@SuppressWarnings("nls")
	@Override
	public String[] getParameterNames(String methodName)
	{
		if ("show".equals(methodName))
		{
			return new String[] { "[element]", "[x]", "[y]" };
		}

		return super.getParameterNames(methodName);
	}

	@Override
	public String getSample(String methodName)
	{
		return WindowProvider.getPopupMenuSample(new StringBuilder(), "plugins.window").toString(); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@Override
	public String getToolTip(String methodName)
	{
		if ("show".equals(methodName))
		{
			return "Show the popup below the element or add x an y values relative to the element, - in Smart client, if no parameters are provided, will use global mouse coordinates";
		}
		return super.getToolTip(methodName);
	}
}
