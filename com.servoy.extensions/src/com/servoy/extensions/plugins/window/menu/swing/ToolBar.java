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
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JToolBar.Separator;

import org.mozilla.javascript.Function;

import com.servoy.extensions.plugins.window.menu.IToolBar;
import com.servoy.extensions.plugins.window.menu.ToolBarButton;
import com.servoy.j2db.Messages;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.toolbar.IToolbarPanel;
import com.servoy.j2db.util.toolbar.Toolbar;
import com.servoy.j2db.util.toolbar.ToolbarButton;
import com.servoy.j2db.util.toolbar.ToolbarToggleButton;

/**
 * @author marceltrapman
 */
@SuppressWarnings("nls")
@ServoyDocumented
public class ToolBar implements IToolBar, IReturnedTypesProvider, IScriptable
{
	public static JToolBar addServoyToolBar(IClientPluginAccess app, JComponent pane, String name)
	{
		Toolbar toolbar = app.getToolbarPanel().getToolBar(name);
		JToolBar nToolbar = new JToolBar("_" + name);
		nToolbar.setUI(toolbar.getUI());
		nToolbar.setSize(toolbar.getSize());
		nToolbar.setPreferredSize(toolbar.getPreferredSize());
		nToolbar.setMaximumSize(toolbar.getMaximumSize());
		nToolbar.setMinimumSize(toolbar.getMinimumSize());

		for (int i = 0; i < toolbar.getComponentCount(); i++)
		{
			try
			{
				final Component component = toolbar.getComponent(i);

				if (component instanceof ToolbarButton)
				{
					if (((ToolbarButton)component).getToolTipText() != null && ((ToolbarButton)component).getToolTipText().startsWith("more"))
					{
						continue;
					}

					AbstractButton button = new ToolbarButton(((ToolbarButton)component).getIcon());
					button.setUI(((AbstractButton)component).getUI());
					button.setActionCommand(((ToolbarButton)component).getActionCommand());
					button.setActionMap(((ToolbarButton)component).getActionMap());
					button.setModel(((ToolbarButton)component).getModel());
					button.setToolTipText(((ToolbarButton)component).getToolTipText());
					button.setSize(component.getSize());
					button.setPreferredSize(component.getPreferredSize());
					button.setMaximumSize(component.getMaximumSize());
					button.setText(null);
					// button.addActionListener(((ToolbarButton) component).getActionListeners()[0]);
					button.setEnabled(component.isEnabled());
					button.setBorder(((AbstractButton)component).getBorder());

					nToolbar.add(button);
				}
				else if (component instanceof ToolbarToggleButton)
				{
					ToolbarToggleButton button = new ToolbarToggleButton(((ToolbarToggleButton)component).getIcon());
					button.setUI(((AbstractButton)component).getUI());
					button.setActionCommand(((ToolbarToggleButton)component).getActionCommand());
					button.setActionMap(((ToolbarToggleButton)component).getActionMap());
					button.setModel(((ToolbarToggleButton)component).getModel());
					button.setToolTipText(((ToolbarToggleButton)component).getToolTipText());
					button.setSize(component.getSize());
					button.setPreferredSize(component.getPreferredSize());
					button.setMaximumSize(component.getMaximumSize());
					button.setText(null);
					button.setEnabled(component.isEnabled());
					button.setBorder(((AbstractButton)component).getBorder());

					nToolbar.add(button);
				}
				else if (component instanceof JComboBox)
				{
					final JComboBox button = new JComboBox();
					component.addPropertyChangeListener(new PropertyChangeListener()
					{
						public void propertyChange(PropertyChangeEvent e)
						{
							if (e.getPropertyName().equals("enabled") && e.getNewValue().equals(Boolean.TRUE))
							{
								if (button.getModel() == null || button.getModel().getSize() == 0)
								{
									button.setModel(((JComboBox)component).getModel());
								}
							}
							button.setEnabled(component.isEnabled());
						}
					});
					button.setUI(((JComboBox)component).getUI());
					// button.setAction(((JComboBox) component).getAction());
					// button.setActionCommand(((JComboBox) component).getActionCommand());
					// button.setActionMap(((JComboBox) component).getActionMap());
					button.setEditor(((JComboBox)component).getEditor());
					button.setModel(((JComboBox)component).getModel());
					button.setToolTipText(((JComboBox)component).getToolTipText());
					button.setSize(component.getSize());
					button.setPreferredSize(component.getPreferredSize());
					button.setMaximumSize(component.getMaximumSize());
					button.setSelectedIndex(((JComboBox)component).getSelectedIndex());
					button.setEnabled(component.isEnabled());
					button.setBorder(((JComboBox)component).getBorder());

					nToolbar.add(button);
				}
				else if (component instanceof Separator)
				{
					Separator separator = new Separator(((Separator)component).getSize());
					separator.setUI(((Separator)component).getUI());
					nToolbar.add(separator);
				}
				else
				{
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.LEFT);
		layout.setHgap(5);
		pane.setLayout(layout);

		// if (index == 0) {
		pane.add(nToolbar);// , BorderLayout.LINE_START, index);
		// } else {
		// pane.add(nToolbar);//, BorderLayout.AFTER_LINE_ENDS, index);
		// }

		pane.validate();

		return nToolbar;
	}

	public static void removeServoyToolBar(JComponent pane, String name)
	{
		for (int i = 0; i < pane.getComponentCount(); i++)
		{
			if (pane.getComponent(i).getName().equals("_" + name))
			{
				pane.remove(i);
			}
		}

		pane.repaint();// validate();
	}

	private IClientPluginAccess _application;

	private String _displayName;

	private String _internalName;

	private JToolBar _jToolBar = null;

	private Toolbar _toolBar = null;

	public ToolBar()
	{
	}// only used by script engine

	public ToolBar(IClientPluginAccess app, JToolBar bar)
	{
		_application = app;
		_internalName = bar.getName();
		_jToolBar = bar;
	}

	public ToolBar(IClientPluginAccess app, String name, String displayname, int row, boolean add, boolean remove) throws PluginException
	{
		if ((name == null) || (name == ""))
		{
			throw new PluginException("A new toolBar can only be retrieved or created by name!");
		}

		_application = app;
		_internalName = name;

		IToolbarPanel panel = _application.getToolbarPanel();

		if (add)
		{
			_toolBar = panel.getToolBar(_internalName);

			if (_toolBar == null)
			{
				if ((displayname == null) || (displayname == ""))
				{
					_displayName = "";

					_toolBar = panel.createToolbar(_internalName, "");
				}
				else
				{
					if (displayname.startsWith("i18n:"))
					{
						_displayName = Messages.getString(displayname.replaceFirst("i18n:", ""));
					}
					else
					{
						_displayName = displayname;
					}

					_toolBar = panel.createToolbar(_internalName, _displayName, row);
				}
			}
			else
			{
				throw new PluginException("Error: A toolbar with name '" + _internalName + "' already exists!");
			}
		}
		else if (remove)
		{
			panel.removeToolBar(_internalName);
			IRuntimeWindow runtimeWindow = _application.getRuntimeWindow(null);
			if (runtimeWindow instanceof ISmartRuntimeWindow)
			{
				((ISmartRuntimeWindow)runtimeWindow).getWindow().validate();
			}
		}
		else
		{
			_toolBar = panel.getToolBar(_internalName);
		}
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}

	/**
	 * Add a Button to the toolbar.
	 *
	 * @sample
	 * // create a new toolbar
	 * var toolbar = plugins.window.addToolBar('toolbar_0');
	 * // add a button with a text and a method
	 * toolbar.addButton("button", feedback_button);
	 * // add an input array to the button for feedback in the selected method
	 * toolbar.addButton("button", feedback_button, [1, "2", "three"]);
	 * // add an icon to the button
	 * toolbar.addButton("button", feedback_button, [1, "2", "three"], "media:///yourimage.gif");
	 * // add a tooltip to the button
	 * toolbar.addButton("button", feedback_button, [1, "2", "three"], "media:///yourimage.gif", "tooltip");
	 * // show only an icon on the button and disable the button
	 * toolbar.addButton(null, feedback_button, [1, "2", "three"], "media:///yourimage.gif", "tooltip", false);
	 * // make the button non visible
	 * toolbar.addButton(null, feedback_button, [1, "2", "three"], "media:///yourimage.gif", "tooltip", true, false);
	 * 
	 * @param text
	 * @param method
	 */
	public void js_addButton(String text, Function method) throws PluginException
	{
		js_addButton(text, method, null, null, null, true, true);
	}

	/**
	 * Add a Button to the toolbar.
	 *
	 * @sampleas js_addButton(String, Function)
	 * 
	 * @param text
	 * @param method
	 * @param arguments
	 */
	public void js_addButton(String text, Function method, Object[] arguments) throws PluginException
	{
		js_addButton(text, method, arguments, null, null, true, true);
	}

	/**
	 * Add a Button to the toolbar.
	 *
	 * @sampleas js_addButton(String, Function)
	 * 
	 * @param text
	 * @param method
	 * @param arguments
	 * @param icon
	 */
	public void js_addButton(String text, Function method, Object[] arguments, Object icon) throws PluginException
	{
		js_addButton(text, method, arguments, icon, null, true, true);
	}

	/**
	 * Add a Button to the toolbar.
	 *
	 * @sampleas js_addButton(String, Function)
	 * 
	 * @param text
	 * @param method
	 * @param arguments
	 * @param icon
	 * @param tooltip
	 */
	public void js_addButton(String text, Function method, Object[] arguments, Object icon, String tooltip) throws PluginException
	{
		js_addButton(text, method, arguments, icon, tooltip, true, true);
	}

	/**
	 * Add a Button to the toolbar.
	 *
	 * @sampleas js_addButton(String, Function)
	 * 
	 * @param text
	 * @param method
	 * @param arguments
	 * @param icon
	 * @param tooltip
	 * @param enabled
	 */
	public void js_addButton(String text, Function method, Object[] arguments, Object icon, String tooltip, boolean enabled) throws PluginException
	{
		js_addButton(text, method, arguments, icon, tooltip, enabled, true);
	}

	/**
	 * Add a Button to the toolbar.
	 *
	 * @sampleas js_addButton(String, Function)
	 * 
	 * @param text
	 * @param method
	 * @param arguments
	 * @param icon
	 * @param tooltip
	 * @param enabled
	 * @param visible
	 */
	public void js_addButton(String text, Function method, Object[] arguments, Object icon, String tooltip, boolean enabled, boolean visible)
		throws PluginException
	{
		if (_toolBar != null)
		{
			_toolBar.add(ToolBarButton.addButton(text, method, arguments, icon, tooltip, enabled, visible, _application));
			validate();
		}
		else if (_jToolBar != null)
		{
			_jToolBar.add(ToolBarButton.addButton(text, method, arguments, icon, tooltip, enabled, visible, _application));
			_jToolBar.validate();
		}
	}

	/**
	 * Add a CheckBox to the toolbar.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_1");
	 * // add a checkbox with a text and a method
	 * toolbar.addCheckBox("checkbox", feedback_checkbox);
	 * // add an checkbox and set it's state to selected (not selected by default)
	 * toolbar.addCheckBox("checkbox", feedback_checkbox, true);
	 * // add a tooltip to the checkbox
	 * toolbar.addCheckBox("checkbox", feedback_checkbox, false, "tooltip");
	 * // disable the checkbox and select it
	 * toolbar.addCheckBox("checkbox", feedback_checkbox, true, "tooltip", false);
	 * // make the checkbox non visible
	 * toolbar.addCheckBox("checkbox", feedback_checkbox, false, "tooltip", false, false);
	 * 
	 * @param text
	 * @param method
	 */
	public void js_addCheckBox(String text, Function method) throws PluginException
	{
		js_addCheckBox(text, method, false, null, true, true);
	}

	/**
	 * Add a CheckBox to the toolbar.
	 *
	 * @sampleas js_addCheckBox(String, Function)
	 * 
	 * @param text
	 * @param method
	 * @param selected
	 */
	public void js_addCheckBox(String text, Function method, boolean selected) throws PluginException
	{
		js_addCheckBox(text, method, selected, null, true, true);
	}

	/**
	 * Add a CheckBox to the toolbar.
	 *
	 * @sampleas js_addCheckBox(String, Function)
	 * 
	 * @param text
	 * @param method
	 * @param selected
	 * @param tooltip
	 */
	public void js_addCheckBox(String text, Function method, boolean selected, String tooltip) throws PluginException
	{
		js_addCheckBox(text, method, selected, tooltip, true, true);
	}

	/**
	 * Add a CheckBox to the toolbar.
	 *
	 * @sampleas js_addCheckBox(String, Function)
	 * 
	 * @param text
	 * @param method
	 * @param selected
	 * @param tooltip
	 * @param enabled
	 */
	public void js_addCheckBox(String text, Function method, boolean selected, String tooltip, boolean enabled) throws PluginException
	{
		js_addCheckBox(text, method, selected, tooltip, enabled, true);
	}

	/**
	 * Add a CheckBox to the toolbar.
	 *
	 * @sampleas js_addCheckBox(String, Function)
	 * 
	 * @param text
	 * @param method
	 * @param selected
	 * @param tooltip
	 * @param enabled
	 * @param visible
	 */
	public void js_addCheckBox(String text, Function method, boolean selected, String tooltip, boolean enabled, boolean visible) throws PluginException
	{
		if (_toolBar != null)
		{
			_toolBar.add(ToolBarButton.addCheckBox(text, method, selected, tooltip, enabled, visible, _application));
			validate();
		}
		else if (_jToolBar != null)
		{
			_jToolBar.add(ToolBarButton.addCheckBox(text, method, selected, tooltip, enabled, visible, _application));
			_jToolBar.validate();
		}
	}

	/**
	 * Add a ComboBox to the toolbar.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_2");
	 * // add a combobox with the attached method, selected index and input (list) array
	 * toolbar.addComboBox(feedback_combobox, 0, ["input","array","combobox",1]);
	 * // add a tooltip to the combobox
	 * toolbar.addComboBox(feedback_combobox, 1, ["input","array","combobox",2], "tooltip");
	 * // disable the combobox
	 * toolbar.addComboBox(feedback_combobox, 2, ["input","array","combobox",3], "tooltip", false);
	 * // make the combobox non visible
	 * toolbar.addComboBox(feedback_combobox, 3, ["input","array","combobox",4], "tooltip", false, false);
	 * 
	 * @param method
	 * @param index
	 * @param values
	 */
	public void js_addComboBox(Function method, int index, String[] values) throws PluginException
	{
		js_addComboBox(method, index, values, null, true, true);
	}

	/**
	 * Add a ComboBox to the toolbar.
	 *
	 * @sampleas js_addComboBox(Function, int, String[])
	 * 
	 * @param method
	 * @param index
	 * @param values
	 * @param tooltip
	 */
	public void js_addComboBox(Function method, int index, String[] values, String tooltip) throws PluginException
	{
		js_addComboBox(method, index, values, tooltip, true, true);
	}

	/**
	 * Add a ComboBox to the toolbar.
	 *
	 * @sampleas js_addComboBox(Function, int, String[])
	 * 
	 * @param method
	 * @param index
	 * @param values
	 * @param tooltip
	 * @param enabled
	 */
	public void js_addComboBox(Function method, int index, String[] values, String tooltip, boolean enabled) throws PluginException
	{
		js_addComboBox(method, index, values, tooltip, enabled, true);
	}

	/**
	 * Add a ComboBox to the toolbar.
	 *
	 * @sampleas js_addComboBox(Function, int, String[])
	 * 
	 * @param method
	 * @param index
	 * @param values
	 * @param tooltip
	 * @param enabled
	 * @param visible
	 */
	public void js_addComboBox(Function method, int index, String[] values, String tooltip, boolean enabled, boolean visible) throws PluginException
	{
		if (_toolBar != null)
		{
			_toolBar.add(ToolBarButton.addComboBox(method, index, values, tooltip, enabled, visible, _application));
			validate();
		}
		else if (_jToolBar != null)
		{
			_jToolBar.add(ToolBarButton.addComboBox(method, index, values, tooltip, enabled, visible, _application));
			_jToolBar.validate();
		}
	}

	/**
	 * Add a Field to the toolbar.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_3");
	 * // add a field with the attached method and a default text
	 * toolbar.addField(feedback_field, null);
	 * // set the length of the field. 
	 * // default length = 8 when length is not set or set to 0
	 * toolbar.addField(feedback_field, "field", 0, "tooltip");
	 * // add a tooltip to the field
	 * toolbar.addField(feedback_field, null, 10, "tooltip");
	 * // disable the field
	 * toolbar.addField(feedback_field, "field", 5, "tooltip", false);
	 * // make the field non visible
	 * toolbar.addField(feedback_field, "field", 0, "tooltip", false, false);
	 * 
	 * @param method
	 * @param text
	 */
	public void js_addField(Function method, String text) throws PluginException
	{
		js_addField(method, text, 8, null, true, true);
	}

	/**
	 * Add a Field to the toolbar.
	 *
	 * @sampleas js_addField(Function, String)
	 * 
	 * @param method
	 * @param text
	 * @param length
	 */
	public void js_addField(Function method, String text, int length) throws PluginException
	{
		js_addField(method, text, 8, null, true, true);
	}

	/**
	 * Add a Field to the toolbar.
	 *
	 * @sampleas js_addField(Function, String)
	 * 
	 * @param method
	 * @param text
	 * @param length
	 * @param tooltip
	 */
	public void js_addField(Function method, String text, int length, String tooltip) throws PluginException
	{
		js_addField(method, text, length, tooltip, true, true);
	}

	/**
	 * Add a Field to the toolbar.
	 *
	 * @sampleas js_addField(Function, String)
	 * 
	 * @param method
	 * @param text
	 * @param length
	 * @param tooltip
	 * @param enabled
	 */
	public void js_addField(Function method, String text, int length, String tooltip, boolean enabled) throws PluginException
	{
		js_addField(method, text, length, tooltip, enabled, true);
	}

	/**
	 * Add a Field to the toolbar.
	 *
	 * @sampleas js_addField(Function, String)
	 * 
	 * @param method
	 * @param text
	 * @param length
	 * @param tooltip
	 * @param enabled
	 * @param visible
	 */
	public void js_addField(Function method, String text, int length, String tooltip, boolean enabled, boolean visible) throws PluginException
	{
		if (_toolBar != null)
		{
			_toolBar.add(ToolBarButton.addField(method, text, length, tooltip, enabled, visible, _application));
			validate();
		}
		else if (_jToolBar != null)
		{
			_jToolBar.add(ToolBarButton.addField(method, text, length, tooltip, enabled, visible, _application));
			_jToolBar.validate();
		}
	}

	/**
	 * Add a Separator to the toolbar.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_0");
	 * // add a button 
	 * toolbar.addButton("button", feedback_button);
	 * // add a separator
	 * toolbar.addSeparator();
	 * // add a checkbox
	 * toolbar.addCheckBox("checkbox", feedback_checkbox);
	 */
	public void js_addSeparator()
	{
		if (_toolBar != null)
		{
			_toolBar.addSeparator();
			validate();
		}
		else if (_jToolBar != null)
		{
			_jToolBar.addSeparator();
			_jToolBar.validate();
		}
	}

	/**
	 * Enable/disable the item at the specified index.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_1");
	 * // add a checkbox
	 * toolbar.addCheckBox("checkbox", feedback_checkbox);
	 * // disable the button
	 * // REMARK: the pitfall here is that the indexes start at position 1 here
	 * // position 0 is reserved for the toolbar handle!
	 * toolbar.enableItem(1, false);
	 * 
	 * @param index
	 * @param enabled
	 */
	public void js_enableItem(int index, boolean enabled) throws PluginException
	{
		if (index < 1)
		{
			throw new PluginException("Buttons start at position 1");
		}
		else if (index >= _toolBar.getComponentCount())
		{
			throw new PluginException("A button with index " + index + " doesn't exist.");
		}

		if (_toolBar != null)
		{
			_toolBar.getComponentAtIndex(index).setEnabled(enabled);
			validate();
		}
		else if (_jToolBar != null)
		{
			_jToolBar.remove(index);
			_jToolBar.validate();
		}
	}

	/**
	 * Remove all Buttons, Checkboxes etc. from the toolbar.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_1");
	 * // add a button
	 * toolbar.addButton("button", feedback_button);
	 * // add a checkbox
	 * toolbar.addCheckBox("checkbox", feedback_checkbox);
	 * // remove all items from the toolbar
	 * toolbar.removeAllItems();
	 */
	public void js_removeAllItems()
	{
		if (_toolBar != null)
		{
			_toolBar.removeAll();
		}
		else if (_jToolBar != null)
		{
			_jToolBar.removeAll();
		}
		_toolBar.validate();

		IRuntimeWindow runtimeWindow = _application.getRuntimeWindow(null);
		if (runtimeWindow instanceof ISmartRuntimeWindow)
		{
			((ISmartRuntimeWindow)runtimeWindow).getWindow().validate();
		}
	}

	/**
	 * Remove a Button, CheckBox, ComboBox from the toolbar.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_1");
	 * // add a button
	 * toolbar.addButton("button", feedback_button);
	 * // add a checkbox
	 * toolbar.addCheckBox("checkbox", feedback_checkbox);
	 * // remove the first item (the button in this case) from the toolbar
	 * // REMARK: the pitfall here is that the indexes start at position 1 here
	 * // position 0 is reserved for the toolbar handle!
	 * toolbar.removeItem(1);
	 * 
	 * @param index
	 */
	public void js_removeItem(int index) throws PluginException
	{
		if (index < 1)
		{
			throw new PluginException("Buttons start at position 1");
		}
		else if (index >= _toolBar.getComponentCount())
		{
			throw new PluginException("A button with index " + index + " doesn't exist.");
		}

		if (_toolBar != null)
		{
			_toolBar.remove(index);
			validate();
		}
		else if (_jToolBar != null)
		{
			_jToolBar.remove(index);
			_jToolBar.validate();
		}
	}

	/**
	 * Set the CheckBox selection.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_1");
	 * // add a checkbox
	 * toolbar.addCheckBox("checkbox 1", feedback_checkbox);
	 * // add another checkbox
	 * toolbar.addCheckBox("checkbox 2", feedback_checkbox);
	 * // set the selection of the checkboxes
	 * // REMARK: the pitfall here is that the indexes start at position 1 here
	 * // position 0 is reserved for the toolbar handle!
	 * toolbar.selectCheckBox(1, false);
	 * toolbar.selectCheckBox(2, true);
	 * 
	 * @param index
	 * @param selected
	 */
	public void js_selectCheckBox(int index, boolean selected) throws PluginException
	{
		if (index < 1)
		{
			throw new PluginException("Buttons start at position 1");
		}
		else if (index >= _toolBar.getComponentCount())
		{
			throw new PluginException("A button with index " + index + " doesn't exist.");
		}

		JCheckBox box = null;

		try
		{
			if (_toolBar != null)
			{
				box = (JCheckBox)_toolBar.getComponent(index);
			}
			else if (_jToolBar != null)
			{
				box = (JCheckBox)_jToolBar.getComponent(index);
			}
		}
		catch (Exception e)
		{
			throw new PluginException(e.toString());
		}
		box.setSelected(selected);

		if (_toolBar != null) validate();
		else if (_jToolBar != null) _jToolBar.validate();
	}

	/**
	 * Select a row of the ComboBox via the index.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_1");
	 * // add a combobox
	 * toolbar.addComboBox(feedback_combobox, 1, ["one", "two", "three"]);
	 * // add another combobox
	 * toolbar.addComboBox(feedback_combobox, 2, [1, 2, 3, 4, 5]);
	 * // set the selection of the comboboxes
	 * // REMARK: the pitfall here is that the indexes start at position 1 here
	 * // position 0 is reserved for the toolbar handle!
	 * toolbar.selectComboBox(1, 0); // entry "one" will be selected in the first combobox
	 * toolbar.selectComboBox(2, 3); // entry 4 will be selected in the second combobox
	 * 
	 * @param index
	 * @param selection
	 */
	public void js_selectComboBox(int index, int selection) throws PluginException
	{
		if (index < 1)
		{
			throw new PluginException("Buttons start at position 1");
		}
		else if (index >= _toolBar.getComponentCount())
		{
			throw new PluginException("A button with index " + index + " doesn't exist.");
		}

		JComboBox box = null;

		try
		{
			if (_toolBar != null)
			{
				box = (JComboBox)_toolBar.getComponent(index);
			}
			else if (_jToolBar != null)
			{
				box = (JComboBox)_jToolBar.getComponent(index);
			}
		}
		catch (Exception e)
		{
			throw new PluginException(e.toString());
		}
		box.setSelectedIndex(selection);

		if (_toolBar != null) validate();
		else if (_jToolBar != null) _jToolBar.validate();
	}

	/**
	 * Set a (default) text of the field at the given index.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_1");
	 * // add a field
	 * toolbar.addField(feedback_field, "field one");
	 * // add another field
	 * toolbar.addField(feedback_field , "field_two");
	 * // set the text of the fields
	 * // REMARK: the pitfall here is that the indexes start at position 1 here
	 * // position 0 is reserved for the toolbar handle!
	 * toolbar.setFieldText(1, "new text 1");
	 * toolbar.setFieldText(2, "new text 2");
	 * 
	 * @param index
	 * @param text
	 */
	public void js_setFieldText(int index, String text) throws PluginException
	{
		if (index < 1)
		{
			throw new PluginException("Buttons start at position 1");
		}
		else if (index >= _toolBar.getComponentCount())
		{
			throw new PluginException("A button with index " + index + " doesn't exist.");
		}

		JTextField field = null;

		try
		{
			if (_toolBar != null)
			{
				field = (JTextField)_toolBar.getComponent(index);
			}
			else if (_jToolBar != null)
			{
				field = (JTextField)_jToolBar.getComponent(index);
			}
		}
		catch (Exception e)
		{
			throw new PluginException(e.toString());
		}
		field.setText(text);

		if (_toolBar != null) validate();
		else if (_jToolBar != null) _jToolBar.validate();
	}

	/**
	 * You need to call this method after adding or removing items to/from the toolbar.
	 *
	 * @sampleas js_removeItem(int)
	 */
	@Deprecated
	public void js_validate()
	{
		_toolBar.validate();

		IRuntimeWindow runtimeWindow = _application.getRuntimeWindow(null);
		if (runtimeWindow instanceof ISmartRuntimeWindow)
		{
			((ISmartRuntimeWindow)runtimeWindow).getWindow().validate();
		}
	}

	public void validate()
	{
		_toolBar.validate();

		IRuntimeWindow runtimeWindow = _application.getRuntimeWindow(null);
		if (runtimeWindow instanceof ISmartRuntimeWindow)
		{
			((ISmartRuntimeWindow)runtimeWindow).getWindow().validate();
		}
	}

	/**
	 * Make the item at the specified index visible/invisible.
	 *
	 * @sample
	 * // add a toolbar
	 * var toolbar = plugins.window.addToolBar("toolbar_1");
	 * // add a button
	 * toolbar.addButton("button", feedback_button);
	 * // add a checkbox
	 * toolbar.addCheckBox("checkbox", feedback_checkbox);
	 * // make the first item (the button) invisible
	 * // REMARK: the pitfall here is that the indexes start at position 1 here
	 * // position 0 is reserved for the toolbar handle!
	 * toolbar.visibleItem(1, false);
	 * 
	 * @param index
	 * @param visible
	 */
	public void js_visibleItem(int index, boolean visible) throws PluginException
	{
		if (index < 1)
		{
			throw new PluginException("Buttons start at position 1");
		}
		else if (index >= _toolBar.getComponentCount())
		{
			throw new PluginException("A button with index " + index + " doesn't exist.");
		}

		if (_toolBar != null)
		{
			_toolBar.getComponentAtIndex(index).setVisible(visible);
		}
		else if (_jToolBar != null)
		{
			_jToolBar.remove(index);
		}

		if (_toolBar != null) validate();
		else if (_jToolBar != null) _jToolBar.validate();
	}
}