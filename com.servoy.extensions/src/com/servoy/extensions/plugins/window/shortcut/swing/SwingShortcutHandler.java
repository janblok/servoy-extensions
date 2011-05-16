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
package com.servoy.extensions.plugins.window.shortcut.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;

import com.servoy.extensions.plugins.window.WindowProvider;
import com.servoy.extensions.plugins.window.shortcut.IShortcutHandler;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISwingRuntimeWindow;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.ui.IFormUI;
import com.servoy.j2db.util.Debug;


/**
 * This class handles (de)registering of shortcuts in the smart client (Swing).
 * 
 * @author rgansevles
 * 
 */
public class SwingShortcutHandler implements IShortcutHandler
{
	static final String SHORTCUT_PREFIX = "_plugin_window_shortcut."; //$NON-NLS-1$

	private final IClientPluginAccess access;
	private final WindowProvider windowProvider;

	public SwingShortcutHandler(IClientPluginAccess access, WindowProvider windowProvider)
	{
		this.access = access;
		this.windowProvider = windowProvider;
	}

	public boolean addShortcut(final KeyStroke key)
	{
		RootPaneContainer rootPaneContainer = getRootPane();
		if (rootPaneContainer == null)
		{
			Debug.error("SwingShortcutHandler: Could not find root pane for installing shortcuts"); //$NON-NLS-1$
			return false;
		}

		JRootPane rootPane = rootPaneContainer.getRootPane();
		InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		String mapKey = SHORTCUT_PREFIX + key;

		im.put(key, mapKey);
		ActionMap am = rootPane.getActionMap();
		am.put(mapKey, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				Component component = findIComponent(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());

				String formName = null;
				for (Component c = component; c != null; c = c.getParent())
				{
					if (c instanceof IFormUI)
					{
						formName = ((IFormUI)c).getController().getName();
						break;
					}
				}

				windowProvider.shortcutHit(key, (IComponent)component, formName);
			}
		});
		return true;
	}

	private RootPaneContainer getRootPane()
	{
		IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
		Component w = null;
		if (runtimeWindow instanceof ISwingRuntimeWindow) w = ((ISwingRuntimeWindow)runtimeWindow).getWindow();
		while (w != null)
		{
			if (w instanceof RootPaneContainer)
			{
				return (RootPaneContainer)w;
			}
			w = w.getParent();
		}
		return null;
	}

	/**
	 * Find the first IComponent child (breath-first search)
	 * 
	 * @param component
	 * @return
	 */
	protected static Component findIComponent(Component component)
	{
		List<Component> lst = null;
		for (Component comp = component; true; comp = (lst == null || lst.size() == 0) ? null : lst.remove(0))
		{
			if (comp == null || comp instanceof IComponent)
			{
				return comp;
			}

			Component parentIComponent = getParentIComponent(comp);
			if (parentIComponent != null) return parentIComponent;

			if (comp instanceof Container)
			{
				if (lst == null)
				{
					lst = new ArrayList<Component>();
				}
				for (Component c : ((Container)comp).getComponents())
				{
					lst.add(c);
				}
			}
		}
	}

	private static Component getParentIComponent(Component component)
	{
		Component parent = component;
		while (parent != null && !(parent instanceof IComponent))
		{
			parent = parent.getParent();
		}

		return parent;
	}

	public boolean removeShortcut(KeyStroke key)
	{
		RootPaneContainer rootPaneContainer = getRootPane();
		if (rootPaneContainer == null)
		{
			Debug.error("SwingShortcutHandler: Could not find root pane for removing shortcuts"); //$NON-NLS-1$
			return false;
		}

		JRootPane rootPane = rootPaneContainer.getRootPane();
		InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		im.remove(key);
		String mapKey = SHORTCUT_PREFIX + key;

		ActionMap am = rootPane.getActionMap();
		am.remove(mapKey);
		return true;
	}
}
