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

import java.util.Arrays;

import com.servoy.extensions.plugins.window.WindowProvider;
import com.servoy.extensions.plugins.window.util.DescendingNumberComparator;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;


/**
 * Handles menubar actions, delegates to client-specific (Swing/Wicket) handler.
 * 
 * @author rgansevles
 *
 */
@SuppressWarnings("nls")
@ServoyDocumented
public class MenuBar implements IScriptable, IReturnedTypesProvider
{

	public MenuBar()
	{
		// for scripting
		windowName = null;
		windowProvider = null;
		initializeMenuBarResult = null;
	}

	private final WindowProvider windowProvider;
	private final String windowName; // null for main app frame

	private final Object initializeMenuBarResult;

	public MenuBar(String windowName, WindowProvider windowProvider)
	{
		this.windowName = windowName;
		this.windowProvider = windowProvider;
		initializeMenuBarResult = windowProvider.getMenuHandler().initializeMenuBar(windowName);
	}

	public void removeMenu(int index) throws PluginException
	{
		windowProvider.getMenuHandler().removeFromMenuBar(windowName, index);
	}

	// Javascript methods

	/**
	 * Get the number of (top level) menu's.
	 *
	 * @sample
	 * // Note: method getMenuCount only works in the smart client.
	 * 
	 * // add a new menu before the last menu
	 * var menubar = plugins.window.getMenuBar();
	 * var count = menubar.getMenuCount();
	 * var menu = menubar.addMenu(count-1);
	 * menu.text = 'new menu';
	 */
	public int js_getMenuCount()
	{
		return windowProvider.getMenuHandler().getMenubarSize(windowName);
	}

	/**
	 * Retrieve the index of the item by text.
	 *
	 * @sample
	 * // Note: method getMenuIndexByText only works in the smart client.
	 * 
	 * var menubar = plugins.window.getMenuBar();
	 * // find the index of the View menu
	 * var idx = menubar.getMenuIndexByText("View");
	 * // add a menu before the View menu
	 * var menu = menubar.addMenu(idx);
	 * menu.text = "new menu";
	 * 
	 * @param name
	 */
	public int js_getMenuIndexByText(String name)
	{
		if (name == null || "".equals(name))
		{
			Debug.warn("You can not search for a name with a null or empty value.");
			return -1;
		}

		return windowProvider.getMenuHandler().getMenuIndexByText(windowName, name);
	}

	/**
	 * Remove all menus from the menubar.
	 *
	 * @sample
	 * // Note: method removeAllMenus only works in the smart client.
	 * 
	 * // Potentially dangerous because all accelerator (short) keys
	 * // will be deleted also (including the quit item)
	 * var menubar = plugins.window.getMenuBar();
	 * menubar.removeAllMenus();
	 */
	public void js_removeAllMenus() throws PluginException
	{
		removeMenu(-1);
	}

	/**
	 * Remove the menu(s) at the selected index/indices.
	 *
	 * @sample
	 * // Note: method removeMenu only works in the smart client.
	 * 
	 * var menubar = plugins.window.getMenuBar();
	 * // To remove the last menu in the menubar we count the number of menu's in the menubar
	 * // because the index starts at 0 we have to substract 1 from the counted menu's
	 * // to actually remove the last menu from the menubar
	 * var index = menubar.getMenuCount() - 1;
	 * menubar.removeMenu(index);
	 * 
	 * // To remove the last 3 (three) menu's from the menubar we
	 * // can do that by adding additional indexes to the method
	 * // and delimit them with a comma.
	 * index = menubar.getMenuCount() - 1;
	 * menubar.removeMenu(index, index-1, index-2);
	 *  
	 * // For 'security' reasons it is best to ALWAYS remove the menu with the last index
	 * // first to avoid index out of range issues and other issues
	 * // EXAMPLE: when you first remove the menu at index 2 and then the menu at index 4
	 * // you actually remove the menu at index 2 and index 5
	 * // after removing the menu at index 2 all other menu's moved one index to the left
	 * // so the menu at index 4 moved to index 3 and the menu at index 5 moved to index 4 etc.
	 *
	 * @param index_1 
	 * @param index_2_to_n optional 
	 */
	public void js_removeMenu(Object[] index) throws PluginException
	{
		if (index.length > 1)
		{
			Arrays.sort(index, new DescendingNumberComparator());
		}

		for (Object element : index)
		{
			int idx = Utils.getAsInteger(element);
			if (idx >= 0)
			{
				removeMenu(idx);
			}
		}
	}

