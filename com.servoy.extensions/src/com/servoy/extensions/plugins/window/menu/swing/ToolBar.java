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
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.toolbar.IToolbarPanel;
import com.servoy.j2db.util.toolbar.Toolbar;
import com.servoy.j2db.util.toolbar.ToolbarButton;
import com.servoy.j2db.util.toolbar.ToolbarToggleButton;

/**
 * @author marceltrapman
 */
@SuppressWarnings("nls")
public class ToolBar implements IToolBar, IScriptObject
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
			_application.getWindow(null).validate();
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

	public String[] getParameterNames(String methodName)
	{
		if ("addButton".equals(methodName))
		{
			return new String[] { "text", "method", "[arguments", "[icon", "[tooltip", "[enabled", "[visible]]]]]" };
		}
		else if ("addComboBox".equals(methodName))
		{
			return new String[] { "method", "index", "input", "[tooltip", "[enabled", "[visible]]]" };
		}
		else if ("addCheckBox".equals(methodName))
		{
			return new String[] { "text", "method", "[selected", "[tooltip", "[enabled", "[visible]]]]" };
		}
		else if ("addField".equals(methodName))
		{
			return new String[] { "method", "text", "[tooltip", "[enabled", "[visible]]]" };
		}
		else if ("addSeparator".equals(methodName))
		{
			return new String[] { };
		}
		else if ("removeItem".equals(methodName))
		{
			return new String[] { "index" };
		}
		else if ("selectCheckBox".equals(methodName))
		{
			return new String[] { "index", "boolean" };
		}
		else if ("selectComboBox".equals(methodName))
		{
			return new String[] { "index", "rowindex" };
		}
		else if ("setFieldText".equals(methodName))
		{
			return new String[] { "index", "text" };
		}
		else if ("removeAllItems".equals(methodName))
		{
			return new String[] { };
		}
		else if ("validate".equals(methodName))
		{
			return new String[] { };
		}
		else if ("getItemIndexByText".equals(methodName))
		{
			return new String[] { "name" };
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("addButton".equals(methodName) || "addSeparator".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// get the toolbar at the panel by name\n");
			sample.append("var toolbar = plugins.window.getToolBar(\"toolbar_0\");\n");
			sample.append("\n");
			sample.append("// add a button with a text and a method\n");
			sample.append("toolbar.addButton(\"button\", feedback_button);\n");
			sample.append("\n");
			sample.append("// add an input array to the button for feedback in the selected method\n");
			sample.append("toolbar.addButton(\"button\", feedback_button, [1, \"2\", \"three\"]);\n");
			sample.append("\n");
			sample.append("// add an icon to the button\n");
			sample.append("toolbar.addButton(\"button\", feedback_button, [1, \"2\", \"three\"], \"media:///yourimage.gif\");\n");
			sample.append("\n");
			sample.append("// add a tooltip to the button\n");
			sample.append("toolbar.addButton(\"button\", feedback_button, [1, \"2\", \"three\"], \"media:///yourimage.gif\", \"tooltip.\");\n");
			sample.append("\n");
			sample.append("// show only an icon on the button and disable the button\n");
			sample.append("toolbar.addButton(null, feedback_button, [1, \"2\", \"three\"], \"media:///yourimage.gif\", \"tooltip.\", false);\n");
			sample.append("\n");
			sample.append("// add a separator\n");
			sample.append("toolbar.addSeparator();\n");
			sample.append("\n");
			sample.append("// make the button non visible\n");
			sample.append("toolbar.addButton(null, feedback_button, [1, \"2\", \"three\"], \"media:///yourimage.gif\", \"tooltip.\",true, false);\n");
			sample.append("\n");
			sample.append("// and validate the changes\n");
			sample.append("// to make them know to the user interface)\n");
			sample.append("toolbar.validate();\n");
			return sample.toString();
		}
		else if ("addComboBox".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// add a toolbar with a name and internal name at the given row index\n");
			sample.append("var toolbar = plugins.window.addToolBar(\"toolbar_2\", \"toolbar_2\", 3);\n");
			sample.append("\n");
			sample.append("// add a combobox with the attached method, selected index and input (list) array\n");
			sample.append("toolbar.addComboBox(feedback_button, 0, [\"input\",\"array\",\"combobox\",1]);\n");
			sample.append("\n");
			sample.append("// add a tooltip to the combobox\n");
			sample.append("toolbar.addComboBox(feedback_button, 1, [\"input\",\"array\",\"combobox\",2], \"tooltip\");\n");
			sample.append("\n");
			sample.append("// add a separator\n");
			sample.append("toolbar.addSeparator();\n");
			sample.append("\n");
			sample.append("// disable the combobox\n");
			sample.append("toolbar.addComboBox(feedback_button, 2, [\"input\",\"array\",\"combobox\",3], \"tooltip\",false);\n");
			sample.append("\n");
			sample.append("// make the combobox non visible\n");
			sample.append("toolbar.addComboBox(feedback_button, 3, [\"input\",\"array\",\"combobox\",4], \"tooltip\",false, false);\n");
			sample.append("\n");
			sample.append("// and validate the changes\n");
			sample.append("// to make them know to the user interface)\n");
			sample.append("toolbar.validate();\n");
			return sample.toString();
		}
		else if ("addCheckBox".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// add a toolbar with a name and internal name\n");
			sample.append("var toolbar = plugins.window.addToolBar(\"toolbar_1\", \"toolbar_1\");\n");
			sample.append("\n");
			sample.append("// add a checkbox with a text and a method\n");
			sample.append("toolbar.addCheckBox(\"checkbox\", feedback_button);\n");
			sample.append("\n");
			sample.append("// add an checkbox and set it's state to selected (not selected by default)\n");
			sample.append("toolbar.addCheckBox(\"checkbox\", feedback_button, true);\n");
			sample.append("\n");
			sample.append("// add a tooltip to the checkbox\n");
			sample.append("toolbar.addCheckBox(\"checkbox\", feedback_button, false, \"tooltip\");\n");
			sample.append("\n");
			sample.append("// add a separator\n");
			sample.append("toolbar.addSeparator();\n");
			sample.append("\n");
			sample.append("// disable the checkbox and select it\n");
			sample.append("toolbar.addCheckBox(\"checkbox\", feedback_button, true, \"tooltip\",false);\n");
			sample.append("\n");
			sample.append("// make the button non visible\n");
			sample.append("toolbar.addCheckBox(\"checkbox\", feedback_button, false, \"tooltip\",false, false);\n");
			sample.append("\n");
			sample.append("// and validate the changes\n");
			sample.append("// to make them know to the user interface)\n");
			sample.append("toolbar.validate();\n");
			return sample.toString();
		}
		else if ("addField".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// add a toolbar with a name and internal name at the given row index\n");
			sample.append("var toolbar = plugins.window.addToolBar(\"toolbar_3\", \"toolbar_3\", 4);\n");
			sample.append("\n");
			sample.append("// add a field with the attached method and a default text\n");
			sample.append("toolbar.addField(feedback_button, null);\n");
			sample.append("\n");
			sample.append("// add a separator\n");
			sample.append("toolbar.addSeparator();\n");
			sample.append("\n");
			sample.append("// set the length of the field. \n");
			sample.append("// default length = 8 when length is not set or set to 0\n");
			sample.append("toolbar.addField(feedback_button, \"field\", 0, \"tooltip\");\n");
			sample.append("\n");
			sample.append("// add a separator\n");
			sample.append("toolbar.addSeparator();\n");
			sample.append("\n");
			sample.append("// add a tooltip to the field\n");
			sample.append("toolbar.addField(feedback_button, \"field\", 10, \"tooltip\");\n");
			sample.append("\n");
			sample.append("// add a separator\n");
			sample.append("toolbar.addSeparator();\n");
			sample.append("\n");
			sample.append("// disable the field\n");
			sample.append("toolbar.addField(feedback_button, null, 5, \"tooltip\",false);\n");
			sample.append("\n");
			sample.append("// add a separator\n");
			sample.append("toolbar.addSeparator();\n");
			sample.append("\n");
			sample.append("// make the field non visible\n");
			sample.append("toolbar.addField(feedback_button, \"field\", 0, \"tooltip\",false, false);\n");
			sample.append("\n");
			sample.append("// and validate the changes\n");
			sample.append("// to make them know to the user interface)\n");
			sample.append("toolbar.validate();\n");
			return sample.toString();
		}
		else if ("removeItem".equals(methodName) || "validate".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// get the toolbar at the panel by name\n");
			sample.append("var toolbar = plugins.window.getToolBar(\"toolbar_0\");\n");
			sample.append("\n");
			sample.append("// remove the button, checkbox, combobox, separator or field from the toolbar\n");
			sample.append("// REMARK: the pitfall here is that the indexes start at position 1 here\n");
			sample.append("// position 0 is reserved for the toolbar handle!\n");
			sample.append("toolbar.removeItem(1);\n");
			sample.append("\n");
			sample.append("// and validate the changes\n");
			sample.append("// to make them know to the user interface)\n");
			sample.append("toolbar.validate();\n");
			return sample.toString();
		}
		else if ("enableItem".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// get the toolbar at the panel by name\n");
			sample.append("var toolbar = plugins.window.getToolBar(\"toolbar_1\");\n");
			sample.append("\n");
			sample.append("// enable/disable the selected item at the index\n");
			sample.append("// REMARK: the pitfall here is that the indexes start at position 1 here\n");
			sample.append("// position 0 is reserved for the toolbar handle!\n");
			sample.append("toolbar.enableItem(1, false);\n");
			return sample.toString();
		}
		else if ("visibleItem".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// get the toolbar at the panel by name\n");
			sample.append("var toolbar = plugins.window.getToolBar(\"toolbar_2\");\n");
			sample.append("\n");
			sample.append("// make the selected item at the index visible/invisible\n");
			sample.append("// REMARK: the pitfall here is that the indexes start at position 1 here\n");
			sample.append("// position 0 is reserved for the toolbar handle!\n");
			sample.append("toolbar.visibleItem(1, false);\n");
			return sample.toString();
		}
		else if ("selectCheckBox".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// get the toolbar at the panel by name\n");
			sample.append("var toolbar = plugins.window.getToolBar(\"toolbar_1\");\n");
			sample.append("\n");
			sample.append("// set the selection of the checkbox at the index\n");
			sample.append("// REMARK: the pitfall here is that the indexes start at position 1 here\n");
			sample.append("// position 0 is reserved for the toolbar handle!\n");
			sample.append("toolbar.selectCheckBox(1, false);\n");
			sample.append("toolbar.selectCheckBox(2, true);\n");
			return sample.toString();
		}
		else if ("selectComboBox".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// get the toolbar at the panel by name\n");
			sample.append("var toolbar = plugins.window.getToolBar(\"toolbar_2\");\n");
			sample.append("\n");
			sample.append("// set the selection of the combobox at the index\n");
			sample.append("// REMARK: the pitfall here is that the indexes start at position 1 here\n");
			sample.append("// position 0 is reserved for the toolbar handle!\n");
			sample.append("toolbar.selectComboBox(1, 0);\n");
			sample.append("toolbar.selectComboBox(2, 0);\n");
			return sample.toString();
		}
		else if ("setFieldText".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// get the toolbar at the panel by name\n");
			sample.append("var toolbar = plugins.window.getToolBar(\"toolbar_3\");\n");
			sample.append("\n");
			sample.append("// set the text of the field at the index\n");
			sample.append("// REMARK: the pitfall here is that the indexes start at position 1 here\n");
			sample.append("// position 0 is reserved for the toolbar handle!\n");
			sample.append("toolbar.setFieldText(1, \"new text 1\");\n");
			sample.append("toolbar.setFieldText(2, \"new text 2\");\n");
			return sample.toString();
		}
		else if ("removeAllItems".equals(methodName))
		{
			StringBuilder sample = new StringBuilder();
			sample.append("// get the toolbar at the panel by name\n");
			sample.append("var toolbar = plugins.window.getToolBar(\"toolbar_0\");\n");
			sample.append("\n");
			sample.append("// remove all buttons from the toolbar\n");
			sample.append("toolbar.removeAllItems();\n");
			return sample.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("addButton".equals(methodName))
		{
			return "Add a Button to the toolbar.";
		}
		if ("addComboBox".equals(methodName))
		{
			return "Add a ComboBox to the toolbar.";
		}
		if ("addField".equals(methodName))
		{
			return "Add a Field to the toolbar.";
		}
		if ("addCheckBox".equals(methodName))
		{
			return "Add a CheckBox to the toolbar.";
		}
		if ("addSeparator".equals(methodName))
		{
			return "Add a Separator to the toolbar.";
		}
		if ("removeItem".equals(methodName))
		{
			return "Remove a Button, CheckBox, ComboBox from the toolbar.";
		}
		if ("selectCheckBox".equals(methodName))
		{
			return "Set the CheckBox selection.";
		}
		if ("selectComboBox".equals(methodName))
		{
			return "Select a row of the ComboBox via the index.";
		}
		if ("setFieldText".equals(methodName))
		{
			return "Set a (default) text of the field at the given index.";
		}
		if ("removeAllItems".equals(methodName))
		{
			return "Remove all Buttons, Checkboxes etc. from the toolbar.";
		}
		if ("validate".equals(methodName))
		{
			return "You need to call this method after adding or removing items to/from the toolbar.";
		}
		if ("getItemIndexByText".equals(methodName))
		{
			return "Retrieve the index of the item by text.";
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	public void js_addButton(String text, Function method) throws PluginException
	{
		js_addButton(text, method, null, null, null, true, true);
	}

	public void js_addButton(String text, Function method, Object[] arguments) throws PluginException
	{
		js_addButton(text, method, arguments, null, null, true, true);
	}

	public void js_addButton(String text, Function method, Object[] arguments, Object icon) throws PluginException
	{
		js_addButton(text, method, arguments, icon, null, true, true);
	}

	public void js_addButton(String text, Function method, Object[] arguments, Object icon, String tooltip) throws PluginException
	{
		js_addButton(text, method, arguments, icon, tooltip, true, true);
	}

	public void js_addButton(String text, Function method, Object[] arguments, Object icon, String tooltip, boolean enabled) throws PluginException
	{
		js_addButton(text, method, arguments, icon, tooltip, enabled, true);
	}

	public void js_addButton(String text, Function method, Object[] arguments, Object icon, String tooltip, boolean enabled, boolean visible)
		throws PluginException
	{
		if (_toolBar != null)
		{
			_toolBar.add(ToolBarButton.addButton(text, method, arguments, icon, tooltip, enabled, visible, _application));
		}
		else if (_jToolBar != null)
		{
			_jToolBar.add(ToolBarButton.addButton(text, method, arguments, icon, tooltip, enabled, visible, _application));
		}
	}

	public void js_addCheckBox(String text, Function method) throws PluginException
	{
		js_addCheckBox(text, method, false, null, true, true);
	}

	public void js_addCheckBox(String text, Function method, boolean selected) throws PluginException
	{
		js_addCheckBox(text, method, selected, null, true, true);
	}

	public void js_addCheckBox(String text, Function method, boolean selected, String tooltip) throws PluginException
	{
		js_addCheckBox(text, method, selected, tooltip, true, true);
	}

	public void js_addCheckBox(String text, Function method, boolean selected, String tooltip, boolean enabled) throws PluginException
	{
		js_addCheckBox(text, method, selected, tooltip, enabled, true);
	}

	public void js_addCheckBox(String text, Function method, boolean selected, String tooltip, boolean enabled, boolean visible) throws PluginException
	{
		if (_toolBar != null)
		{
			_toolBar.add(ToolBarButton.addCheckBox(text, method, selected, tooltip, enabled, visible, _application));
		}
		else if (_jToolBar != null)
		{
			_jToolBar.add(ToolBarButton.addCheckBox(text, method, selected, tooltip, enabled, visible, _application));
		}
	}

	public void js_addComboBox(Function method, int index, String[] arguments) throws PluginException
	{
		js_addComboBox(method, index, arguments, null, true, true);
	}

	public void js_addComboBox(Function method, int index, String[] arguments, String tooltip) throws PluginException
	{
		js_addComboBox(method, index, arguments, tooltip, true, true);
	}

	public void js_addComboBox(Function method, int index, String[] arguments, String tooltip, boolean enabled) throws PluginException
	{
		js_addComboBox(method, index, arguments, tooltip, enabled, true);
	}

	public void js_addComboBox(Function method, int index, String[] arguments, String tooltip, boolean enabled, boolean visible) throws PluginException
	{
		if (_toolBar != null)
		{
			_toolBar.add(ToolBarButton.addComboBox(method, index, arguments, tooltip, enabled, visible, _application));
		}
		else if (_jToolBar != null)
		{
			_jToolBar.add(ToolBarButton.addComboBox(method, index, arguments, tooltip, enabled, visible, _application));
		}
	}

	public void js_addField(Function method, String text) throws PluginException
	{
		js_addField(method, text, 8, null, true, true);
	}

	public void js_addField(Function method, String text, int length) throws PluginException
	{
		js_addField(method, text, 8, null, true, true);
	}

	public void js_addField(Function method, String text, int length, String tooltip) throws PluginException
	{
		js_addField(method, text, length, tooltip, true, true);
	}

	public void js_addField(Function method, String text, int length, String tooltip, boolean enabled) throws PluginException
	{
		js_addField(method, text, length, tooltip, enabled, true);
	}

	public void js_addField(Function method, String text, int length, String tooltip, boolean enabled, boolean visible) throws PluginException
	{
		if (_toolBar != null)
		{
			_toolBar.add(ToolBarButton.addField(method, text, length, tooltip, enabled, visible, _application));
		}
		else if (_jToolBar != null)
		{
			_jToolBar.add(ToolBarButton.addField(method, text, length, tooltip, enabled, visible, _application));
		}
	}

	public void js_addSeparator()
	{
		if (_toolBar != null)
		{
			_toolBar.addSeparator();
		}
		else if (_jToolBar != null)
		{
			_jToolBar.addSeparator();
		}
	}

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
		}
		else if (_jToolBar != null)
		{
			_jToolBar.remove(index);
		}
	}

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

		_application.getWindow(null).validate();
	}

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
		}
		else if (_jToolBar != null)
		{
			_jToolBar.remove(index);
		}
	}

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
	}

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
	}

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
	}

	public void js_validate()
	{
		_toolBar.validate();

		_application.getWindow(null).validate();
	}

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
	}
}