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
package com.servoy.extensions.plugins.window;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.Point;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.mozilla.javascript.Function;

import com.servoy.extensions.plugins.window.menu.AbstractMenu;
import com.servoy.extensions.plugins.window.menu.AbstractMenu.MenuItemArgs;
import com.servoy.extensions.plugins.window.menu.AbstractMenuItem;
import com.servoy.extensions.plugins.window.menu.CheckBox;
import com.servoy.extensions.plugins.window.menu.IButtonGroup;
import com.servoy.extensions.plugins.window.menu.IMenu;
import com.servoy.extensions.plugins.window.menu.IMenuHandler;
import com.servoy.extensions.plugins.window.menu.IMenuItem;
import com.servoy.extensions.plugins.window.menu.IPopupMenu;
import com.servoy.extensions.plugins.window.menu.IRadioButtonMenuItem;
import com.servoy.extensions.plugins.window.menu.JSMenuItem;
import com.servoy.extensions.plugins.window.menu.Menu;
import com.servoy.extensions.plugins.window.menu.MenuBar;
import com.servoy.extensions.plugins.window.menu.MenuItem;
import com.servoy.extensions.plugins.window.menu.Popup;
import com.servoy.extensions.plugins.window.menu.RadioButton;
import com.servoy.extensions.plugins.window.menu.swing.SwingMenuHandler;
import com.servoy.extensions.plugins.window.menu.swing.ToolBar;
import com.servoy.extensions.plugins.window.menu.wicket.WicketMenuHandler;
import com.servoy.extensions.plugins.window.shortcut.IShortcutHandler;
import com.servoy.extensions.plugins.window.shortcut.swing.SwingShortcutHandler;
import com.servoy.extensions.plugins.window.shortcut.wicket.WicketShortcutHandler;
import com.servoy.extensions.plugins.window.util.Utilities;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.scripting.JSEvent;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.ui.IContainer;
import com.servoy.j2db.ui.IFormUI;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;
import com.servoy.j2db.util.toolbar.IToolbarPanel;

/**
 * Provider for the Window plugin.
 * 
 * @author rgansevles
 */
@SuppressWarnings("nls")
public class WindowProvider implements IScriptObject
{
	private final WindowPlugin plugin;


	private IMenuHandler menuHandler;

	private final Map<String, MenuBar> menuBars = new HashMap<String, MenuBar>(); // null key for main menubar


	WindowProvider(WindowPlugin plugin)
	{
		this.plugin = plugin;
	}

	public IClientPluginAccess getClientPluginAccess()
	{
		return plugin.getClientPluginAccess();
	}

	/* shortcut methods */
	public static class ShortcutCallData
	{
		public final String shortcut;
		public final FunctionDefinition functionDefinition;
		public final Object[] arguments;

		public ShortcutCallData(String shortcut, FunctionDefinition functionDefinition, Object[] arguments)
		{
			this.shortcut = shortcut;
			this.functionDefinition = functionDefinition;
			this.arguments = arguments;
		}

	}

	private IShortcutHandler shortcutHandler;
	Map<KeyStroke, Map<String, ShortcutCallData>> shortcuts = new HashMap<KeyStroke, Map<String, ShortcutCallData>>(); // KeyStroke -> context -> call data

	private IShortcutHandler getShortcutHandler()
	{
		if (shortcutHandler == null)
		{
			if (plugin.getClientPluginAccess().getApplicationType() == IClientPluginAccess.WEB_CLIENT)
			{
				shortcutHandler = new WicketShortcutHandler((IWebClientPluginAccess)plugin.getClientPluginAccess(), this);
			}
			else
			{
				shortcutHandler = new SwingShortcutHandler(plugin.getClientPluginAccess(), this);
			}
		}
		return shortcutHandler;
	}

	protected void cleanupShortcuts()
	{
		if (shortcutHandler != null)
		{
			for (KeyStroke key : shortcuts.keySet())
			{
				shortcutHandler.removeShortcut(key);
			}
			shortcuts.clear();
			shortcutHandler = null;
		}
	}

	private void reinstallShortcuts()
	{
		for (KeyStroke key : shortcuts.keySet())
		{
			getShortcutHandler().addShortcut(key);
		}
	}

	public static KeyStroke parseShortcut(IClientPluginAccess pluginAccess, String shortcut)
	{
		if (shortcut == null)
		{
			return null;
		}
		String keyStrokeString = shortcut.trim();
		if (keyStrokeString.length() == 1)
		{
			keyStrokeString = "pressed " + keyStrokeString;
		}

		if (pluginAccess.getPlatform() == IPluginAccess.PLATFORM_MAC)
		{
			keyStrokeString = keyStrokeString.replaceAll("menu", "meta");
		}
		else
		{
			keyStrokeString = keyStrokeString.replaceAll("menu", "control");
		}

		return KeyStroke.getKeyStroke(keyStrokeString);
	}

