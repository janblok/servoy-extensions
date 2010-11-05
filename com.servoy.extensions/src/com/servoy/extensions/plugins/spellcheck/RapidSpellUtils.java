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

package com.servoy.extensions.plugins.spellcheck;

import java.util.Arrays;
import java.util.List;

import com.servoy.extensions.plugins.spellcheck2.SpellCheckerUtils;

/**
 * @author andrei
 *
 */
public class RapidSpellUtils
{
	public static final String DutchDictionary = "nl-dutch-v2.dict"; //$NON-NLS-1$
	public static final String GermanDictionary = "de-German-v2.dict"; //$NON-NLS-1$
	public static final String ItalianDictionary = "DICT-IT-IT-Italian.dict"; //$NON-NLS-1$
	public static final String SpanishDictionary = "es-Spanish-v2.dict"; //$NON-NLS-1$

	/** 
	 * Get the supported languages, corresponding to the available dictionaries for RapidSpell.
	 * @return
	 */
	public static List<String> getAvailableDictionaries()
	{
		String[] s = { SpellCheckerUtils.ENGLISH, SpellCheckerUtils.DUTCH, SpellCheckerUtils.GERMAN, SpellCheckerUtils.ITALIAN, SpellCheckerUtils.SPANISH };
		return Arrays.asList(s);
	}

	public static String getDictionaryForLanguage(String lang)
	{
		if (SpellCheckerUtils.DUTCH.equals(lang)) return DutchDictionary;
		else if (SpellCheckerUtils.GERMAN.equals(lang)) return GermanDictionary;
		else if (SpellCheckerUtils.ITALIAN.equals(lang)) return ItalianDictionary;
		else if (SpellCheckerUtils.SPANISH.equals(lang)) return SpanishDictionary;
		else return null;//for English
	}

}
