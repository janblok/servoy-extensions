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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.servoy.j2db.IApplication;
import com.servoy.j2db.Messages;
import com.servoy.j2db.persistence.Column;
import com.servoy.j2db.persistence.ColumnWrapper;
import com.servoy.j2db.persistence.IServer;
import com.servoy.j2db.persistence.Relation;
import com.servoy.j2db.persistence.RepositoryException;
import com.servoy.j2db.persistence.Table;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.SafeArrayList;
import com.servoy.j2db.util.StringComparator;
import com.servoy.j2db.util.gui.SortedComboModel;
import com.servoy.j2db.util.wizard.IWizard;
import com.servoy.j2db.util.wizard.IWizardPanel;
import com.servoy.j2db.util.wizard.IWizardState;

/**
 * @author jblok
 */
public class ImportSpecifyDestinationPanel extends JPanel implements IWizardPanel, ItemListener
{
	protected SpecialTableModel columnsModel;
	protected JComboBox columns = new JComboBox();
	protected JComboBox dateFormat;
	protected List columnsData = new ArrayList();

	private final JTable jtable;

	private final IWizard parent;
	private final IWizardState state;
	private final JComboBox serverCombobox;
	private final JComboBox tableCombobox;
	private final IApplication application;

	public ImportSpecifyDestinationPanel(IWizard parent, IWizardState state, IApplication app) throws RepositoryException
	{
		this.parent = parent;
		this.state = state;
		this.application = app;

		setName("SpecifyDestinationPanel");
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());

		jtable = new JTable();//columnsModel);
		jtable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		jtable.setToolTipText(Messages.getString("servoy.plugin.import.columnTable.tooltip"));

		//Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(jtable);