	public void shortcutHit(KeyStroke key, IComponent component, String formName)
	{
		Map<String, ShortcutCallData> shortcutMap = shortcuts.get(key);
		if (shortcutMap == null)
		{
			// unknown shortcut
			return;
		}

		ShortcutCallData globalHandler = shortcutMap.get(null);
		ShortcutCallData formHandler = formName == null ? null : shortcutMap.get(formName);

		JSEvent event = new JSEvent();
		if (component != null && !(component instanceof IFormUI) && !(component instanceof IContainer))
		{
			// only use the component when it is really an element
			event.setSource(component);
			event.setElementName(component.getName());
		}
		event.setFormName(formName);
		Object ret = null;
		if (globalHandler != null)
		{
			try
			{
				event.setType(globalHandler.shortcut);
				Object[] arguments = Utils.arrayJoin(new Object[] { event }, globalHandler.arguments);
				ret = globalHandler.functionDefinition.executeSync(plugin.getClientPluginAccess(), arguments);
			}
			catch (Exception e)
			{
				plugin.getClientPluginAccess().handleException("Error executing method " + globalHandler.functionDefinition, e);
			}
		}
		if (formHandler != null)
		{
			if (Boolean.FALSE.equals(ret))
			{
				Debug.trace("WindowPlugin: shortcut form form not executed because global shortcut handler returned false");
			}
			else
			{
				try
				{
					event.setType(formHandler.shortcut);
					Object[] arguments = Utils.arrayJoin(new Object[] { event }, formHandler.arguments);
					formHandler.functionDefinition.executeSync(plugin.getClientPluginAccess(), arguments);
				}
				catch (Exception e)
				{
					plugin.getClientPluginAccess().handleException("Error executing method " + formHandler.functionDefinition, e);
				}
			}
		}
	}

	public boolean js_createShortcut(Object[] vargs)
	{
		if (vargs == null || vargs.length < 2)
		{
			return false;
		}
		FunctionDefinition functionDef;
		int n = 0;
		String shortcut = String.valueOf(vargs[n++]);
		Object callback = vargs[n++];
		String context = null;
		Object[] callbackArgs = null;
		if (vargs.length > n)
		{
			Object arg = vargs[n++];
			//check if this parameter is really the context and not the other optional parameter
			if (arg != null && !(arg instanceof String))
			{
				return false;
			}
			context = arg == null ? null : arg.toString();
		}

		if (vargs.length > n)
		{
			Object[] arg = (Object[])vargs[n++];
			callbackArgs = arg == null ? null : arg;
		}

		if (callback instanceof String)
		{
			// string callback
			// 1. formname.method
			// 2. globals.method
			// 3. method (on context form)
			String str = ((String)callback);
			int dot = str.indexOf('.');
			String methodName;
			String formName = null;
			if (dot == -1)
			{
				if (context == null)
				{
					methodName = "globals." + str;
				}
				else
				{
					formName = context;
					methodName = context + '.' + str;
				}
			}
			else
			{
				if (!str.startsWith("globals."))
				{
					formName = str.substring(0, dot);
				}
				methodName = str.substring(dot + 1);
			}
			functionDef = new FunctionDefinition(formName, methodName);
		}
		else if (callback instanceof Function)
		{
			functionDef = new FunctionDefinition((Function)callback);
		}
		else
		{
			Debug.error("WindowPlugin: could not find method name for method argument");
			return false;
		}

		KeyStroke key = parseShortcut(plugin.getClientPluginAccess(), shortcut);
		if (key == null)
		{
			Debug.error("Could not parse shortcut '" + shortcut + '\'');
			return false;
		}

		Map<String, ShortcutCallData> shortcutMap = shortcuts.get(key);
		if (shortcutMap == null)
		{
			// first time this shortcut was used
			shortcutMap = new HashMap<String, ShortcutCallData>();
			shortcuts.put(key, shortcutMap);
			getShortcutHandler().addShortcut(key);
		}
		shortcutMap.put(context, new ShortcutCallData(shortcut, functionDef, callbackArgs));

		return true;
	}

	public boolean js_removeShortcut(Object[] vargs)
	{
		if (vargs == null || vargs.length == 0)
		{
			return false;
		}
		int n = 0;
		String shortcut = String.valueOf(vargs[n++]);
		String context = null;
		if (vargs.length > n)
		{
			Object arg = vargs[n++];
			context = arg == null ? null : arg.toString();
		}

		KeyStroke key = parseShortcut(plugin.getClientPluginAccess(), shortcut);
		if (key == null)
		{
			Debug.error("Could not parse shortcut '" + shortcut + '\'');
			return false;
		}

		Map<String, ShortcutCallData> shortcutMap = shortcuts.get(key);
		if (shortcutMap == null || shortcutMap.remove(context) == null)
		{
			// was not used
			return false;
		}
		if (shortcutMap.size() == 0)
		{
			// not used anymore
			shortcuts.remove(key);
			getShortcutHandler().removeShortcut(key);
		}
		return true;
	}


