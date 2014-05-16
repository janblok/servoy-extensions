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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.servoy.j2db.IApplication;
import com.servoy.j2db.Messages;
import com.servoy.j2db.dataprocessing.IFoundSetInternal;
import com.servoy.j2db.persistence.Column;
import com.servoy.j2db.persistence.ColumnWrapper;
import com.servoy.j2db.persistence.IColumn;
import com.servoy.j2db.persistence.IDataProvider;
import com.servoy.j2db.property.DataProviderEditor;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.wizard.IWizard;
import com.servoy.j2db.util.wizard.IWizardPanel;
import com.servoy.j2db.util.wizard.IWizardState;

/**
 * @author jblok
 */
public class ExportSpecifyDestinationPanel extends JPanel implements ActionListener, IWizardPanel
{
	private final DataProviderEditor dpe;
	private final JList rlist;

	protected ButtonGroup delimButtonGroup = new ButtonGroup();
	protected JRadioButton tabRadio = new JRadioButton(Messages.getString("servoy.plugin.tabxport.separator.tab"));
	protected JRadioButton semicolonRadio = new JRadioButton(Messages.getString("servoy.plugin.tabxport.separator.semicolon"));
	protected JRadioButton commaRadio = new JRadioButton(Messages.getString("servoy.plugin.tabxport.separator.comma"));
	protected JRadioButton spaceRadio = new JRadioButton(Messages.getString("servoy.plugin.tabxport.separator.space"));
	protected JRadioButton otherRadio = new JRadioButton(Messages.getString("servoy.plugin.tabxport.separator.other"));
	protected JTextField otherSeparator = new JTextField(2);

	private final IWizard parent;
	private final IWizardState state;
	private final IApplication application;

	public ExportSpecifyDestinationPanel(IWizard parent, IWizardState state, IApplication app)
	{
		application = app;
		this.parent = parent;
		this.state = state;
		setName("start");
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());

		// Lay out topmost Part pane
		JPanel formatPanel = new JPanel();
		formatPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		formatPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("servoy.plugin.tabxport.choose.separator")));
		formatPanel.add(tabRadio);
		tabRadio.setActionCommand("separatorIsTab");
		tabRadio.addActionListener(this);
		formatPanel.add(commaRadio);
		commaRadio.setActionCommand("separatorIsComma");
		commaRadio.addActionListener(this);
		formatPanel.add(semicolonRadio);
		semicolonRadio.setActionCommand("separatorIsSemicolon");
		semicolonRadio.addActionListener(this);
		formatPanel.add(spaceRadio);
		spaceRadio.setActionCommand("separatorIsSpace");
		spaceRadio.addActionListener(this);
		formatPanel.add(otherRadio);
		otherRadio.setActionCommand("separatorIsOther");
		otherRadio.addActionListener(this);
		formatPanel.add(otherSeparator);
		otherSeparator.setEditable(false);
		delimButtonGroup.add(tabRadio);
		delimButtonGroup.add(semicolonRadio);
		delimButtonGroup.add(commaRadio);
		delimButtonGroup.add(spaceRadio);
		delimButtonGroup.add(otherRadio);
		tabRadio.setSelected(true);

		JPanel movePane = new JPanel();
		movePane.setLayout(new BoxLayout(movePane, BoxLayout.Y_AXIS));
		movePane.setMaximumSize(new Dimension(100, 200));

		JButton downButton = new JButton(Messages.getString("servoy.button.moveDown"));
		Dimension minimumSize = downButton.getPreferredSize();//new Dimension(100,20);
		final JButton rightButton = new JButton(" >> ");
		rightButton.addActionListener(this);
		rightButton.setActionCommand("right");
		rightButton.setPreferredSize(minimumSize);
		rightButton.setMinimumSize(minimumSize);
		rightButton.setMaximumSize(minimumSize);
