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
package com.servoy.extensions.plugins.window.menu.swing;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.servoy.extensions.plugins.window.menu.AbstractMenuItem;
import com.servoy.extensions.plugins.window.menu.IMenu;
import com.servoy.extensions.plugins.window.menu.IMenuItem;
import com.servoy.extensions.plugins.window.util.Utilities;

/**
 * Menu item in smart client.
 * 
 * @author jblok
 */

public class ScriptableJMenuItem implements IMenuItem
{
	private final JMenuItem jMenuItem;
	private AbstractMenuItem scriptObjectWrapper;

	public ScriptableJMenuItem()
	{
		this(new JMenuItem());
	}

	public ScriptableJMenuItem(JMenuItem jMenuItem)
	{
		this.jMenuItem = jMenuItem;
	}

	public JMenuItem getMenuComponent()
	{
		return jMenuItem;
	}

	public boolean isSelected()
	{
		return false;
	}

	public void setSelected(boolean selected)
	{
		//nop
	}

	public IMenu getParentMenu()
	{
		return ScriptableJMenu.getParentMenu(jMenuItem);
	}

	public void addActionListener(ActionListener l)
	{
		jMenuItem.addActionListener(l);
	}

	public void doClick()
	{
		jMenuItem.doClick();
	}

	public ActionListener[] getActionListeners()
	{
		return jMenuItem.getActionListeners();
	}

	public String getName()
	{
		return jMenuItem.getName();
	}

	public String getText()
	{
		return jMenuItem.getText();
	}

	public boolean isEnabled()
	{
		return jMenuItem.isEnabled();
	}

	public void setAccelerator(KeyStroke keyStroke)
	{
		jMenuItem.setAccelerator(keyStroke);
	}

	public void setEnabled(boolean b)
	{
		jMenuItem.setEnabled(b);
	}

	public void setHorizontalAlignment(int alignment)
	{
		jMenuItem.setHorizontalAlignment(alignment);
	}

	public void setHorizontalTextPosition(int textPosition)
	{
		jMenuItem.setHorizontalTextPosition(textPosition);
	}

	public void setIcon(Icon defaultIcon)
	{
		jMenuItem.setIcon(defaultIcon);
	}

	public void setMnemonic(char mnemonic)
	{
		jMenuItem.setMnemonic(mnemonic);
	}

	public void setName(String name)
	{
		jMenuItem.setName(name);
	}

	public void setText(String text)
	{
		jMenuItem.setText(text);
	}

	public void setVisible(boolean aFlag)
	{
		jMenuItem.setVisible(aFlag);
	}

	public boolean isVisible()
	{
		return jMenuItem.isVisible();
	}

	public void setIconURL(String iconURL)
	{
		setIcon(Utilities.getImageIcon(iconURL));
	}

	public AbstractMenuItem getScriptObjectWrapper()
	{
		return scriptObjectWrapper;
	}

	public void setScriptObjectWrapper(AbstractMenuItem abstractMenuItem)
	{
		this.scriptObjectWrapper = abstractMenuItem;
	}

	public void setBackgroundColor(String bgColor)
	{
		jMenuItem.setBackground(Utilities.createColor(bgColor));
	}

	public void setForegroundColor(String fgColor)
	{
		jMenuItem.setForeground(Utilities.createColor(fgColor));
	}

	public void putClientProperty(Object key, Object value)
	{
		jMenuItem.putClientProperty(key, value);
	}

	public Object getClientProperty(Object key)
	{
		return jMenuItem.getClientProperty(key);
	}
}
