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

import org.mozilla.javascript.Function;

import com.servoy.extensions.plugins.window.util.Utilities;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.IClientPluginAccess;

/**
 * Checkbox button scriptable.
 * 
 */
@ServoyDocumented
public class CheckBox extends AbstractMenuItem
{

	public CheckBox()
	{
		// only used by script engine
	}

	public CheckBox(IClientPluginAccess pluginAccess, IMenuHandler menuHandler, ICheckboxMenuItem menuItem)
	{
		super(pluginAccess, menuHandler, menuItem);
	}

	/**
	 * Sets the alignment of the checkbox.
	 *
	 * @sample
	 * // add a new menu to the menubar
	 * var menubar = plugins.window.getMenuBar();
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * // alternatively create a popup menu
	 * //var menu = plugins.window.createPopupMenu();
	 * 
	 * // add a checkbox
	 * var entry = menu.addCheckBox("menu entry", feedback);
	 * // alternatively add a radiobutton
	 * //var entry = menu.addRadioButton("menu entry", feedback);
	 * 
	 * // enable alignment of the new entry
	 * entry.setAlign(true);	
	 * 
	 * @param align
	 */
	public CheckBox js_setAlign(boolean align)
	{
		this.align = align;
		return this;
	}

	@Override
	public CheckBox js_setIcon(Object icon)
	{
		if ((System.getProperty("mrj.version") == null) && (" ".equals(icon) || (align && ((icon == null) || "".equals(icon))))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			menuItem.setIcon(Utilities.getImageIcon("images/empty.gif")); //$NON-NLS-1$
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

	@Override
	public CheckBox js_setAccelerator(String accelerator)
	{
		super.js_setAccelerator(accelerator);
		return this;
	}

	@Override
	public CheckBox js_setMethod(Function method)
	{
		super.js_setMethod(method);
		return this;
	}

	@Override
	public CheckBox js_setMethod(Function method, Object[] arguments)
	{
		super.js_setMethod(method, arguments);
		return this;
	}

	@Override
	public CheckBox js_setMnemonic(String mnemonic)
	{
		super.js_setMnemonic(mnemonic);
		return this;
	}

	@Override
	public CheckBox js_setVisible(boolean visible)
	{
		super.js_setVisible(visible);
		return this;
	}


	/**
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)} and {@link #setSelected(boolean)}.
	 */
	@Override
	@Deprecated
	public void js_set(String text, Function method)
	{
		set(text, method, null, null, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
		js_setSelected(false);
	}

	/**
	 * 
	 * @param text the text of the checkbox
	 * @param method feedback method
	 * @param selected true for marking as selected, false otherwise
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setSelected(boolean)}
	 */
	@Deprecated
	public void js_set(String text, Function method, boolean selected)
	{
		set(text, method, null, null, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
		js_setSelected(selected);
	}

	/**
	 * 
	 * @param text the text of the checkbox
	 * @param method feedback method
	 * @param selected true for marking as selected, false otherwise
	 * @param arguments the arguments to the feedback method
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setSelected(boolean)}
	 */
	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments)
	{
		set(text, method, arguments, null, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
		js_setSelected(selected);
	}

	/**
	 * 
	 * @param text the text of the checkbox
	 * @param method feedback method
	 * @param selected true for marking as selected, false otherwise
	 * @param arguments the arguments to the feedback method
	 * @param icon the icon for the checkbox
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setSelected(boolean)}, {@link #setIcon(Object)}
	 */
	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments, Object icon)
	{
		set(text, method, arguments, icon, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
		js_setSelected(selected);
	}

	/**
	 * 
	 * @param text the text of the checkbox
	 * @param method feedback method
	 * @param selected true for marking as selected, false otherwise
	 * @param arguments the arguments to the feedback method
	 * @param icon the icon for the checkbox
	 * @param accelerator the accelerator key binding
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setSelected(boolean)}, {@link #setIcon(Object)}, {@link #setAccelerator(String)}
	 */
	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments, Object icon, String accelerator)
	{
		set(text, method, arguments, icon, accelerator, "", true, true); //$NON-NLS-1$ 
		js_setSelected(selected);
	}

	/**
	 * 
	 * @param text the text of the checkbox
	 * @param method feedback method
	 * @param selected true for marking as selected, false otherwise
	 * @param arguments the arguments to the feedback method
	 * @param icon the icon for the checkbox
	 * @param accelerator the accelerator key binding
	 * @param mnemonic the mnemonic
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setSelected(boolean)}, {@link #setIcon(Object)}, {@link #setAccelerator(String)}, {@link #setMnemonic(Function)}
	 */
	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments, Object icon, String accelerator, String mnemonic)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, true, true);
		js_setSelected(selected);
	}

	/**
	 * 
	 * @param text the text of the checkbox
	 * @param method feedback method
	 * @param selected true for marking as selected, false otherwise
	 * @param arguments the arguments to the feedback method
	 * @param icon the icon for the checkbox
	 * @param accelerator the accelerator key binding
	 * @param mnemonic the mnemonic
	 * @param enabled true for enabled, false otherwise
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setSelected(boolean)}, {@link #setIcon(Object)}, {@link #setAccelerator(String)}, {@link #setMnemonic(Function)}, {@link #setEnabled(boolean)}
	 */
	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments, Object icon, String accelerator, String mnemonic, boolean enabled)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, enabled, true);
		js_setSelected(selected);
	}

	/**
	 * 
	 * @param text the text of the checkbox
	 * @param method feedback method
	 * @param selected true for marking as selected, false otherwise
	 * @param arguments the arguments to the feedback method
	 * @param icon the icon for the checkbox
	 * @param accelerator the accelerator key binding
	 * @param mnemonic the mnemonic
	 * @param enabled true for enabled, false otherwise
	 * @param visible true if this item is visible, false otherwise
	 * 
	 * @deprecated Replaced by {@link #setText(String)}, {@link #setMethod(Function)}, {@link #setMethod(Function,Object[])}, {@link #setSelected(boolean)}, {@link #setIcon(Object)}, {@link #setAccelerator(String)}, {@link #setMnemonic(Function)}, {@link #setEnabled(boolean)}, {@link #setVisible(boolean)}
	 */
	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments, Object icon, String accelerator, String mnemonic, boolean enabled,
		boolean visible)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, enabled, visible);
		js_setSelected(selected);
	}

}