	/* kioskmode methods */

	private GraphicsDevice graphicsDevice;

	private GraphicsDevice getGraphicsDevice()
	{
		if (graphicsDevice == null)
		{
			IRuntimeWindow runtimeWindow = plugin.getClientPluginAccess().getCurrentRuntimeWindow();
			Window currentWindow = null;
			if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
			graphicsDevice = currentWindow.getGraphicsConfiguration().getDevice();
		}
		return graphicsDevice;
	}

	public void js_setFullScreen(boolean full)
	{
		if (plugin.isSwingClient())
		{
			if (full)
			{
				IRuntimeWindow runtimeWindow = plugin.getClientPluginAccess().getCurrentRuntimeWindow();
				Window currentWindow = null;
				if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
				if (getGraphicsDevice().getFullScreenWindow() == currentWindow) return;
				if (currentWindow instanceof JFrame)
				{
					currentWindow.dispose();
					((JFrame)currentWindow).setUndecorated(true);
				}

				getGraphicsDevice().setFullScreenWindow(currentWindow);
			}
			else
			{
				IRuntimeWindow runtimeWindow = plugin.getClientPluginAccess().getCurrentRuntimeWindow();
				Window currentWindow = null;
				if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
				if (currentWindow instanceof JFrame && getGraphicsDevice().getFullScreenWindow() == currentWindow)
				{
					currentWindow.dispose();
					getGraphicsDevice().setFullScreenWindow(null);
					((JFrame)currentWindow).setUndecorated(false);
					currentWindow.setVisible(true);
				}
				else
				{
					getGraphicsDevice().setFullScreenWindow(null);
				}
			}
			// this shouldn't be needed now
			//JPopupMenu.setDefaultLightWeightPopupEnabled(full); //in full screen heavy weight popups do not work
		}
	}

	@Deprecated
	public void js_setMenuVisible(boolean v)
	{
		getMenubar(null).js_setVisible(v);
	}

	@Deprecated
	public void js_setToolBarVisible(boolean v)
	{
		js_setToolBarAreaVisible(v);
	}

	public void js_setToolBarAreaVisible(boolean v)
	{
		if (plugin.isSwingClient())
		{
			IClientPluginAccess app = plugin.getClientPluginAccess();
			boolean canResize = Utils.getAsBoolean(app.getSettings().getProperty("window.resize.location.enabled", "true"));

			if (!canResize) return;

			IToolbarPanel panel = app.getToolbarPanel();
			if (panel != null)
			{
				((JPanel)panel).setVisible(v);
			}
		}
	}

	public void js_setStatusBarVisible(boolean v)
	{
		if (plugin.isSwingClient())
		{
			IClientPluginAccess app = plugin.getClientPluginAccess();
			boolean canResize = Utils.getAsBoolean(app.getSettings().getProperty("window.resize.location.enabled", "true"));
			if (!canResize) return;

			IToolbarPanel panel = app.getToolbarPanel();
			if (panel instanceof Container && ((Container)panel).getParent() != null)
			{
				Container mainPanel = ((Container)panel).getParent();
				Component[] comps = mainPanel.getComponents();
				for (Component element : comps)
				{
					if (element instanceof JComponent && "statusbar".equals(((JComponent)element).getName()))
					{
						((JComponent)element).setVisible(v);
						return;
					}
				}
			}
		}
	}

	/* menu methods */

	@Deprecated
	public void js_showPopupMenu(Object[] vargs) throws PluginException
	{
		if (vargs.length == 3)
		{
			int x = Utils.getAsInteger(vargs[0]);
			int y = Utils.getAsInteger(vargs[1]);
			Object[] items = (Object[])vargs[2];
			if (items != null)
			{
				IPopupMenu pop = createPopupMenu(items);
				Point loc = new Point(x, y);
				Object comp = getMenuHandler().findComponentAt(loc);
				if (comp != null)
				{
					loc = getMenuHandler().makeLocationWindowRelative(comp, loc);
				}
				pop.showPopup(comp, loc.x, loc.y);
			}
		}
		else if (vargs.length >= 2 && vargs[0] instanceof IComponent && vargs[1] instanceof Object[])
		{
			IComponent comp = (IComponent)vargs[0];
			Object[] items = (Object[])vargs[1];
			if (comp != null && items != null)
			{
				IPopupMenu pop = createPopupMenu(items);

				int popY = comp.getSize().height;
				getMenuHandler().showPopup(pop, comp, 0, popY);
			}
		}
	}

