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

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * Interface for client type independent (smart/web) menu items.
 * 
 */
public interface IMenuItem
{
	static final int MENU_ITEM_BUTTON = 0;
	static final int MENU_ITEM_RADIO = 1;
	static final int MENU_ITEM_CHECK = 2;

	//http://developer.yahoo.com/yui/menu/
	//http://yuiblog.com/assets/pdf/cheatsheets/menu.pdf
	public IMenu getParentMenu();

	public void setText(String name);

	public String getText();

	public void setMnemonic(char mnemonic);

	public void setIconURL(String iconURL);

	public void setIcon(Icon image);

	public void setHorizontalAlignment(int align);

	public void setEnabled(boolean enabled);

	public boolean isEnabled();

	public void setVisible(boolean visible);

	public void addActionListener(ActionListener actionListener);

	public ActionListener[] getActionListeners();

	public void setSelected(boolean selected);

	public boolean isSelected();

	public void doClick();

	public void setAccelerator(KeyStroke key);

	public void setName(String name);

	public String getName();

	public Object getMenuComponent();

	public void setScriptObjectWrapper(AbstractMenuItem abstractMenuItem);

	public AbstractMenuItem getScriptObjectWrapper();
}
