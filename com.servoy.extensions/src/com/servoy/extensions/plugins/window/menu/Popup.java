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

import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.ui.IComponent;

/**
 * Popup menu scriptable.
 * 
 */
@ServoyDocumented
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

	/**
	 * Show the popup above the specified element.
	 *
	 * @sample 
	 * // NOTE: usually this code is placed in a handler of an event (e.g. right click on some component)
	 * // create a popup menu
	 * var menu = plugins.window.createPopupMenu();
	 * // add a menu item
	 * menu.addMenuItem("item", feedback_item);
	 * // add another menu item
	 * menu.addMenuItem("item 2", feedback_item);
	 * 
	 * if (event.getSource())
	 * {
	 * 	// display the popup over the component which is the source of the event
	 * 	menu.show(event.getSource());
	 * 	// display the popup over the components, at specified coordinates relative to the component
	 * 	//menu.show(event.getSource(), 10, 10);
	 * 	// display the popup at specified coordinates relative to the main window
	 * 	//menu.show(100, 100);
	 * }
	 * 
	 * @param component
	 */
	public void js_show(IComponent component) throws PluginException
	{
		if (component != null)
		{
			getMenuHandler().showPopup(getMenu(), component, 0, component.getSize().height);
		}
	}

	/**
	 * Show the popup above the specified element, adding x an y values relative to the element.
	 *
	 * @sampleas js_show(IComponent)
	 * 
	 * @param component
	 * @param x
	 * @param y
	 */
	public void js_show(IComponent component, int x, int y) throws PluginException
	{
		getMenuHandler().showPopup(getMenu(), component, x, y);
	}

	/**
	 * Show the popup at x an y coordinates.
	 *
	 * @sampleas js_show(IComponent)
	 * 
	 * @param x
	 * @param y
	 */
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
}
