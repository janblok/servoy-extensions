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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import org.apache.poi.ss.usermodel.DateUtil;

import com.servoy.j2db.IApplication;
import com.servoy.j2db.IForm;
import com.servoy.j2db.Messages;
import com.servoy.j2db.dataprocessing.IFoundSet;
import com.servoy.j2db.dataprocessing.IFoundSetManagerInternal;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.dataprocessing.IRecordInternal;
import com.servoy.j2db.persistence.Column;
import com.servoy.j2db.persistence.IColumnTypes;
import com.servoy.j2db.persistence.IDataProvider;
import com.servoy.j2db.persistence.Table;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.wizard.IWizard;
import com.servoy.j2db.util.wizard.IWizardPanel;
import com.servoy.j2db.util.wizard.IWizardState;

/**
 * @author jblok
 */
public class ImportTransferPanel extends JPanel implements IWizardPanel
{
	protected JLabel doneLabel;

	private final IWizard parent;
	private final IWizardState state;
	private final IApplication application;

	private final JButton cancelButton;

	private final AtomicBoolean importSemaphore;


	public ImportTransferPanel(final IWizard parent, IWizardState state, IApplication app)
	{
		this.parent = parent;
		this.state = state;
		this.application = app;

		setName("TransferPanel"); //$NON-NLS-1$
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		setLayout(new BorderLayout());

		doneLabel = new JLabel(Messages.getString("servoy.plugin.import.importing.label")); //$NON-NLS-1$
		cancelButton = new JButton(Messages.getString("servoy.general.cancel.title")); //$NON-NLS-1$

		doneLabel.setHorizontalAlignment(SwingConstants.CENTER);


		cancelButton.setHorizontalAlignment(SwingConstants.CENTER);

		JPanel fieldPanel = new JPanel();
		fieldPanel.setPreferredSize(new Dimension(500, 20));
		fieldPanel.setMaximumSize(new Dimension(500, 20));
		fieldPanel.setSize(new Dimension(500, 20));
		fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
		fieldPanel.add(Box.createHorizontalGlue());
		fieldPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("servoy.plugin.import.chooseFile"))); //$NON-NLS-1$

		fieldPanel.add(doneLabel);
		fieldPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		fieldPanel.add(cancelButton);
		fieldPanel.add(Box.createHorizontalGlue());

		add(fieldPanel, BorderLayout.CENTER);

		cancelButton.setEnabled(true);

		cancelButton.addMouseListener(new MouseListener()
		{

			public void mouseClicked(MouseEvent e)
			{
			}

			public void mouseEntered(MouseEvent e)
			{
				if (cancelButton.isEnabled())
				{
					parent.getMainApplicationWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}

			}

			public void mouseExited(MouseEvent e)
			{
				if (cancelButton.isEnabled())
				{
					parent.getMainApplicationWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				}
				else
				{
					parent.getMainApplicationWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}

			public void mousePressed(MouseEvent e)
			{
			}

			public void mouseReleased(MouseEvent e)
			{
			}

		});

		cancelButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				cancelTheCurrentLoad();
				cancelButton.setEnabled(false);
			}
		});

		this.importSemaphore = new AtomicBoolean();
	}

	private void cancelTheCurrentLoad()
	{
		this.importSemaphore.set(false);
	}

	public String getNextPanelName()
	{
		return null;
	}

	public boolean isDone()
	{
		return true;
	}

	public Runnable needsToRunFirst(boolean forward)
	{
		importSemaphore.set(true);
		return new ExcelImportRunner();
	}

	class ExcelImportRunner implements Runnable
	{

		public void run()
		{
			parent.semiBlockGUI(Messages.getString("servoy.plugin.import.status.loadingData")); //$NON-NLS-1$
			try
			{
				final IFoundSetManagerInternal fsm = application.getFoundSetManager();

				AbstractTableModel data = (AbstractTableModel)state.getProperty("data"); //$NON-NLS-1$
				AbstractTableModel columns = (AbstractTableModel)state.getProperty("columns"); //$NON-NLS-1$
				Table table = (Table)state.getProperty("table"); //$NON-NLS-1$
				String dateformat = (String)state.getProperty("dateFormat"); //$NON-NLS-1$
				if (dateformat == null || dateformat.trim().length() == 0)
				{
					dateformat = "yyyy-MM-dd HH:mm:ss.S"; //$NON-NLS-1$
				}

				//new: final
				final IFoundSet fs = fsm.getNewFoundSet(table, null, fsm.getDefaultPKSortColumns(table.getDataSource()));

				long dataRowCount = data.getRowCount();
				boolean goOn = true;

				for (int i = 0; i < dataRowCount; i++)
				{
					if (goOn == false) break;
					if (i > 0 && (i % 200) == 0)
					{
						if (!importSemaphore.get())
						{
							goOn = false;
							break;
						}
					}

					final IRecord s = fs.getRecord(fs.newRecord(true, false));
					if (((IRecordInternal)s).startEditing(false))
					{
						long maxCount = Math.max(columns.getRowCount(), data.getColumnCount());

						for (int k = 0; k < maxCount; k++)
						{

							Object fixval = columns.getValueAt(k, 0);
							if (fixval != null && fixval.toString().startsWith("fixed:")) //$NON-NLS-1$
							{
								fixval = fixval.toString().substring(6);
							}
							else
							{
								fixval = null;
							}
							Object c = columns.getValueAt(k, 1);

							if (c instanceof IDataProvider)
							{
								IDataProvider dp = (IDataProvider)c;
								Column column = (Column)dp.getColumnWrapper().getColumn();
								Object obj = data.getValueAt(i, k);

								if (fixval != null)
								{
									obj = fixval;
								}
								String id = column.getDataProviderID();
								int type = Column.mapToDefaultType(column.getType());
								String format = null;
								if (type == IColumnTypes.DATETIME)
								{
									if (obj instanceof Number)
									{
										try
										{
											obj = DateUtil.getJavaDate(((Number)obj).doubleValue());
										}
										catch (Exception e)
										{
											Debug.error(e);
										}
									}
									else
									{
										format = dateformat;
									}
								}
								s.setValue(id, column.getAsRightType(obj, format));
							}
						}
					}
					if (i % 200 == 0)
					{
						final Exception[] possibleException = new Exception[1];

						application.invokeAndWait(new Runnable()
						{
							public void run()
							{
								try
								{
									fsm.startTransaction();
									fsm.commitTransaction(true);
								}
								catch (Exception ex)
								{
									possibleException[0] = ex;
								}
							}
						});


						if (possibleException[0] != null) throw possibleException[0];
					}
				}

				final Exception[] possibleException = new Exception[1];

				application.invokeAndWait(new Runnable()
				{
					public void run()
					{
						try
						{
							fsm.startTransaction();
							fsm.commitTransaction(true);
						}
						catch (Exception ex)
						{
							possibleException[0] = ex;
						}
					}
				});

				if (possibleException[0] != null) throw possibleException[0];
				doneLabel.setText(Messages.getString("servoy.plugin.import.importingDone.label")); //$NON-NLS-1$
				IForm dm = application.getFormManager().getCurrentForm();
				if (dm != null) dm.loadAllRecords();//refresh screen in background
			}
			catch (Exception ex)
			{
				parent.reportError(Messages.getString("servoy.plugin.import.exception"), ex); //$NON-NLS-1$
			}
			finally
			{
				parent.semiReleaseGUI();
				cancelButton.setEnabled(false);
			}
		}
	}
}
