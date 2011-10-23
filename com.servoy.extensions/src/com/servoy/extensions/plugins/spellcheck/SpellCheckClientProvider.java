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

import java.awt.Component;

import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.IDelegate;

@ServoyDocumented(publicName = "spellcheck")
public class SpellCheckClientProvider implements IScriptable, IReturnedTypesProvider
{
	private final SpellCheckClientPlugin plugin;

	public SpellCheckClientProvider(SpellCheckClientPlugin app)
	{
		plugin = app;
	}

	/**
	 * Spellcheck the form element/component.
	 *
	 * @sample
	 * // The desired spellcheck provider and language are set via the SpellCheck Preference Page, in the Client Preferences.
	 * // Spellchecking currently works in SmartClient only.
	 * plugins.spellcheck.checkTextComponent(forms.actionDetails.elements.actionText);
	 * // Optionally, the language can be sent as an argument to the function.
	 * // The language string is provided from the language constants class, as in the sample below
	 * // NOTE: the optional language, if provided, overrides the Preference Panel page setting, of the current SpellCheck provider (RapidSpell/Google).
	 * // plugins.spellcheck.checkTextComponent(textInDutch, SpellCheck_Languages.DUTCH);
	 *
	 * @param textComponent 
	 */
	public void js_checkTextComponent(Object textComponent)
	{
		try
		{
			if (textComponent instanceof IDelegate)
			{
				textComponent = ((IDelegate< ? >)textComponent).getDelegate();
			}
			if (textComponent instanceof Component)
			{
				plugin.check((Component)textComponent, null);
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
	}

	/**
	 * @clonedesc js_checkTextComponent(Object)
	 * @sampleas js_checkTextComponent(Object)
	 * 
	 * @param textComponent
	 * @param language
	 */
	public void js_checkTextComponent(Object textComponent, String language)
	{
		try
		{
			if (textComponent instanceof IDelegate)
			{
				textComponent = ((IDelegate)textComponent).getDelegate();
			}
			if (textComponent instanceof Component)
			{
				plugin.check((Component)textComponent, language);
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { LANGUAGES.class };
	}
}
