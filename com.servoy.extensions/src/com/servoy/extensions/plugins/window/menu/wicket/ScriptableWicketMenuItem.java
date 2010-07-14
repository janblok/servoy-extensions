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
package com.servoy.extensions.plugins.window.menu.wicket;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import com.servoy.extensions.plugins.window.menu.AbstractMenuItem;
import com.servoy.extensions.plugins.window.menu.IMenu;
import com.servoy.extensions.plugins.window.menu.IMenuItem;

/**
 * Menu item in webclient.
 * 
 * @author jblok
 */

public class ScriptableWicketMenuItem implements IMenuItem
{
	private final IMenu parentMenu;

	private boolean selected;
	private boolean enabled;

	private AbstractMenuItem scriptObjectWrapper;

	public ScriptableWicketMenuItem(IMenu parentMenu)
	{
		this.parentMenu = parentMenu;
		this.enabled = true;
	}

	public Object getMenuComponent()
	{
		return null;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public IMenu getParentMenu()
	{
		return parentMenu;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setName(String name)
	{
	}

	public String getName()
	{
		return null;
	}

	public void setVisible(boolean visible)
	{
	}

	public void setHorizontalAlignment(int align)
	{
	}

	private String iconURL;

	public void setIconURL(String iconURL)
	{
		this.iconURL = iconURL;
	}

	public String getIconURL()
	{
		return this.iconURL;
	}

	public void setIcon(Icon image)
	{
	}

	public void setMnemonic(char mnemonic)
	{
	}

	public void setAccelerator(KeyStroke key)
	{
	}

	private String text;

	public void setText(String t)
	{
		if (t != null && t.length() < 1) text = "&nbsp"; //$NON-NLS-1$
		else text = t;
	}

	public String getText()
	{
		return text;
	}

	private ActionListener listener;//allow only one

	public void addActionListener(ActionListener actionListener)
	{
		listener = actionListener;
	}

	public ActionListener[] getActionListeners()
	{
		return listener == null ? new ActionListener[0] : new ActionListener[] { listener };
	}

	public void doClick()
	{
	}

	public AbstractMenuItem getScriptObjectWrapper()
	{
		return scriptObjectWrapper;
	}

	public void setScriptObjectWrapper(AbstractMenuItem abstractMenuItem)
	{
		this.scriptObjectWrapper = abstractMenuItem;
	}
}
