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

import com.servoy.extensions.plugins.window.WindowProvider;
import com.servoy.extensions.plugins.window.menu.AbstractMenu.MenuItemArgs;
import com.servoy.extensions.plugins.window.util.Utilities;
import com.servoy.j2db.Messages;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Utils;

/**
 * Base class for Menu items. Wraps IMenuItem client-specific menu item implementation, is exposed in scripting.
 * 
 * @author rgansevles
 *
 */
public abstract class AbstractMenuItem implements IScriptObject
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

	public String js_getText()
	{
		return menuItem.getText();
	}

	@Deprecated
	public void js_set(String text, Function method)
	{
		set(text, method, null, null, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Deprecated
	public void js_set(String text, Function method, Object[] arguments)
	{
		set(text, method, arguments, null, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Deprecated
	public void js_set(String text, Function method, Object[] arguments, Object icon)
	{
		set(text, method, arguments, icon, "", "", true, true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Deprecated
	public void js_set(String text, Function method, Object[] arguments, Object icon, String accelerator)
	{
		set(text, method, arguments, icon, accelerator, "", true, true); //$NON-NLS-1$ 
	}

	@Deprecated
	public void js_set(String text, Function method, Object[] arguments, Object icon, String accelerator, String mnemonic)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, true, true);
	}

	@Deprecated
	public void js_set(String text, Function method, Object[] arguments, Object icon, String accelerator, String mnemonic, boolean enabled)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, enabled, true);
	}

	@Deprecated
	public void js_set(String text, Function method, Object[] arguments, Object icon, String accelerator, String mnemonic, boolean enabled, boolean visible)
	{
		set(text, method, arguments, icon, accelerator, mnemonic, enabled, visible);
	}

	public AbstractMenuItem js_setMethod(Function method)
	{
		js_setMethod(method, null);
		return this;
	}

	public AbstractMenuItem js_setMethod(Function method, Object[] arguments)
	{
		functionDefinition = new FunctionDefinition(method);
		methodArguments = arguments;
		return this;
	}

	public AbstractMenuItem js_setAccelerator(String accelerator)
	{
		KeyStroke key = WindowProvider.parseShortcut(pluginAccess, accelerator);
		menuItem.setAccelerator(key);
		return this;
	}

	@Deprecated
	public void js_setArguments(Object[] arguments)
	{
		methodArguments = arguments;
	}

	public void js_setEnabled(boolean enabled)
	{
		menuItem.setEnabled(enabled);
	}

	public boolean js_isEnabled()
	{
		return menuItem.isEnabled();
	}


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

	public Object[] js_getMethodArguments()
	{
		return methodArguments;
	}

	public void js_setMethodArguments(Object[] arguments)
	{
		methodArguments = arguments;
	}

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

	public String js_getName()
	{
		return menuItem.getName();
	}

	public void js_setBackgroundColor(String bgColor)
	{
		menuItem.setBackgroundColor(bgColor);
	}

	public void js_setForegroundColor(String fgColor)
	{
		menuItem.setForegroundColor(fgColor);
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

	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}

	public String[] getParameterNames(String methodName)
	{
		if ("setAccelerator".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "key" }; //$NON-NLS-1$ 
		}
		if ("setEnabled".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "enabled" }; //$NON-NLS-1$ 
		}
		if ("setIcon".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "icon" }; //$NON-NLS-1$ 
		}
		if ("setMethod".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "method" }; //$NON-NLS-1$ 
		}
		if ("setMnemonic".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "key" }; //$NON-NLS-1$ 
		}
		if ("setText".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "text" }; //$NON-NLS-1$ 
		}
		if ("setVisible".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "visible" }; //$NON-NLS-1$ 
		}
		if ("setMethod".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "method", "[methodArguments]" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		if ("setBackgroundColor".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "backgroundColor" }; //$NON-NLS-1$
		}
		if ("setForegroundColor".equals(methodName)) //$NON-NLS-1$ 
		{
			return new String[] { "foregroundColor" }; //$NON-NLS-1$
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("setAccelerator".equals(methodName) || "setMethod".equals(methodName) || "methodArguments".equals(methodName) || "setEnabled".equals(methodName) || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"setIcon".equals(methodName) || "setMethod".equals(methodName) || "setMnemonic".equals(methodName) || "setText".equals(methodName) || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"setVisible".equals(methodName) || "setBackgroundColor".equals(methodName) || "setForegroundColor".equals(methodName)) //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
		{
			StringBuilder sample = new StringBuilder();
			sample.append("var menu = plugins.window.getMenu(2).getItem(0);\n// "); //$NON-NLS-1$ 
			sample.append(getToolTip("setText")); //$NON-NLS-1$ 
			sample.append("\nmenu.setText(\"Servoy\");\n// "); //$NON-NLS-1$ 
			sample.append(getToolTip("setMethod")); //$NON-NLS-1$ 
			sample.append("\nmenu.setMethod(callback);\n// "); //$NON-NLS-1$ 
			sample.append(getToolTip("methodArguments")).append(" - array elements will be passed as arguments 5, 6 and so on to the callback method\n"); //$NON-NLS-1$ //$NON-NLS-2$
			sample.append("menu.methodArguments = [\"a\",\"b\"];\n// "); //$NON-NLS-1$ 
			sample.append(getToolTip("setIcon")); //$NON-NLS-1$ 
			sample.append("\nmenu.setIcon(\"media:///TipOfTheDay16.gif\");\n// "); //$NON-NLS-1$ 
			sample.append(getToolTip("setAccelerator")); //$NON-NLS-1$ 
			sample.append("\nmenu.setAccelerator(\"meta 4\");\n// "); //$NON-NLS-1$ 
			sample.append(getToolTip("setMnemonic")); //$NON-NLS-1$ 
			sample.append("\nmenu.setMnemonic(\"e\");\n// "); //$NON-NLS-1$ 
			sample.append(getToolTip("setEnabled")); //$NON-NLS-1$ 
			sample.append("\nmenu.setEnabled(false);\n// "); //$NON-NLS-1$ 
			sample.append(getToolTip("setVisible")); //$NON-NLS-1$ 
			sample.append("\nmenu.setVisible(true);\n"); //$NON-NLS-1$ 
			sample.append("\nmenu.setBackgroundColor('#ff0000');\n"); //$NON-NLS-1$
			sample.append("\nmenu.setForegroundColor('#0000ff');\n"); //$NON-NLS-1$
			return sample.toString();
		}
		else if ("doClick".equals(methodName)) //$NON-NLS-1$ 
		{
			StringBuilder sample = new StringBuilder("// "); //$NON-NLS-1$
			sample.append(getToolTip(methodName));
			sample.append("\n// Clicking a separator will throw an error!\n"); //$NON-NLS-1$ 
			sample.append("plugins.window.getMenu(2).getItem(0).doClick();\n"); //$NON-NLS-1$ 
			return sample.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("doClick".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Script the selection (emulate a mouse click) of the item."; //$NON-NLS-1$ 
		}
		if ("setAccelerator".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Set the accelerator key of the item."; //$NON-NLS-1$ 
		}
		if ("methodArguments".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Set the arguments that can be read by the defined method."; //$NON-NLS-1$ 
		}
		if ("setEnabled".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Enable/disable the item."; //$NON-NLS-1$ 
		}
		if ("setIcon".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Set the icon of the item."; //$NON-NLS-1$ 
		}
		if ("setMethod".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Set the method for the item."; //$NON-NLS-1$ 
		}
		if ("setMnemonic".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Set the mnemonic key of the item."; //$NON-NLS-1$ 
		}
		if ("setText".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Set the text of the item."; //$NON-NLS-1$ 
		}
		if ("setVisible".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Set the item visible."; //$NON-NLS-1$ 
		}
		if ("getText".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Retrieve the text."; //$NON-NLS-1$ 
		}
		if ("setBackgroundColor".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Set the background color of the item."; //$NON-NLS-1$ 
		}
		if ("setForegroundColor".equals(methodName)) //$NON-NLS-1$ 
		{
			return "Set the foreground color of the item."; //$NON-NLS-1$ 
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		if ("setArguments".equals(methodName)) //$NON-NLS-1$ 
		{
			return true;
		}
		if ("set".equals(methodName)) //$NON-NLS-1$
		{
			return true;
		}
		return false;
	}
}