		// Lay out topmost Part pane
		JPanel formatPanel = new JPanel();
		formatPanel.setLayout(new BorderLayout(5, 5));
		formatPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("servoy.plugin.import.specifyDateFormat.title")));
		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		//        p1.setLayout (new FlowLayout (FlowLayout.LEFT, 0, 0));
		dateFormat = new JComboBox(new Object[] { "dd-MM-yyyy", "dd/MM/yyyy", "MM-dd-yyyy",
		"MM/dd/yyyy", "dd-MM-yyyy HH:mm:ss", "MM-dd-yyyy hh:mm:ss", "dd-MM-yyyy HH:mm",
		"MM-dd-yyyy hh:mm", "yyyy-MM-dd HH:mm:ss.S" });
		dateFormat.setSelectedItem("yyyy-MM-dd HH:mm:ss.S");

		dateFormat.setEditable(true);
		dateFormat.setPreferredSize(new Dimension(200, 24));
		dateFormat.setSize(new Dimension(200, 24));
		p1.add(dateFormat);

		IApplication application = (IApplication)state.getProperty("application");
		Table table = (Table)state.getProperty("table");
		IServer server = table == null ? null : application.getSolution().getServer(table.getServerName());

		Map<String, IServer> sp = application.getSolution().getServerProxies();
		IServer[] v;
		synchronized (sp)
		{
			v = sp.values().toArray(new IServer[sp.size()]);
		}
		serverCombobox = new JComboBox(v);
		if (server == null && v.length > 0)
		{
			server = v[0];
		}

		if (server != null) serverCombobox.setSelectedItem(server);
		serverCombobox.addItemListener(this);
		p1.add(serverCombobox);

		tableCombobox = new JComboBox();
		try
		{
			tableCombobox.setModel(new SortedComboModel(StringComparator.INSTANCE, server == null ? Collections.EMPTY_LIST : server.getTableAndViewNames(false)));
			if (table != null) tableCombobox.setSelectedItem(table.getName());
		}
		catch (RemoteException e)
		{
			Debug.error(e);
		}
		tableCombobox.addItemListener(this);
		p1.add(tableCombobox);

		formatPanel.add(p1, BorderLayout.CENTER);
		add(formatPanel, BorderLayout.NORTH);

		JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("servoy.plugin.import.specifyMapping.title")));
		borderPanel.add(scrollPane, BorderLayout.CENTER);
		add(borderPanel, BorderLayout.CENTER);
	}

	public AbstractTableModel getModel()
	{
		return columnsModel;
	}

	class SpecialTableModel extends AbstractTableModel
	{
		protected AbstractTableModel dataModel;
		protected SafeArrayList fixedData = new SafeArrayList();

		public SpecialTableModel(AbstractTableModel dataTableModel)
		{
			super();
			dataModel = dataTableModel;
		}

		@Override
		public String getColumnName(int col)
		{
			if (col == 0) return Messages.getString("servoy.plugin.import.columnname.importField");
			else return Messages.getString("servoy.plugin.import.columnname.databaseField");
		}

		public int getRowCount()
		{
			return dataModel.getColumnCount() + 10;
		}

		public int getColumnCount()
		{
			return 2;
		}

		public Object getValueAt(int row, int col)
		{
			if (col == 0) return (fixedData.get(row) != null ? fixedData.get(row) : dataModel.getColumnName(row));
			else return columnsData.get(row);
		}

		@Override
		public boolean isCellEditable(int row, int col)
		{
			if (col == 0) return true;
			else return true;
		}

		@Override
		public void setValueAt(Object value, int row, int col)
		{
			if (col == 0) fixedData.set(row, value);
			else columnsData.set(row, value);
		}
	}

	public String getNextPanelName()
	{
		return "TransferPanel";
	}

	public boolean isDone()
	{
		if (jtable.isEditing())
		{
			if (jtable.getCellEditor() != null) jtable.getCellEditor().stopCellEditing();
		}

		boolean bcontinue = false;
		for (int i = 0; i < columnsModel.getRowCount(); i++)
		{
			Object c = columnsModel.getValueAt(i, 1);
			if (c instanceof Column)
			{
				bcontinue = true;
				break;
			}
		}
		if (bcontinue)
		{
			state.setProperty("columns", columnsModel);
			state.setProperty("dateFormat", dateFormat.getSelectedItem());
		}

		return bcontinue;
	}

	public Runnable needsToRunFirst(final boolean forward)
	{
		return new Runnable()
		{
			public void run()
			{
				try
				{
					parent.blockGUI(Messages.getString("servoy.plugin.import.status.organizingData"));
					AbstractTableModel data = (AbstractTableModel)state.getProperty("data");
					columnsModel = new SpecialTableModel(data);

					if (columnsModel.getRowCount() != columnsData.size())
					{
						columnsData = new ArrayList();
						for (int i = 0; i < columnsModel.getRowCount(); i++)
						{
							columnsData.add(null);
						}
					}
					jtable.setModel(columnsModel);

					TableColumn columnColumn = jtable.getColumnModel().getColumn(1);
					columnColumn.setCellEditor(new DefaultCellEditor(columns));
					if (columns.getItemCount() == 0)
					{
						columns.addItem("-none-");
						try
						{
							Table possTable = (Table)state.getProperty("table");
							if (possTable != null)
							{
								Iterator e1 = possTable.getColumnsSortedByName();
								while (e1.hasNext())
								{
									columns.addItem(e1.next());
								}
								Iterator it = application.getFlattenedSolution().getRelations(possTable, true, true);
								while (it.hasNext())
								{
									Relation r = (Relation)it.next();
									if (r.isValid() && !r.isGlobal() && r.getAllowCreationRelatedRecords())
									{
										possTable = r.getForeignTable();
										if (possTable != null)
										{
											Iterator e2 = possTable.getColumnsSortedByName();
											while (e2.hasNext())
											{
												columns.addItem(new ColumnWrapper((Column)e2.next(), r));
											}
										}
									}
								}
							}
						}
						catch (RepositoryException e)
						{
							Debug.error(e);
						}
					}

					outer : for (int i = 0; i < columnsModel.getRowCount(); i++)
					{
						String importColumn = (String)columnsModel.getValueAt(i, 0);
						importColumn = importColumn != null ? importColumn.toLowerCase() : null;
						for (int j = 0; j < columns.getItemCount(); j++)
						{
							if (columns.getItemAt(j).toString().toLowerCase().equalsIgnoreCase(importColumn))
							{
								columnsModel.setValueAt(columns.getItemAt(j), i, 1);
								continue outer;
							}
						}
						columnsModel.setValueAt(null, i, 1);
					}

					if (jtable.getModel().getRowCount() > 0 && forward)
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{
								jtable.editCellAt(0, 1);
							}
						});
					}
				}
				finally
				{
					parent.releaseGUI();
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			if (e.getSource() == serverCombobox)
			{
				IServer server = (IServer)serverCombobox.getSelectedItem();
				try
				{
					tableCombobox.setModel(new SortedComboModel(StringComparator.INSTANCE, server.getTableAndViewNames(false)));
				}
				catch (Exception e1)
				{
					Debug.error(e1);
				}
			}
			else if (e.getSource() == tableCombobox)
			{
				IServer server = (IServer)serverCombobox.getSelectedItem();
				try
				{
					CellEditor ce = this.jtable.getCellEditor();
					if (ce != null) ce.stopCellEditing();
					columns.removeAllItems();
					Table table = (Table)server.getTable((String)tableCombobox.getSelectedItem());
					state.setProperty("table", table);

					SwingUtilities.invokeLater(needsToRunFirst(true));
				}
				catch (Exception e1)
				{
					Debug.error(e1);
				}
			}
		}
	}

}
