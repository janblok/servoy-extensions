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

import org.mozilla.javascript.Function;

import com.servoy.extensions.plugins.window.util.DescendingNumberComparator;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * Base class for Menus. Wraps IMenu client-specific menu implementation, is exposed in scripting.
 * 
 * @author rgansevles
 *
 */
public abstract class AbstractMenu implements IScriptObject
{
	private final IClientPluginAccess pluginAccess;
	private final IMenuHandler menuHandler;
	private IMenu menu;

	private IButtonGroup buttonGroup;

	public AbstractMenu()
	{
		this(null, null, null); // used for scripting
	}

	public AbstractMenu(IClientPluginAccess pluginAccess, IMenuHandler menuHandler, IMenu menu)
	{
		this.pluginAccess = pluginAccess;
		this.menuHandler = menuHandler;
		this.menu = menu;
	}

	public IMenuHandler getMenuHandler()
	{
		return menuHandler;
	}

	public IMenu getMenu()
	{
		return menu;
	}

	protected void setMenu(IMenu menu)
	{
		this.menu = menu;
	}

	public String getText()
	{
		return menu == null ? null : menu.getText();
	}

	public CheckBox js_addCheckBox(Object[] vargs) throws PluginException
	{
		int index;
		Object[] args;
		if (vargs != null && vargs.length == 1 && vargs[0] instanceof Number)
		{
			index = Utils.getAsInteger(vargs[0]);
			args = null;
		}
		else
		{
			index = -1;
			args = vargs;
		}
		IMenuItem menuItem = menuHandler.createMenuItem(menu, IMenuItem.MENU_ITEM_CHECK);
		menu.addMenuItem(menuItem, index);
		return (CheckBox)AbstractMenuItem.createmenuItem(pluginAccess, getMenuHandler(), menuItem, parseMenuItemArgs(pluginAccess, args), true);
	}

	@Deprecated
	public MenuItem js_addItem(Object[] vargs) throws PluginException
	{
		return js_addMenuItem(vargs);
	}

	public MenuItem js_addMenuItem(Object[] vargs) throws PluginException
	{
		int index;
		Object[] args;
		if (vargs != null && vargs.length == 1 && vargs[0] instanceof Number)
		{
			index = Utils.getAsInteger(vargs[0]);
			args = null;
		}
		else
		{
			index = -1;
			args = vargs;
		}
		IMenuItem menuItem = menuHandler.createMenuItem(menu, IMenuItem.MENU_ITEM_BUTTON);
		menu.addMenuItem(menuItem, index);
		return (MenuItem)AbstractMenuItem.createmenuItem(pluginAccess, getMenuHandler(), menuItem, parseMenuItemArgs(pluginAccess, args), true);
	}

	public RadioButton js_addRadioButton(Object[] vargs) throws PluginException
	{
		int index;
		Object[] args;
		if (vargs != null && vargs.length == 1 && vargs[0] instanceof Number)
		{
			index = Utils.getAsInteger(vargs[0]);
			args = null;
		}
		else
		{
			index = -1;
			args = vargs;
		}
		IRadioButtonMenuItem menuItem = (IRadioButtonMenuItem)menuHandler.createMenuItem(menu, IMenuItem.MENU_ITEM_RADIO);
		if (buttonGroup == null)
		{
			buttonGroup = menuHandler.createButtonGroup();
		}
		buttonGroup.add(menuItem);
		menu.addMenuItem(menuItem, index);
		return (RadioButton)AbstractMenuItem.createmenuItem(pluginAccess, getMenuHandler(), menuItem, parseMenuItemArgs(pluginAccess, args), true);
	}

	public void js_addRadioGroup() throws PluginException
	{
		buttonGroup = menuHandler.createButtonGroup();
	}

	// separator
	public void js_addSeparator()
	{
		menu.addSeparator(-1);
	}

	public void js_addSeparator(int index)
	{
		menu.addSeparator(index);
	}

	// submenu
	@Deprecated
	public Menu js_addSubMenu() throws PluginException
	{
		return js_addMenu(null);
	}

	@Deprecated
	public Menu js_addSubMenu(int index) throws PluginException
	{
		return js_addMenu(new Object[] { new Integer(index) });
	}

