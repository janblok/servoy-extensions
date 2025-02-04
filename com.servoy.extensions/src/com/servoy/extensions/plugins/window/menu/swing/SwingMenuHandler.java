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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.MenuElement;

import com.servoy.extensions.plugins.window.menu.IButtonGroup;
import com.servoy.extensions.plugins.window.menu.IMenu;
import com.servoy.extensions.plugins.window.menu.IMenuHandler;
import com.servoy.extensions.plugins.window.menu.IMenuItem;
import com.servoy.extensions.plugins.window.menu.IPopupMenu;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.util.Utils;

/**
 * Handle menu actions for smart client (Swing).
 * 
 * @author rgansevles
 *
 */
public class SwingMenuHandler implements IMenuHandler
{

	private final IClientPluginAccess clientPluginAccess;
	private final Map<String, InitialMenuState> hiddenMenubarStates = new HashMap<String, InitialMenuState>();

	public SwingMenuHandler(IClientPluginAccess iClientPluginAccess)
	{
		this.clientPluginAccess = iClientPluginAccess;
	}

	public IPopupMenu createPopupMenu()
	{
		return new ScriptableJPopupMenu();
	}

	public IButtonGroup createButtonGroup()
	{
		return new SwingButtonGroup();
	}

	public IMenuItem createMenuItem(IMenu parentMenu, int type)
	{
		switch (type)
		{
			case IMenuItem.MENU_ITEM_RADIO :
				return new ScriptableJRadioButtonMenuItem();

			case IMenuItem.MENU_ITEM_CHECK :
				return new ScriptableJCheckBoxMenuItem();

			default :
				return new ScriptableJMenuItem();
		}
	}

	public IMenu createMenu(IMenu parentMenu)
	{
		return new ScriptableJMenu();
	}

	public Component findComponentAt(Point location)
	{
		IRuntimeWindow runtimeWindow = clientPluginAccess.getCurrentRuntimeWindow();
		Window currentWindow = null;
		if (runtimeWindow instanceof ISmartRuntimeWindow)
		{
			currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
			return currentWindow.findComponentAt(location);
		}
		else return null;
	}

	public Point makeLocationWindowRelative(Object component, Point location)
	{
		if (component instanceof Component)
		{
			IRuntimeWindow runtimeWindow = clientPluginAccess.getCurrentRuntimeWindow();
			Point windowLocation = null;
			if (runtimeWindow instanceof ISmartRuntimeWindow)
			{
				windowLocation = ((ISmartRuntimeWindow)runtimeWindow).getWindow().getLocationOnScreen();
			}
			Point compLocation = ((Component)component).getLocationOnScreen();
			Point compLocationToWindow = new Point((int)(compLocation.getX() - windowLocation.getX()), (int)(compLocation.getY() - windowLocation.getY()));
			return new Point((int)(location.x - compLocationToWindow.x - windowLocation.getX()),
				(int)(location.y - compLocationToWindow.y - windowLocation.getY()));
		}
		return location;
	}

	protected Component findContainerChild(Component comp, Class< ? > cls)
	{
		if (comp == null || cls.isAssignableFrom(comp.getClass()))
		{
			return comp;
		}
		if (comp instanceof Container)
		{
			Component[] components = ((Container)comp).getComponents();
			if (components != null)
			{
				for (Component c : components)
				{
					Component child = findContainerChild(c, cls);
					if (child != null)
					{
						return child;
					}
				}
			}
		}
		return null;
	}

	public void showPopup(IPopupMenu popupMenu, Object component, int x, int y)
	{
		Component jcomp = (Component)component;
		JPopupMenu jpop = (JPopupMenu)popupMenu;
		int popX = x;
		int popY = y;

		Dimension prefSize = jpop.getPreferredSize();
		if (prefSize != null)
		{
			int prefSizeH = (int)prefSize.getHeight();

			Container parent = jcomp.getParent();

			if (parent != null && jcomp.getY() + jcomp.getHeight() + prefSizeH > parent.getHeight() && jcomp.getY() > prefSizeH)
			{
				popY = -prefSizeH;
			}
		}
		if (!jcomp.isShowing())
		{
			IRuntimeWindow runtimeWindow = clientPluginAccess.getCurrentRuntimeWindow();
			Window currentWindow = null;
			if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
			Component container = searchContainer(currentWindow, jcomp.getName());
			if (container != null)
			{
				popupMenu.showPopup(container, popX + jcomp.getX(), popY + jcomp.getY());
			}
			else
			{
				// container was not found, just show it in current window
				popupMenu.showPopup(currentWindow, popX + jcomp.getX(), popY + jcomp.getY());
			}
		}
		else
		{
			popupMenu.showPopup(component, popX, popY);
		}
	}

