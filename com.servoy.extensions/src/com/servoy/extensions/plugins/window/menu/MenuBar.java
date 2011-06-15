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
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;


/**
 * Handles menubar actions, delegates to client-specific (Swing/Wicket) handler.
 * 
 * @author rgansevles
 *
 */
@SuppressWarnings("nls")
public class MenuBar implements IScriptObject
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

	public int js_getMenuCount()
	{
		return windowProvider.getMenuHandler().getMenubarSize(windowName);
	}

	public int js_getMenuIndexByText(String name)
	{
		if (name == null || "".equals(name))
		{
			Debug.warn("You can not search for a name with a null or empty value.");
			return -1;
		}

		return windowProvider.getMenuHandler().getMenuIndexByText(windowName, name);
	}

	public void js_removeAllMenus() throws PluginException
	{
		removeMenu(-1);
	}

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

	public void js_reset()
	{
		windowProvider.getMenuHandler().resetMenuBar(windowName, initializeMenuBarResult);
	}

	@Deprecated
	public void js_validate()
	{
		windowProvider.getMenuHandler().validateMenuBar(windowName);
	}

	public Menu js_addMenu() throws PluginException
	{
		return js_addMenu(-1);
	}

	public Menu js_addMenu(int index) throws PluginException
	{
		Menu menu = new Menu(windowProvider.getClientPluginAccess(), windowProvider.getMenuHandler(), windowProvider.getMenuHandler().createMenu(null));
		windowProvider.getMenuHandler().addToMenuBar(windowName, menu.getMenu(), index);
		return menu;
	}

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

	public void js_setVisible(boolean visible)
	{
		windowProvider.getMenuHandler().setMenubarVisible(windowName, visible);
	}

	/* IScriptObject methods */

	public String[] getParameterNames(String methodName)
	{
		if ("addMenu".equals(methodName))
		{
			return new String[] { "[index]" };
		}
		if ("getMenu".equals(methodName))
		{
			return new String[] { "index" };
		}
		if ("removeMenu".equals(methodName))
		{
			return new String[] { "index 1", "[index 2-n]" };
		}
		if ("getMenuIndexByText".equals(methodName))
		{
			return new String[] { "menuName" };
		}
		if ("setVisible".equals(methodName))
		{
			return new String[] { "visible" };
		}

		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	public String getSample(String methodName)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("// Note: method ").append(methodName).append(" only works in the smart client.\n\n");

		String toolTip = getToolTip(methodName);
		if (toolTip != null)
		{
			sb.append("// ").append(toolTip).append('\n');
		}

		else if ("addMenu".equals(methodName))
		{
			sb.append("// add a menu at the given index\n");
			sb.append("// when you don't define an index the menu will be added at the last\n");
			sb.append("// positon of the menubar\n");
			sb.append("var menu = %%elementName%%.addMenu();\n");
			sb.append("\n");
			sb.append("// set the text of the menu at the chose position\n");
			sb.append("menu.setText(\"add menu\");\n");
			sb.append("\n");
			sb.append("// set the mnemonic key\n");
			sb.append("menu.setMnemonic(\"a\");\n");
			sb.append("\n");
			sb.append("// enable the menu\n");
			sb.append("menu.setEnabled(true);\n");
			sb.append("\n");
			sb.append("// REMARK: normally you would add menu items, checkboxes etc in the same method\n");
			sb.append("// this example will show no menu items for now!\n");
			sb.append("\n");
			sb.append("// IMPORTANT: Working with menu's on developer and client can differ");
		}
		else if ("getMenu".equals(methodName))
		{
			sb.append("// get the menu from at index 2\n");
			sb.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n");
			sb.append("var menu = %%elementName%%.getMenu(2);\n");
			sb.append("\n");
			sb.append("// set the text of the menu at the chose position\n");
			sb.append("menu.setText(\"get menu\");$\n");
			sb.append("\n");
			sb.append("// set the mnemonic key\n");
			sb.append("menu.setMnemonic(\"g\");\n");
			sb.append("\n");
			sb.append("// disable the menu\n");
			sb.append("menu.setEnabled(false);\n");
			sb.append("\n");
			sb.append("// REMARK: we actually changed an original menu! As a result resetting the\n");
			sb.append("// menubar will NOT reset the above changes. We need to reset the menu \n");
			sb.append("// manually the following way:\n");
			sb.append("\n");
			sb.append("// get the menu\n");
			sb.append("// var menu = %%elementName%%.getMenu(2);\n");
			sb.append("\n");
			sb.append("// reset the values to default\n");
			sb.append("// notice we use an i18n message here the same way you would use it with\n");
			sb.append("// standard Servoy methods and plugins\n");
			sb.append("// menu.setText(\"i18n:servoy.menuitem.showAll\");");
			sb.append("// menu.setEnabled(true);");
		}
		else if ("removeMenu".equals(methodName))
		{
			sb.append("// To remove the last menu in the menubar we count the number of menu's in the menubar\n");
			sb.append("// because the index starts at 0 we have to substract 1 from the counted menu's\n");
			sb.append("// to actually remove the last menu from the menubar\n");
			sb.append("%%elementName%%.removeMenu(%%elementName%%.getMenuCount() - 1);\n");
			sb.append("\n");
			sb.append("// To remove the last 3 (three) menu's from the menubar we\n");
			sb.append("// can do that by adding additional indexes to the method\n");
			sb.append("// and delimit them with a comma.\n");
			sb.append("var index = %%elementName%%.getMenuCount() - 1;\n");
			sb.append("\n");
			sb.append("// remove the last item\n");
			sb.append("%%elementName%%.removeMenu(index);\n");
			sb.append("\n");
			sb.append("// remove 3 (three) items\n");
			sb.append("// %%elementName%%.removeMenu(this.index, index-1, 2);\n");
			sb.append("\n");
			sb.append("// For 'security' reasons it is best to ALWAYS remove the menu with the last index\n");
			sb.append("// first to avoid index out of range issues and other issues\n");
			sb.append("// EXAMPLE: when you first remove the menu at index 2 and then the menu at index 4\n");
			sb.append("// you actually remove the menu at index 2 and index 5\n");
			sb.append("// after removing the menu at index 2 all other menu's moved one index to the left\n");
			sb.append("// so the menu at index 4 moved to index 3 and the menu at index 5 moved to index 4 etc.");
		}
		else if ("removeAllMenus".equals(methodName))
		{
			sb.append("// Remove all menus from the menubar.\n");
			sb.append("// Potentially dangerous because all accelerator (short) keys\n");
			sb.append("// will be deleted also (including the quit item)\n");
			sb.append("%%elementName%%.removeAllMenus();");
		}
		else if ("reset".equals(methodName))
		{
			sb.append("// When the menubar settings are solution specific it is advised to reset\n");
			sb.append("// the bar to its default settings when closing the solution.\n");
			sb.append("// Another reason is that when a client/developer is started first the\n");
			sb.append("// plugin will save the current settings in memory\n");
			sb.append("// REMARK: Don't manipulate standard Servoy menuitems but remove\n");
			sb.append("// them and create new ones! Due to the way menuitems are managed by java it is not\n");
			sb.append("// possible to reset a menuitem anymore.\n");
			sb.append("%%elementName%%.reset();");
		}
		else if ("setVisible".equals(methodName))
		{
			sb.append("%%elementName%%.setVisible(false)");
		}

		// undocumented?
		else
		{
			sb.append("%%elementName%%.").append(methodName).append("()");
		}

		return sb.append('\n').toString();
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(String)
	 */
	public String getToolTip(String methodName)
	{
		if ("addMenu".equals(methodName))
		{
			return "Add the menu at the selected index (starting at 0) or add it at the end (empty).";
		}
		if ("getMenu".equals(methodName))
		{
			return "Get the menu at the selected index (starting at 0).";
		}
		if ("getMenuCount".equals(methodName))
		{
			return "Get the number of (top level) menu's.";
		}
		if ("removeAllMenus".equals(methodName))
		{
			return "Remove all menus from the menubar.";
		}
		if ("removeMenu".equals(methodName))
		{
			return "Remove the menu(s) at the selected index/indices.";
		}
		if ("reset".equals(methodName))
		{
			return "Reset the menubar to the default.";
		}
		if ("validate".equals(methodName))
		{
			return "Use this when your add/remove/edit operation won't refresh.";
		}
		if ("getMenuIndexByText".equals(methodName))
		{
			return "Retrieve the index of the item by text.";
		}
		if ("setVisible".equals(methodName))
		{
			return "Show/hide the menu bar";
		}

		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}

}