	public Menu js_addMenu(Object[] vargs) throws PluginException
	{
		int index;
		Object[] args;
		if (vargs != null && vargs.length == 1 && vargs[0] instanceof Number)
		{
			index = Utils.getAsInteger(vargs[0]);
			args = null;
		}
		else
		{
			index = -1;
			args = vargs;
		}

		MenuItemArgs menuItemArgs = parseMenuItemArgs(pluginAccess, args);

		IMenu subMenu = menuHandler.createMenu(menu);
		menu.addMenuItem(subMenu, index);
		if (menuItemArgs != null && menuItemArgs.name != null)
		{
			subMenu.setText(menuItemArgs.name);
		}
		return new Menu(pluginAccess, menuHandler, subMenu);
	}

	public CheckBox js_getCheckBox(int index)
	{
		AbstractMenuItem item = js_getItem(index);
		if (item instanceof CheckBox)
		{
			return (CheckBox)item;
		}
		return null;
	}

	public AbstractMenuItem js_getItem(int index)
	{
		IMenuItem menuItem = menu.getMenuItem(index);
		if (menuItem != null)
		{
			AbstractMenuItem scriptObjectWrapper = menuItem.getScriptObjectWrapper();
			if (scriptObjectWrapper == null)
			{
				scriptObjectWrapper = AbstractMenuItem.createmenuItem(pluginAccess, getMenuHandler(), menuItem, null, true);
				menuItem.setScriptObjectWrapper(scriptObjectWrapper);
			}

			return scriptObjectWrapper;
		}
		return null;
	}

	public int js_getItemCount()
	{
		return menu.getMenuItemCount();
	}

	public int js_getItemIndexByText(String name)
	{
		if (name == null || "".equals(name)) //$NON-NLS-1$
		{
			Debug.error("You can not search for a name with a null or empty value."); //$NON-NLS-1$
			return -1;
		}

		for (int i = 0; i < menu.getMenuItemCount(); i++)
		{
			IMenuItem menuItem = menu.getMenuItem(i);
			if (menuItem != null && name.equals(menuItem.getText()))
			{
				return i;
			}
		}
		return -1;
	}


	public int getItemIndex(IMenuItem menuItem)
	{
		for (int i = 0; menuItem != null && i < menu.getMenuItemCount(); i++)
		{
			if (menuItem.equals(menu.getMenuItem(i)))
			{
				return i;
			}
		}
		return -1;
	}

	public RadioButton js_getRadioButton(int index)
	{
		AbstractMenuItem item = js_getItem(index);
		if (item instanceof RadioButton)
		{
			return (RadioButton)item;
		}
		return null;
	}

	@Deprecated
	public Menu js_getSubMenu(int index)
	{
		return js_getMenu(index);
	}

	public Menu js_getMenu(int index)
	{
		IMenuItem menuItem = menu.getMenuItem(index);
		if (menuItem instanceof IMenu)
		{
			return new Menu(pluginAccess, menuHandler, (IMenu)menuItem);
		}
		return null;
	}

	public void js_removeAllItems()
	{
		menu.removeAllItems();
	}

	public void js_removeItem(Object[] index) throws PluginException
	{
		if (index == null)
		{
			return;
		}

		Arrays.sort(index, new DescendingNumberComparator());

		for (Object element : index)
		{
			int idx = Utils.getAsInteger(element);

			if (idx < 0 || idx >= menu.getMenuItemCount())
			{
				throw new PluginException("The item with index " + idx + " doesn't exist."); //$NON-NLS-1$ //$NON-NLS-2$
			}

			menu.removeMenuItem(idx);
		}
	}

	public void js_putClientProperty(Object key, Object value)
	{
		menu.putClientProperty(key, value);
	}

	public Object js_getClientProperty(Object key)
	{
		return menu.getClientProperty(key);
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}

