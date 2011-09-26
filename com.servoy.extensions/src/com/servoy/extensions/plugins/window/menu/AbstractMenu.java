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
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * Base class for Menus. Wraps IMenu client-specific menu implementation, is exposed in scripting.
 * 
 * @author rgansevles
 *
 */
public abstract class AbstractMenu implements IReturnedTypesProvider, IScriptable
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

	/**
	 * Add a checkbox at the selected index (starting at 0) or at the end.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // when you don't define an index the checkbox will be added at the last position
	 * // this is what you usually do to build a new menu
	 * // minimum settings are the text and method 
	 * // the method can be a global or form method
	 * // be sure to enter the method WITHOUT '()' at the end
	 * menu.addCheckBox("checkbox", feedback_checkbox);
	 * // add a checkbox with an icon
	 * menu.addCheckBox("checkbox with icon", feedback_checkbox, "media:///yourimage.gif");
	 * // add a checkbox with a mnemonic
	 * menu.addCheckBox("checkbox with mnemonic", feedback_checkbox, "media:///yourimage.gif", "c");
	 * // add a disabled checkbox
	 * menu.addCheckBox("checkbox disabled", feedback_checkbox, "media:///yourimage.gif", "d", false);
	 * // add a checkbox with text aligned to the right
	 * menu.addCheckBox("align right", feedback_checkbox, null, null, true, MenuItem.ALIGN_RIGHT);
	 * 
	 * // add a checkbox at a given index (checkbox properties must be configured after creation)
	 * // indexes start at 0 (zero) so index 2 is in fact position 3
	 * var chk = menu.addCheckBox(2);
	 * chk.text = "checkbox at index";
	 * chk.setMethod(feedback_checkbox);
	 *
	 * @param name/index optional 
	 * @param method optional 
	 * @param icon optional 
	 * @param mnemonic optional 
	 * @param enabled optional 
	 * @param align optional 
	 */
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

	/**
	 * Add a menu item at the selected index (starting at 0) or at the end.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // when you don't define an index the item will be added at the last position
	 * // this is what you usually do to build a new menu
	 * // minimum settings are the text and method
	 * // the method can be a global or form method
	 * // be sure to enter the method WITHOUT '()' at the end
	 * menu.addMenuItem("item", feedback_item);
	 * // add an item with an icon
	 * menu.addMenuItem("item with icon", feedback_item, "media:///yourimage.gif");
	 * // add an item with a mnemonic
	 * menu.addMenuItem("item with mnemonic", feedback_item, "media:///yourimage.gif", "i");
	 * // add a disabled item
	 * menu.addMenuItem("disabled item", feedback_item, "media:///yourimage.gif", "d", false);
	 * // add an item with text aligned to the right
	 * menu.addMenuItem("align right", feedback_item, null, null, true, SM_ALIGNMENT.RIGHT);
	 * 
	 * // add an item at a given index (item properties must be configured after creation)
	 * // indexes start at 0 (zero) so index 2 is in fact position 3
	 * var item = menu.addMenuItem(2);
	 * item.text = "item at index";
	 * item.setMethod(feedback_item);
	 *
	 * @param name/index optional 
	 * @param method optional 
	 * @param icon optional 
	 * @param mnemonic optional 
	 * @param enabled optional 
	 * @param align optional 
	 */
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

	/**
	 * Add a radiobutton at the selected index (starting at 0) or at the end.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 	
	 * // when you don't define an index the radiobutton will be added at the last position
	 * // this is what you usually do to build a new menu
	 * // minimum settings are the text and method
	 * // the method can be a global or form method
	 * // be sure to enter the method WITHOUT '()' at the end
	 * menu.addRadioButton("radio", feedback_radiobutton);
	 * // add a radiobutton with an icon
	 * menu.addRadioButton("radio with icon", feedback_radiobutton, "media:///yourimage.gif");
	 * 	
	 * // add a new radiobutton group
	 * // a group will 'bind' all added radiobuttons after the group together
	 * // as a result checking one item will uncheck the other
	 * // if no group is added, a group is created automatically when the first radiobutton is added to the menu
	 * // so in this case we will have two groups, one with the radiobuttons added until now and one with the ones added from now on
	 * menu.addRadioGroup();
	 * 	
	 * // add a radiobutton with a mnemonic
	 * menu.addRadioButton("radio with mnemonic", feedback_radiobutton, "media:///yourimage.gif", "i");
	 * // add a disabled radiobutton
	 * menu.addRadioButton("disabled radio", feedback_radiobutton, "media:///yourimage.gif", "d", false);
	 * // add a radiobutton with text aligned to the right
	 * menu.addRadioButton("align right", feedback_radiobutton, null, null, true, SM_ALIGNMENT.RIGHT);
	 * // add a radiobutton at a given index (item properties must be configured after creation)
	 * // indexes start at 0 (zero) so index 2 is in fact position 3
	 * var rd = menu.addRadioButton(2);
	 * rd.text = "radio at index";
	 * rd.setMethod(feedback_item);
	 *
	 * @param name/index optional 
	 * @param method optional 
	 * @param icon optional 
	 * @param mnemonic optional 
	 * @param enabled optional 
	 * @param align optional 
	 */
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

	/**
	 * Add a radiogroup for radiobuttons. A radiogroup groups together all radiobuttons that are added
	 * after the group is added. From all radiobuttons that belong to the same radiogroup only one can be
	 * checked at a time.
	 * 
	 * If no radiogroup is added, one is created automatically when the first radiobutton is added.
	 *
	 * @sampleas js_addRadioButton(Object[])
	 */
	public void js_addRadioGroup() throws PluginException
	{
		buttonGroup = menuHandler.createButtonGroup();
	}

	/**
	 * Add the separator at the selected index (starting at 0) or at the end (empty).
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add an item and a checkbox
	 * menu.addMenuItem("item", feedback_item);
	 * menu.addCheckBox("checkbox", feedback_checkbox);
	 * // add a separator
	 * menu.addSeparator();
	 * // add a radiobutton. it will be separated from the rest of the control by the separator
	 * menu.addRadioButton("radio", feedback_radiobutton);
	 * // add another separator between the item and the checkbox 
	 * menu.addSeparator(1);
	 * 
	 * @param index optional
	 */
	public void js_addSeparator()
	{
		menu.addSeparator(-1);
	}

	/**
	 * @sameas js_addSeparator()
	 */
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

	/**
	 * Add a submenu at the selected index (starting at 0) or it at the end.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add a first submenu
	 * var submenu1 = menu.addMenu("submenu 1");
	 * submenu1.addMenuItem("sub item 1 - 1", feedback_item);
	 * // add a submenu as child of the first submenu
	 * var submenu1_2 = submenu1.addMenu("submenu 1 - 2");
	 * submenu1_2.addMenuItem("sub item 1 - 2 - 1", feedback_item);
	 * // add another submenu as a child of the first submenu
	 * var submenu1_3 = submenu1.addMenu("submenu 1 - 3");
	 * submenu1_3.addMenuItem("sub item 1 - 3 - 1", feedback_item);
	 * // add a submenu to the second submenu of the first submenu
	 * var submenu1_3_2 = submenu1_2.addMenu("submenu 1 - 2 - 2");
	 * submenu1_3_2.addMenuItem("sub item 1 - 2 - 2 - 1", feedback_item);
	 * // add a submenu directly to the menu, at the first position
	 * var submenu0 = menu.addMenu(0);
	 * submenu0.text = "submenu 0";
	 * submenu0.addMenuItem("sub item 0 - 1", feedback_item);
	 *
	 * @param name/index optional 
	 */
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

	/**
	 * Get the checkbox at the selected index (starting at 0).
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add two radiobuttons
	 * menu.addRadioButton("radio one", feedback_radiobutton);
	 * menu.addRadioButton("radio two", feedback_radiobutton);
	 * // add a menu item, with a separator before it
	 * menu.addSeparator();
	 * menu.addMenuItem("item", feedback_item);
	 * // add a checkbox, with a separator before it
	 * menu.addSeparator();
	 * menu.addCheckBox("check", feedback_checkbox);
	 * // add a submenu with an item under it
	 * var submenu = menu.addMenu("submenu");
	 * submenu.addMenuItem("subitem", feedback_item);
	 * 
	 * // depending on some state, update the entries in the menu
	 * var some_state = true;
	 * if (some_state) {
	 * 	// select the first radiobutton
	 * 	menu.getRadioButton(0).selected = true;
	 * } else {
	 * 	// select the first radiobutton
	 * 	menu.getRadioButton(1).selected = true;
	 * }
	 * // enable/disable the menu item
	 * // remember to include the separators also when counting the index
	 * menu.getItem(3).enabled = !some_state;
	 * // select/unselect the checkbox
	 * // remember to include the separators also when counting the index
	 * menu.getCheckBox(5).selected = some_state;
	 * // change the text of the submenu and its item
	 * application.output(menu.getItemCount());
	 * if (some_state) {
	 * 	menu.getMenu(6).text = "some state";
	 * 	menu.getMenu(6).getItem(0).text = "some text";
	 * }
	 * else {
	 * 	menu.getMenu(6).text = "not some state";
	 * 	menu.getMenu(6).getItem(0).text = "other text";
	 * }
	 * 
	 * @param index
	 */
	public CheckBox js_getCheckBox(int index)
	{
		AbstractMenuItem item = js_getItem(index);
		if (item instanceof CheckBox)
		{
			return (CheckBox)item;
		}
		return null;
	}

	/**
	 * Get the item at the selected index (starting at 0).
	 *
	 * @sampleas js_getCheckBox(int)
	 * 
	 * @param index
	 */
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

	/**
	 * Get the number of items in the menu.
	 *
	 * @sample
	 * // REMARK: indexes start at 0, disabled items, non visible items and seperators are counted also
	 * // REMARK: this is especially important when getting items by the index
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add two radiobuttons
	 * menu.addRadioButton("radio one", feedback_radiobutton);
	 * menu.addRadioButton("radio two", feedback_radiobutton);
	 * // add a checkbox
	 * menu.addCheckBox("check", feedback_checkbox);
	 * // add a menu item
	 * menu.addMenuItem("item", feedback_item);
	 * // add another menu item
	 * menu.addMenuItem("item 2", feedback_item);
	 * 
	 * // remove the last item
	 * menu.removeItem(menu.getItemCount() - 1);
	 */
	public int js_getItemCount()
	{
		return menu.getMenuItemCount();
	}

	/**
	 * Retrieve the index of the item by text.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add two radiobuttons
	 * menu.addRadioButton("radio one", feedback_radiobutton);
	 * menu.addRadioButton("radio two", feedback_radiobutton);
	 * // add a checkbox
	 * menu.addCheckBox("check", feedback_checkbox);
	 * // add a menu item
	 * menu.addMenuItem("item", feedback_item);
	 * // add another menu item
	 * menu.addMenuItem("item 2", feedback_item);
	 * 
	 * // find the index of the checkbox
	 * var idx = menu.getItemIndexByText("check");
	 * // remove the checkbox by its index
	 * menu.removeItem(idx);
	 * // remove both radiobuttons by their indices
	 * menu.removeItem([0, 1]);
	 * // remove all remaining entries
	 * menu.removeAllItems();
	 * // add back an item
	 * menu.addMenuItem("new item", feedback_item);
	 * 
	 * @param text
	 */
	public int js_getItemIndexByText(String text)
	{
		if (text == null || "".equals(text)) //$NON-NLS-1$
		{
			Debug.error("You can not search for a text with a null or empty value."); //$NON-NLS-1$
			return -1;
		}

		for (int i = 0; i < menu.getMenuItemCount(); i++)
		{
			IMenuItem menuItem = menu.getMenuItem(i);
			if (menuItem != null && text.equals(menuItem.getText()))
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

	/**
	 * Get the radiobutton at the selected index (starting at 0).
	 *
	 * @sampleas js_getCheckBox(int)
	 * 
	 * @param index
	 */
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

	/**
	 * Get the submenu at the selected index (starting at 0).
	 *
	 * @sampleas js_getCheckBox(int)
	 * 
	 * @param index
	 */
	public Menu js_getMenu(int index)
	{
		IMenuItem menuItem = menu.getMenuItem(index);
		if (menuItem instanceof IMenu)
		{
			return new Menu(pluginAccess, menuHandler, (IMenu)menuItem);
		}
		return null;
	}

	/**
	 * Remove all items from the menu.
	 *
	 * @sampleas js_getItemIndexByText(String)
	 */
	public void js_removeAllItems()
	{
		menu.removeAllItems();
	}

	/**
	 * Remove the item(s) at the selected index/indices.
	 *
	 * @sampleas js_getItemIndexByText(String)
	 *
	 * @param index_1 
	 * @param index_2_to_n optional 
	 */
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

	/**
	 * Sets the value for the specified element client property key.
	 *
	 * @sampleas js_getClientProperty(Object)
	 * 
	 * @param key
	 * @param value
	 */
	public void js_putClientProperty(Object key, Object value)
	{
		menu.putClientProperty(key, value);
	}

	/**
	 * Gets the specified client property for the element based on a key.
	 *
	 * @sample
	 * // NOTE: Depending on the operating system, a user interface property name may be available.
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add an item to the menu
	 * menu.addMenuItem("item", feedback_item);
	 * 
	 * // set the tooltip of the menu via client properties
	 * // keep the original tooltip in a form or global variable
	 * originalTooltip = menu.getClientProperty("ToolTipText");
	 * menu.putClientProperty("ToolTipText", "changed tooltip");
	 * 
	 * // later restore the original tooltip from the variable
	 * //var menubar = plugins.window.getMenuBar();
	 * //var menu = menubar.getMenu(menubar.getMenuCount()-1);
	 * //menu.putClientProperty("ToolTipText", originalTooltip);
	 * 
	 * @param key
	 */
	public Object js_getClientProperty(Object key)
	{
		return menu.getClientProperty(key);
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
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
