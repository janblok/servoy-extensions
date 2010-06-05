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
package com.servoy.extensions.plugins.spellcheck;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URL;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.text.JTextComponent;

import com.keyoti.rapidSpell.desktop.RapidSpellGUIDialog;
import com.servoy.j2db.J2DBGlobals;
import com.servoy.j2db.plugins.IClientPlugin;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.preference.PreferencePanel;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Settings;
import com.servoy.j2db.util.toolbar.IToolbarPanel;
import com.servoy.j2db.util.toolbar.Toolbar;
import com.servoy.j2db.util.toolbar.ToolbarButton;

/**
 * @author jblok
 */
public class SpellCheckerPlugin implements IClientPlugin, ActionListener
{
	private IClientPluginAccess application;
	private JButton check;
	private RapidSpellGUIDialog rapidGUI = null;
	private Window lastWindow = null;
	private SpellCheckProvider spellCheckProvider;

	/*
	 * @see IPlugin#load()
	 */
	public void load() throws PluginException
	{
		spellCheckProvider = new SpellCheckProvider(this);
	}

	/*
	 * @see IPlugin#initialize(IApplication)
	 */
	public void initialize(IClientPluginAccess app) throws PluginException
	{
		application = app;

		IToolbarPanel tbp = app.getToolbarPanel();
		if (tbp != null)
		{
			Toolbar toolBar = null;
			Toolbar textToolBar = tbp.getToolBar("text"); //$NON-NLS-1$
			if (textToolBar == null)
			{
				toolBar = tbp.createToolbar("spellcheck", "spellcheck"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			//else add to text toolbar if possible
			{
				textToolBar.addSeparator();
				toolBar = textToolBar;
			}

			if (toolBar != null)
			{
				Icon ico = getImage();
				check = new ToolbarButton(null, ico);
				check.addActionListener(this);
				toolBar.add(check);
				check.setEnabled(false);
			}
		}
	}

	/*
	 * @see IPlugin#unload()
	 */
	public void unload() throws PluginException
	{
		spellCheckProvider = null;
		application = null;
		lastWindow = null;
		if (rapidGUI != null)
		{
			rapidGUI.dispose();
			rapidGUI = null;
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		check(c);
	}


	void check(Component c)
	{
		if (c instanceof JTextComponent)
		{
			Window w = application.getCurrentWindow();
			if (w != null)
			{
				if (rapidGUI == null || w != lastWindow)
				{
					lastWindow = w;
					if (rapidGUI != null) rapidGUI.dispose();
					if (w instanceof JDialog)
					{
						rapidGUI = new RapidSpellGUIDialog((JDialog)w, false);
					}
					else if (w instanceof JFrame)
					{
						rapidGUI = new RapidSpellGUIDialog((JFrame)w, false);
					}
					else
					{
						return;//no valid window parent
					}
					rapidGUI.setName("SpellingCheckDialog"); // for saving the position in the settings file //$NON-NLS-1$
					if (!Settings.getInstance().loadBounds(rapidGUI))
					{
						rapidGUI.setLocationRelativeTo(w);
					}
					rapidGUI.addWindowListener(new WindowAdapter()
					{
						@Override
						public void windowClosing(WindowEvent e)
						{
							if (e.getWindow() == rapidGUI)
							{
								Settings.getInstance().saveBounds(rapidGUI);
							}
						}
					});

					rapidGUI.setIgnoreXML(false);//I still recommend you set ignore XML to false (otherwise it will treat < and > in the text as tag characters).
					rapidGUI.setIncludeUserDictionaryInSuggestions(true);
					File dir = new File(System.getProperty("user.home"), J2DBGlobals.CLIENT_LOCAL_DIR); //$NON-NLS-1$
					if (!dir.exists()) dir.mkdirs();
					rapidGUI.setUserDictionaryFile(new File(dir, "user_spell_check.dict")); //$NON-NLS-1$
				}
				rapidGUI.toFront();
				rapidGUI.check((JTextComponent)c);
				c.requestFocus(); // to make sure servoy sees the new values
			}
		}
	}

	public Properties getProperties()
	{
		Properties props = new Properties();
		props.put(DISPLAY_NAME, "Spellcheck"); //$NON-NLS-1$
		return props;
	}

	/*
	 * @see IPlugin#getPreferencePanels()
	 */
	public PreferencePanel[] getPreferencePanels()
	{
		return null;
	}

	/*
	 * @see IPlugin#getName()
	 */
	public String getName()
	{
		return "spellcheck"; //$NON-NLS-1$
	}

	/*
	 * @see IPlugin#getScriptObject()
	 */
	public IScriptObject getScriptObject()
	{
		return spellCheckProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.plugins.IClientPlugin#getImage()
	 */
	public Icon getImage()
	{
		URL iconUrl = this.getClass().getResource("images/spell.gif"); //$NON-NLS-1$
		if (iconUrl != null)
		{
			return new ImageIcon(iconUrl);
		}
		else
		{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals("solution") && check != null) //$NON-NLS-1$
		{
			check.setEnabled(evt.getNewValue() != null);
		}
	}

}
