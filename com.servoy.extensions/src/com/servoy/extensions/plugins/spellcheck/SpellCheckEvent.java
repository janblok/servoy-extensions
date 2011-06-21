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

import java.util.List;

public class SpellCheckEvent
{
	/** Field indicating that the incorrect word should be ignored*/
	public static final short IGNORE = 0;
	/** Field indicating that the incorrect word should be ignored forever*/
	public static final short IGNOREALL = 1;
	/** Field indicating that the incorrect word should be replaced*/
	public static final short REPLACE = 2;
	/** Field indicating that the incorrect word should be replaced always*/
	public static final short REPLACEALL = 3;
	/** Field indicating that the incorrect word should be added to the dictionary*/
	public static final short ADDTODICT = 4;
	/** Field indicating that the spell checking should be terminated*/
	public static final short CANCEL = 5;
	/** Initial case for the action */
	public static final short INITIAL = -1;
	/**The list holding the suggested Word objects for the misspelt word*/
	private final List<SpellCorrection> suggestions;
	/**The misspelt word*/
	private final String invalidWord;
	/**The action to be done when the event returns*/
	private short action = INITIAL;
	/**Contains the word to be replaced if the action is REPLACE or REPLACEALL*/
	private String replaceWord = null;

	/**Constructs the SpellCheckEvent
	 * @param invalidWord The word that is misspelt
	 * event to fire.
	 */
	public SpellCheckEvent(String invalidWord, List<SpellCorrection> suggestions)
	{
		this.invalidWord = invalidWord;
		this.suggestions = suggestions;
	}

	/** Returns the list of suggested Word objects
	 * @return A list of words phonetically close to the misspelt word
	 */
	public List<SpellCorrection> getSuggestions()
	{
		return suggestions;
	}

	/** Returns the currently misspelt word
	 * @return The text misspelt
	 */
	public String getInvalidWord()
	{
		return invalidWord;
	}


	/** Returns the action type the user has to handle
	 * @return The type of action the event is carrying
	 */
	public short getAction()
	{
		return action;
	}

	/** Returns the text to replace
	 * @return the text of the word to replace
	 */
	public String getReplaceWord()
	{
		return replaceWord;
	}

	public void setReplaceWord(String word)
	{
		replaceWord = word;
	}

	/** Set the action to replace the currently misspelt word with the new word
	 *  @param newWord The word to replace the currently misspelt word
	 *  @param replaceAll If set to true, the SpellChecker will replace all
	 *  further occurrences of the misspelt word without firing a SpellCheckEvent.
	 */
	public void replaceWord(String newWord, boolean replaceAll)
	{
		if (action != INITIAL) throw new IllegalStateException("The action can can only be set once"); //$NON-NLS-1$
		if (replaceAll) action = REPLACEALL;
		else action = REPLACE;
		replaceWord = newWord;
	}

	/**
	 * Set the action it ignore the currently misspelt word.
	 * @param ignoreAll If set to true, the SpellChecker will replace all
	 *  further occurrences of the misspelt word without firing a SpellCheckEvent.
	 */
	public void ignoreWord(boolean ignoreAll)
	{
		if (action != INITIAL) throw new IllegalStateException("The action can can only be set once"); //$NON-NLS-1$
		if (ignoreAll) action = IGNOREALL;
		else action = IGNORE;
	}

	/** Set the action to add a new word into the dictionary. This will also replace the
	 *  currently misspelt word.
	 * @param newWord The new word to add to the dictionary.
	 */
	public void addToDictionary(String newWord)
	{
		if (action != INITIAL) throw new IllegalStateException("The action  can only be set once"); //$NON-NLS-1$
		action = ADDTODICT;
		replaceWord = newWord;
	}

	/** Set the action to terminate processing of the spellchecker.
	 */
	public void cancel()
	{
		if (action != INITIAL) throw new IllegalStateException("The action can can only be set once"); //$NON-NLS-1$
		action = CANCEL;
	}

}