//		rightButton.setAlignmentX(0);
//		rightButton.setAlignmentY(0);
		movePane.add(rightButton);

		movePane.add(Box.createRigidArea(new Dimension(0, 5)));

		final JButton leftButton = new JButton(" << ");
		leftButton.addActionListener(this);
		leftButton.setActionCommand("left");
		leftButton.setPreferredSize(minimumSize);
		leftButton.setMinimumSize(minimumSize);
		leftButton.setMaximumSize(minimumSize);
//		leftButton.setAlignmentX(0);
//		leftButton.setAlignmentY(0);
		movePane.add(leftButton);

		movePane.add(Box.createRigidArea(new Dimension(0, 5)));

		JButton upButton = new JButton(Messages.getString("servoy.button.moveUp"));
		upButton.addActionListener(this);
		upButton.setActionCommand("up");
		upButton.setPreferredSize(minimumSize);
		upButton.setMinimumSize(minimumSize);
		upButton.setMaximumSize(minimumSize);
//		upButton.setAlignmentX(0);
//		upButton.setAlignmentY(0);
		movePane.add(upButton);

		movePane.add(Box.createRigidArea(new Dimension(0, 5)));

//        JButton downButton = new JButton("move down");
		downButton.addActionListener(this);
		downButton.setActionCommand("down");
		downButton.setPreferredSize(minimumSize);
		downButton.setMinimumSize(minimumSize);
		downButton.setMaximumSize(minimumSize);
//		downButton.setAlignmentX(0);
//		downButton.setAlignmentY(0);
		movePane.add(downButton);

		movePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		dpe = new DataProviderEditor();
		dpe.init(application);
//        llist.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
//                    rightButton.doClick();
//                }
//            }
//        });
//		JScrollPane listScroll = new JScrollPane(llist);
//		listScroll.setPreferredSize(new Dimension(200,200));

		rlist = new JList();
		rlist.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					leftButton.doClick();
				}
			}
		});
		JScrollPane tableScroll = new JScrollPane(rlist);
		tableScroll.setPreferredSize(new Dimension(200, 200));
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(tableScroll, BorderLayout.CENTER);
		rightPanel.add(new JLabel(Messages.getString("servoy.plugin.export.toFile")), BorderLayout.NORTH);

		JPanel toppanel = new JPanel();
//		toppanel.setLayout(new BorderLayout());
		toppanel.setLayout(new BoxLayout(toppanel, BoxLayout.X_AXIS));