	private Component searchContainer(Component component, String name)
	{
		if (component instanceof JTable && component.isShowing() && component.isVisible())
		{
			if (name == null)
			{
				// if name null, not much to check; we could use introspection here to be 100% sure
				return component;
			}
			try
			{
				((JTable)component).getColumn(name); // throws IllegalArgumentException when column is not found
				return component;
			}
			catch (IllegalArgumentException e)
			{
				// not found
			}
		}
		if (component instanceof Container && ((Container)component).getComponentCount() > 0)
		{
			for (Component child : ((Container)component).getComponents())
			{
				Component searched = searchContainer(child, name);
				if (searched != null)
				{
					return searched;
				}
			}
		}
		return null;
	}


	public void installPopupTrigger(final IPopupMenu popupMenu, IComponent icomponent, final int x, final int y, final int popupTrigger)
	{
		Component component = (Component)icomponent;

		if (component instanceof JScrollPane)
		{
			JViewport viewPort = ((JScrollPane)component).getViewport();
			if (viewPort != null) component = viewPort.getView();
			if (component == null) return;
		}

		component.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				evaluateMouseOver(e, true);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				evaluatePopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				evaluatePopup(e);
			}

			private void evaluateMouseOver(MouseEvent e, boolean show)
			{
				if (popupTrigger == IMenuHandler.TRIGGER_MOUSEOVER)
				{
					if (show)
					{
						Component comp = e.getComponent();
						popupMenu.showPopup(comp, x, comp.getHeight() + y);
					}
					else
					{
						popupMenu.hidePopup();
					}
				}
			}

