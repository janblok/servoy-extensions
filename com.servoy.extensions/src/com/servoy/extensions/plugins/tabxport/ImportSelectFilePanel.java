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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.servoy.j2db.IApplication;
import com.servoy.j2db.Messages;
import com.servoy.j2db.util.FileNameFilter;
import com.servoy.j2db.util.wizard.IWizard;
import com.servoy.j2db.util.wizard.IWizardPanel;
import com.servoy.j2db.util.wizard.IWizardState;

/**
 * @author jblok
 */
public class ImportSelectFilePanel extends JPanel implements ActionListener, IWizardPanel
{
	private File selectedFile;
	private final IWizard parent;
	private final IWizardState state;

	public ImportSelectFilePanel(IWizard parent, IWizardState state)
	{
		this.parent = parent;
		this.state = state;
		setName("start"); //$NON-NLS-1$
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());

		JPanel fieldPanel = new JPanel();
//		fieldPanel.setPreferredSize(new Dimension(500,20));
//		fieldPanel.setMaximumSize(new Dimension(500,20));
//		fieldPanel.setSize(new Dimension(500,20));
		fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
		fieldPanel.add(Box.createHorizontalGlue());
		fieldPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("servoy.plugin.import.chooseFile"))); //$NON-NLS-1$

		JButton browse = new JButton(Messages.getString("servoy.button.browse")); //$NON-NLS-1$
		browse.setActionCommand("browse"); //$NON-NLS-1$
		browse.addActionListener(this);

		JLabel label = new JLabel(Messages.getString("servoy.plugin.import.selectFile"), SwingConstants.LEFT); //$NON-NLS-1$

		fieldPanel.add(label);
		fieldPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		fieldPanel.add(browse);
		fieldPanel.add(Box.createHorizontalGlue());

		add(fieldPanel, BorderLayout.CENTER);

//		JPanel titlePanel = new JPanel();
//		titlePanel.add(new JLabel(" "));
//		add(titlePanel,BorderLayout.NORTH);
		setPreferredSize(new Dimension(550, 400));
	}

	public void actionPerformed(ActionEvent event)
	{
		// Get the "action command" of the event, and dispatch based on that.
		// This method calls a lot of the interesting methods in this class.
		String command = event.getActionCommand();
		if (command.equals("browse")) browse(); //$NON-NLS-1$
	}

	private void browse()
	{
		try
		{
//			if (WebStart.isRunningWebStart())
//			{
//				is = WebStart.loadFile(null);
//			}
//			else
//			{
			JFileChooser fc = new JFileChooser();
			FileNameFilter csv = new FileNameFilter("csv"); //$NON-NLS-1$
			fc.addChoosableFileFilter(csv);
			fc.addChoosableFileFilter(new FileNameFilter("tab")); //$NON-NLS-1$
			fc.setFileFilter(csv);
			IApplication application = null;
			String initialPath = null;
			if (state != null)
			{
				application = (IApplication)state.getProperty("application");
				if (application != null && application.getRuntimeProperties().containsKey("textImportInitialPath"))
				{
					initialPath = (String)application.getRuntimeProperties().get("textImportInitialPath");
				}
			}
			if (initialPath != null)
			{
				fc.setCurrentDirectory(new File(initialPath));
			}
			int returnVal = fc.showOpenDialog(parent.getMainApplicationWindow());
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				selectedFile = fc.getSelectedFile();
				if (application != null) application.getRuntimeProperties().put("textImportInitialPath", selectedFile.getParentFile().getPath());
			}
//			}
			parent.doNext();
		}
		catch (Exception ex)
		{
			parent.reportError(Messages.getString("servoy.plugin.exportImport.fileSelect.exception"), ex);
		}
	}

//	public File getFileName()
//	{
//		File file = new File(fileName.getText());
//		if (!file.exists())
//		{
//			getToolkit().beep();
//			fileName.requestFocus();
//			return null;
//		}
//		return file;
//	}

	public String getNextPanelName()
	{
		return "SeparatorPanel"; //$NON-NLS-1$
	}

	public boolean isDone()
	{
		if (selectedFile == null) return false;
		try
		{
			FileInputStream is = new FileInputStream(selectedFile);
			Vector lines = new Vector();
			state.setProperty("lines", lines); //$NON-NLS-1$
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			// only read first 200 lines..
			int counter = 0;
			while ((line = br.readLine()) != null && counter++ < 200)
			{
				lines.addElement(line);
			}
			if (line != null)
			{
				lines.addElement(line);
				state.setProperty("reader", br); //$NON-NLS-1$
			}
			else
			{
				br.close();
				is = null;
			}
			return true;
		}
		catch (Exception ex)
		{
			parent.reportError(Messages.getString("servoy.plugin.import.fileLoad.exception"), ex); //$NON-NLS-1$
			return false;
		}
	}

	public Runnable needsToRunFirst(boolean forward)
	{
		return null;
	}
}