//        JPanel comboPanel = new JPanel();
//        comboPanel.setLayout(new BorderLayout(5,5));
//		comboPanel.add(new JComboBox(),BorderLayout.NORTH);
//        comboPanel.add(listScroll,BorderLayout.CENTER);
		toppanel.add(dpe);//, BorderLayout.WEST);
		toppanel.add(movePane);//,BorderLayout.CENTER);
		toppanel.add(rightPanel);//, BorderLayout.EAST);
		//toppanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(formatPanel, BorderLayout.NORTH);
		add(toppanel, BorderLayout.CENTER);

		//adddefault
		String sep = "\t";
		state.setProperty("separator", sep);
	}

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();
		if (command.equals("left")) left();
		else if (command.equals("right")) right();
		else if (command.equals("up")) up();
		else if (command.equals("down")) down();
		else
		{
			otherSeparator.setEditable(false);
			String sep = "\t";
			if (otherRadio.isSelected())
			{
				otherSeparator.setEditable(true);
			}
			else if (tabRadio.isSelected())
			{
				sep = "\t";
			}
			else if (semicolonRadio.isSelected())
			{
				sep = ";";
			}
			else if (commaRadio.isSelected())
			{
				sep = ",";
			}
			else if (spaceRadio.isSelected())
			{
				sep = " ";
			}
			state.setProperty("separator", sep);
		}
	}

	private void up()
	{
		Object o = rlist.getSelectedValue();
		if (o != null)
		{
			int index = rlist.getSelectedIndex();
			if (index >= 1)
			{
				DefaultListModel rdlm = (DefaultListModel)rlist.getModel();
				rdlm.removeElement(o);
				rdlm.add(index - 1, o);
				rlist.setSelectedValue(o, true);
			}
		}
	}

	private void down()
	{
		Object o = rlist.getSelectedValue();
		if (o != null)
		{
			DefaultListModel rdlm = (DefaultListModel)rlist.getModel();
			int index = rlist.getSelectedIndex();
			if (index < rdlm.getSize() - 1)
			{
				rdlm.removeElement(o);
				rdlm.add(index + 1, o);
				rlist.setSelectedValue(o, true);
			}
		}
	}

	private void left()
	{
		Object[] o = rlist.getSelectedValues();
		if (o != null)
		{
//			DefaultListModel ldlm = (DefaultListModel)llist.getModel();
//			ldlm.addElement(o);
//			llist.setSelectedValue(o, true);

			DefaultListModel rdlm = (DefaultListModel)rlist.getModel();
			for (Object element : o)
			{
				rdlm.removeElement(element);
			}
		}
	}

	private void right()
	{
		DefaultListModel rdlm = (DefaultListModel)rlist.getModel();
		Object o = dpe.getValue();
		if (o != null)
		{
			if (o instanceof IDataProvider)
			{
				rdlm.addElement(getColumnDisplayText((IDataProvider)o));
				rlist.setSelectedValue(o, true);
			}
			else
			{
				IDataProvider[] array = (IDataProvider[])o;
				for (IDataProvider element : array)
				{
					if ((element) instanceof IDataProvider)
					{
						rdlm.addElement(getColumnDisplayText(element));
					}
				}
			}

//			DefaultListModel ldlm = (DefaultListModel)llist.getModel();
//			ldlm.removeElement(o);
		}
	}

	private DataProviderWithLabel getColumnDisplayText(IDataProvider dataProvider)
	{
		String textToReturn = dataProvider.getDataProviderID();
		ColumnWrapper cw = dataProvider.getColumnWrapper();
		if (cw != null)
		{
			IColumn col = cw.getColumn();
			if (col instanceof Column) textToReturn = ((Column)col).getTitle();
		}
		return new DataProviderWithLabel(dataProvider, textToReturn);
	}

	public String getNextPanelName()
	{
		if (otherRadio.isSelected()) state.setProperty("separator", otherSeparator.getText());
		return "SpecifyFilePanel";
	}

	public boolean isDone()
	{
		DefaultListModel dlm = (DefaultListModel)rlist.getModel();
		if (dlm.getSize() == 0)
		{
			return false;
		}
		else
		{
			state.setProperty("dataProviderIDs", dlm);
			return true;
		}
	}

	public Runnable needsToRunFirst(boolean forward)
	{
		return new Runnable()
		{
			public void run()
			{
				try
				{
					parent.blockGUI(Messages.getString("servoy.plugin.export.status.buildingUI"));
					IFoundSetInternal data = (IFoundSetInternal)state.getProperty("foundset");
					if (data != null && data.getTable() != null)
					{
						dpe.setDefinedTable(data.getTable());
						dpe.setAllowMultipleSelections(true);
						dpe.setShowRelatedOnly(false);
						dpe.setShowColumnsOnly(false);
						dpe.setRelatedEnabled(true);
						dpe.dontShowNoneOption();
						dpe.setReturnValueAsString(false);
						dpe.showDataEx(null);

						if (rlist.getModel().getSize() == 0)
						{
							rlist.setModel(new DefaultListModel());
						}
					}
				}
				catch (Exception e)
				{
					Debug.error(e);
				}
				finally
				{
					parent.releaseGUI();
				}
			}
		};
	}

	class DataProviderWithLabel
	{
		public IDataProvider dataProvider;
		public String label;

		public DataProviderWithLabel(IDataProvider dataProvider, String label)
		{
			this.dataProvider = dataProvider;
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

}