	@Deprecated
	public RadioButton js_createRadioButtonMenuItem(Object[] vargs) throws PluginException
	{
		return (RadioButton)createMenuItem(null, vargs, IMenuItem.MENU_ITEM_RADIO);
	}

	@Deprecated
	public JToolBar js_addServoyToolBar(JComponent pane, String name) throws Exception
	{
		return ToolBar.addServoyToolBar(plugin.getClientPluginAccess(), pane, name);
	}

	@Deprecated
	public void js_removeServoyToolBar(JComponent pane, String name) throws Exception
	{
		ToolBar.removeServoyToolBar(pane, name);
	}

	public ToolBar js_addToolBar(String name) throws Exception
	{
		return js_addToolBar(name, name);
	}

	public ToolBar js_addToolBar(String name, int row) throws Exception
	{
		return js_addToolBar(name, name, row);
	}

	public ToolBar js_addToolBar(String name, String displayname) throws Exception
	{
		return js_addToolBar(name, displayname, -1);
	}

	public ToolBar js_addToolBar(String name, String displayname, int row) throws Exception
	{
		return new ToolBar(plugin.getClientPluginAccess(), name, displayname, row, true, false);
	}

	public ToolBar js_getToolBar(String name) throws Exception
	{
		return new ToolBar(plugin.getClientPluginAccess(), name, null, -1, false, false);
	}

	public void js_removeToolBar(String name) throws Exception
	{
		if (plugin.isSwingClient())
		{
			IToolbarPanel panel = plugin.getClientPluginAccess().getToolbarPanel();

			if (panel.getToolBar(name) != null)
			{
				panel.removeToolBar(name);
			}

			IRuntimeWindow runtimeWindow = getClientPluginAccess().getRuntimeWindow(null);
			if (runtimeWindow instanceof ISmartRuntimeWindow)
			{
				((ISmartRuntimeWindow)runtimeWindow).getWindow().validate();
			}
		}
	}

	public String[] js_getToolbarNames()
	{
		return plugin.getClientPluginAccess().getToolbarPanel().getToolBarNames();
	}

	public MenuBar js_getMenuBar()
	{
		return getMenubar(null);
	}

	public MenuBar js_getMenuBar(String windowName)
	{
		return getMenubar(windowName);
	}

	@Deprecated
	public int js_getMenuCount()
	{
		return getMenubar(null).js_getMenuCount();
	}

	@Deprecated
	public int js_getMenuIndexByText(String name)
	{
		return getMenubar(null).js_getMenuIndexByText(name);
	}

	@Deprecated
	public Menu js_removeAllMenus() throws PluginException
	{
		getMenubar(null).js_removeAllMenus();
		return null;
	}

	@Deprecated
	public void js_removeMenu(Object[] index) throws PluginException
	{
		getMenubar(null).js_removeMenu(index);
	}

	@Deprecated
	public void js_resetMenuBar()
	{
		getMenubar(null).js_reset();
	}

	@Deprecated
	public void js_validateMenuBar()
	{
		getMenubar(null).js_validate();
	}

	@Deprecated
	public Menu js_addMenu() throws PluginException
	{
		return getMenubar(null).js_addMenu();
	}

	@Deprecated
	public Menu js_addMenu(int index) throws Exception
	{
		return getMenubar(null).js_addMenu(index);
	}

	@Deprecated
	public Menu js_getMenu(int index) throws Exception
	{
		return getMenubar(null).js_getMenu(index);
	}

	@Deprecated
	public Popup js_setMouseOverPopup(Object[] args) throws PluginException
	{
		return new Popup(plugin.getClientPluginAccess(), getMenuHandler(), createTriggeredPopup(args, IMenuHandler.TRIGGER_MOUSEOVER));
	}

	@Deprecated
	public Popup js_setPopup() throws Exception
	{
		return js_setPopup(null);
	}

	@Deprecated
	public Popup js_setPopup(Object[] args) throws Exception
	{
		return new Popup(plugin.getClientPluginAccess(), getMenuHandler(), createTriggeredPopup(args, IMenuHandler.TRIGGER_RIGHTCLICK));
	}

	@Deprecated
	public MenuItem js_createMenuItem(Object[] vargs) throws PluginException
	{
		return (MenuItem)createMenuItem(null, vargs, IMenuItem.MENU_ITEM_BUTTON);
	}

	@Deprecated
	public CheckBox js_createCheckboxMenuItem(Object[] vargs) throws PluginException
	{
		return (CheckBox)createMenuItem(null, vargs, IMenuItem.MENU_ITEM_CHECK);
	}

	public Popup js_createPopupMenu() throws PluginException
	{
		return new Popup(getClientPluginAccess(), getMenuHandler(), getMenuHandler().createPopupMenu());
	}

	private MenuBar getMenubar(String windowName)
	{
		if (!menuBars.containsKey(windowName))
		{
			menuBars.put(windowName, new MenuBar(windowName, this));
		}
		return menuBars.get(windowName);
	}

