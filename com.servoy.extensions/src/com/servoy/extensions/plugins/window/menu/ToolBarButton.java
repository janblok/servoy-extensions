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
package com.servoy.extensions.plugins.window.menu;


import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.mozilla.javascript.Function;

import com.servoy.extensions.plugins.window.util.Utilities;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.toolbar.Toolbar;
import com.servoy.j2db.util.toolbar.ToolbarButton;

/**
 * @version v1.0 3-jan-2005 marceltrapman
 * @author marceltrapman
 */

public class ToolBarButton
{
	public static ToolbarButton addButton(String text, Function method, final Object[] arguments, Object icon, String tooltip, boolean enabled,
		boolean visible, final IClientPluginAccess pluginAccess)
	{
		ToolbarButton button = new ToolbarButton(text);
		button.setToolTipText(tooltip);
		button.setMargin(new Insets(2, 2, 2, 2));
		button.setIcon(Utilities.getImageIcon(icon));

		if (text == null || "".equals(text))
		{
			button.setPreferredSize(new Dimension(22, 22));
		}
		final FunctionDefinition functionDef = new FunctionDefinition(method);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ToolbarButton source = (ToolbarButton)(e.getSource());

				int length = 4;

				Object args[] = new Object[length];

				if ((arguments != null) && (arguments.length > 0))
				{
					length += arguments.length;

					args = new Object[length];

					for (int i = 0; i < arguments.length; i++)
					{
						args[i + 4] = arguments[i];
					}
				}

				args[0] = ((Toolbar)source.getParent()).getName();
				args[1] = ((Toolbar)source.getParent()).getDisplayName();
				args[2] = new Boolean(false);
				args[3] = source.getText();

				functionDef.executeAsync(pluginAccess, args);
			}
		});

		return button;
	}

	public static JCheckBox addCheckBox(String text, Function method, boolean selected, String tooltip, boolean enabled, boolean visible,
		final IClientPluginAccess pluginAccess)
	{
		JCheckBox box = new JCheckBox(text);
		box.setSelected(selected);
		box.setEnabled(enabled);
		box.setVisible(visible);
		box.setToolTipText(tooltip);

		final FunctionDefinition functionDef = new FunctionDefinition(method);
		box.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JCheckBox source = (JCheckBox)e.getSource();

				Object args[] = new Object[4];

				args[0] = ((Toolbar)source.getParent()).getName();
				args[1] = ((Toolbar)source.getParent()).getDisplayName();
				args[2] = new Boolean(source.isSelected());
				args[3] = source.getText();

				functionDef.executeAsync(pluginAccess, args);
			}
		});

		return box;
	}

	public static JComboBox addComboBox(Function method, int index, String[] arguments, String tooltip, boolean enabled, boolean visible,
		final IClientPluginAccess pluginAccess)
	{
		JComboBox box = new JComboBox(arguments);
		box.setPrototypeDisplayValue("XXXXXXXX"); // Set a desired width //$NON-NLS-1$
		box.setMaximumSize(box.getMinimumSize());
		box.setSelectedIndex(index);
		box.setEnabled(enabled);
		box.setVisible(visible);
		box.setToolTipText(tooltip);

		final FunctionDefinition functionDef = new FunctionDefinition(method);
		box.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JComboBox source = (JComboBox)e.getSource();

				Object args[] = new Object[4];

				args[0] = ((Toolbar)source.getParent()).getName();
				args[1] = ((Toolbar)source.getParent()).getDisplayName();
				args[2] = new Boolean(false);
				args[3] = source.getSelectedItem();

				try
				{
					functionDef.executeAsync(pluginAccess, args);
				}
				catch (Exception e1)
				{
					Debug.error(e1);
				}
			}
		});

		return box;
	}

	public static JTextField addField(Function method, String text, int length, String tooltip, boolean enabled, boolean visible,
		final IClientPluginAccess pluginAccess)
	{
		JTextField field = new JTextField(text);
		field.setEnabled(enabled);
		field.setVisible(visible);
		field.setToolTipText(tooltip);

		if (length == 0)
		{
			field.setColumns(8);
		}
		field.setColumns(length);

		final FunctionDefinition functionDef = new FunctionDefinition(method);
		field.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JTextField source = (JTextField)e.getSource();

				Object args[] = new Object[4];

				args[0] = ((Toolbar)source.getParent()).getName();
				args[1] = ((Toolbar)source.getParent()).getDisplayName();
				args[2] = new Boolean(false);
				args[3] = source.getText();

				functionDef.executeAsync(pluginAccess, args);
			}
		});
		return field;
	}
}