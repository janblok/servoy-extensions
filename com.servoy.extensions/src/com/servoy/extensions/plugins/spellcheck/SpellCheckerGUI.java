package com.servoy.extensions.plugins.spellcheck;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.servoy.extensions.plugins.spellcheck2.SpellCheckClientPlugin;
import com.servoy.extensions.plugins.spellcheck2.SpellCheckEvent;
import com.servoy.extensions.plugins.spellcheck2.SpellCheckerForm;
import com.servoy.j2db.util.JEscapeDialog;

public class SpellCheckerGUI extends JEscapeDialog implements ActionListener, WindowListener
{
	private final SpellCheckClientPlugin plugin;
	private final SpellCheckerForm spellCheckerForm = new SpellCheckerForm();

	private SpellCheckEvent spellEvent;

	private int currentInvalidWordBeginingPosition = -1;
	private int currentInvalidWordEndingPosition = -1;
	private boolean highlighted = false;

	public SpellCheckerGUI(SpellCheckClientPlugin plugin, JDialog w, boolean modal)
	{
		super(w, modal);
		this.plugin = plugin;
		init();
	}

	public SpellCheckerGUI(SpellCheckClientPlugin plugin, JFrame w, boolean modal)
	{
		super(w, modal);
		this.plugin = plugin;
		init();
	}


	private void init()
	{
		this.setTitle("Spelling"); //$NON-NLS-1$
		this.getContentPane().add(spellCheckerForm);
		spellCheckerForm.addActionListener(this);
		this.addWindowListener(this);
		this.addWindowFocusListener(new WindowAdapter()
		{
			@Override
			public void windowGainedFocus(WindowEvent e)
			{
				//to highlight the word which was highlighted before the window lost focus
				highlightInvalidWordFromPosition(currentInvalidWordBeginingPosition, currentInvalidWordEndingPosition);
			}

			@Override
			public void windowLostFocus(WindowEvent e)
			{
				currentInvalidWordEndingPosition = plugin.getCheckedComponent().getCaretPosition();
				currentInvalidWordBeginingPosition = currentInvalidWordEndingPosition - spellEvent.getInvalidWord().length();
				highlighted = false; //"highlighted" will become false twice for a SKIP/IGNORE or CANCEL (not good!!!)
			}
		});
		highlighted = false;
		pack();

	}

	public void check(SpellCheckEvent spellCheckEvent)
	{
		if (spellCheckEvent.getSuggestions().size() <= 0) //this does not seem ok
		{
			this.setVisible(false);
		}
		spellCheckerForm.addSpellEvent(spellCheckEvent);

	}

	public void setTheCurrentSpellCheckEvent(SpellCheckEvent spellEvent)
	{
		this.spellEvent = spellEvent;
		spellCheckerForm.setSpellEvent(spellEvent);
		highlightInvalidWordOfSpellEvent(spellEvent);
	}

	public void highlightInvalidWordOfSpellEvent(SpellCheckEvent spellEvent)
	{
		if (!highlighted)
		{
			//get location of spellEvent word in the text to check;
			String invalidWord = spellEvent.getInvalidWord();
			String checkedText = plugin.getCheckedComponent().getText();
			int currentCaretPosition = plugin.getCheckedComponent().getCaretPosition();
			if (currentCaretPosition >= checkedText.length())
			{
				plugin.getCheckedComponent().setCaretPosition(0);
				currentCaretPosition = plugin.getCheckedComponent().getCaretPosition();
			}
			int start = checkedText.indexOf(invalidWord, currentCaretPosition);
			int end = spellEvent.getInvalidWord().length() + start;

//		if (start + end >= checkedText.length() - 1) plugin.getCheckedComponent().setCaretPosition(0);
//		else plugin.getCheckedComponent().setCaretPosition(start + end);

			plugin.getCheckedComponent().setSelectionStart(start);
			plugin.getCheckedComponent().setSelectionEnd(end);
			currentInvalidWordBeginingPosition = start;
			currentInvalidWordEndingPosition = end;
			highlighted = true;
		}
	}

	public void highlightInvalidWordFromPosition(int begin, int end)
	{
		if (begin < end && !highlighted)
		{
			highlighted = true;
			plugin.getCheckedComponent().setSelectionStart(begin);
			plugin.getCheckedComponent().setSelectionEnd(end);
		}
	}

	public void unhighlight()
	{
		highlighted = false;
		plugin.getCheckedComponent().setCaretPosition(0);
	}

	public void setTheFirstSpellEvent()
	{
		//we have at least one spellCheckEvent
		if (spellCheckerForm.getSpellEventsList().size() > 0)
		{
			SpellCheckEvent sce = spellCheckerForm.getSpellEventsList().get(0);
			setTheCurrentSpellCheckEvent(sce);
		}
		else
		{
			//nothing to spell check - the text is correct.
			this.setVisible(false);
			JOptionPane.showMessageDialog(this, "The spelling check is complete.", "Spelling", JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	public void emptySpellCheckEvents()
	{
		spellCheckerForm.getSpellEventsList().clear();
	}

	public void emptyCurrentSpellCheckEventIfAny()
	{
		spellCheckerForm.setCurrentSpellCheckEvent(null);
	}

	@Override
	protected void cancel()
	{
		this.setVisible(false);
	}

	public void actionPerformed(ActionEvent e)
	{
		plugin.fireAndHandleEvent(spellEvent);

		// remove former spell check event(s) (which have been consumed)
		if (spellEvent.getAction() == SpellCheckEvent.REPLACEALL) spellCheckerForm.removeAllOccurencesOfCurrentSpellCheckEvent(spellEvent);
		else if (spellEvent.getAction() == SpellCheckEvent.CANCEL) emptySpellCheckEvents();
		else spellCheckerForm.removeSpellEvent(spellEvent);

		highlighted = false;

		// set next spell check event
		if (spellCheckerForm.getSpellEventsList().size() > 0)
		{
			setTheCurrentSpellCheckEvent(spellCheckerForm.getSpellEventsList().get(0));
			this.setVisible(true);
		}
		else
		{
			//no more spell events, therefore the spell check is complete
			this.setVisible(false);
			//we do not want the message to show up if we clicked "Cancel"
			if (spellEvent.getAction() != SpellCheckEvent.CANCEL) JOptionPane.showMessageDialog(this,
				"The spelling check is complete.", "Spelling", JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void windowClosed(WindowEvent e)
	{
		//we unhighlight (or put the caret to position zero) 
		unhighlight();
	}

	public void windowClosing(WindowEvent e)
	{
		//we unhighlight (or put the caret to position zero)
		unhighlight();
	}

	public void windowDeactivated(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowOpened(WindowEvent e)
	{
	}
}
