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
package com.servoy.extensions.plugins.agent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.servoy.j2db.Messages;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.preference.PreferencePanel;

/**
 * @author	Johan Compagner
 */
public class AgentPreferencePanel extends PreferencePanel implements ActionListener
{
	protected IClientPluginAccess application;
	/**
	 * Constructor for AgentPreferencePanel.
	 */
	public AgentPreferencePanel(IClientPluginAccess app)
	{
		super();
		application = app;
		boolean showAgent = new Boolean( app.getSettings().getProperty("plugin.agent.showAgentOnStart","false")).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$
		
		JCheckBox check = new JCheckBox(Messages.getString("servoy.plugin.agent.preference.showOnStartup") , showAgent); //$NON-NLS-1$
		check.addActionListener(this);
		this.setLayout(new BorderLayout());
		this.add(check,BorderLayout.NORTH);
	}

	private ChangeListener listener;
	public void addChangeListener(ChangeListener l)
	{
		listener = l;
	}
	private void fireChangeEvent()
	{
		changed = true;
		listener.stateChanged(new ChangeEvent(this));
	}
	private boolean changed = false;
	public int getRequiredUserAction()
	{
		int retval = PreferencePanel.NO_USER_ACTION_REQUIRED;
		if (changed)
		{
			retval = PreferencePanel.APPLICATION_RESTART_NEEDED;
		}
		changed = false;
		return retval;
	}
	
	/*
	 * @see PreferencePanel#cancel()
	 */
	public boolean handleCancel()
	{
		return true;
	}
	/*
	 * @see PreferencePanel#ok()
	 */
	public boolean handleOK()
	{
		return true;
	}
	/*
	 * @see PreferencePanel#getTabName()
	 */
	public String getTabName()
	{
		return Messages.getString("servoy.plugin.agent.preference.tabname"); //$NON-NLS-1$
	}
	
	/**
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		fireChangeEvent();
		if(((JCheckBox)e.getSource()).isSelected())
		{
			application.getSettings().setProperty("plugin.agent.showAgentOnStart", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
		{
			application.getSettings().setProperty("plugin.agent.showAgentOnStart", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