	public IPopupMenu createTriggeredPopup(Object[] args, int popupTrigger) throws PluginException
	{
		IPopupMenu popupMenu = createPopupMenu(null);
		if (args != null)
		{
			int x, y;
			int length = args.length;
			if (length == 1 || (length > 1 && (args[length - 1] instanceof IComponent) && (args[length - 2] instanceof IComponent)))
			{
				x = 0;
				y = 0;
			}
			else
			{
				x = Utils.getAsInteger(args[length - 2]);
				y = Utils.getAsInteger(args[length - 1]);

				length -= 2;
			}

			for (int i = 0; i < length; i++)
			{
				if (args[i] instanceof IComponent)
				{
					getMenuHandler().installPopupTrigger(popupMenu, (IComponent)args[i], x, y, popupTrigger);
				}
			}
		}
		return popupMenu;
	}


	@Deprecated
	public boolean js_register(@SuppressWarnings("unused")
	String code, @SuppressWarnings("unused")
	String developer)
	{
		return true;
	}

	/**
	 * Cleanup this plugin for reuse in another solution.
	 */
	public void cleanup()
	{
		cleanupShortcuts();
		// reset main menubar
		MenuBar mainMenubar = menuBars.get(null);
		if (mainMenubar != null)
		{
			mainMenubar.js_reset();
		}
		menuBars.clear();
	}

	public void unload()
	{
		shortcuts = null;
		shortcutHandler = null;
		menuBars.clear();
	}

	public void currentWindowChanged()
	{
		reinstallShortcuts();
	}


	public IMenuHandler getMenuHandler()
	{
		if (menuHandler == null)
		{
			if (plugin.getClientPluginAccess().getApplicationType() == IClientPluginAccess.WEB_CLIENT)
			{
				menuHandler = new WicketMenuHandler((IWebClientPluginAccess)plugin.getClientPluginAccess());
			}
			else
			{
				menuHandler = new SwingMenuHandler(plugin.getClientPluginAccess());
			}
		}
		return menuHandler;
	}


	private AbstractMenuItem createMenuItem(IMenu parentMenu, Object[] vargs, int type) throws PluginException
	{
		MenuItemArgs menuItemArgs = AbstractMenu.parseMenuItemArgs(plugin.getClientPluginAccess(), vargs);

		IMenuItem impl;
		if (menuItemArgs.submenu == null)
		{
			impl = getMenuHandler().createMenuItem(null, type);
		}
		else
		{
			IMenu menu = getMenuHandler().createMenu(parentMenu);
			fillSubMenu(menu, menuItemArgs.submenu);
			impl = menu;
		}

		impl.setText(menuItemArgs.name);
		if (menuItemArgs.mnemonic != 0)
		{
			impl.setMnemonic(menuItemArgs.mnemonic);
		}

		if (menuItemArgs.imageURL != null)
		{
			impl.setIconURL(menuItemArgs.imageURL);
		}
		else if (menuItemArgs.imageBytes != null)
		{
			impl.setIcon(Utilities.getImageIcon(menuItemArgs.imageBytes));
		}
		if (menuItemArgs.align != -1)
		{
			impl.setHorizontalAlignment(menuItemArgs.align);
		}
		impl.setEnabled(menuItemArgs.enabled);

		AbstractMenuItem menuItem = AbstractMenuItem.createmenuItem(plugin.getClientPluginAccess(), getMenuHandler(), impl, null, false);
		if (menuItemArgs.method != null)
		{
			menuItem.setFunctionDefinition(new FunctionDefinition(menuItemArgs.method));
		}
		return menuItem;
	}

	private IPopupMenu createPopupMenu(Object[] items) throws PluginException
	{
		IPopupMenu menu = getMenuHandler().createPopupMenu();
		fillSubMenu(menu, items);

		return menu;
	}

	private void fillSubMenu(IMenu menu, Object[] items) throws PluginException
	{
		if (items == null) return;

		IButtonGroup bg = null;
		for (Object item : items)
		{
			if (item instanceof AbstractMenuItem)
			{
				IMenuItem menuItem = ((AbstractMenuItem)item).getMenuItem();
				if ("-".equals(menuItem.getText()))
				{
					menu.addSeparator(-1);
				}
				else
				{
					if (menuItem.getParentMenu() != null)
					{
						menuItem.getParentMenu().removeMenuItem(menuItem);//safety
					}
					if (menuItem instanceof IRadioButtonMenuItem)
					{
						if (bg == null)
						{
							bg = getMenuHandler().createButtonGroup();
						}
						bg.add((IRadioButtonMenuItem)menuItem);
					}
					menu.addMenuItem(menuItem, -1);
				}
			}
		}
	}

	/* IScriptObject methods */

