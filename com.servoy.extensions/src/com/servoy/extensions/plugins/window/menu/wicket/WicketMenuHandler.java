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
package com.servoy.extensions.plugins.window.menu.wicket;

import java.awt.Point;

import com.servoy.extensions.plugins.window.menu.IButtonGroup;
import com.servoy.extensions.plugins.window.menu.IMenu;
import com.servoy.extensions.plugins.window.menu.IMenuHandler;
import com.servoy.extensions.plugins.window.menu.IMenuItem;
import com.servoy.extensions.plugins.window.menu.IPopupMenu;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.ui.IComponent;

/**
 * Handle menu actions for web client (Wicket).
 * 
 * @author rob
 *
 */
public class WicketMenuHandler implements IMenuHandler
{

	private final IWebClientPluginAccess clientPluginAccess;

	public WicketMenuHandler(IWebClientPluginAccess clientPluginAccess)
	{
		this.clientPluginAccess = clientPluginAccess;
	}

	public IPopupMenu createPopupMenu()
	{
		return new ScriptableWicketPopupMenu(clientPluginAccess);
	}

	public IButtonGroup createButtonGroup()
	{
		return new WicketButtonGroup();
	}

	public IMenuItem createMenuItem(IMenu parentMenu, int type)
	{
		ScriptableWicketMenuItem menuItem;
		switch (type)
		{
			case IMenuItem.MENU_ITEM_RADIO :
				menuItem = new ScriptableWicketRadioButtonMenuItem(parentMenu);
				break;

			case IMenuItem.MENU_ITEM_CHECK :
				menuItem = new ScriptableWicketCheckBoxMenuItem(parentMenu);
				break;

			default :
				menuItem = new ScriptableWicketMenuItem(parentMenu);
				break;
		}
		return menuItem;
	}

	public IMenu createMenu(IMenu parentMenu)
	{
		return new ScriptableWicketMenu(parentMenu, clientPluginAccess);
	}

	public Object findComponentAt(Point location)
	{
		// Not implemented in WebClient
		return null;
	}

	public Point makeLocationWindowRelative(Object component, Point location)
	{
		// is already window relative
		return location;
	}

	public void showPopup(IPopupMenu popupMenu, Object component, int x, int y)
	{
		popupMenu.showPopup(component, x, y);
	}

	public void installPopupTrigger(IPopupMenu popupMenu, IComponent component, int x, int y, int popupTrigger)
	{
		// Not implemented in WebClient
	}

	public Object initializeMenuBar(String windowName)
	{
		// Not implemented in WebClient
		return null;
	}

	public void resetMenuBar(String windowName, Object initializeMenuBarResult)
	{
		// Not implemented in WebClient
	}

	public int getMenubarSize(String windowName)
	{
		// Not implemented in WebClient
		return 0;
	}

	public void addToMenuBar(String windowName, IMenu impl, int index)
	{
		// Not implemented in WebClient
	}

	public void removeFromMenuBar(String windowName, int index)
	{
		// Not implemented in WebClient
	}

	public IMenu getMenubarMenu(String windowName, int index)
	{
		// Not implemented in WebClient
		return null;
	}

	public int getMenuIndexByText(String windowName, String name)
	{
		// Not implemented in WebClient
		return -1;
	}

	public void validateMenuBar(String windowName)
	{
		// Not implemented in WebClient
	}

	public void setMenubarVisible(String windowName, boolean visible)
	{
		// Not implemented in WebClient
	}

	public void execute(FunctionDefinition functionDefinition, IClientPluginAccess pluginAccess, Object[] arguments)
	{
		// execute synchronous in webclient, generateAjaxResponse() may be called immediately after
		functionDefinition.executeSync(pluginAccess, arguments);
	}
}