	@SuppressWarnings("nls")
	public String[] getParameterNames(String methodName)
	{
		if ("addMenuItem".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "[name]", "[method]", "[icon]", "[mnemonic]", "[enabled]", "[align]", };
		}
		if ("addCheckBox".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "[name]", "[method]", "[icon]", "[mnemonic]", "[enabled]", "[align]", };
		}
		if ("addRadioButton".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "[name]", "[method]", "[icon]", "[mnemonic]", "[enabled]", "[align]", };
		}
		if ("getItem".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "index" }; //$NON-NLS-1$ 
		}
		if ("getCheckBox".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "index" }; //$NON-NLS-1$ 
		}
		if ("getRadioButton".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "index" }; //$NON-NLS-1$ 
		}
		if ("removeItem".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "index 1", "[index 2-n]" }; //$NON-NLS-1$ //$NON-NLS-2$ 
		}
		if ("addMenu".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "[name]", "[menu]", "[icon]", "[mnemonic]", "[enabled]", "[align]", };
		}
		if ("getMenu".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "index" }; //$NON-NLS-1$ 
		}
		if ("getItemIndexByText".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "name" }; //$NON-NLS-1$ 
		}
		if ("putClientProperty".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "key", "value" }; //$NON-NLS-1$ 
		}
		if ("getClientProperty".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "key" }; //$NON-NLS-1$ 
		}
		return null;
	}

	@SuppressWarnings("nls")
	public String getSample(String methodName)
	{
		StringBuilder sample = new StringBuilder();
		if ("getItemCount".equals(methodName)) //$NON-NLS-1$ 
		{
			sample.append("// " + getToolTip(methodName) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ 
			sample.append("// REMARK: indexes start at 0, disabled items, non visible items and seperators are counted also\n"); //$NON-NLS-1$ 
			sample.append("// REMARK: this is especially important when getting items by the index\n"); //$NON-NLS-1$ 
			sample.append("application.output(plugins.window.getMenu(0).getItemCount());\n"); //$NON-NLS-1$ 
		}
		else if ("addMenuItem".equals(methodName)) //$NON-NLS-1$ 
		{
			sample.append("// " + getToolTip(methodName) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ 
			sample.append("// get the menu at the last index\n"); //$NON-NLS-1$ 
			sample.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n"); //$NON-NLS-1$ 
			sample.append("var menu = plugins.window.getMenu(plugins.window.getMenuCount() - 1);\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("// when you don't define an index the item will be added at the last position\n"); //$NON-NLS-1$ 
			sample.append("// this is what you usually do to build a new menu\n"); //$NON-NLS-1$ 
			sample.append("// create the settings for the specified menu item\n"); //$NON-NLS-1$ 
			sample.append("// minimum settings are the text and method properties\n"); //$NON-NLS-1$ 
			sample.append("// the method can be a global or form method\n"); //$NON-NLS-1$ 
			sample.append("// be sure to enter the method WITHOUT '()' at the end\n"); //$NON-NLS-1$ 
			sample.append("var item = menu.addMenuItem(\"item with feedback\",globals.feedback_item);\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("var item = menu.addMenuItem();\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("// add an 'input' array. the array will be concatenated to the end of the arguments\n"); //$NON-NLS-1$ 
			sample.append("// array which can be read out in the selected method\n"); //$NON-NLS-1$ 
			sample.append("var input = [1,\"is\",\"the\",\"added\",\"input\",false];\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("item.text = \"item with input\";\n"); //$NON-NLS-1$ 
			sample.append("item.setMethod(globals.feedback_item,input);\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("var item = menu.addMenuItem();\n"); //$NON-NLS-1$ 
			sample.append("// add an icon to the item\n"); //$NON-NLS-1$ 
			sample.append("item.text = \"item with icon\";\n"); //$NON-NLS-1$ 
			sample.append("item.setMethod(globals.feedback_item, input);\n"); //$NON-NLS-1$ 
			sample.append("item.setIcon(\"media:///yourimage.gif\");\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("var item = menu.addMenuItem();\n"); //$NON-NLS-1$ 
			sample.append("// add an accelerator key ('alt shift 2' in the below example)\n"); //$NON-NLS-1$ 
			sample.append("// REMARK: always test the accelerator key. sometimes they will not work because\n"); //$NON-NLS-1$ 
			sample.append("// these keys already have an 'action' assigned to them via the operating system.\n"); //$NON-NLS-1$ 
			sample.append("item.text = \"item with accelerator\";\n"); //$NON-NLS-1$ 
			sample.append("item.setMethod(globals.feedback_item, input);\n"); //$NON-NLS-1$ 
			sample.append("item.setIcon(\"media:///yourimage.gif\");\n"); //$NON-NLS-1$ 
			sample.append("item.setAccelerator(\"alt shift 2\");\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("var item = menu.addMenuItem();\n"); //$NON-NLS-1$ 
			sample.append("// add a mnemonic key  ('i' in our example) which is the underlined shortkey on windows\n"); //$NON-NLS-1$ 
			sample.append("// REMARK: setting the mnemonic key is platform dependent\n"); //$NON-NLS-1$ 
			sample.append("// the accelerator key will not work in this and the next example\n"); //$NON-NLS-1$ 
			sample.append("item.text = \"item with mnemonic\";\n"); //$NON-NLS-1$ 
			sample.append("item.setMethod(globals.feedback_item, input);\n"); //$NON-NLS-1$ 
			sample.append("item.setIcon(\"media:///yourimage.gif\");\n"); //$NON-NLS-1$ 
			sample.append("item.setAccelerator(\"pressed COMMA\");\n"); //$NON-NLS-1$ 
			sample.append("item.setMnemonic(\"i\");\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("// create a disabled menu item\n"); //$NON-NLS-1$ 
			sample.append("var item = menu.addMenuItem(\"item disabled\",globals.feedback_item,\"media:///yourimage.gif\",\"t\",false);\n"); //$NON-NLS-1$ 
			sample.append("// set the method args\n"); //$NON-NLS-1$ 
			sample.append("item.setMethodArguments(input);\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("var item = menu.addMenuItem(\"item visible\",globals.feedback_item,\"media:///yourimage.gif\",\"e\");\n"); //$NON-NLS-1$ 
			sample.append("// this accelerator key will work\n"); //$NON-NLS-1$ 
			sample.append("item.setAccelerator(\"shift meta PAGE_DOWN\");\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("var item = menu.addMenuItem(\"item invisible\",globals.feedback_item,\"media:///yourimage.gif\");\n"); //$NON-NLS-1$ 
			sample.append("// now the item is enabled and NOT visible\n"); //$NON-NLS-1$ 
			sample.append("item.setVisible(false);\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$ 
			sample.append("// add a separator at the last position or at a given index\n"); //$NON-NLS-1$ 
			sample.append("menu.addSeparator();\n"); //$NON-NLS-1$ 
			sample.append("return;\n"); //$NON-NLS-1$ 
			sample.append("\n"); //$NON-NLS-1$  
		}
		else if ("getItem".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// get the menu at the last position\n");
			sample.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n");
			sample.append("var menu = plugins.window.getMenu(plugins.window.getMenuCount() - 1);\n");
			sample.append("\n");
			sample.append("var item = menu.getItem(0);\n");
			sample.append("\n");
			sample.append("item.setText(\"Changed menu item\");\n");
			sample.append("\n");
			sample.append("// REMARK: we actually changed an original menu (item)! As a result resetting the\n");
			sample.append("// menubar will NOT reset the above changes. We need to reset the menu (item)\n");
			sample.append("// manually the following way:\n");
			sample.append("\n");
			sample.append("// get the menu\n");
			sample.append("// var menu = plugins.window.getMenu(2);\n");
			sample.append("\n");
			sample.append("// get the item\n");
			sample.append("// var item = menu.getItem(0);\n");
			sample.append("\n");
			sample.append("// reset the values to default\n");
			sample.append("// notice we use an i18n message here the same way you would use it with\n");
			sample.append("// standard Servoy methods and plugins\n");
			sample.append("// item.setText(\"i18n:servoy.menuitem.viewAsRecord\");\n");
		}
		else if ("addCheckBox".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// get the menu at the last index\n");
			sample.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n");
			sample.append("var menu = plugins.window.getMenu(plugins.window.getMenuCount() - 1);\n");
			sample.append("\n");
			sample.append("// when you don't define an index the checkbox will be added at the last position\n");
			sample.append("// this is what you usually do to build a new menu\n");
			sample.append("// minimum settings are the text and method properties\n");
			sample.append("// the method can be a global or form method\n");
			sample.append("// be sure to enter the method WITHOUT '()' at the end\n");
			sample.append("var checkbox = menu.addCheckBox(\"checkbox with feedback\",feedback_checkbox);\n");
			sample.append("\n");
			sample.append("var checkbox = menu.addCheckBox(\"checkbox selected\",feedback_checkbox);\n");
			sample.append("// set the checkbox to selected\n");
			sample.append("checkbox.setSelected(true);\n");
			sample.append("\n");
			sample.append("var checkbox = menu.addCheckBox(\"checkbox with input\");\n");
			sample.append("\n");
			sample.append("// add an 'input' array. the array will be concatenated to the end of the arguments\n");
			sample.append("// array which can be read out in the selected method\n");
			sample.append("var input = [1,\"is\",\"the\",\"added\",\"input\",false];\n");
			sample.append("\n");
			sample.append("checkbox.setMethod(feedback_checkbox, input);\n");
			sample.append("\n");
			sample.append("// create a checkbox with an icon\n");
			sample.append("var checkbox = menu.addCheckBox(\"checkbox with icon\",feedback_checkbox,\"media:///yourimage.gif\");\n");
			sample.append("\n");
			sample.append("var checkbox = menu.addCheckBox(\"checkbox with accelerator\",feedback_checkbox,\"media:///yourimage.gif\");\n");
			sample.append("// add an accelerator key ('alt shift a' in the below example)\n");
			sample.append("// REMARK: always test the accelerator key. sometimes they will not work because\n");
			sample.append("// these keys already have an 'action' assigned to them via the operating system.\n");
			sample.append("checkbox.setAccelerator(\"alt shift a\");\n");
			sample.append("\n");
			sample.append("var checkbox = menu.addCheckBox(\"checkbox with mnemonic\",feedback_checkbox,false,input,\"media:///yourimage.gif\");\n");
			sample.append("// add a mnemonic key  ('i' in our example) which is the underlined shortkey on windows\n");
			sample.append("// REMARK: setting the mnemonic key is platform dependent\n");
			sample.append("checkbox.setMnemonic(\"i\");\n");
			sample.append("\n");
			sample.append("var checkbox = menu.addCheckBox(\"checkbox disabled\",feedback_checkbox);\n");
			sample.append("// disable the menu item\n");
			sample.append("checkbox.setEnabled(false);\n");
			sample.append("\n");
			sample.append("var checkbox = menu.addCheckBox(\"checkbox invisible\",feedback_checkbox);\n");
			sample.append("// set the menu item disabled and NOT visible\n");
			sample.append("checkbox.setVisible(false);\n");
			sample.append("\n");
			sample.append("// add a separator at the last position or at a given index\n");
			sample.append("menu.addSeparator();\n");
			sample.append("\n");
		}
		else if ("getCheckBox".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// get the menu at the last position\n");
			sample.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n");
			sample.append("var menu = plugins.window.getMenu(plugins.window.getMenuCount() - 1);\n");
			sample.append("\n");
			sample.append("var checkbox = menu.getCheckBox(0);\n");
			sample.append("\n");
			sample.append("checkbox.setText(\"Changed menu item\");\n");
			sample.append("\n");
			sample.append("// REMARK: we actually changed an original menu (item)! As a result resetting the\n");
			sample.append("// menubar will NOT reset the above changes. We need to reset the menu (item)\n");
			sample.append("// manually the following way:\n");
			sample.append("\n");
			sample.append("// get the menu\n");
			sample.append("// var menu = plugins.window.getMenu(2);\n");
			sample.append("\n");
			sample.append("// get the item\n");
			sample.append("// var item = menu.getItem(0);\n");
			sample.append("\n");
			sample.append("// reset the values to default\n");
			sample.append("// notice we use an i18n message here the same way you would use it with\n");
			sample.append("// standard Servoy methods and plugins\n");
			sample.append("// item.setText(\"i18n:servoy.menuitem.viewAsRecord\");\n");
		}
		else if ("addRadioGroup".equals(methodName) || "addRadioButton".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// get the menu at the last index\n");
			sample.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n");
			sample.append("var menu = plugins.window.getMenu(plugins.window.getMenuCount() - 1);\n");
			sample.append("\n");
			sample.append("// add a new Radiobutton group\n");
			sample.append("// a group will 'bind' all added radiobuttons after the group together\n");
			sample.append("// as a result checking one item will uncheck the other\n");
			sample.append("menu.addRadioGroup();\n");
			sample.append("\n");
			sample.append("// when you don't define an index the radiobutton will be added at the last position\n");
			sample.append("// this is what you usually do to build a new menu\n");
			sample.append("\n");
			sample.append("// create the settings for the specified menu item\n");
			sample.append("// minimum settings are the text and method properties\n");
			sample.append("// the method can be a global or form method\n");
			sample.append("// be sure to enter the method WITHOUT '()' at the end\n");
			sample.append("var radiobutton = menu.addRadioButton(\"radiobutton with feedback\",feedback_radiobutton);\n");
			sample.append("\n");
			sample.append("var radiobutton = menu.addRadioButton(\"radiobutton selected\",feedback_radiobutton);\n");
			sample.append("// set the radiobutton to selected\n");
			sample.append("radiobutton.setSelected(true);\n");
			sample.append("\n");
			sample.append("var radiobutton = menu.addRadioButton(\"radiobutton with input\");\n");
			sample.append("\n");
			sample.append("// add an 'input' array. the array will be concatenated to the end of the arguments\n");
			sample.append("// array which can be read out in the selected method\n");
			sample.append("var input = [1,\"is\",\"the\",\"added\",\"input\",false];\n");
			sample.append("\n");
			sample.append("radiobutton.setMethod(feedback_radiobutton,input);\n");
			sample.append("\n");
			sample.append("// create an item with an icon\n");
			sample.append("var radiobutton = menu.addRadioButton(\"radiobutton with icon\",feedback_radiobutton,\"media:///yourimage.gif\");\n");
			sample.append("\n");
			sample.append("var radiobutton = menu.addRadioButton(\"radiobutton with accelerator\",feedback_radiobutton);\n");
			sample.append("// add an accelerator key ('alt shift 3' in the below example)\n");
			sample.append("// REMARK: always test the accelerator key. sometimes they will not work because\n");
			sample.append("// these keys already have an 'action' assigned to them via the operating system.\n");
			sample.append("radiobutton.setAccelerator(\"alt shift 3\");\n");
			sample.append("\n");
			sample.append("// add a separator at the last position or at a given index\n");
			sample.append("menu.addSeparator();\n");
			sample.append("\n");
			sample.append("// add a new Radiobutton group\n");
			sample.append("menu.addRadioGroup();\n");
			sample.append("\n");
			sample.append("// add a mnemonic key  ('i' in our example) which is the underlined shortkey on windows\n");
			sample.append("// REMARK: setting the mnemonic key is platform dependent\n");
			sample.append("var radiobutton = menu.addRadioButton(\"radiobutton with mnemonic\",feedback_radiobutton,\"media:///yourimage.gif\",\"i\");\n");
			sample.append("\n");
			sample.append("var radiobutton = menu.addRadioButton(\"radiobutton disabled\",feedback_radiobutton);\n");
			sample.append("// disable the menu item\n");
			sample.append("radiobutton.setEnabled(false);\n");
			sample.append("\n");
			sample.append("var radiobutton = menu.addRadioButton(\"radiobutton invisible\",feedback_radiobutton);\n");
			sample.append("// now the item is enabled and NOT visible\n");
			sample.append("radiobutton.setVisible(false);\n");
			sample.append("\n");
			sample.append("// add a separator at the last position or at a given index\n");
			sample.append("menu.addSeparator();\n");
			sample.append("\n");
		}
		else if ("getRadioButton".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// get the menu at the last position\n");
			sample.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n");
			sample.append("var menu = plugins.window.getMenu(plugins.window.getMenuCount() - 1);\n");
			sample.append("\n");
			sample.append("var checkbox = menu.getItem(0);\n");
			sample.append("\n");
			sample.append("checkbox.setText(\"Changed menu item\");\n");
			sample.append("\n");
			sample.append("// REMARK: we actually changed an original menu (item)! As a result resetting the\n");
			sample.append("// menubar will NOT reset the above changes. We need to reset the menu (item)\n");
			sample.append("// manually the following way:\n");
			sample.append("\n");
			sample.append("// get the menu\n");
			sample.append("// var menu = plugins.window.getMenu(2);\n");
			sample.append("\n");
			sample.append("// get the item\n");
			sample.append("// var item = menu.getItem(0);\n");
			sample.append("\n");
			sample.append("// reset the values to default\n");
			sample.append("// notice we use an i18n message here the same way you would use it with\n");
			sample.append("// standard Servoy methods and plugins\n");
			sample.append("// item.setText(\"i18n:servoy.menuitem.viewAsRecord\");\n");
		}
		else if ("removeItem".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// get the menu at the last index\n");
			sample.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n");
			sample.append("var menu = plugins.window.getMenu(plugins.window.getMenuCount() - 1);\n");
			sample.append("\n");
			sample.append("// remove only one item at the selected index\n");
			sample.append("// from the selected menu\n");
			sample.append("// menu.removeItem(0);\n");
			sample.append("\n");
			sample.append("// remove more than one item at the selected indices\n");
			sample.append("// from the selected menu\n");
			sample.append("menu.removeItem(1,2);\n");
		}
		else if ("removeAllItems".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// get the menu at the last index\n");
			sample.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n");
			sample.append("var menu = plugins.window.getMenu(plugins.window.getMenuCount() - 1);\n");
			sample.append("\n");
			sample.append("// remove all menu items from the selected menu\n");
			sample.append("menu.removeAllItems();\n");
		}
		else if ("addSeparator".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("plugins.window.getMenu(0).addSeparator();\n");
		}
		else if ("addMenu".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// get the menu at the last index\n");
			sample.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n");
			sample.append("var menu = plugins.window.getMenu(plugins.window.getMenuCount() - 1);\n");
			sample.append("\n");
			sample.append("// add a (first) submenu\n");
			sample.append("var submenu1 = menu.addMenu(\"submenu 1\");\n");
			sample.append("submenu1.addMenuItem(\"sub item 1\",globals.feedback_item);\n");
			sample.append("\n");
			sample.append("// add a (second) submenu\n");
			sample.append("var submenu2 = submenu1.addMenu(\"submenu 2\");\n");
			sample.append("submenu2.addMenuItem(\"sub item 2\",globals.feedback_item);\n");
			sample.append("\n");
			sample.append("// add a (third) submenu\n");
			sample.append("var submenu3 = submenu1.addMenu(\"submenu 3\");\n");
			sample.append("submenu3.addMenuItem(\"sub item 3\",globals.feedback_item);\n");
			sample.append("\n");
			sample.append("// add a (first) submenu to the (third) submenu\n");
			sample.append("var submenu4 = submenu3.addMenu(\"submenu 4\");\n");
			sample.append("submenu4.addMenuItem(\"sub item 4\",globals.feedback_item);\n");
			sample.append("\n");
			sample.append("// add a (first) submenu to the (first) submenu of the (third) submenu\n");
			sample.append("var submenu5 = submenu4.addMenu(\"submenu 5\");\n");
			sample.append("submenu5.addMenuItem(\"sub item 5\",globals.feedback_item);\n");
		}
		else if ("getMenu".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// get the menu at the last position\n");
			sample.append("// indexes start at 0 (zero) so index 2 is in fact position 3\n");
			sample.append("var menu = plugins.window.getMenu(plugins.window.getMenuCount() - 1);\n");
			sample.append("\n");
			sample.append("var checkbox = menu.getMenu(0);\n");
			sample.append("\n");
			sample.append("checkbox.setText(\"Changed menu item\");\n");
		}
		else if ("putClientProperty".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// NOTE: Depending on the operating system, a user interface property name may be available.\n");
			sample.append("plugins.window.putClientProperty('ToolTipText','some text');\n");
		}
		else if ("getClientProperty".equals(methodName))
		{
			sample.append("// " + getToolTip(methodName) + "\n");
			sample.append("// NOTE: Depending on the operating system, a user interface property name may be available.\n");
			sample.append("var property = plugins.window.getClientProperty('ToolTipText');\n");
		}
		else
		{
			return null;
		}
		return sample.toString();
	}

	@SuppressWarnings("nls")
	public String getToolTip(String methodName)
	{
		if ("getItemCount".equals(methodName))
		{
			return "Get the number of items in the menu.";
		}
		if ("addMenuItem".equals(methodName))
		{
			return "Add the item at the selected index (starting at 0) or add it at the end (empty).";
		}
		if ("addCheckBox".equals(methodName))
		{
			return "Add the Checkbox at the selected index (starting at 0) or add it at the end (empty).";
		}
		if ("addRadioButton".equals(methodName))
		{
			return "Add the Radiobutton at the selected index (starting at 0) or add it at the end (empty).";
		}
		if ("addRadioGroup".equals(methodName))
		{
			return "Add a Radiogroup for the Radiobuttons.";
		}
		if ("addSeparator".equals(methodName))
		{
			return "Add the separator at the selected index (starting at 0) or add it at the end (empty).";
		}
		if ("getItem".equals(methodName))
		{
			return "Get the item at the selected index (starting at 0).";
		}
		if ("getCheckBox".equals(methodName))
		{
			return "Get the Checkbox at the selected index (starting at 0).";
		}
		if ("getRadioButton".equals(methodName))
		{
			return "Get the Radiobutton at the selected index (starting at 0).";
		}
		if ("removeItem".equals(methodName))
		{
			return "Remove the item(s) at the selected index/indices.";
		}
		if ("removeAllItems".equals(methodName))
		{
			return "Remove all items from the menu.";
		}
		if ("addMenu".equals(methodName))
		{
			return "Add the submenu at the selected index (starting at 0) or add it at the end (empty).";
		}
		if ("getMenu".equals(methodName))
		{
			return "Get the submenu at the selected index (starting at 0).";
		}
		if ("getItemIndexByText".equals(methodName))
		{
			return "Retrieve the index of the item by text.";
		}
		if ("putClientProperty".equals(methodName))
		{
			return "Sets the value for the specified element client property key.";
		}
		if ("getClientProperty".equals(methodName))
		{
			return "Gets the specified client property for the element based on a key.";
		}
		return null;
	}

	@SuppressWarnings("nls")
	public boolean isDeprecated(String methodName)
	{
		if ("addItem".equals(methodName))
		{
			return true;
		}
		if ("addSubMenu".equals(methodName) || "getSubMenu".equals(methodName))
		{
			return true;
		}
		return false;
	}

	public static MenuItemArgs parseMenuItemArgs(IClientPluginAccess pluginAccess, Object[] args)
	{
		if (args == null) return null;

		String name = null;
		if (args.length >= 1 && args[0] != null)
		{
			name = args[0].toString();
		}
		if (name == null) name = "noname"; //$NON-NLS-1$
		if (name.startsWith("i18n:")) name = pluginAccess.getI18NMessage(name, null); //$NON-NLS-1$

		Object[] submenu = null;
		Function method = null;
		if (args.length >= 2 && args[1] != null)
		{
			if (args[1] instanceof Function)
			{
				method = (Function)args[1];
			}
			else if (args[1].getClass().isArray())
			{
				submenu = (Object[])args[1];
			}
		}

		String imageURL = null;
		byte[] imageBytes = null;
		if (args.length >= 3 && args[2] != null && !"".equals(args[2])) //$NON-NLS-1$
		{
			if (args[2] instanceof String && ((String)args[2]).length() > 0)
			{
				imageURL = (String)args[2];
			}
			else if (args[2] instanceof byte[])
			{
				imageBytes = (byte[])args[2];
			}
		}

		char mnemonic = 0;
		if (args.length >= 4 && args[3] != null)
		{
			if (args[3] instanceof Character)
			{
				mnemonic = ((Character)args[3]).charValue();
			}
			if (args[3] instanceof String && args[3].toString().length() >= 1)
			{
				mnemonic = args[3].toString().charAt(0);
			}
		}

		boolean enabled = true;
		if (args.length >= 5 && args[4] != null)
		{
			enabled = Utils.getAsBoolean(args[4]);
		}

		int align = -1;//use 2,0,4
		if (args.length >= 6 && args[5] != null)
		{
			align = Utils.getAsInteger(args[5]);
		}

		return new MenuItemArgs(name, submenu, method, imageURL, imageBytes, mnemonic, align, enabled);
	}

	public static class MenuItemArgs
	{
		public final String name;
		public final Object[] submenu;
		public final Function method;
		public final String imageURL;
		public final byte[] imageBytes;
		public final char mnemonic;
		public final int align;
		public final boolean enabled;

		public MenuItemArgs(String name, Object[] submenu, Function method, String imageURL, byte[] imageBytes, char mnemonic, int align, boolean enabled)
		{
			this.name = name;
			this.submenu = submenu;
			this.method = method;
			this.imageURL = imageURL;
			this.imageBytes = imageBytes;
			this.mnemonic = mnemonic;
			this.align = align;
			this.enabled = enabled;
		}
	}
}
