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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import com.servoy.extensions.plugins.window.menu.AbstractMenuItem;
import com.servoy.extensions.plugins.window.menu.IMenu;
import com.servoy.extensions.plugins.window.menu.IMenuItem;
import com.servoy.extensions.plugins.window.util.Utilities;

/**
 * Menu in smart client.
 * 
 * @author jblok
 */

public class ScriptableJMenu implements IMenu
{
	final private JComponent jmenu; // Either JMenu or JPopupMenu
	private AbstractMenuItem scriptObjectWrapper;

	public ScriptableJMenu()
	{
		this(new JMenu());
	}

	public ScriptableJMenu(JMenu jmenu)
	{
		this.jmenu = jmenu;
	}

	public ScriptableJMenu(JPopupMenu jpopupmenu)
	{
		this.jmenu = jpopupmenu;
	}

	public JComponent getMenuComponent()
	{
		return jmenu;
	}

	public static IMenu getParentMenu(JComponent component)
	{
		Container parent = component.getParent();
		if (parent instanceof IMenu)
		{
			return (IMenu)parent;
		}
		if (parent instanceof JMenu)
		{
			return new ScriptableJMenu((JMenu)parent);
		}
		if (parent instanceof JPopupMenu)
		{
			return new ScriptableJMenu((JPopupMenu)parent);
		}
		return null;
	}

	public IMenu getParentMenu()
	{
		return getParentMenu(jmenu);
	}

	public void addMenuItem(IMenuItem menuItem, int index)
	{
		int ncomponents;
		if (jmenu instanceof JMenu)
		{
			ncomponents = ((JMenu)jmenu).getMenuComponentCount();
		}
		else if (jmenu instanceof JPopupMenu)
		{
			ncomponents = ((JPopupMenu)jmenu).getComponentCount();
		}
		else
		{
			ncomponents = 0;
		}

		if (index < 0 || index >= ncomponents)
		{
			jmenu.add((JMenuItem)menuItem.getMenuComponent());
		}
		else
		{
			jmenu.add((JMenuItem)menuItem.getMenuComponent(), index);
		}

	}

	public void removeMenuItem(IMenuItem menuItem)
	{
		if (menuItem.getMenuComponent() instanceof JMenuItem)
		{
			jmenu.remove((JMenuItem)menuItem.getMenuComponent());
		}
	}

	public void removeMenuItem(int pos)
	{
		jmenu.remove(pos);
	}

	public void removeAllItems()
	{
		jmenu.removeAll();
	}

	public IMenuItem getMenuItem(int index)
	{
		Component item = null;
		if (jmenu instanceof JMenu)
		{
			item = ((JMenu)jmenu).getMenuComponent(index);
		}
		else if (jmenu instanceof JPopupMenu)
		{
			item = ((JPopupMenu)jmenu).getComponent(index);
		}

		if (item instanceof IMenuItem)
		{
			return (IMenuItem)item;
		}
		if (item instanceof JMenuItem)
		{
			return new ScriptableJMenuItem((JMenuItem)item);
		}
		return null;
	}

	public int getMenuItemCount()
	{
		if (jmenu instanceof JMenu)
		{
			return ((JMenu)jmenu).getMenuComponentCount();
		}
		if (jmenu instanceof JPopupMenu)
		{
			return ((JPopupMenu)jmenu).getComponentCount();
		}
		return 0;
	}

	public void addSeparator(int index)
	{
		if (index < 0)
		{
			if (jmenu instanceof JMenu)
			{
				((JMenu)jmenu).addSeparator();
			}
			else if (jmenu instanceof JPopupMenu)
			{
				((JPopupMenu)jmenu).addSeparator();
			}
		}
		else
		{
			if (jmenu instanceof JMenu)
			{
				((JMenu)jmenu).insertSeparator(index);
			}
			else if (jmenu instanceof JPopupMenu)
			{
				((JPopupMenu)jmenu).add(new JPopupMenu.Separator(), index);
			}
		}
	}

	public void addActionListener(ActionListener l)
	{
		if (jmenu instanceof JMenu)
		{
			((JMenu)jmenu).addActionListener(l);
		}
	}

	public void doClick()
	{
		if (jmenu instanceof JMenu)
		{
			((JMenu)jmenu).doClick();
		}
	}

	public ActionListener[] getActionListeners()
	{
		if (jmenu instanceof JMenu)
		{
			((JMenu)jmenu).getActionListeners();
		}
		return new ActionListener[0];
	}

	public String getName()
	{
		return jmenu.getName();
	}

	public String getText()
	{
		if (jmenu instanceof JMenu)
		{
			return ((JMenu)jmenu).getText();
		}
		if (jmenu instanceof JPopupMenu)
		{
			return ((JPopupMenu)jmenu).getLabel();
		}
		return ""; //$NON-NLS-1$
	}

	public boolean isEnabled()
	{
		return jmenu.isEnabled();
	}

	public boolean isSelected()
	{
		if (jmenu instanceof JMenu)
		{
			return ((JMenu)jmenu).isSelected();
		}
		return false;
	}

	public void setAccelerator(KeyStroke keyStroke)
	{
		if (jmenu instanceof JMenu)
		{
			((JMenu)jmenu).setAccelerator(keyStroke);
		}
	}

	public void setEnabled(boolean b)
	{
		jmenu.setEnabled(b);
	}

	public void setHorizontalAlignment(int alignment)
	{
		if (jmenu instanceof JMenu)
		{
			((JMenu)jmenu).setHorizontalAlignment(alignment);
		}
	}

	public void setIcon(Icon defaultIcon)
	{
		if (jmenu instanceof JMenu)
		{
			((JMenu)jmenu).setIcon(defaultIcon);
		}
	}

	public void setMnemonic(char mnemonic)
	{
		if (jmenu instanceof JMenu)
		{
			((JMenu)jmenu).setMnemonic(mnemonic);
		}
	}

	public void setName(String name)
	{
		jmenu.setName(name);
	}

	public void setSelected(boolean b)
	{
		if (jmenu instanceof JMenu)
		{
			((JMenu)jmenu).setSelected(b);
		}
	}

	public void setText(String text)
	{
		if (jmenu instanceof JMenu)
		{
			((JMenu)jmenu).setText(text);
		}
		else if (jmenu instanceof JPopupMenu)
		{
			((JPopupMenu)jmenu).setLabel(text);
		}
	}

	public void setVisible(boolean aFlag)
	{
		jmenu.setVisible(aFlag);
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
		jmenu.setBackground(Utilities.createColor(bgColor));
	}
}
