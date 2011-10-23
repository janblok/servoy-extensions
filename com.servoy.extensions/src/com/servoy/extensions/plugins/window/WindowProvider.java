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
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;
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
@ServoyDocumented(publicName = WindowPlugin.PLUGIN_NAME, scriptingName = "plugins." + WindowPlugin.PLUGIN_NAME)
public class WindowProvider implements IReturnedTypesProvider, IScriptable
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

		public ShortcutCallData(String shortcut, FunctionDefinition functionDefinition)
		{
			this.shortcut = shortcut;
			this.functionDefinition = functionDefinition;
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
				ret = globalHandler.functionDefinition.executeSync(plugin.getClientPluginAccess(), new Object[] { event });
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
					formHandler.functionDefinition.executeSync(plugin.getClientPluginAccess(), new Object[] { event });
				}
				catch (Exception e)
				{
					plugin.getClientPluginAccess().handleException("Error executing method " + formHandler.functionDefinition, e);
				}
			}
		}
	}

	/**
	 * Create a shortcut.
	 *
	 * @sample
	 * // this plugin uses the java keystroke parser
	 * // see http://java.sun.com/j2se/1.5.0/docs/api/javax/swing/KeyStroke.html#getKeyStroke(java.lang.String)
	 * // global shortcut (on all forms) - 'apple 1' on a mac client and 'control 1' on other client platforms
	 * plugins.window.createShortcut('menu 1', 'globals.handleShortcut');
	 * // global handler, only triggered when on form frm_orders
	 * plugins.window.createShortcut('control shift I', globals.handleOrdersShortcut, 'frm_orders');
	 * // form method called when shortcut is used
	 * plugins.window.createShortcut('control LEFT', 'frm_products.handleShortcut', 'frm_products');
	 * // same, but use method in stead of string
	 * plugins.window.createShortcut('control RIGHT', forms.frm_contacts.handleMyShortcut, 'frm_contacts');
	 * // remove global shortcut and form-level shortcut
	 * plugins.window.removeShortcut('menu 1');
	 * plugins.window.removeShortcut('control RIGHT', 'frm_contacts');
	 * // shortcut handlers are called with an JSEvent argument
	 * ///* 
	 * // * Handle keyboard shortcut.
	 * // * 
	 * // * @param {JSEvent} event the event that triggered the action
	 * // *&#47;
	 * //function handleShortcut(event)
	 * //{
	 * //  application.output(event.getType()) // returns 'menu 1'
	 * //  application.output(event.getFormName()) // returns 'frm_contacts'
	 * //  application.output(event.getElementName()) // returns 'contact_name_field' or null when no element is selected
	 * //}
	 * // NOTE: shortcuts will not override existing operating system or browser shortcuts,
	 * // choose your shortcuts careful to make sure they work in all clients.
	 *
	 * @param shortcut 
	 * @param method 
	 * @param form_name optional 
	 */
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
		if (vargs.length > n)
		{
			Object arg = vargs[n++];
			context = arg == null ? null : arg.toString();
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
		shortcutMap.put(context, new ShortcutCallData(shortcut, functionDef));

		return true;
	}

	/**
	 * Remove a shortcut.
	 *
	 * @sampleas js_createShortcut(Object[])
	 *
	 * @param shortcut 
	 * @param form_name optional 
	 */
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

	/**
	 * Bring the window into/out of fullsceen mode.
	 * 
	 * @sample
	 * // active fullscreen mode 
	 * plugins.window.setFullScreen(true);
	 * 
	 * @param full
	 */
	public void js_setFullScreen(boolean full)
	{
		if (plugin.getClientPluginAccess().getApplicationType() == IClientPluginAccess.CLIENT)
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

	/**
	 * @deprecated Replaced by {@link Menu#setVisible(boolean)}.
	 */
	@Deprecated
	public void js_setMenuVisible(boolean v)
	{
		getMenubar(null).js_setVisible(v);
	}

	/**
	 * @deprecated Replaced by {@link #setToolBarAreaVisible(boolean)}.
	 */
	@Deprecated
	public void js_setToolBarVisible(boolean v)
	{
		js_setToolBarAreaVisible(v);
	}

	/**
	 * Show or hide the toolbar area.
	 * 
	 * @sample
	 * // hide the toolbar area
	 * plugins.window.setToolBarAreaVisible(false);
	 * 
	 * @param visible
	 */
	public void js_setToolBarAreaVisible(boolean visible)
	{
		if (plugin.getClientPluginAccess().getApplicationType() == IClientPluginAccess.CLIENT)
		{
			IClientPluginAccess app = plugin.getClientPluginAccess();
			boolean canResize = Utils.getAsBoolean(app.getSettings().getProperty("window.resize.location.enabled", "true"));
			if (!canResize) return;

			IToolbarPanel panel = app.getToolbarPanel();
			if (panel != null)
			{
				((JPanel)panel).setVisible(visible);
			}
		}
	}

	/**
	 * Show or hide the statusbar.
	 * 
	 * @sample
	 * // hide the statusbar
	 * plugins.window.setStatusBarVisible(false);
	 * 
	 * @param visible
	 */
	public void js_setStatusBarVisible(boolean visible)
	{
		if (plugin.getClientPluginAccess().getApplicationType() == IClientPluginAccess.CLIENT)
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
						((JComponent)element).setVisible(visible);
						return;
					}
				}
			}
		}
	}

	/* menu methods */

	/**
	 * @deprecated Replaced by {@link #createPopupMenu(Object[])}.
	 */
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

	/**
	 * @deprecated Replaced by Menu class functionality.
	 */
	@Deprecated
	public RadioButton js_createRadioButtonMenuItem(Object[] vargs) throws PluginException
	{
		return (RadioButton)createMenuItem(null, vargs, IMenuItem.MENU_ITEM_RADIO);
	}

	/**
	 * @deprecated Replaced by {@link #addToolBar(String)}.
	 */
	@Deprecated
	public JToolBar js_addServoyToolBar(JComponent pane, String name) throws Exception
	{
		return ToolBar.addServoyToolBar(plugin.getClientPluginAccess(), pane, name);
	}

	/**
	 * @deprecated Replaced by {@link #removeToolBar(String)}.
	 */
	@Deprecated
	public void js_removeServoyToolBar(JComponent pane, String name) throws Exception
	{
		ToolBar.removeServoyToolBar(pane, name);
	}

	/**
	 * Add a toolbar.
	 *
	 * @sample
	 * // Note: method addToolBar only works in the smart client.
	 * 
	 * // add a toolbar with only a name
	 * var toolbar0 = plugins.window.addToolBar("toolbar_0");
	 * toolbar0.addButton("click me 0", feedback_button);
	 * 
	 * // add a toolbar with a name and the row you want it to show at
	 * // row number starts at 0
	 * var toolbar1 = plugins.window.addToolBar("toolbar_1", 2);
	 * toolbar1.addButton("click me 1", feedback_button);
	 * 
	 * // add a toolbar with a name and display name
	 * var toolbar2 = plugins.window.addToolBar("toolbar_2", "toolbar_2_internal_name");
	 * toolbar2.addButton("click me 2", feedback_button);
	 * 
	 * // add a toolbar with a name, display name and the row you want the
	 * // toolbar to show at. row number starts at 0 
	 * var toolbar3 = plugins.window.addToolBar("toolbar_3", "toolbar_3_internal_name", 3);
	 * toolbar3.addButton("click me 3", feedback_button);
	 * 
	 * @param name
	 */
	public ToolBar js_addToolBar(String name) throws Exception
	{
		return js_addToolBar(name, name);
	}

	/**
	 * Add a toolbar.
	 * 
	 * @sampleas js_addToolBar(String)
	 * 
	 * @param name
	 * @param row
	 */
	public ToolBar js_addToolBar(String name, int row) throws Exception
	{
		return js_addToolBar(name, name, row);
	}

	/**
	 * Add a toolbar.
	 * 
	 * @sampleas js_addToolBar(String)
	 * 
	 * @param name
	 * @param displayname
	 */
	public ToolBar js_addToolBar(String name, String displayname) throws Exception
	{
		return js_addToolBar(name, displayname, -1);
	}

	/**
	 * Add a toolbar.
	 * 
	 * @sampleas js_addToolBar(String)
	 * 
	 * @param name
	 * @param displayname
	 * @param row
	 */
	public ToolBar js_addToolBar(String name, String displayname, int row) throws Exception
	{
		return new ToolBar(plugin.getClientPluginAccess(), name, displayname, row, true, false);
	}

	/**
	 * Get the toolbar from the toolbar panel by name.
	 *
	 * @sample
	 * // Note: method getToolBar only works in the smart client.
	 * 
	 * // the toolbar must first be create with a call to addToolbar
	 * plugins.window.addToolBar("toolbar_0");
	 * 
	 * // get the toolbar at the panel by name
	 * var toolbar = plugins.window.getToolBar("toolbar_0");
	 * // add a button to the toolbar
	 * toolbar.addButton("button", feedback_button);
	 * 
	 * @param name
	 */
	public ToolBar js_getToolBar(String name) throws Exception
	{
		return new ToolBar(plugin.getClientPluginAccess(), name, null, -1, false, false);
	}

	/**
	 * Remove the toolbar from the toolbar panel.
	 *
	 * @sample
	 * // Note: method removeToolBar only works in the smart client.
	 * 
	 * // the toolbar must first be create with a call to addToolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_0");
	 * 
	 * // add a button to the toolbar
	 * toolbar.addButton("button", feedback_button);
	 * 
	 * // removing a toolbar from the toolbar panel is done by name
	 * // the plugin checks the existence of the toolbar
	 * // when the toolbar does not exist it will not throw an error though.
	 * plugins.window.removeToolBar("toolbar_0");
	 * 
	 * @param name
	 */
	public void js_removeToolBar(String name) throws Exception
	{
		if (plugin.getClientPluginAccess().getApplicationType() == IClientPluginAccess.CLIENT)
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

	/**
	 * Get all toolbar names from the toolbar panel.
	 *
	 * @sample
	 * // Note: method getToolbarNames only works in the smart client.
	 * 
	 * // create an array of toolbar names
	 * var names = plugins.window.getToolbarNames();
	 * 
	 * // create an empty message variable
	 * var message = "";
	 * 
	 * // loop through the array
	 * for (var i = 0 ; i < names.length ; i++) {
	 * 	//add the name(s) to the message
	 * 	message += names[i] + "\n";
	 * }
	 * 
	 * // show the message
	 * plugins.dialogs.showInfoDialog("toolbar names", message);
	 */
	public String[] js_getToolbarNames()
	{
		return plugin.getClientPluginAccess().getToolbarPanel().getToolBarNames();
	}

	/**
	 * Get the menubar of the main window, or of a named window.
	 *
	 * @sample
	 * // create a new window
	 * var win = application.createWindow("windowName", JSWindow.WINDOW);
	 * // show a form in the new window
	 * forms.my_form.controller.show(win);
	 * // retrieve the menubar of the new window
	 * var menubar = plugins.window.getMenuBar("windowName");
	 * // add a new menu to the menubar, with an item in it
	 * var menu = menubar.addMenu();
	 * menu.text = "New Menu";
	 * menu.addMenuItem("an entry", feedback);
	 * // retrieve the menubar of the main window
	 * var mainMenubar = plugins.window.getMenuBar();
	 * // add a new menu to the menubar of the main window
	 * var menuMain = mainMenubar.addMenu();
	 * menuMain.text = "New Menu in Main Menubar";
	 * menuMain.addMenuItem("another entry", feedback);
	 */
	public MenuBar js_getMenuBar()
	{
		return getMenubar(null);
	}

	/**
	 * @sameas js_getMenuBar()
	 * 
	 * @param windowName.
	 */
	public MenuBar js_getMenuBar(String windowName)
	{
		return getMenubar(windowName);
	}

	/**
	 * @deprecated Replaced by {@link MenuBar#getMenuCount()}.
	 */
	@Deprecated
	public int js_getMenuCount()
	{
		return getMenubar(null).js_getMenuCount();
	}

	/**
	 * @deprecated Replaced by {@link MenuBar#getMenuIndexByText(String)}.
	 */
	@Deprecated
	public int js_getMenuIndexByText(String name)
	{
		return getMenubar(null).js_getMenuIndexByText(name);
	}

	/**
	 * @deprecated Replaced by {@link MenuBar#removeAllMenus()}.
	 */
	@Deprecated
	public Menu js_removeAllMenus() throws PluginException
	{
		getMenubar(null).js_removeAllMenus();
		return null;
	}

	/**
	 * @deprecated Replaced by {@link MenuBar#removeMenu(Object[])}.
	 */
	@Deprecated
	public void js_removeMenu(Object[] index) throws PluginException
	{
		getMenubar(null).js_removeMenu(index);
	}

	/**
	 * @deprecated Replaced by {@link MenuBar#reset()}.
	 */
	@Deprecated
	public void js_resetMenuBar()
	{
		getMenubar(null).js_reset();
	}

	/**
	 * @deprecated Replaced by {@link MenuBar#validate()}.
	 */
	@Deprecated
	public void js_validateMenuBar()
	{
		getMenubar(null).js_validate();
	}

	/**
	 * @deprecated Replaced by {@link MenuBar#addMenu()}.
	 */
	@Deprecated
	public Menu js_addMenu() throws PluginException
	{
		return getMenubar(null).js_addMenu();
	}

	/**
	 * @deprecated Replaced by {@link MenuBar#addMenu()}.
	 */
	@Deprecated
	public Menu js_addMenu(int index) throws Exception
	{
		return getMenubar(null).js_addMenu(index);
	}

	/**
	 * @deprecated Replaced by {@link MenuBar#getMenu(int)}.
	 */
	@Deprecated
	public Menu js_getMenu(int index) throws Exception
	{
		return getMenubar(null).js_getMenu(index);
	}

	/**
	 * @deprecated Replaced by {@link #createPopupMenu()}.
	 */
	@Deprecated
	public Popup js_setMouseOverPopup(Object[] args) throws PluginException
	{
		return new Popup(plugin.getClientPluginAccess(), getMenuHandler(), createTriggeredPopup(args, IMenuHandler.TRIGGER_MOUSEOVER));
	}

	/**
	 * @deprecated Replaced by {@link #createPopupMenu()}.
	 */
	@Deprecated
	public Popup js_setPopup() throws Exception
	{
		return js_setPopup(null);
	}

	/**
	 * @deprecated Replaced by {@link #createPopupMenu()}.
	 */
	@Deprecated
	public Popup js_setPopup(Object[] args) throws Exception
	{
		return new Popup(plugin.getClientPluginAccess(), getMenuHandler(), createTriggeredPopup(args, IMenuHandler.TRIGGER_RIGHTCLICK));
	}

	/**
	 * @deprecated Replaced by Menu class functionality.
	 */
	@Deprecated
	public MenuItem js_createMenuItem(Object[] vargs) throws PluginException
	{
		return (MenuItem)createMenuItem(null, vargs, IMenuItem.MENU_ITEM_BUTTON);
	}

	/**
	 * @deprecated Replaced by Menu class functionality.
	 */
	@Deprecated
	public CheckBox js_createCheckboxMenuItem(Object[] vargs) throws PluginException
	{
		return (CheckBox)createMenuItem(null, vargs, IMenuItem.MENU_ITEM_CHECK);
	}

	/**
	 * Creates a new popup menu that can be populated with items and displayed.
	 * 
	 * @sample
	 * // create a popup menu
	 * var menu = plugins.window.createPopupMenu();
	 * // add a menu item
	 * menu.addMenuItem("an entry", feedback);
	 * 
	 * if (event.getSource()) {
	 * 	// display the popup over the component which is the source of the event
	 * 	menu.show(event.getSource());
	 * 	// display the popup over the components, at specified coordinates relative to the component
	 * 	//menu.show(event.getSource(), 10, 10);
	 * 	// display the popup at specified coordinates relative to the main window
	 * 	//menu.show(100, 100);
	 * }
	 */
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

	/**
	 * @deprecated Obsolete method.
	 */
	@Deprecated
	public boolean js_register(@SuppressWarnings("unused") String code, @SuppressWarnings("unused") String developer)
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

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	@SuppressWarnings("deprecation")
	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { Menu.class, RadioButton.class, CheckBox.class, MenuItem.class, JSMenuItem.class/* deprecated */, Popup.class, ToolBar.class, MenuBar.class };
	}

	/**
	 * Maximize the current window or the window with the specified name (Smart client only).
	 *
	 * @sample
	 * // maximize the main window:
	 * plugins.window.maximize();
	 * 
	 * // create a new window
	 * var win = application.createWindow("windowName", JSWindow.WINDOW);
	 * // show a form in the new window
	 * forms.my_form.controller.show(win);
	 * // maximize the window
	 * plugins.window.maximize("windowName");
	 */
	public void js_maximize()
	{
		js_maximize(null);
	}

	/**
	 * @clonedesc js_maximize()
	 * @sampleas js_maximize()
	 * 
	 * @param windowName
	 */
	public void js_maximize(final String windowName)
	{
		if (getClientPluginAccess().getApplicationType() == IClientPluginAccess.CLIENT)
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
