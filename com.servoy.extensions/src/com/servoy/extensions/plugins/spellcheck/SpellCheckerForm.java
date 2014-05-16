/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2011 Servoy BV

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

package com.servoy.extensions.plugins.spellcheck;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.servoy.j2db.smart.J2DBClient;

/*
 * This class to display a form for misspelled words
 */
public class SpellCheckerForm extends JPanel implements ActionListener, ListSelectionListener
{

	private static final long serialVersionUID = 1L;
	private JLabel wrongWordLabel;

	/* Accessible GUI Components */
	protected JList suggestList;
	protected JTextField checkText;

	protected SpellCheckEvent spellEvent;
	protected List<SpellCheckEvent> spellEvents = new ArrayList<SpellCheckEvent>();
	private final ResourceBundle messages;

	public SpellCheckerForm(ResourceBundle messages)
	{
		this.messages = messages;
		initializeGUI();
	}

	/**
	 * Helper method to create a JButton with a command, a text label and a
	 * listener
	 */
	private static final JButton createButton(String command, String text, ActionListener listener)
	{
		JButton btn = new JButton(text);
		btn.setActionCommand(command);
		btn.addActionListener(listener);
		return btn;
	}

	/** Creates the buttons on the left hand side of the panel */
	protected JPanel makeEastPanel()
	{
		JPanel jPanel1 = new JPanel();
		BoxLayout layout = new BoxLayout(jPanel1, BoxLayout.PAGE_AXIS);
		jPanel1.setLayout(layout);
		jPanel1.add(Box.createHorizontalGlue());
		jPanel1.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		JButton ignoreBtn = createButton(SpellCheckerUtils.IGNORE_CMD, messages.getString(SpellCheckerUtils.SKIP), this);
		ignoreBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		jPanel1.add(Box.createHorizontalGlue());
		jPanel1.add(ignoreBtn);
		jPanel1.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

//		JButton addBtn = createButton(SpellCheckerUtils.ADD_CMD, SpellCheckerUtils.ADD_BUTTON, this);
//		addBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
//		jPanel1.add(addBtn);
//		jPanel1.add(Box.createHorizontalGlue());
//		jPanel1.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		JButton changeBtn = createButton(SpellCheckerUtils.REPLACE_CMD, messages.getString(SpellCheckerUtils.REPLACE), this);
		changeBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		jPanel1.add(changeBtn);
		jPanel1.add(Box.createHorizontalGlue());
		jPanel1.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		JButton changeAllBtn = createButton(SpellCheckerUtils.REPLACEALL_CMD, messages.getString(SpellCheckerUtils.REPLACE_ALL), this);
		changeAllBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		jPanel1.add(changeAllBtn);
		jPanel1.add(Box.createHorizontalGlue());
		jPanel1.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		JButton cancelBtn = createButton(SpellCheckerUtils.CANCEL_CMD, messages.getString(SpellCheckerUtils.CANCEL), this);
		cancelBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		jPanel1.add(cancelBtn);
		jPanel1.add(Box.createHorizontalGlue());
		jPanel1.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		return jPanel1;
	}

	protected JPanel makeCentrePanel()
	{
		JPanel jPanel2 = new JPanel();
		jPanel2.setLayout(new BoxLayout(jPanel2, BoxLayout.PAGE_AXIS));
		jPanel2.add(Box.createHorizontalGlue());
		jPanel2.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		JLabel lbl1 = new JLabel(messages.getString(SpellCheckerUtils.NOT_IN_DICTIONARY));
		lbl1.setHorizontalTextPosition(SwingConstants.LEFT);
		jPanel2.add(lbl1);
		jPanel2.add(Box.createHorizontalGlue());
		jPanel2.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		wrongWordLabel = new JLabel("");
		wrongWordLabel.setForeground(Color.red);
		jPanel2.add(wrongWordLabel);
		jPanel2.add(Box.createHorizontalGlue());
		jPanel2.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		checkText = new JTextField();
		jPanel2.add(checkText);
		jPanel2.add(Box.createHorizontalGlue());
		jPanel2.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		JLabel lbl2 = new JLabel(messages.getString(SpellCheckerUtils.SUGGESTIONS));
		jPanel2.add(lbl2);
		jPanel2.add(Box.createHorizontalGlue());
		jPanel2.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		suggestList = new JList();
		suggestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jPanel2.add(new JScrollPane(suggestList));
		suggestList.addListSelectionListener(this);
		suggestList.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
				//we should not set the suggested text twice (should fix this)
				//this is because of first list suggestion not replacing the checktext after the plugin GUI shows up
				replaceCheckedTextWithSuggestionFromList();
			}

