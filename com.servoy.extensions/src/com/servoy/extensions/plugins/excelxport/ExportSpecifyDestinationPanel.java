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
package com.servoy.extensions.plugins.excelxport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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

	private final IWizard parent;
	private final IWizardState state;
	private final IApplication application;

	public ExportSpecifyDestinationPanel(IWizard parent, IWizardState state, IApplication app)
	{
		application = app;
		this.parent = parent;
		this.state = state;
		setName("start"); //$NON-NLS-1$
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());

		JPanel movePane = new JPanel();
		movePane.setLayout(new BoxLayout(movePane, BoxLayout.Y_AXIS));
		movePane.setMaximumSize(new Dimension(100, 200));

		JButton downButton = new JButton(Messages.getString("servoy.button.moveDown")); //$NON-NLS-1$
		Dimension minimumSize = downButton.getPreferredSize();//new Dimension(100,20);
		final JButton rightButton = new JButton(" >> "); //$NON-NLS-1$
		rightButton.addActionListener(this);
		rightButton.setActionCommand("right"); //$NON-NLS-1$
		rightButton.setPreferredSize(minimumSize);
		rightButton.setMinimumSize(minimumSize);
		rightButton.setMaximumSize(minimumSize);
//		rightButton.setAlignmentX(0);
//		rightButton.setAlignmentY(0);
		movePane.add(rightButton);

		movePane.add(Box.createRigidArea(new Dimension(0, 5)));

		final JButton leftButton = new JButton(" << "); //$NON-NLS-1$
		leftButton.addActionListener(this);
		leftButton.setActionCommand("left"); //$NON-NLS-1$
		leftButton.setPreferredSize(minimumSize);
		leftButton.setMinimumSize(minimumSize);
		leftButton.setMaximumSize(minimumSize);
//		leftButton.setAlignmentX(0);
//		leftButton.setAlignmentY(0);
		movePane.add(leftButton);

		movePane.add(Box.createRigidArea(new Dimension(0, 5)));

		JButton upButton = new JButton(Messages.getString("servoy.button.moveUp")); //$NON-NLS-1$
		upButton.addActionListener(this);
		upButton.setActionCommand("up"); //$NON-NLS-1$
		upButton.setPreferredSize(minimumSize);
		upButton.setMinimumSize(minimumSize);
		upButton.setMaximumSize(minimumSize);
//		upButton.setAlignmentX(0);
//		upButton.setAlignmentY(0);
		movePane.add(upButton);

		movePane.add(Box.createRigidArea(new Dimension(0, 5)));

//        JButton downButton = new JButton("move down");
		downButton.addActionListener(this);
		downButton.setActionCommand("down"); //$NON-NLS-1$
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
		rightPanel.add(new JLabel(Messages.getString("servoy.plugin.export.toFile")), BorderLayout.NORTH); //$NON-NLS-1$

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
		add(toppanel, BorderLayout.CENTER);

	}

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();
		if (command.equals("left")) left(); //$NON-NLS-1$
		else if (command.equals("right")) right(); //$NON-NLS-1$
		else if (command.equals("up")) up(); //$NON-NLS-1$
		else if (command.equals("down")) down(); //$NON-NLS-1$
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
		return "SpecifyFilePanel"; //$NON-NLS-1$
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
			state.setProperty("dataProviderIDs", dlm); //$NON-NLS-1$
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
					parent.blockGUI(Messages.getString("servoy.plugin.export.status.buildingUI")); //$NON-NLS-1$
					IFoundSetInternal data = (IFoundSetInternal)state.getProperty("foundset"); //$NON-NLS-1$
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
