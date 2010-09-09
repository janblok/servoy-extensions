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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import com.servoy.extensions.plugins.window.menu.AbstractMenuItem;
import com.servoy.extensions.plugins.window.menu.IMenu;
import com.servoy.extensions.plugins.window.menu.IMenuItem;
import com.servoy.extensions.plugins.window.menu.IPopupMenu;

/**
 * Popupmenu in smart client.
 * 
 * @author jblok
 */


public class ScriptableJPopupMenu extends JPopupMenu implements IPopupMenu
{
	private AbstractMenuItem scriptObjectWrapper;

	public ScriptableJPopupMenu()
	{
		addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent e)
			{
				// ignore
			}

			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					MenuElement selectedPath[] = MenuSelectionManager.defaultManager().getSelectedPath();
					if (selectedPath != null && selectedPath.length > 0)
					{
						Component menuComp = selectedPath[selectedPath.length - 1].getComponent();
						if (menuComp instanceof JMenuItem && ((JMenuItem)menuComp).getSubElements().length == 0)
						{
							((JMenuItem)menuComp).doClick();
							ScriptableJPopupMenu.this.hidePopup();
							e.consume();
						}
					}
				}
			}

			public void keyTyped(KeyEvent e)
			{
				// ignore
			}
		});
	}

	public Object getMenuComponent()
	{
		return this;
	}

	public void addMenuItem(IMenuItem menuItem, int index)
	{
		if (index < 0 || index >= getComponentCount())
		{
			add((Component)menuItem.getMenuComponent());
		}
		else
		{
			add((Component)menuItem.getMenuComponent(), index);
		}
	}

	public void removeMenuItem(IMenuItem menuItem)
	{
		remove((Component)menuItem.getMenuComponent());
	}

	public void removeMenuItem(int pos)
	{
		remove(pos);
	}

	public void removeAllItems()
	{
		removeAll();
	}

	public IMenuItem getMenuItem(int index)
	{
		Component item = getComponent(index);
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
		return getComponentCount();
	}

	public void showPopup(Object comp, int x, int y)
	{
		show((Component)comp, x, y);
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				requestFocus();
			}
		});
	}

	public void hidePopup()
	{
		setVisible(false);
	}

	public IMenu getParentMenu()
	{
		return null;
	}

	public void setHorizontalAlignment(int align)
	{
	}

	public void setIcon(Icon image)
	{
	}

	public void setIconURL(String iconURL)
	{
	}

	public void setMnemonic(char mnemonic)
	{
	}

	public void setAccelerator(KeyStroke key)
	{
	}

	public void setText(String name)
	{
		//ignore
	}

	public String getText()
	{
		return null;//ignore
	}

	public void addActionListener(ActionListener actionListener)
	{
		//ignore
	}

	public ActionListener[] getActionListeners()
	{
		//ignore
		return null;
	}

	public void addSeparator(int index)
	{
		add(new JPopupMenu.Separator(), index);
	}

	public void doClick()
	{
	}

	public void setSelected(boolean selected)
	{
	}

	public boolean isSelected()
	{
		return false;
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
	}

	public String getBackgroundColor()
	{
		return null;
	}

	public void setForegroundColor(String fgColor)
	{
	}

	public String getForegroundColor()
	{
		return null;
	}
}