	/**
	 * Reset the menubar to the default.
	 *
	 * @sample
	 * // Note: method removeMenu only works in the smart client.
	 * 
	 * // When the menubar settings are solution specific it is advised to reset
	 * // the bar to its default settings when closing the solution.
	 * // Another reason is that when a client/developer is started first the
	 * // plugin will save the current settings in memory.
	 * // REMARK: Don't manipulate standard Servoy menuitems but remove
	 * // them and create new ones! Due to the way menuitems are managed by java it is not
	 * // possible to reset a menuitem anymore.
	 * var menubar = plugins.window.getMenuBar();
	 * // add a menu
	 * var menu = menubar.addMenu();
	 * menu.text = "new menu";
	 * // reset the menubar, the newly added menu will dissapear
	 * menubar.reset();
	 */
	public void js_reset()
	{
		windowProvider.getMenuHandler().resetMenuBar(windowName, initializeMenuBarResult);
	}

	@Deprecated
	public void js_validate()
	{
		windowProvider.getMenuHandler().validateMenuBar(windowName);
	}

	/**
	 * Add a menu to the menubar.
	 *
	 * @sample
	 * // Note: method addMenu only works in the smart client.
	 * 
	 * // when you don't define an index the menu will be added at the last
	 * // positon of the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * // set the text of the menu
	 * menu.text = "add menu";
	 * // set the mnemonic key
	 * menu.setMnemonic("a");
	 * // add another menu at a specific position in the menubar
	 * var another_menu = menubar.addMenu(2);
	 * another_menu.text = "another menu";
	 * another_menu.setMnemonic("t")
	 * // REMARK: normally you would add menu items, checkboxes etc in the same method
	 * // this example will show no menu items for now!
	 * // IMPORTANT: Working with menu's on developer and client can differ
	 */
	public Menu js_addMenu() throws PluginException
	{
		return js_addMenu(-1);
	}

	/**
	 * Add a menu to the menubar.
	 *
	 * @sampleas js_addMenu()
	 * 
	 * @param index
	 */
	public Menu js_addMenu(int index) throws PluginException
	{
		Menu menu = new Menu(windowProvider.getClientPluginAccess(), windowProvider.getMenuHandler(), windowProvider.getMenuHandler().createMenu(null));
		windowProvider.getMenuHandler().addToMenuBar(windowName, menu.getMenu(), index);
		return menu;
	}

	/**
	 * Get the menu at the selected index (starting at 0).
	 *
	 * @sample
	 * // Note: method getMenu only works in the smart client.
	 * 
	 * var menubar = plugins.window.getMenuBar();
	 * // get the menu at index 2
	 * // indexes start at 0 (zero) so index 2 is in fact position 3
	 * var menu = menubar.getMenu(2);
	 * // set the text of the menu at the chose position
	 * menu.text = "get menu";
	 * // set the mnemonic key
	 * menu.setMnemonic("g");
	 * // disable the menu
	 * menu.setEnabled(false);
	 * // REMARK: we actually changed an original menu! As a result resetting the
	 * // menubar will NOT reset the above changes. We need to reset the menu 
	 * // manually the following way:
	 * // get the menu
	 * //var menu = menubar.getMenu(2);
	 * // reset the values to default
	 * // notice we use an i18n message here the same way you would use it with
	 * // standard Servoy methods and plugins
	 * //menu.text = "i18n:servoy.menuitem.showAll";
	 * //menu.setEnabled(true);
	 * 
	 * @param index
	 */
	public Menu js_getMenu(int index) throws Exception
	{
		IMenu impl = windowProvider.getMenuHandler().getMenubarMenu(windowName, index);
		if (impl == null)
		{
			// menubar plugin was creating menu in get if it was not there yet
			return js_addMenu(index);
		}
		return new Menu(windowProvider.getClientPluginAccess(), windowProvider.getMenuHandler(), impl);
	}

	/**
	 * Show/hide the menu bar
	 *
	 * @sample
	 * // Note: method setVisible only works in the smart client.
	 * 
	 * // hide the menu bar
	 * var menubar = plugins.window.getMenuBar();
	 * menubar.setVisible(false);
	 * 
	 * @param visible
	 */
	public void js_setVisible(boolean visible)
	{
		windowProvider.getMenuHandler().setMenubarVisible(windowName, visible);
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}

}
