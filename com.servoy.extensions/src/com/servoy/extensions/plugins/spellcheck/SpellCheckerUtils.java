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

import java.util.Arrays;
import java.util.List;

public class SpellCheckerUtils
{
	// Dialog title
	public static String GUI_TITLE = "Spelling"; //$NON-NLS-1$

	// Buttons and labels names - can be in a properties file

	public static String IGNORE_BUTTON = "Skip"; //$NON-NLS-1$
	public static String IGNORE_ALL_BUTTON = "Ignore All"; //$NON-NLS-1$
	public static String REPLACE_BUTTON = "Replace"; //$NON-NLS-1$
	public static String REPLACE_ALL_BUTTON = "Replace All"; //$NON-NLS-1$
	public static String ADD_BUTTON = "Add to dictionary"; //$NON-NLS-1$
	public static String CANCEL_BUTTON = "Cancel"; //$NON-NLS-1$
	public static String NOT_IN_DICTIONARY_LABEL = "Not in dictionary: "; //$NON-NLS-1$
	public static String SUGGESTIONS_LABEL = "Suggestions: "; //$NON-NLS-1$
	public static String FIND_SUGGESTIONS_CHECK_BOX = "Find suggestions? "; //$NON-NLS-1$

	public static final String GOOGLE_SPELL = "GOOGLE SPELL"; //$NON-NLS-1$
	public static final String RAPID_SPELL = "RAPID SPELL"; //$NON-NLS-1$

	/** The Ignore button click action command */
	public static final String IGNORE_CMD = "IGNORE"; //$NON-NLS-1$
	/** The Ignore All button click action command */
	public static final String IGNOREALL_CMD = "IGNOREALL"; //$NON-NLS-1$
	/** The Add button click action command */
	public static final String ADD_CMD = "ADD"; //$NON-NLS-1$
	/** The Replace button click action command */
	public static final String REPLACE_CMD = "REPLACE"; //$NON-NLS-1$
	/** The Replace All button click action command */
	public static final String REPLACEALL_CMD = "REPLACEALL"; //$NON-NLS-1$
	/** The Cancel button click action command */
	public static final String CANCEL_CMD = "CANCEL"; //$NON-NLS-1$
	/** The resource for the Suggestions label */
	private static final String SUGGESTIONS_RES = "SUGGESTIONS"; //$NON-NLS-1$
	private static final String INVALIDWORD_RES = "INVALIDWORD"; //$NON-NLS-1$

	/** 
	 * Language Strings
	 */
	public static final String ENGLISH = "English"; //$NON-NLS-1$
	public static final String DUTCH = "Dutch"; //$NON-NLS-1$
	public static final String GERMAN = "German"; //$NON-NLS-1$
	public static final String ITALIAN = "Italian"; //$NON-NLS-1$
	public static final String SPANISH = "Spanish"; //$NON-NLS-1$
	public static final String DEFAULT = ENGLISH;

	/**
	 * Get available languages/dictionaries as a list.
	 * 
	 * @return list of available language dictionaries
	 */
	public static List<String> getAvailableDictionaries()
	{
		String[] s = { SpellCheckerUtils.ENGLISH, SpellCheckerUtils.DUTCH, SpellCheckerUtils.GERMAN, SpellCheckerUtils.ITALIAN, SpellCheckerUtils.SPANISH };
		return Arrays.asList(s);
	}
}
