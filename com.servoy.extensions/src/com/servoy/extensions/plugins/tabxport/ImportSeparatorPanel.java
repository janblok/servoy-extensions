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
package com.servoy.extensions.plugins.tabxport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import com.servoy.j2db.Messages;
import com.servoy.j2db.util.wizard.IWizard;
import com.servoy.j2db.util.wizard.IWizardPanel;
import com.servoy.j2db.util.wizard.IWizardState;

/**
 * @author jblok
 */
public class ImportSeparatorPanel extends JPanel implements IWizardPanel
{
	private final JTable table_forPanel1;

	private final IWizard parent;
	private final IWizardState state;

	public ImportSeparatorPanel(IWizard parent, IWizardState state)
	{
		this.parent = parent;
		this.state = state;
		setName("SeparatorPanel"); //$NON-NLS-1$
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());
		// need to have a final local reference to the data vector in an enclosing block
		table_forPanel1 = new JTable();
		table_forPanel1.setPreferredScrollableViewportSize(new Dimension(500, 70));

		//Create the scroll pane and add the table to it. 
		JScrollPane scrollPane = new JScrollPane(table_forPanel1);

		JRadioButton useSeparator = new JRadioButton(Messages.getString("servoy.plugin.tabxport.useSeparator")); //$NON-NLS-1$
		JRadioButton useFixedWidth = new JRadioButton(Messages.getString("servoy.plugin.tabxport.useFixedWidth")); //$NON-NLS-1$
		ButtonGroup delimButtonGroup = new ButtonGroup();

		// Lay out topmost Part pane
		JPanel formatPanel = new JPanel();
		formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.Y_AXIS));
		formatPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("servoy.plugin.tabxport.format"))); //$NON-NLS-1$
		formatPanel.add(useSeparator);
		formatPanel.add(useFixedWidth);
		useFixedWidth.setEnabled(false);
		delimButtonGroup.add(useSeparator);
		delimButtonGroup.add(useFixedWidth);
		useSeparator.setSelected(true);

		//Lay out the master content pane.
		add(formatPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	class myTAbleModel extends AbstractTableModel
	{
		private final Vector localData;

		myTAbleModel(Vector vector)
		{
			localData = vector;
		}

		@Override
		public String getColumnName(int col)
		{
			return Messages.getString("servoy.plugin.tabxport.linesFromFile"); //$NON-NLS-1$
		}

		public int getRowCount()
		{
			return localData.size();
		}

		public int getColumnCount()
		{
			return 1;
		}

		public Object getValueAt(int row, int col)
		{
			return localData.elementAt(row);
		}

		@Override
		public boolean isCellEditable(int row, int col)
		{
			return false;
		}

		@Override
		public void setValueAt(Object value, int row, int col)
		{
		}
	}

	public String getNextPanelName()
	{
		return "SeparatorTypePanel"; //$NON-NLS-1$
	}

	public boolean isDone()
	{
		return true;
	}

	public Runnable needsToRunFirst(boolean forward)
	{
		return new Runnable()
		{
			public void run()
			{
				try
				{
					parent.blockGUI(Messages.getString("servoy.plugin.import.status.organizingData")); //$NON-NLS-1$
					Vector vector = (Vector)state.getProperty("lines"); //$NON-NLS-1$
					table_forPanel1.setModel(new myTAbleModel(vector));
				}
				finally
				{
					parent.releaseGUI();
				}
			}
		};
	}
}