			private void evaluatePopup(MouseEvent e)
			{
				if (popupTrigger == IMenuHandler.TRIGGER_RIGHTCLICK && e.isPopupTrigger())
				{
					popupMenu.showPopup(e.getComponent(), e.getX() + x, e.getY() + y);
				}
			}
		});
	}

	private JMenuBar getJMenuBar(String windowName, boolean create)
	{
		IRuntimeWindow window = clientPluginAccess.getRuntimeWindow(windowName);
		JMenuBar menuBar = null;
		if (window instanceof ISmartRuntimeWindow)
		{
			menuBar = ((ISmartRuntimeWindow)window).getJMenuBar();
		}

		if (menuBar == null && create)
		{
			menuBar = new JMenuBar();
			if (window instanceof ISmartRuntimeWindow)
			{
				((ISmartRuntimeWindow)window).setJMenuBar(menuBar);
			}
		}

		return menuBar;
	}

	public Object initializeMenuBar(String windowName)
	{
		return getMenubarState(getJMenuBar(windowName, false));
	}

	/**
	 * @param menubar
	 * @return
	 */
	private InitialMenuState getMenubarState(JMenuBar menubar)
	{
		if (menubar == null)
		{
			return null;
		}
		MenuElement[] initialMenuObjects = menubar.getSubElements().clone();
		JMenuItem[][] initialItemObjects = new JMenuItem[menubar.getComponentCount()][];

		MenuElement[] element = initialMenuObjects;

		for (int i = 0; i < menubar.getComponentCount(); i++)
		{
			JMenu menu = (JMenu)element[i];
			JMenuItem[] item = new JMenuItem[menu.getItemCount()];
			for (int ii = 0; ii < menu.getItemCount(); ii++)
			{
				item[ii] = menu.getItem(ii);
			}

			initialItemObjects[i] = item;
		}
		return new InitialMenuState(initialMenuObjects, initialItemObjects);
	}

	public void resetMenuBar(String windowName, Object initializeMenuBarResult)
	{
		JMenuBar menubar = getJMenuBar(windowName, false);
		if (menubar != null && initializeMenuBarResult instanceof InitialMenuState)
		{
			menubar.invalidate();
			menubar.removeAll();

			MenuElement[] element = ((InitialMenuState)initializeMenuBarResult).initialMenuObjects;

			for (int i = 0; i < element.length; i++)
			{
				JMenu menu = (JMenu)element[i];
				Component[] item = ((InitialMenuState)initializeMenuBarResult).initialItemObjects[i];

				menubar.add(menu);
				menu.removeAll();

				for (Component element2 : item)
				{
					if (element2 instanceof JMenuItem)
					{
						menu.add(element2);
					}
					else
					{
						menu.addSeparator();
					}
				}
			}

			menubar.validate();
		}
	}

	public int getMenubarSize(String windowName)
	{
		JMenuBar jMenuBar = getJMenuBar(windowName, false);
		return jMenuBar == null ? 0 : jMenuBar.getMenuCount();
	}

	public void addToMenuBar(String windowName, IMenu impl, int index) throws PluginException
	{
		JMenuBar jMenuBar = getJMenuBar(windowName, true);
		if (jMenuBar != null)
		{
			jMenuBar.add((Component)impl.getMenuComponent(), index);
			jMenuBar.revalidate();
		}
	}

	public void removeFromMenuBar(String windowName, int index) throws PluginException
	{
		JMenuBar jMenuBar = getJMenuBar(windowName, false);
		if (jMenuBar == null)
		{
			return;
		}
		if (index < 0)
		{
			jMenuBar.removeAll();
		}
		else
		{
			if (index >= jMenuBar.getMenuCount())
			{
				throw new PluginException("A _menu with index " + index + " doesn't exist."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			jMenuBar.remove(index);
		}
		jMenuBar.revalidate();
	}

	public IMenu getMenubarMenu(String windowName, int index)
	{
		JMenuBar jMenuBar = getJMenuBar(windowName, false);
		if (jMenuBar == null || index >= jMenuBar.getComponentCount())
		{
			return null;
		}
		JMenu menu = jMenuBar.getMenu(index);
		if (menu == null)
		{
			return null;
		}
		return new ScriptableJMenu(menu);
	}

	public int getMenuIndexByText(String windowName, String name)
	{
		JMenuBar jMenuBar = getJMenuBar(windowName, false);
		for (int i = 0; jMenuBar != null && i < jMenuBar.getMenuCount(); i++)
		{
			if (name.equals(jMenuBar.getMenu(i).getText()))
			{
				return i;
			}
		}
		return -1;
	}

	public void validateMenuBar(String windowName)
	{
		JMenuBar jMenuBar = getJMenuBar(windowName, false);
		if (jMenuBar != null)
		{
			jMenuBar.validate();
		}
	}

	public void setMenubarVisible(String windowName, boolean visible)
	{
		JMenuBar jMenuBar = getJMenuBar(windowName, visible);
		if (jMenuBar != null && jMenuBar.isVisible() != visible)
		{
			jMenuBar.setVisible(visible);
			if (Utils.isAppleMacOS())
			{
				// menubar does not want to go on mac, so remove all items
				if (visible)
				{
					resetMenuBar(windowName, hiddenMenubarStates.remove(windowName));
				}
				else
				{
					hiddenMenubarStates.put(windowName, getMenubarState(jMenuBar));
					jMenuBar.removeAll();
				}
			}
		}
	}

	public static class InitialMenuState
	{

		public final MenuElement[] initialMenuObjects;
		public final JMenuItem[][] initialItemObjects;

		public InitialMenuState(MenuElement[] initialMenuObjects, JMenuItem[][] initialItemObjects)
		{
			this.initialMenuObjects = initialMenuObjects;
			this.initialItemObjects = initialItemObjects;
		}
	}

	public void execute(FunctionDefinition functionDefinition, IClientPluginAccess pluginAccess, Object[] arguments)
	{
		functionDefinition.executeAsync(pluginAccess, arguments);
	}
}
