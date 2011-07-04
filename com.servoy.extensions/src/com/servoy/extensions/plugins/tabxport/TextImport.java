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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.servoy.j2db.IApplication;
import com.servoy.j2db.ISmartClientApplication;
import com.servoy.j2db.Messages;
import com.servoy.j2db.persistence.ITable;
import com.servoy.j2db.util.wizard.IWizardState;
import com.servoy.j2db.util.wizard.WizardWindow;

/**
 * @author	jblok
*/
public class TextImport extends WizardWindow
{
/*
 * _____________________________________________________________ Declaration of attributes
 */

	private final ITable table;
	private final IApplication application;

/*
 * _____________________________________________________________ Declaration and definition of constructors
 */
	public TextImport(IApplication app)
	{
		super(new Dimension(640, 480));
		application = app;
		table = application.getFormManager().getCurrentForm().getTable();
		createState();
		IWizardState state = getState();
		state.setProperty("application", application); //$NON-NLS-1$
	}

	@Override
	protected void createPanels() throws Exception
	{
		IWizardState state = getState();
		state.setProperty("table", table); //$NON-NLS-1$
		state.setProperty("application", application); //$NON-NLS-1$

		addPanel(new ImportSelectFilePanel(this, getState()));
		addPanel(new ImportSeparatorPanel(this, getState()));
		addPanel(new ImportSeparatorTypePanel(this, getState()));
		addPanel(new ImportSpecifyDestinationPanel(this, getState(), application));
		addPanel(new ImportTransferPanel(this, getState(), application));
	}

	@Override
	public ImageIcon getImageIcon(String name)
	{
		return application.loadImage(name);
	}


	// Shows the frame
	public void showFrame() throws Exception
	{
		super.showDialog(Messages.getString("servoy.plugin.import.title"), ((ISmartClientApplication)application).getMainApplicationFrame()); //$NON-NLS-1$
	}

	@Override
	protected int showCancelDialog()
	{
		int res = JOptionPane.showConfirmDialog(window, Messages.getString("servoy.plugin.import.cancelImport"), //$NON-NLS-1$
			Messages.getString("servoy.general.cancel.title"), //$NON-NLS-1$
			JOptionPane.YES_NO_OPTION);
		return res;
	}

	@Override
	protected void realExit()
	{
	}

	@Override
	public void reportError(Component parentComponent, String msg, Exception ex)
	{
		if (application instanceof ISmartClientApplication)
		{
			((ISmartClientApplication)application).reportError(parentComponent, msg, ex);
		}
		else
		{
			application.reportError(msg, ex);
		}
	}

	@Override
	public void reportError(String msg, Exception ex)
	{
		reportError(window, msg, ex);
	}
}
