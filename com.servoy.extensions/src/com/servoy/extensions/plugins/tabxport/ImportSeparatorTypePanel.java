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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import com.servoy.j2db.Messages;
import com.servoy.j2db.util.model.SeparatedASCIIImportTableModel;
import com.servoy.j2db.util.wizard.IWizard;
import com.servoy.j2db.util.wizard.IWizardPanel;
import com.servoy.j2db.util.wizard.IWizardState;

/**
 * @author jblok
 */
public class ImportSeparatorTypePanel extends JPanel implements ActionListener, IWizardPanel
{
	protected SeparatedASCIIImportTableModel tableModel;

	protected ButtonGroup delimButtonGroup = new ButtonGroup();
	protected JRadioButton tabRadio = new JRadioButton(Messages.getString("servoy.plugin.tabxport.separator.tab")); //$NON-NLS-1$
	protected JRadioButton semicolonRadio = new JRadioButton(Messages.getString("servoy.plugin.tabxport.separator.semicolon")); //$NON-NLS-1$
	protected JRadioButton commaRadio = new JRadioButton(Messages.getString("servoy.plugin.tabxport.separator.comma")); //$NON-NLS-1$
	protected JRadioButton spaceRadio = new JRadioButton(Messages.getString("servoy.plugin.tabxport.separator.space")); //$NON-NLS-1$
	protected JRadioButton otherRadio = new JRadioButton(Messages.getString("servoy.plugin.tabxport.separator.other")); //$NON-NLS-1$
	protected JTextField otherSeparator = new JTextField(2);

	protected JCheckBox headerRows = new JCheckBox(Messages.getString("servoy.plugin.import.rowContainsFieldnames")); //$NON-NLS-1$
	protected JComboBox textQualifierCombo = new JComboBox();

	protected boolean canceled = true;
	protected boolean next = true;
	private final JTable table;

	private final IWizard parent;
	private final IWizardState state;

	public ImportSeparatorTypePanel(IWizard parent, IWizardState state)
	{
		this.parent = parent;
		this.state = state;
		setName("SeparatorTypePanel"); //$NON-NLS-1$
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());

		table = new JTable();

		table.setPreferredScrollableViewportSize(new Dimension(500, 70));

		//Create the scroll pane and add the table to it. 
		JScrollPane scrollPane = new JScrollPane(table);

		// Lay out topmost Part pane
		JPanel formatPanel = new JPanel();
		formatPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		formatPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("servoy.plugin.tabxport.choose.separator"))); //$NON-NLS-1$
		formatPanel.add(tabRadio);
		tabRadio.setActionCommand("separatorIsTab"); //$NON-NLS-1$
		tabRadio.addActionListener(this);
		formatPanel.add(commaRadio);
		commaRadio.setActionCommand("separatorIsComma"); //$NON-NLS-1$
		commaRadio.addActionListener(this);
		formatPanel.add(semicolonRadio);
		semicolonRadio.setActionCommand("separatorIsSemicolon"); //$NON-NLS-1$
		semicolonRadio.addActionListener(this);
		formatPanel.add(spaceRadio);
		spaceRadio.setActionCommand("separatorIsSpace"); //$NON-NLS-1$
		spaceRadio.addActionListener(this);
		formatPanel.add(otherRadio);
		otherRadio.setActionCommand("separatorIsOther"); //$NON-NLS-1$
		otherRadio.addActionListener(this);
		formatPanel.add(otherSeparator);
		delimButtonGroup.add(tabRadio);
		delimButtonGroup.add(semicolonRadio);
		delimButtonGroup.add(commaRadio);
		delimButtonGroup.add(spaceRadio);
		delimButtonGroup.add(otherRadio);

		JPanel extraPane = new JPanel();
		extraPane.setLayout(new BorderLayout(5, 5));
		JPanel textQualifierPane = new JPanel();
		textQualifierPane.add(new JLabel(Messages.getString("servoy.plugin.tabxport.textQualifier"))); //$NON-NLS-1$
		textQualifierCombo.addItem(SeparatedASCIIImportTableModel.DUBBLE_QUOTE_QUALIFIER);
		textQualifierCombo.addItem(SeparatedASCIIImportTableModel.SINGLE_QUOTE_QUALIFIER);
		textQualifierCombo.addItem(SeparatedASCIIImportTableModel.NONE_QUALIFIER);
		textQualifierPane.add(textQualifierCombo);
		textQualifierCombo.setActionCommand("TextQualifier"); //$NON-NLS-1$
		textQualifierCombo.addActionListener(this);
		extraPane.add(formatPanel, BorderLayout.NORTH);
		extraPane.add(textQualifierPane, BorderLayout.EAST);
		extraPane.add(headerRows, BorderLayout.WEST);
		headerRows.setActionCommand("UseHeaderRows"); //$NON-NLS-1$
		headerRows.addActionListener(this);

		//Lay out the master content pane.
		add(extraPane, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();
		if (command.equals("UseHeaderRows")) //$NON-NLS-1$
		{
			if (tableModel != null) tableModel.setUseHeaderRow(headerRows.isSelected());
		}
		else if (command.equals("TextQualifier")) //$NON-NLS-1$
		{
			if (tableModel != null) tableModel.setTextQualifier((String)textQualifierCombo.getSelectedItem());
		}
		else
		{
			String sep = "\t"; //$NON-NLS-1$
			if (otherRadio.isSelected())
			{
				sep = otherSeparator.getText();
			}
			else if (tabRadio.isSelected())
			{
				sep = "\t"; //$NON-NLS-1$
			}
			else if (semicolonRadio.isSelected())
			{
				sep = ";"; //$NON-NLS-1$
			}
			else if (commaRadio.isSelected())
			{
				sep = ","; //$NON-NLS-1$
			}
			else if (spaceRadio.isSelected())
			{
				sep = " "; //$NON-NLS-1$
			}
			if (tableModel != null) tableModel.setSeparator(sep);
		}
		table.sizeColumnsToFit(JTable.AUTO_RESIZE_ALL_COLUMNS);
		revalidate();
		repaint();
	}

	public AbstractTableModel getModel()
	{
		return tableModel;
	}

	public String getNextPanelName()
	{
		return "SpecifyDestinationPanel"; //$NON-NLS-1$
	}

	public boolean isDone()
	{
		state.setProperty("data", tableModel); //$NON-NLS-1$
		return true;
	}

	public Runnable needsToRunFirst(final boolean forward)
	{
		return new Runnable()
		{
			public void run()
			{
				try
				{
					parent.blockGUI(Messages.getString("servoy.plugin.import.status.organizingData")); //$NON-NLS-1$
					if (forward)
					{
						Vector items = (Vector)state.getProperty("lines"); //$NON-NLS-1$
						tableModel = new SeparatedASCIIImportTableModel(items);
						tableModel.setTextQualifier(SeparatedASCIIImportTableModel.DUBBLE_QUOTE_QUALIFIER);
						table.setModel(tableModel);
					}

					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							otherRadio.setSelected(true);
							textQualifierCombo.setSelectedItem(SeparatedASCIIImportTableModel.DUBBLE_QUOTE_QUALIFIER);
							headerRows.setSelected(false);
						}
					});
				}
				finally
				{
					parent.releaseGUI();
				}
			}
		};
	}
}
