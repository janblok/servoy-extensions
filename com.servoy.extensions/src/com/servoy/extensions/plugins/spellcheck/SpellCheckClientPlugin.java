package com.servoy.extensions.plugins.spellcheck;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.text.JTextComponent;

import com.servoy.j2db.plugins.IClientPlugin;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.preference.PreferencePanel;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Settings;
import com.servoy.j2db.util.Utils;
import com.servoy.j2db.util.toolbar.IToolbarPanel;
import com.servoy.j2db.util.toolbar.Toolbar;
import com.servoy.j2db.util.toolbar.ToolbarButton;

public class SpellCheckClientPlugin implements IClientPlugin, ActionListener
{
	private IClientPluginAccess application;
	private JButton check;
	private SpellCheckerGUI gui = null;
	private Window lastWindow = null;
	private SpellCheckClientProvider spellCheckProvider;
	private SpellResult spellResponse = null;
	private JTextComponent checkedComponent = null;

	/*
	 * @see IPlugin#load()
	 */
	public void load() throws PluginException
	{
		spellCheckProvider = new SpellCheckClientProvider(this);
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
			application.getSettings().setProperty("plugin.spellcheck.googleServiceProvider", "false"); //$NON-NLS-1$//$NON-NLS-2$
			SpellCheckerPreferencePanel.setDesiredLanguage(SpellCheckerUtils.DEFAULT);
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
		if (gui != null)
		{
			gui.dispose();
			gui = null;
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
				if (gui == null || w != lastWindow)
				{
					lastWindow = w;
					if (gui != null) gui.dispose();
					if (w instanceof JDialog)
					{
						gui = new SpellCheckerGUI(this, (JDialog)w, true);
					}
					else if (w instanceof JFrame)
					{
						gui = new SpellCheckerGUI(this, (JFrame)w, true);
					}
					else
					{
						return;//no valid window parent
					}
					gui.setName("SpellingCheckDialog"); //$NON-NLS-1$
					if (!Settings.getInstance().loadBounds(gui))
					{
						gui.setLocationRelativeTo(w);
					}
					gui.addWindowListener(new WindowAdapter()
					{
						@Override
						public void windowClosing(WindowEvent e)
						{
							if (e.getWindow() == gui)
							{
								Settings.getInstance().saveBounds(gui);
							}
						}
					});
				}

				checkedComponent = (JTextComponent)c;
				this.check(checkedComponent);
				//if we don't have to check we don't pop-up at all
				//NOTE: if we put this code before the check, the GUI unnecessarily shows up (has no real thing to display)
				if (gui.hasAtLeastOneSpellEvent())
				{
					checkedComponent.requestFocus(); // to make sure servoy sees the new values
					gui.toFront();
					gui.setVisible(true);
				}
			}
		}
	}

	public void check(JTextComponent c)
	{
		gui.removeEventsAndGuiForm();

		//reset caret position
		checkedComponent.setCaretPosition(0);

		String text = c.getText();
		String strUrl = null;
		try
		{
			if (Utils.getAsBoolean(application.getSettings().getProperty("plugin.spellcheck.googleServiceProvider"))) //$NON-NLS-1$ 
			{
				String selected = SpellCheckerPreferencePanel.getDesiredLanguage();
				String language = GoogleSpellUtils.getBasicLanguageName(selected);
				strUrl = "https://www.google.com/tbproxy/spell?lang=" + language + "&hl=" + language; //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				URL url = getPluginAccess().getServerURL();
				URL serviceURL = new URL(url, "/servoy-service/" + SpellCheckServerPlugin.WEBSERVICE_NAME); //$NON-NLS-1$
				strUrl = serviceURL.toString();
			}

			ServiceHandler serviceHandler = new ServiceHandler(strUrl);

			String[] words = text.split("[\\p{Space}\\p{Punct}]+"); //$NON-NLS-1$
			for (String eachWord : words)
			{
				String xmlString = serviceHandler.handleTextSpellChecking(eachWord);
				ResponseSAXParser.getInstance().parseXMLString(xmlString);
				spellResponse = ResponseSAXParser.getInstance().getResponse();
				if (spellResponse.getSpellCorrections().size() <= 0) continue;
				SpellCheckEvent spellEvent = new SpellCheckEvent(eachWord, spellResponse.getSpellCorrections());
				gui.check(spellEvent);
			}
			gui.setTheFirstSpellEvent();
		}
		catch (MalformedURLException e1)
		{
			e1.printStackTrace();
		}

	}

	public boolean fireAndHandleEvent(SpellCheckEvent event)
	{
		//fireSpellCheckEvent(event);
		String word = event.getInvalidWord();
		String t = checkedComponent.getText();

		//Work out what to do in response to the event.
		switch (event.getAction())
		{
			case SpellCheckEvent.INITIAL :
				break;
			case SpellCheckEvent.IGNORE : //SKIP
				break;
			case SpellCheckEvent.REPLACE :
				checkedComponent.replaceSelection(event.getReplaceWord());
				if (checkedComponent.getCaretPosition() >= t.length() - 1) checkedComponent.setCaretPosition(0);
				break;
			case SpellCheckEvent.REPLACEALL :
				//just a check to make sure we don't loop around forever
				if (word.equals(event.getReplaceWord())) break;
				//replace all occurences of word, that is why we start from the begining of the text
				checkedComponent.replaceSelection(event.getReplaceWord());
				t = checkedComponent.getText();
				int start = 0;
				int end = word.length();
				while ((start = t.indexOf(word)) >= 0)
				{
					checkedComponent.setSelectionStart(start);
					checkedComponent.setSelectionEnd(start + end);
					checkedComponent.replaceSelection(event.getReplaceWord());
					if (start + end >= t.length() - 1) checkedComponent.setCaretPosition(0);
					else checkedComponent.setCaretPosition(start + end);
					t = checkedComponent.getText();
				}
				if (checkedComponent.getCaretPosition() >= t.length() - 1) checkedComponent.setCaretPosition(0);
				break;
			case SpellCheckEvent.ADDTODICT :
				break;
			case SpellCheckEvent.CANCEL :
				break;
			default :
				throw new IllegalArgumentException("Unhandled case."); //$NON-NLS-1$
		}
		return false;
	}

	public SpellResult getSpellResponse()
	{
		return this.spellResponse;
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
		return new PreferencePanel[] { new SpellCheckerPreferencePanel(application) };
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

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals("solution") && check != null) //$NON-NLS-1$
		{
			check.setEnabled(evt.getNewValue() != null);
		}
	}

	public IClientPluginAccess getPluginAccess()
	{
		return application;
	}

	public JTextComponent getCheckedComponent()
	{
		return checkedComponent;
	}

}
