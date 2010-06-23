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
package com.servoy.extensions.plugins.mail;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.servoy.j2db.Messages;
import com.servoy.j2db.plugins.IServerAccess;
import com.servoy.j2db.preference.PreferencePanel;
import com.servoy.j2db.util.Debug;

/**
 * This panels sets the default java mail property "mail.smtp.host"
 * @author 		jblok
 */
public class MailPreferencePanel extends PreferencePanel implements DocumentListener
{
	private JLabel mailOutgoingHostNameLabel;
	private JTextField mailOutgoingHostNameField;

	private JLabel mailIncomingHostNameLabel;
	private JTextField mailIncomingHostNameField;

	private Properties properties;
	
	public MailPreferencePanel(IServerAccess application)
	{
		super();

		properties = application.getSettings();

		setLayout(new BorderLayout());
		
		JPanel namePanel = new JPanel(false);
		namePanel.setLayout(new GridLayout(0, 1,5,5));

		JPanel fieldPanel = new JPanel(false);
		fieldPanel.setLayout(new GridLayout(0, 1,5,5));

		mailOutgoingHostNameLabel = new JLabel(Messages.getString("servoy.plugin.mail.outgoing.mailserver.label"), JLabel.RIGHT); //$NON-NLS-1$
		namePanel.add(mailOutgoingHostNameLabel);

		mailOutgoingHostNameField = new JTextField();
		mailOutgoingHostNameField.setPreferredSize(new Dimension(300, 20));
		fieldPanel.add(mailOutgoingHostNameField);

		
		mailIncomingHostNameLabel = new JLabel(Messages.getString("servoy.plugin.mail.incoming.mailserver.label"), JLabel.RIGHT); //$NON-NLS-1$
		namePanel.add(mailIncomingHostNameLabel);

		mailIncomingHostNameField = new JTextField();
		mailIncomingHostNameField.setPreferredSize(new Dimension(300, 20));
		fieldPanel.add(mailIncomingHostNameField);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(namePanel, BorderLayout.WEST);
		panel.add(fieldPanel, BorderLayout.CENTER);

		add(panel,BorderLayout.NORTH);
		
		fillFields();

		mailOutgoingHostNameField.getDocument().addDocumentListener(this);
	}
	
	private void fillFields()
	{
		mailOutgoingHostNameField.setText(properties.getProperty("mail.smtp.host")); //$NON-NLS-1$
		mailIncomingHostNameField.setText(properties.getProperty("mail.pop3.host")); //$NON-NLS-1$
	}
	/*
	 * @see PreferencePanel#cancel()
	 */
	public boolean handleCancel()
	{
		fillFields();
		return true;
	}

	/*
	 * @see PreferencePanel#ok()
	 */
	public boolean handleOK()
	{
		try
		{
			properties.put("mail.smtp.host", mailOutgoingHostNameField.getText()); //$NON-NLS-1$
			properties.put("mail.pop3.host", mailIncomingHostNameField.getText()); //$NON-NLS-1$
		}
		catch(Exception ex)
		{
			Debug.error(ex);
		}
		return true;
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
	public void insertUpdate(DocumentEvent e)
	{
		fireChangeEvent();
	}
	public void removeUpdate(DocumentEvent e)
	{
		fireChangeEvent();
	}
	public void changedUpdate(DocumentEvent e)
	{
		fireChangeEvent();
	}

	/*
	 * @see PreferencePanel#getTabName()
	 */
	public String getTabName()
	{
		return Messages.getString("servoy.plugin.mail.tabname"); //$NON-NLS-1$
	}
}