			public void mouseEntered(MouseEvent e)
			{
			}

			public void mouseExited(MouseEvent e)
			{
			}

			public void mousePressed(MouseEvent e)
			{
			}

			public void mouseReleased(MouseEvent e)
			{
			}

		});
		jPanel2.add(Box.createHorizontalGlue());
		jPanel2.add(Box.createRigidArea(new Dimension(J2DBClient.BUTTON_SPACING, 3)));

		return jPanel2;
	}

	/** Called by the constructor to initialize the GUI */
	protected void initializeGUI()
	{
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		this.add(makeEastPanel(), BorderLayout.EAST);
		this.add(makeCentrePanel(), BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e)
	{
		if (SpellCheckerUtils.IGNORE_CMD.equals(e.getActionCommand()))
		{
			spellEvent.ignoreWord(false);
			//javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
		}
		else if (SpellCheckerUtils.REPLACE_CMD.equals(e.getActionCommand()))
		{
			spellEvent.replaceWord(checkText.getText(), false);
		}
		else if (SpellCheckerUtils.REPLACEALL_CMD.equals(e.getActionCommand()))
		{
			spellEvent.replaceWord(checkText.getText(), true);
			//removeAllOccurencesOfCurrentSpellCheckEvent(spellEvent);
		}
		else if (SpellCheckerUtils.ADD_CMD.equals(e.getActionCommand()))
		{
			// at this moment same functionality as IGNORE
			//javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
		}
		else if (SpellCheckerUtils.CANCEL_CMD.equals(e.getActionCommand()))
		{
			spellEvent.cancel();
			javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
		}
		fireActionEvent(e);
	}

	private void fireActionEvent(ActionEvent e)
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners[i] == ActionListener.class)
			{
				((ActionListener)listeners[i + 1]).actionPerformed(e);
			}
		}

	}

	public void valueChanged(ListSelectionEvent e)
	{
		//if the selection is not changing any more
		if (!e.getValueIsAdjusting())
		{
			replaceCheckedTextWithSuggestionFromList();
		}

	}

	public void replaceCheckedTextWithSuggestionFromList()
	{
		Object selectedValue = suggestList.getSelectedValue();
		if (selectedValue != null)
		{
			checkText.setText(selectedValue.toString());
		}
	}

	public void addActionListener(SpellCheckerGUI spellCheckerGUI)
	{
		listenerList.add(ActionListener.class, spellCheckerGUI);

	}

	/** Sets the current spell check event that is being shown to the user*/
	public void setSpellEvent(SpellCheckEvent event)
	{
		spellEvent = event;
		String[] words = null;
		DefaultListModel m = new DefaultListModel();
		if (event.getSuggestions().size() > 0)
		{
			SpellCorrection s = event.getSuggestions().get(0);
			words = s.toString().split("\\s+");

			for (String w : words)
			{
				m.addElement(w);
			}
		}
		suggestList.setModel(m);
		wrongWordLabel.setText(event.getInvalidWord());
		if (m.size() > 0)
		{
			suggestList.setSelectedIndex(0);
			checkText.setText(event.getInvalidWord());
			this.setVisible(true);
		}
//		else
//		{
//			this.setVisible(false);
//			JOptionPane.showMessageDialog(this, "The spelling check is complete.", "Spelling", JOptionPane.INFORMATION_MESSAGE);
//		}
		spellEvent.setReplaceWord(checkText.getText());
	}

	public void addSpellEvent(SpellCheckEvent se)
	{
		spellEvents.add(se);
	}

	public void removeSpellEvent(SpellCheckEvent se)
	{
		spellEvents.remove(se);
	}

	public void removeAllOccurencesOfCurrentSpellCheckEvent(SpellCheckEvent sce)
	{
		Iterator<SpellCheckEvent> it = spellEvents.iterator();
		while (it.hasNext())
		{
			SpellCheckEvent temp = it.next();
			if (temp.getInvalidWord().equals(sce.getInvalidWord())) it.remove();
		}
	}

	public List<SpellCheckEvent> getSpellEventsList()
	{
		return spellEvents;
	}

	public SpellCheckEvent getCurrentSpellCheckEvent()
	{
		return spellEvent;
	}

	public void setCurrentSpellCheckEvent(SpellCheckEvent se)
	{
		this.spellEvent = se;
	}

	public void clearWrongWordLabel()
	{
		this.wrongWordLabel.setText(null);
	}

	public void clearSuggestionsList()
	{
		this.suggestList.removeAll();
	}

	public void clearCheckedText()
	{
		this.checkText.setText(null);
	}
}
