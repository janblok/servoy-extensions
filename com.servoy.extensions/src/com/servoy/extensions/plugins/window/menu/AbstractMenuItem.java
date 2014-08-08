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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.mozilla.javascript.Function;

import com.servoy.base.scripting.annotations.ServoyClientSupport;
import com.servoy.extensions.plugins.window.WindowProvider;
import com.servoy.extensions.plugins.window.menu.AbstractMenu.MenuItemArgs;
import com.servoy.extensions.plugins.window.util.Utilities;
import com.servoy.j2db.Messages;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Utils;

/**
 * Base class for Menu items. Wraps IMenuItem client-specific menu item implementation, is exposed in scripting.
 * 
 * @author rgansevles
 *
 */
@ServoyClientSupport(ng = true, wc = true, sc = true)
public abstract class AbstractMenuItem implements IScriptable, IJavaScriptType
{
	protected IMenuItem menuItem;
	private IClientPluginAccess pluginAccess;
	private FunctionDefinition functionDefinition;
	protected Object[] methodArguments;

	protected boolean align = true;
	private boolean legacyMenubarArguments = false;
	private IMenuHandler menuHandler;

	public AbstractMenuItem()
	{
		// only used by script engine
	}

	public AbstractMenuItem(IClientPluginAccess pluginAccess, IMenuHandler menuHandler, IMenuItem menuItem)
	{
		this.pluginAccess = pluginAccess;
		this.menuHandler = menuHandler;
		this.menuItem = menuItem;
		menuItem.addActionListener(createActionListener());
		menuItem.setScriptObjectWrapper(this);
	}

	public void setLegacyMenubarArguments(boolean legacyMenubarArguments)
	{
		this.legacyMenubarArguments = legacyMenubarArguments;
	}

	public IMenuItem getMenuItem()
	{
		return menuItem;
	}

	public void setFunctionDefinition(FunctionDefinition functionDefinition)
	{
		this.functionDefinition = functionDefinition;
	}

