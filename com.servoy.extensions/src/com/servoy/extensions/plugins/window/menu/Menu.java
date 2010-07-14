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
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;

/**
 * Menu scriptable.
 * 
 */
public class Menu extends AbstractMenu
{
	public Menu()
	{ // used for scripting
	}

	public Menu(IClientPluginAccess pluginAccess, IMenuHandler menuHandler, IMenu menu)
	{
		super(pluginAccess, menuHandler, menu);
	}

	public void js_doClick()
	{
		getMenu().doClick();
	}

	public String js_getText()
	{
		return getMenu().getText();
	}

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
			throw new PluginException("Unexpected parameter type: " + obj.getClass()); //$NON-NLS-1$
		}
	}

	@Deprecated
	public void js_set(String text, boolean enabled) throws PluginException
	{
		js_setText(text);
		js_setEnabled(enabled);
	}

	@Deprecated
	public void js_set(String text, String mnemonic) throws PluginException
	{
		js_setText(text);
		js_setMnemonic(mnemonic);
	}

	@Deprecated
	public void js_set(String text, String mnemonic, boolean enabled) throws PluginException
	{
		js_setText(text);
		js_setMnemonic(mnemonic);
		getMenu().setEnabled(enabled);
	}

	public void js_setEnabled(boolean enabled)
	{
		getMenu().setEnabled(enabled);
	}

	public void js_setIcon(Object icon)
	{
		if (" ".equals(icon)/* || (_align && ((icon == null) || "".equals(icon))) */) //$NON-NLS-1$
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

	public void js_setMnemonic(String mnemonic)
	{
		if ((mnemonic == null) || mnemonic.equals("")) //$NON-NLS-1$
		{
			getMenu().setMnemonic((char)KeyEvent.VK_CLEAR);
		}
		else
		{
			if (mnemonic.startsWith("i18n:")) //$NON-NLS-1$
			{
				mnemonic = Messages.getString(mnemonic.replaceFirst("i18n:", "")); //$NON-NLS-1$ //$NON-NLS-2$ 
			}
			getMenu().setMnemonic(mnemonic.charAt(0));
		}
	}

	public void js_setText(String text) throws PluginException
	{
		if ((text == null) || (text == "")) //$NON-NLS-1$
		{
			throw new PluginException("A new _menu text can not be empty!"); //$NON-NLS-1$
		}

		if (text.startsWith("i18n:")) //$NON-NLS-1$
		{
			text = Messages.getString(text.replaceFirst("i18n:", "")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		getMenu().setText(text);
	}

	@SuppressWarnings("nls")
	@Override
	public boolean isDeprecated(String methodName)
	{
		if ("set".equals(methodName))
		{
			return true;
		}
		return super.isDeprecated(methodName);
	}

	@Override
	public String[] getParameterNames(String methodName)
	{
		if ("doClick".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "click" }; //$NON-NLS-1$ 
		}
		if ("setEnabled".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "enabled" }; //$NON-NLS-1$ 
		}
		if ("setIcon".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "icon" }; //$NON-NLS-1$ 
		}
		if ("setMnemonic".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "mnemonic" }; //$NON-NLS-1$ 
		}
		if ("setText".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "text" }; //$NON-NLS-1$ 
		}


		return super.getParameterNames(methodName);
	}

	@Override
	public String getSample(String methodName)
	{
		StringBuilder sample = new StringBuilder();
		if ("doClick".equals(methodName)) //$NON-NLS-1$ 
		{
			sample.append("// " + getToolTip(methodName) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ 
			sample.append("plugins.window.getMenu(0).doClick();\n"); //$NON-NLS-1$ 
		}
		else if ("setText".equals(methodName) || "setMnemonic".equals(methodName) || "setEnabled".equals(methodName)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			sample.append("// " + getToolTip(methodName) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ 
			sample.append("var menu = plugins.window.getMenu(0);\n"); //$NON-NLS-1$ 
			sample.append("menu.setText(\"Hello\");\n"); //$NON-NLS-1$ 
			sample.append("menu.setMnemonic(\"H\");\n"); //$NON-NLS-1$ 
			sample.append("menu.setEnabled(false);\n"); //$NON-NLS-1$ 
		}
		else
		{
			return super.getSample(methodName);
		}

		return sample.toString();
	}

	@SuppressWarnings("nls")
	@Override
	public String getToolTip(String methodName)
	{
		if ("doClick".equals(methodName))
		{
			return "Script the selection (emulate a mouse click) of the menu.";
		}
		if ("getText".equals(methodName))
		{
			return "Retrieve the text.";
		}
		if ("setEnabled".equals(methodName))
		{
			return "Set the the selected menu enabled or disabled.";
		}
		if ("setIcon".equals(methodName))
		{
			return "Set the icon of the menu.";
		}
		if ("setMnemonic".equals(methodName))
		{
			return "Set the mnemonic of the selected menu.";
		}
		if ("setText".equals(methodName))
		{
			return "Set the text of the selected menu.";
		}

		return super.getToolTip(methodName);
	}
}
