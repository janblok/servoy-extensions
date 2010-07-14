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

import java.awt.Point;

import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.ui.IComponent;


/**
 * Interface for client-specific (Swing/Wicket) actions.
 * 
 * @author rgansevles
 *
 */
public interface IMenuHandler
{
	public static final int TRIGGER_RIGHTCLICK = 1;
	public static final int TRIGGER_MOUSEOVER = 2;

	IPopupMenu createPopupMenu() throws PluginException;

	void showPopup(IPopupMenu popupMenu, Object component, int x, int y) throws PluginException;

	IButtonGroup createButtonGroup() throws PluginException;

	Object findComponentAt(Point location);

	Point makeLocationWindowRelative(Object component, Point location) throws PluginException;

	IMenuItem createMenuItem(IMenu parentMenu, int type) throws PluginException;

	IMenu createMenu(IMenu parentMenu) throws PluginException;

	void installPopupTrigger(IPopupMenu popupMenu, IComponent component, int x, int y, int popupTrigger);

	Object initializeMenuBar(String windowName);

	void resetMenuBar(String windowName, Object initializeMenuBarResult);

	void addToMenuBar(String windowName, IMenu impl, int index) throws PluginException;

	void removeFromMenuBar(String windowName, int index) throws PluginException;

	int getMenubarSize(String windowName);

	IMenu getMenubarMenu(String windowName, int index);

	int getMenuIndexByText(String windowName, String name);

	void validateMenuBar(String windowName);

	void execute(FunctionDefinition functionDefinition, IClientPluginAccess pluginAccess, Object[] arguments);

	void setMenubarVisible(String windowName, boolean visible);
}