	protected ActionListener createActionListener()
	{
		return new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (functionDefinition != null)
				{
					Object[] args;
					if (legacyMenubarArguments)
					{
						// first add 5 arguments as was defined in old menubar plugin
						IMenu parentMenuImpl = menuItem.getParentMenu();
						Menu parentMenu = parentMenuImpl == null ? null : new Menu(pluginAccess, null /* not used here */, parentMenuImpl);
						IMenu grandParentMenuImpl = parentMenu == null ? null : parentMenuImpl.getParentMenu();
						Menu grandParentMenu = grandParentMenuImpl == null ? null : new Menu(pluginAccess, null /* not used here */, grandParentMenuImpl);

						args = new Object[] { //
						new Integer(parentMenu == null ? -1 : parentMenu.getItemIndex(menuItem)),//
						new Integer(grandParentMenu == null ? -1 : grandParentMenu.getItemIndex(parentMenuImpl)), //
						new Boolean(menuItem.isSelected()), //
						parentMenu == null ? null : parentMenu.getText(), //
						menuItem.getText() //
						};
					}
					else
					{
						args = null;
					}
					menuHandler.execute(functionDefinition, pluginAccess, Utils.arrayJoin(args, methodArguments));
				}
			}
		};
	}

	/**
	 * Script the selection (emulate a mouse click) of the item.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add a menu item
	 * var entry = menu.addMenuItem("menu entry", feedback);
	 * // alternatively add a checkbox
	 * //var entry = menu.addCheckBox("menu entry", feedback);
	 * // or alternatively add a radiobutton
	 * //var entry = menu.addRadioButton("menu entry", feedback);
	 * 
	 * // simulate a click on the entry
	 * entry.doClick();
	 */
	public void js_doClick() throws PluginException
	{
		try
		{
			menuItem.doClick();
		}
		catch (Exception e)
		{
			throw new PluginException("You tried to click a non clickable item!"); //$NON-NLS-1$
		}
	}

	/**
	 * Get/set the text of the menu item/checkbox/radiobutton.; This can be also html if enclosed between html tags
	 * 
	 * @sampleas js_isEnabled()
	 */
	public String js_getText()
	{
		return menuItem.getText();
	}

	/**
	 * @param text the text of the menu item/checkbox/radiobutton
	 * @param method the feedback method for the menu item/checkbox/radiobutton 
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setMethodArguments(Object[])}
	 */
	@Deprecated
	public void js_set(String text, Function method)
	{
		set(text, method, null, null, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param text the text of the  menu item/checkbox/radiobutton
	 * @param method the feedback method for the  menu item/checkbox/radiobutton
	 * @param arguments the arguments for the method
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setMethodArguments(Object[])}
	 */
	@Deprecated
	public void js_set(String text, Function method, Object[] arguments)
	{
		set(text, method, arguments, null, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param text the text of the menu item/checkbox/radiobutton
	 * @param method the feedback method for the menu item/checkbox/radiobutton
	 * @param arguments the arguments for the method
	 * @param icon the image of the menu item/checkbox/radiobutton
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setMethodArguments(Object[])}, {@link #setIcon(Object)} 
	 */
	@Deprecated
	public void js_set(String text, Function method, Object[] arguments, Object icon)
	{
		set(text, method, arguments, icon, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @param text the text of the menu item/checkbox/radiobutton
	 * @param method the feedback method for the menu item/checkbox/radiobutton
	 * @param arguments the arguments for the method
	 * @param icon the image of the menu item/checkbox/radiobutton
	 * @param accelerator an accelerator (key binding) for the menu item/checkbox/radiobutton
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setMethodArguments(Object[])}, {@link #setIcon(Object)}, {@link #setAccelerator(String)}  
	 */
	@Deprecated
	public void js_set(String text, Function method, Object[] arguments, Object icon, String accelerator)
	{
		set(text, method, arguments, icon, accelerator, "", true, true); //$NON-NLS-1$ 
	}

	/**
	 * @param text the text of the menu item/checkbox/radiobutton
	 * @param method the feedback method for the menu item/checkbox/radiobutton
	 * @param arguments the arguments for the method
	 * @param icon the image of the menu item/checkbox/radiobutton
	 * @param accelerator an accelerator (key binding) for the menu item/checkbox/radiobutton
	 * @param mnemonic the mnemonic of the menu item/checkbox/radiobutton
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setMethodArguments(Object[])}, {@link #setIcon(Object)}, {@link #setAccelerator(String)}, {@link #setMnemonic(String)} 
	 */
	@Deprecated
	public void js_set(String text, Function method, Object[] arguments, Object icon, String accelerator, String mnemonic)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, true, true);
	}

	/**
	 * 
	 * @param text the text of the menu item/checkbox/radiobutton
	 * @param method the feedback method for the menu item/checkbox/radiobutton
	 * @param arguments the arguments for the method
	 * @param icon the image of the menu item/checkbox/radiobutton
	 * @param accelerator an accelerator (key binding) for the menu item/checkbox/radiobutton
	 * @param mnemonic the mnemonic of the menu item/checkbox/radiobutton
	 * @param enabled the enabled state of this menu item/checkbox/radiobutton
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setMethodArguments(Object[])}, {@link #setIcon(Object)}, {@link #setAccelerator(String)}, {@link #setMnemonic(String)}, {@link #setEnabled(boolean)} 
	 */
	@Deprecated
	public void js_set(String text, Function method, Object[] arguments, Object icon, String accelerator, String mnemonic, boolean enabled)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, enabled, true);
	}

	/**
	 * @param text the text of the menu item/checkbox/radiobutton
	 * @param method the feedback method for the menu item/checkbox/radiobutton
	 * @param arguments the arguments for the method
	 * @param icon the image of the menu item/checkbox/radiobutton
	 * @param accelerator an accelerator (key binding) for the menu item/checkbox/radiobutton
	 * @param mnemonic the mnemonic of the menu item/checkbox/radiobutton
	 * @param enabled the enabled state of this menu item/checkbox/radiobutton
	 * @param visible the visibility of the menu item/checkbox/radiobutton
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setMethodArguments(Object[])}, {@link #setIcon(Object)}, {@link #setAccelerator(String)}, {@link #setMnemonic(String)}, {@link #setEnabled(boolean)}, {@link #setVisible(boolean)} 
	 */
	@Deprecated
	public void js_set(String text, Function method, Object[] arguments, Object icon, String accelerator, String mnemonic, boolean enabled, boolean visible)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, enabled, visible);
	}

	/**
	 * Set the method for the menu item/checkbox/radiobutton.
	 *
	 * @sampleas js_isEnabled()
	 * 
	 * @param method
	 */
	public AbstractMenuItem js_setMethod(Function method)
	{
		js_setMethod(method, null);
		return this;
	}

	/**
	 * Set the method for the menu item/checkbox/radiobutton.
	 *
	 * @sampleas js_setMethod(Function)
	 * 
	 * @param method
	 * @param arguments
	 */
	public AbstractMenuItem js_setMethod(Function method, Object[] arguments)
	{
		functionDefinition = new FunctionDefinition(method);
		methodArguments = arguments;
		return this;
	}

	/**
	 * Set the accelerator key of the menu item/checkbox/radiobutton.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add a menu item
	 * var entry = menu.addMenuItem("menu entry", feedback);
	 * // alternatively add a checkbox
	 * //var entry = menu.addCheckBox("menu entry", feedback);
	 * // or alternatively add a radiobutton
	 * //var entry = menu.addRadioButton("menu entry", feedback);
	 * 
	 * // define an accelerator for the menu entry
	 * entry.setAccelerator("ctrl alt Y");
	 * // also define a mnemonic
	 * entry.setMnemonic("y");
	 * // set a custom background color
	 * entry.setBackgroundColor("#111111");
	 * // set a custom foreground color
	 * entry.setForegroundColor("#EE5555");
	 * // set an icon
	 * entry.setIcon("media:///yourimage.gif");
	 */
	public AbstractMenuItem js_setAccelerator(String accelerator)
	{
		KeyStroke key = WindowProvider.parseShortcut(pluginAccess, accelerator);
		menuItem.setAccelerator(key);
		return this;
	}

	/**
	 * @param arguments the method arguments for the feedback method of this menu item/checkbox/radiobutton.
	 * 
	 * @deprecated Replaced by {@link #setMethodArguments(Object[])}
	 */
	@Deprecated
	public void js_setArguments(Object[] arguments)
	{
		methodArguments = arguments;
	}

	public void js_setEnabled(boolean enabled)
	{
		menuItem.setEnabled(enabled);
	}

	/**
	 * Enable/disable the menu item/checkbox/radiobutton.
	 * 
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add a menu item at the first position in the menu
	 * var entry = menu.addMenuItem(0);
	 * // alternatively add a checkbox at the first position
	 * //var entry = menu.addCheckBox(0);
	 * // or alternatively add a radiobutton at the first position
	 * //var entry = menu.addRadioButton(0);
	 * 
	 * // disable the newly added entry
	 * entry.enabled = false;
	 * // give a name to the entry (the name is not visible anywhere)
	 * entry.name = "my_name";
	 * // make the entry selected (affects checkboxes and radiobuttons)
	 * entry.selected = true;
	 * // set the text of the entry
	 * entry.text = "menu entry";
	 * // set the callback method
	 * entry.setMethod(feedback);
	 * // set the arguments to be sent to the callback method
	 * // (an array of elements which will be passed as arguments 5, 6 and so on to the callback method)
	 * // the first 5 arguments are fixed: 
	 * //	[0] item index
	 * //	[1] parent item index
	 * //	[2] isSelected boolean
	 * //	[3] parent menu text
	 * //	[4] menu text
	 * entry.methodArguments = [17, "data"];
	 */
	public boolean js_isEnabled()
	{
		return menuItem.isEnabled();
	}


	/**
	 * Set the icon of the menu item/checkbox/radiobutton.
	 *
	 * @sampleas js_setAccelerator(String)
	 */
	public AbstractMenuItem js_setIcon(Object icon)
	{
		if (" ".equals(icon) || (align && ((icon == null) || "".equals(icon)))) //$NON-NLS-1$ //$NON-NLS-2$
		{
			menuItem.setIcon(null);
		}
		else if (icon instanceof String)
		{
			menuItem.setIconURL((String)icon);
		}
		else if (icon instanceof byte[])
		{
			byte[] image = (byte[])icon;
			menuItem.setIcon(Utilities.getImageIcon(image));
		}
		return this;
	}

	/**
	 * Set the mnemonic key of the menu item/checkbox/radiobutton.
	 *
	 * @sampleas js_setAccelerator(String)
	 */
	public AbstractMenuItem js_setMnemonic(String mnemonic)
	{
		if ((mnemonic == null) || mnemonic.equals("")) //$NON-NLS-1$ 
		{
			menuItem.setMnemonic((char)KeyEvent.VK_CLEAR);
		}
		else
		{
			menuItem.setMnemonic(mnemonic.charAt(0));
		}
		return this;
	}

	public void js_setText(String text)
	{
		String message = text;
		if (message.startsWith("i18n:")) //$NON-NLS-1$ 
		{
			message = Messages.getString(message.replaceFirst("i18n:", "")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		menuItem.setText(message);
	}

	/**
	 * Set the item visible.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add a menu item
	 * var entry_one = menu.addMenuItem("an entry", feedback);
	 * // add a checkbox
	 * var entry_two = menu.addCheckBox("another entry", feedback);
	 * // add a radiobutton
	 * var entry_three = menu.addRadioButton("yet another entry", feedback);
	 * 
	 * // hide the menu item
	 * entry_one.setVisible(false);
	 * // make sure the checkbox is visible
	 * entry_two.setVisible(true);
	 * // hide the radiobutton
	 * entry_three.setVisible(false);
	 * 
	 * @param visible
	 */
	public AbstractMenuItem js_setVisible(boolean visible)
	{
		menuItem.setVisible(visible);
		return this;
	}

	protected void set(String text, Function method, Object[] arguments, Object icon, String accelerator, String mnemonic, boolean enabled, boolean visible)
	{
		functionDefinition = new FunctionDefinition(method);

		js_setAccelerator(accelerator);

		js_setIcon(icon);
		js_setMnemonic(mnemonic);
		js_setText(text);

		methodArguments = arguments;

		menuItem.setEnabled(enabled);
		menuItem.setVisible(visible);
	}

	/**
	 * Set arguments that are sent to the callback method.
	 *
	 * @sampleas js_isEnabled()
	 */
	public Object[] js_getMethodArguments()
	{
		return methodArguments;
	}

	public void js_setMethodArguments(Object[] arguments)
	{
		methodArguments = arguments;
	}

	/**
	 * Select/unselect the checkbox/radiobutton.
	 * 
	 * @sampleas js_isEnabled()
	 */
	public boolean js_getSelected()
	{
		return menuItem.isSelected();
	}

	public void js_setSelected(boolean selected)
	{
		menuItem.setSelected(selected);
	}

	public void js_setName(String name)
	{
		menuItem.setName(name);
	}

	/**
	 * The name of the menu item/checkbox/radiobutton. The name is used only internally, it is not
	 * visible in the user interface.
	 * 
	 * @sampleas js_isEnabled()
	 */
	public String js_getName()
	{
		return menuItem.getName();
	}

	/**
	 * Set the background color of the menu item/checkbox/radiobutton.
	 *
	 * @sampleas js_setAccelerator(String)
	 */
	public void js_setBackgroundColor(String bgColor)
	{
		menuItem.setBackgroundColor(bgColor);
	}

	/**
	 * Set the foreground color of the menu item/checkbox/radiobutton.
	 *
	 * @sampleas js_setAccelerator(String)
	 */
	public void js_setForegroundColor(String fgColor)
	{
		menuItem.setForegroundColor(fgColor);
	}

	/**
	 * Sets the value for the specified client property key of the menu item/checkbox/radiobutton.
	 *
	 * @sampleas js_getClientProperty(Object)
	 * 
	 * @param key
	 * @param value
	 */
	public void js_putClientProperty(Object key, Object value)
	{
		menuItem.putClientProperty(key, value);
	}

	/**
	 * Gets the specified client property for the menu item/checkbox/radiobutton based on a key.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add a menu item
	 * var entry = menu.addMenuItem("menu entry", feedback);
	 * // alternatively add a checkbox
	 * //var entry = menu.addCheckBox("menu entry", feedback);
	 * // or alternatively add a radiobutton
	 * //var entry = menu.addRadioButton("menu entry", feedback);
	 * 
	 * // NOTE: Depending on the operating system, a user interface property name may be available.
	 * // set the tooltip of the menu item/checkbox/radiobutton via client properties
	 * // keep the original tooltip in a form or global variable
	 * originalTooltip = entry.getClientProperty("ToolTipText");
	 * entry.putClientProperty("ToolTipText", "changed tooltip");
	 * 
	 * // later restore the original tooltip from the variable
	 * //var menubar = plugins.window.getMenuBar();
	 * //var menuIndex = menubar.getMenuIndexByText("New Menu");
	 * //var menu = menubar.getMenu(menuIndex);
	 * //var entry = menu.getItem(0);
	 * //entry.putClientProperty("ToolTipText", originalTooltip);
	 * 
	 * @param key
	 */
	public Object js_getClientProperty(Object key)
	{
		return menuItem.getClientProperty(key);
	}

	public static AbstractMenuItem createmenuItem(IClientPluginAccess pluginAccess, IMenuHandler menuHandler, IMenuItem menuItem, MenuItemArgs menuItemArgs,
		boolean legacyMenubarArguments)
	{
		if (menuItem == null)
		{
			return null;
		}

		if (menuItemArgs != null)
		{
			// apply arguments
			menuItem.setText(menuItemArgs.name);
			if (menuItemArgs.mnemonic != 0)
			{
				menuItem.setMnemonic(menuItemArgs.mnemonic);
			}
			if (menuItemArgs.imageURL != null)
			{
				menuItem.setIconURL(menuItemArgs.imageURL);
			}
			else if (menuItemArgs.imageBytes != null)
			{
				menuItem.setIcon(Utilities.getImageIcon(menuItemArgs.imageBytes));
			}
			if (menuItemArgs.align != -1)
			{
				menuItem.setHorizontalAlignment(menuItemArgs.align);
			}
			menuItem.setEnabled(menuItemArgs.enabled);
		}


		AbstractMenuItem abstractMenuItem;
		if (menuItem instanceof ICheckboxMenuItem)
		{
			abstractMenuItem = new CheckBox(pluginAccess, menuHandler, (ICheckboxMenuItem)menuItem);
		}
		else if (menuItem instanceof IRadioButtonMenuItem)
		{
			abstractMenuItem = new RadioButton(pluginAccess, menuHandler, (IRadioButtonMenuItem)menuItem);
		}
		else
		{
			abstractMenuItem = new MenuItem(pluginAccess, menuHandler, menuItem);
		}

		if (menuItemArgs != null && menuItemArgs.method != null)
		{
			abstractMenuItem.setFunctionDefinition(new FunctionDefinition(menuItemArgs.method));
		}

		abstractMenuItem.setLegacyMenubarArguments(legacyMenubarArguments);
		return abstractMenuItem;
	}

}
