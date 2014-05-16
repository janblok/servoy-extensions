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

import java.awt.event.KeyEvent;

import com.servoy.extensions.plugins.window.util.Utilities;
import com.servoy.j2db.Messages;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;

/**
 * Menu scriptable.
 * 
 */
@ServoyDocumented
public class Menu extends AbstractMenu
{
	public Menu()
	{ // used for scripting
	}

	public Menu(IClientPluginAccess pluginAccess, IMenuHandler menuHandler, IMenu menu)
	{
		super(pluginAccess, menuHandler, menu);
	}

	/**
	 * Script the selection (emulate a mouse click) of the menu.
	 *
	 * @sample
	 * // retrieve the File menu
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.getMenu(0);
	 * // simulate a click on the File menu
	 * menu.doClick();
	 */
	public void js_doClick()
	{
		getMenu().doClick();
	}

	/**
	 * Retrieve/set the text.
	 * 
	 * @sampleas js_setEnabled(boolean)
	 */
	public String js_getText()
	{
		return getMenu().getText();
	}

	/**
	 * 
	 * @param obj the text of the menu or a menu object
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setEnabled(boolean)} and {@link #setMnemonic(String)}.
	 */
	@Deprecated
	public void js_set(Object obj) throws PluginException
	{
		if (obj instanceof String)
		{
			js_setText(((String)obj));
		}
		else if (obj instanceof Menu)
		{
			setMenu(((Menu)obj).getMenu());
		}
		else
		{
			throw new PluginException("Unexpected parameter type: " + obj.getClass());
		}
	}

	/**
	 * @param text the text of the menu
	 * @param enabled true for enabled, false otherwise
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setEnabled(boolean)}
	 */
	@Deprecated
	public void js_set(String text, boolean enabled) throws PluginException
	{
		js_setText(text);
		js_setEnabled(enabled);
	}

	/**
	 * 
	 * @param text the text of the menu
	 * @param mnemonic the mnemonic for this menu
	 * 
	 * @deprecated Replaced by {@link #setText(String)} and {@link #setMnemonic(String)}.
	 */
	@Deprecated
	public void js_set(String text, String mnemonic) throws PluginException
	{
		js_setText(text);
		js_setMnemonic(mnemonic);
	}

	/**
	 * 
	 * @param text the text of the menu
	 * @param mnemonic the mnemonic for this menu
	 * @param enabled true for enabled, false otherwise
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setEnabled(boolean)} and {@link #setMnemonic(String)}.
	 */
	@Deprecated
	public void js_set(String text, String mnemonic, boolean enabled) throws PluginException
	{
		js_setText(text);
		js_setMnemonic(mnemonic);
		getMenu().setEnabled(enabled);
	}

	/**
	 * Set the the selected menu enabled or disabled.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * // set the menu's text
	 * menu.text = "New Menu";
	 * // disable the menu
	 * menu.setEnabled(false);
	 * // set a mnemonic
	 * menu.setMnemonic("u");
	 * // add an icon to the menu
	 * menu.setIcon("media:///yourimage.gif");
	 */
	public void js_setEnabled(boolean enabled)
	{
		getMenu().setEnabled(enabled);
	}

	/**
	 * Set the icon of the menu.
	 *
	 * @sampleas js_setEnabled(boolean)
	 */
	public void js_setIcon(Object icon)
	{
		if (" ".equals(icon)/* || (_align && ((icon == null) || "".equals(icon))) */)
		{
			getMenu().setIcon(null);
		}
		else if (icon instanceof String)
		{
			getMenu().setIconURL((String)icon);
		}
		else if (icon instanceof byte[])
		{
			getMenu().setIcon(Utilities.getImageIcon(icon));
		}
	}

	/**
	 * Set the mnemonic of the selected menu.
	 *
	 * @sampleas js_setEnabled(boolean)
	 */
	public void js_setMnemonic(String mnemonic)
	{
		if ((mnemonic == null) || mnemonic.equals(""))
		{
			getMenu().setMnemonic((char)KeyEvent.VK_CLEAR);
		}
		else
		{
			if (mnemonic.startsWith("i18n:"))
			{
				mnemonic = Messages.getString(mnemonic.replaceFirst("i18n:", ""));
			}
			getMenu().setMnemonic(mnemonic.charAt(0));
		}
	}

	public void js_setText(String text) throws PluginException
	{
		if ((text == null) || (text == ""))
		{
			throw new PluginException("A new _menu text can not be empty!");
		}

		if (text.startsWith("i18n:"))
		{
			text = Messages.getString(text.replaceFirst("i18n:", ""));
		}

		getMenu().setText(text);
	}

}
