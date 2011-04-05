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
import com.servoy.j2db.plugins.IClientPluginAccess;

/**
 * Radio button scriptable.
 * 
 */

public class RadioButton extends AbstractMenuItem
{
	public RadioButton()
	{
		// only used by script engine
	}

	public RadioButton(IClientPluginAccess pluginAccess, IMenuHandler menuHandler, IRadioButtonMenuItem menuItem)
	{
		super(pluginAccess, menuHandler, menuItem);
	}

	@Override
	public String js_getText()
	{
		return menuItem.getName();
	}

	@Override
	@Deprecated
	public void js_set(String text, Function method)
	{
		set(text, method, null, null, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
		js_setSelected(false);
	}

	@Deprecated
	public void js_set(String text, Function method, boolean selected)
	{
		set(text, method, null, null, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
		js_setSelected(selected);
	}

	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments)
	{
		set(text, method, arguments, null, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
		js_setSelected(selected);
	}

	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments, Object icon)
	{
		set(text, method, arguments, icon, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
		js_setSelected(selected);
	}

	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments, Object icon, String accelerator)
	{
		set(text, method, arguments, icon, accelerator, "", true, true); //$NON-NLS-1$ 
		js_setSelected(selected);
	}

	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments, Object icon, String accelerator, String mnemonic)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, true, true);
		js_setSelected(selected);
	}

	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments, Object icon, String accelerator, String mnemonic, boolean enabled)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, enabled, true);
		js_setSelected(selected);
	}

	@Deprecated
	public void js_set(String text, Function method, boolean selected, Object[] arguments, Object icon, String accelerator, String mnemonic, boolean enabled,
		boolean visible)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, enabled, visible);
		js_setSelected(selected);
	}

	public void js_setAlign(boolean a)
	{
		this.align = a;
	}

	@Override
	public RadioButton js_setIcon(Object icon)
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
	public RadioButton js_setAccelerator(String accelerator)
	{
		super.js_setAccelerator(accelerator);
		return this;
	}

	@Override
	public RadioButton js_setMethod(Function method)
	{
		super.js_setMethod(method);
		return this;
	}

	@Override
	public RadioButton js_setMethod(Function method, Object[] arguments)
	{
		super.js_setMethod(method, arguments);
		return this;
	}

	@Override
	public RadioButton js_setMnemonic(String mnemonic)
	{
		super.js_setMnemonic(mnemonic);
		return this;
	}

	@Override
	public RadioButton js_setVisible(boolean visible)
	{
		super.js_setVisible(visible);
		return this;
	}

	@Override
	public String[] getParameterNames(String methodName)
	{
		if ("setAlign".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "boolean" }; //$NON-NLS-1$ 
		}
		return super.getParameterNames(methodName);
	}

	@Override
	public String getSample(String methodName)
	{
		if ("setAlign".equals(methodName)) //$NON-NLS-1$ 
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$ 
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$ 
			retval.append("var align = radioButton.setAlign(true);\n"); //$NON-NLS-1$ 
			return retval.toString();
		}
		return super.getSample(methodName);
	}

	@Override
	public String getToolTip(String methodName)
	{
		if ("setAlign".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Sets the alignment of the radio button."; //$NON-NLS-1$ 
		}
		return super.getToolTip(methodName);
	}
}