	public String[] getParameterNames(String methodName)
	{
		// kioskmode methods

		// shortcut methods

		if ("createShortcut".equals(methodName))
		{
			return new String[] { "shortcut", "method", "[form_name]", "[arguments]" };
		}
		if ("removeShortcut".equals(methodName))
		{
			return new String[] { "shortcut", "[form_name]" };
		}


		// menu methods

		if ("addToolBar".equals(methodName))
		{
			return new String[] { "name", "[displayname]", "[row]" };
		}
		if ("getToolBar".equals(methodName))
		{
			return new String[] { "name" };
		}
		if ("removeToolBar".equals(methodName))
		{
			return new String[] { "name" };
		}
		if ("getMenuBar".equals(methodName))
		{
			return new String[] { "[windowName]" };
		}
		if ("maximize".equals(methodName))
		{
			return new String[] { "[windowName]" };
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		// kioskmode methods

		// shortcut methods

		// menu methods
		if ("register".equals(methodName))
		{
			return true;
		}

		if ("addServoyToolBar".equals(methodName) || //
			"createCheckboxMenuItem".equals(methodName) //
			|| "createMenuItem".equals(methodName) //
			|| "createRadioButtonMenuItem".equals(methodName) //
			|| "removeServoyToolBar".equals(methodName) //
			|| "setMouseOverPopup".equals(methodName) //
			|| "setPopup".equals(methodName) //
			|| "showPopupMenu".equals(methodName)//
			|| "setToolBarVisible".equals(methodName))
		{
			return true;
		}

		// menubar methods moved to menubar object
		if ("getMenuCount".equals(methodName) || //
			"getMenuIndexByText".equals(methodName) //
			|| "removeAllMenus".equals(methodName) //
			|| "removeMenu".equals(methodName) //
			|| "resetMenuBar".equals(methodName) //
			|| "validateMenuBar".equals(methodName) //
			|| "addMenu".equals(methodName) //
			|| "getMenu".equals(methodName) //
			|| "setMenuVisible".equals(methodName))
		{
			return true;
		}

		return false;
	}

	private boolean isSmartClientOnly(String methodName)
	{
		// kioskmode methods

		// shortcut methods

		// menu methods
		if ("addToolBar".equals(methodName) || "getToolBar".equals(methodName) || "removeToolBar".equals(methodName) || "getToolbarNames".equals(methodName))
		{
			return true;
		}

		return false;
	}

	public String getSample(String methodName)
	{
		StringBuilder sb = new StringBuilder();

		if (isSmartClientOnly(methodName))
		{
			sb.append("// Note: method ").append(methodName).append(" only works in the smart client.\n\n");
		}

		String toolTip = getToolTip(methodName);
		if (toolTip != null)
		{
			sb.append("// ").append(toolTip).append('\n');
		}

		// kioskmode methods

		if ("setToolBarAreaVisible".equals(methodName))
		{
			sb.append("%%elementName%%.setToolBarAreaVisible(false)");
		}
		else if ("setFullScreen".equals(methodName))
		{
			sb.append("%%elementName%%.setFullScreen(true)");
		}


		// shortcut methods

		else if (methodName.endsWith("Shortcut"))
		{
			sb.append("// this plugin uses the java keystroke parser\n");
			sb.append("// see http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/KeyStroke.html#getKeyStroke(java.lang.String)\n");
			sb.append("\n// global shortcut (on all forms) on  'apple 1' on a mac client and 'control 1' on other client platforms\n");
			sb.append("%%elementName%%.createShortcut('menu 1', 'globals.handleShortcut');\n");
			sb.append("// global handler, only triggered when on form frm_orders\n");
			sb.append("%%elementName%%.createShortcut('control shift I', globals.handleOrdersShortcut, 'frm_orders');\n");
			sb.append("// form method called when shortcut is used\n");
			sb.append("%%elementName%%.createShortcut('control LEFT', 'frm_products.handleShortcut', 'frm_products');\n");
			sb.append("// same, but use method in stead of string\n");
			sb.append("%%elementName%%.createShortcut('control RIGHT', forms.frm_contacts.handleMyShortcut, 'frm_contacts');\n");
			sb.append("// form method called when shortcut is used and arguments are passed to the method\n");
			sb.append("%%elementName%%.createShortcut('control RIGHT', 'forms.frm_contacts.handleMyShortcut', 'frm_contacts', new Array(argument1, argument2));\n");
			//sb.append("%%elementName%%.createShortcut('control RIGHT', 'forms.frm_contacts.handleMyShortcut', null, new Array(argument1, argument2));\n");
			sb.append("\n// remove global shortcut and form-level shortcut\n");
			sb.append("%%elementName%%.createShortcut('menu 1');\n");
			sb.append("%%elementName%%.removeShortcut('control RIGHT', 'frm_contacts');\n");
			sb.append("\n// shortcut handlers are called with an jsevent argument\n");
			sb.append("///**\n");
			sb.append("//* Handle keyboard shortcut.\n");
			sb.append("//*\n");
			sb.append("//* @param {JSEvent} event the event that triggered the action\n");
			sb.append("//*/\n");
			sb.append("//function handleShortcut(event)\n");
			sb.append("//{\n");
			sb.append("//  application.output(event.getType()) // returns 'menu 1'\n");
			sb.append("//  application.output(event.getFormName()) // returns 'frm_contacts'\n");
			sb.append("//  application.output(event.getElementName()) // returns 'contact_name_field' or null when no element is selected\n");
			sb.append("//}\n");
			sb.append("\n// NOTE: shortcuts will not override existing operating system or browser shortcuts,\n");
			sb.append("// choose your shortcuts careful to make sure they work in all clients.");
		}


		// menu methods

		else if ("addToolBar".equals(methodName))
		{
			sb.append("// add a toolbar with only a name\n");
			sb.append("var toolbar = %%elementName%%.addToolBar(\"toolbar_0\");\n");
			sb.append("\n");
			sb.append("// add a toolbar with a name and internal name\n");
			sb.append("// var toolbar = %%elementName%%.addToolBar(\"toolbar_1\", \"toolbar_1\");\n");
			sb.append("\n");
			sb.append("// add a toolbar with a name, internal name and the row you want the\n");
			sb.append("// toolbar to show at. rownumber starts at 0 \n");
			sb.append("// var toolbar = %%elementName%%.addToolBar(\"toolbar_2\", \"toolbar_2\", 3);\n");
			sb.append("\n");
			sb.append("// REMARK: normally you would add buttons, checkboxes etc in the same method\n");
			sb.append("// this example will show no buttons for now!\n");
			sb.append("// we will add them via the other methods on this form.");
		}
		else if ("getToolBar".equals(methodName))
		{
			sb.append("// get the toolbar at the panel by name\n");
			sb.append("var toolbar = %%elementName%%.getToolBar(\"toolbar_0\");\n");
			sb.append("\n");
			sb.append("// add a button with a text and a method\n");
			sb.append("toolbar.addButton(\"button\", feedback_button);\n");
			sb.append("\n");
			sb.append("// add an input array to the button for feedback in the selected method\n");
			sb.append("toolbar.addButton(\"button\", feedback_button, [1, \"2\", \"three\"]);\n");
			sb.append("\n");
			sb.append("// add an icon to the button\n");
			sb.append("toolbar.addButton(\"button\", feedback_button, [1, \"2\", \"three\"], \"media:///yourimage.gif\");\n");
			sb.append("\n");
			sb.append("// add a tooltip to the button\n");
			sb.append("toolbar.addButton(\"button\", feedback_button, [1, \"2\", \"three\"], \"media:///yourimage.gif\", \"tooltip.\");\n");
			sb.append("\n");
			sb.append("// show only an icon on the button and disable the button\n");
			sb.append("toolbar.addButton(null, feedback_button, [1, \"2\", \"three\"], \"media:///yourimage.gif\", \"tooltip.\", false);\n");
			sb.append("\n");
			sb.append("// add a separator\n");
			sb.append("toolbar.addSeparator();\n");
			sb.append("\n");
			sb.append("// make the button non visible\n");
			sb.append("toolbar.addButton(null, feedback_button, [1, \"2\", \"three\"], \"media:///yourimage.gif\", \"tooltip.\",true, false);\n");
			sb.append("\n");
			sb.append("// and validate the changes\n");
			sb.append("// to make them know to the user interface)\n");
			sb.append("toolbar.validate();");
		}
		else if ("removeToolBar".equals(methodName))
		{
			sb.append("// removing a toolbar from the toolbar panel is done by name\n");
			sb.append("// the plugin checks the existence of the toolbar\n");
			sb.append("// when the toolbar does not exist it will not throw an error though.\n");
			sb.append("%%elementName%%.removeToolBar(\"toolbar_0\");\n");
			sb.append("%%elementName%%.removeToolBar(\"toolbar_1\");\n");
			sb.append("%%elementName%%.removeToolBar(\"toolbar_2\");");
		}
		else if ("getToolbarNames".equals(methodName))
		{
			sb.append("// create an array of toolbar names\n");
			sb.append("var names = %%elementName%%.getToolbarNames();\n");
			sb.append("\n");
			sb.append("// create an empty message variable\n");
			sb.append("var message = \"\";\n");
			sb.append("\n");
			sb.append("// loop through the array\n");
			sb.append("for (var i = 0 ; i < names.length ; i++) {\n");
			sb.append("\t//add the name(s) to the message\n");
			sb.append("\tmessage += names[i] + \"\\n\";\n");
			sb.append("}\n");
			sb.append("\n");
			sb.append("// show the message\n");
			sb.append("plugins.dialogs.showInfoDialog(\"toolbar names\", message);");
		}

		else if ("createPopupMenu".equals(methodName))
		{
			getPopupMenuSample(sb, "%%elementName%%");
		}

		else if ("getMenuBar".equals(methodName))
		{
			sb.append("// get the menubar of the main window\n");
			sb.append("var mainMenubar = %%elementName%%.getMenuBar();\n");
			sb.append("\n");
			sb.append("// get the menubar of a named window\n");
			sb.append("application.showFormInWindow(forms.contacts,100,80,500,300,'my own window title',false,true,'mywindow');\n");
			sb.append("var myWindowMenubar = %%elementName%%.getMenuBar('mywindow');\n");
		}

		else if ("maximize".equals(methodName))
		{
			sb.append("// maximize the main window:\n");
			sb.append("%%elementName%%.maximize();\n");
			sb.append("// or a window constructed with the name 'test':\n");
			sb.append("%%elementName%%.maximize('test');\n");
		}
		// undocumented?
		else
		{
			sb.append("%%elementName%%.").append(methodName).append("()");
		}

		return sb.append('\n').toString();
	}

	public static StringBuilder getPopupMenuSample(StringBuilder sb, String plugin)
	{
		sb.append("var popupmenu = ").append(plugin).append(".createPopupMenu()\n\n");

		sb.append("var menuitem1 = popupmenu.addMenuItem('A',myMethod)\n");
		sb.append("var menuitem2 = popupmenu.addRadioButton('B',myMethod)\n");
		sb.append("var menuitem3 = popupmenu.addRadioButton('C',myMethod)\n");
		sb.append("var menuitem4 = popupmenu.addSeparator()\n");
		sb.append("var menuitem5 = popupmenu.addMenuItem('<html><b>Hello</b></html>',myMethod)\n");
		sb.append("var menuitem6 = popupmenu.addMenuItem('G', globals.myGlobalMethod)\n");
		sb.append("//add arguments to the method call\n");
		sb.append("menuitem6.methodArguments = ['arg1', 'another argument']\n");

		sb.append("\nvar submenu = popupmenu.addMenu('SubMenu')\n");
		sb.append("var subitem1 = submenu.addCheckBox('i18n:bla_bla',myMethod)\n");
		sb.append("var subitem2 = submenu.addCheckBox('he' , globals.myOtherGlobalMethod , 'media:///day_obj.gif')\n");
		sb.append("var subitem3 = submenu.addCheckBox('more' , globals.myOtherGlobalMethod ,null, 'm') //last parameter is mnemonic-key\n");

		sb.append("\nmenuitem2.selected = true;\n");
		sb.append("menuitem6.enabled = false\n");
		sb.append("subitem2.selected = true;\n");

		sb.append("\nvar source = event.getSource()\n");
		sb.append("if (source != null)\n");
		sb.append("{\n");
		sb.append("\tpopupmenu.show(source);\n");
		sb.append("\t//or you can set the coordinates popupmenu.show(10, 10);\n");
		sb.append("}\n");
		return sb;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(String)
	 */
	public String getToolTip(String methodName)
	{
		// kioskmode methods

		// shortcut methods

		if ("createShortcut".equals(methodName))
		{
			return "Create a shortcut.";
		}
		if ("removeShortcut".equals(methodName))
		{
			return "Remove a shortcut.";
		}

		// menu methods

		if ("addToolBar".equals(methodName))
		{
			return "Add a toolbar by name and optional displayname and row.";
		}
		if ("getToolBar".equals(methodName))
		{
			return "Get the toolbar from the toolbar panel by name.";
		}
		if ("removeToolBar".equals(methodName))
		{
			return "Remove the toolbar from the toolbar panel.";
		}
		if ("getToolbarNames".equals(methodName))
		{
			return "Get all toolbar names from the toolbar panel.";
		}

		if ("getMenuBar".equals(methodName))
		{
			return "Get the menubar of a window.";
		}

		if ("maximize".equals(methodName))
		{
			return "Maximize the current window or the window with the name provided (Smart client only)";
		}

		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	@SuppressWarnings("deprecation")
	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { Menu.class, RadioButton.class, CheckBox.class, MenuItem.class, JSMenuItem.class/* deprecated */, Popup.class, ToolBar.class, MenuBar.class };
	}

	public void js_maximize()
	{
		js_maximize(null);
	}

	public void js_maximize(final String windowName)
	{
		if (plugin.isSwingClient())
		{
			IRuntimeWindow runtimeWindow = getClientPluginAccess().getRuntimeWindow(windowName);
			if (runtimeWindow instanceof ISmartRuntimeWindow)
			{
				Window window = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
				if (window != null && window instanceof JFrame)
				{
					JFrame frame = (JFrame)window;
					frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
					frame.repaint();
				}
			}
		}
	}


}
