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
	// Buttons and labels names - in properties files
	public static final String SPELLING = "SPELLING";
	public static String SKIP = "SKIP";
	public static String IGNORE_ALL = "IGNORE_ALL";
	public static String REPLACE = "REPLACE";
	public static String REPLACE_ALL = "REPLACE_ALL";
	public static String ADD = "ADD_TO_DICTIONARY";
	public static String CANCEL = "CANCEL";
	public static String NOT_IN_DICTIONARY = "NOT_IN_DICTIONARY";
	public static String SUGGESTIONS = "SUGGESTIONS";
	public static String FIND_SUGGESTIONS = "FIND_SUGGESTIONS";
	public static String SPELLING_CHECK_COMPLETE = "SPELLING_CHECK_COMPLETE";

	public static final String GOOGLE_SPELL = "GOOGLE SPELL";
	public static final String RAPID_SPELL = "RAPID SPELL";

	/** The Ignore button click action command */
	public static final String IGNORE_CMD = "IGNORE";
	/** The Ignore All button click action command */
	public static final String IGNOREALL_CMD = "IGNOREALL";
	/** The Add button click action command */
	public static final String ADD_CMD = "ADD";
	/** The Replace button click action command */
	public static final String REPLACE_CMD = "REPLACE";
	/** The Replace All button click action command */
	public static final String REPLACEALL_CMD = "REPLACEALL";
	/** The Cancel button click action command */
	public static final String CANCEL_CMD = "CANCEL";

	/** 
	 * Language Strings
	 */
	public static final String ENGLISH = "English";
	public static final String DUTCH = "Dutch";
	public static final String GERMAN = "German";
	public static final String ITALIAN = "Italian";
	public static final String SPANISH = "Spanish";
	public static final String DEFAULT = ENGLISH;

	public static final String MESSAGES = "com.servoy.extensions.plugins.spellcheck.res.SpellcheckMessages";

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
