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

import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IConstantsObject;

/**
 * Class for language constants.
 * 
 * @author acostache
 *
 */
@ServoyDocumented
public abstract class LANGUAGES implements IConstantsObject
{
	/**
	 * Constant for specifying the English language.
	 * 
	 * @sample
	 * plugins.spellcheck.checkTextComponent('textInEnglish', plugins.spellcheck.LANGUAGES.ENGLISH);
	 */
	public static final String ENGLISH = SpellCheckerUtils.ENGLISH;

	/**
	 * Constant for specifying the Dutch language.
	 * 
	 * @sample
	 * plugins.spellcheck.checkTextComponent('textInDutch', plugins.spellcheck.LANGUAGES.DUTCH);
	 */
	public static final String DUTCH = SpellCheckerUtils.DUTCH;

	/**
	 * Constant for specifying the German language.
	 * 
	 * @sample
	 * plugins.spellcheck.checkTextComponent('textInGerman', plugins.spellcheck.LANGUAGES.GERMAN);
	 */
	public static final String GERMAN = SpellCheckerUtils.GERMAN;

	/**
	 * Constant for specifying the Italian language.
	 * 
	 * @sample
	 * plugins.spellcheck.checkTextComponent('textInItalian', plugins.spellcheck.LANGUAGES.ITALIAN);
	 */
	public static final String ITALIAN = SpellCheckerUtils.ITALIAN;

	/**
	 * Constant for specifying the Spanish language.
	 * 
	 * @sample
	 * plugins.spellcheck.checkTextComponent('textInSpanish', plugins.spellcheck.LANGUAGES.SPANISH);
	 */
	public static final String SPANISH = SpellCheckerUtils.SPANISH;
}
