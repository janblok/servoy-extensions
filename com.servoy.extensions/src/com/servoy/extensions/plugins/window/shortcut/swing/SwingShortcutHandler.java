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
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.smart.dataui.DataComboBox;
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
	private final ShortcutDispatcher shortcutDispatcher = new ShortcutDispatcher();

	private final ActionMap specialKeysActionMap = new ActionMap();
	private final InputMap specialKeysInputMap = new InputMap();

	/**
	 * Needed because if you have a Scrollpane and you press UP, DOWN, LEFT, RIGHT
	 * it doesn't get to fire the registered action because it is consumed by scroll pane to scroll around
	 */
	private class ShortcutDispatcher implements KeyEventDispatcher
	{
		@Override
		public boolean dispatchKeyEvent(KeyEvent e)
		{
			Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if (e.getID() == KeyEvent.KEY_PRESSED && (comp == null || !(comp instanceof DataComboBox || comp.getParent() instanceof DataComboBox)))
			{
				int k = e.getKeyCode();
				if (isSpecialKey(k))
				{
					JRootPane rootPane = getRootPane().getRootPane();
					if (rootPane != null)
					{
						String keyMap = (String)specialKeysInputMap.get(KeyStroke.getKeyStroke(k, e.getModifiers()));
						Action action = keyMap != null ? specialKeysActionMap.get(keyMap) : null;
						if (action != null) action.actionPerformed(null);
					}
				}
			}
			return false;
		}
	}

	public SwingShortcutHandler(IClientPluginAccess access, WindowProvider windowProvider)
	{
		this.access = access;
		this.windowProvider = windowProvider;
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(shortcutDispatcher);

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
		int k = key.getKeyCode();

		if (isSpecialKey(k) && key.getModifiers() == 0)
		{
			specialKeysInputMap.put(key, mapKey);
		}
		else
		{
			im.put(key, mapKey);
		}
		ActionMap am = rootPane.getActionMap();
		AbstractAction action = new AbstractAction()
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

				if (!windowProvider.shortcutHit(key, (IComponent)component, formName))
				{
					// if the action didn't result in a real hit, try to find a default mapping that we did override.
					InputMap inputMap = getRootPane().getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
					if (inputMap.getParent() != null)
					{
						// if the input map has a parent look if that one has a action mapping for the keystroke
						Object actionMapping = inputMap.getParent().get(key);
						if (actionMapping != null)
						{
							Action act = getRootPane().getRootPane().getActionMap().get(actionMapping);
							if (act != null) act.actionPerformed(e);
						}
					}
				}
			}
		};
		if (isSpecialKey(k) && key.getModifiers() == 0)
		{
			specialKeysActionMap.put(mapKey, action);
		}
		else
		{
			am.put(mapKey, action);
		}

		return true;
	}

	private RootPaneContainer getRootPane()
	{
		IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
		Component w = null;
		if (runtimeWindow instanceof ISmartRuntimeWindow) w = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
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

	public static boolean isSpecialKey(int keyCode)
	{
		if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
			keyCode == KeyEvent.VK_ENTER)
		{
			return true;
		}
		return false;
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

	public void cleanup()
	{
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(shortcutDispatcher);
	}
}
