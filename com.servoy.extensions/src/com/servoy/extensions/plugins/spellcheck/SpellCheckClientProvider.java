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

import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.IDelegate;

public class SpellCheckClientProvider implements IScriptObject
{
	private final SpellCheckClientPlugin plugin;

	public SpellCheckClientProvider(SpellCheckClientPlugin app)
	{
		plugin = app;
	}

	/**
	 * Spellcheck the form element/component
	 *
	 * @sample plugins.spellcheck.checkTextComponent(forms.actionDetails.elements.actionText);
	 *
	 * @param textComponent 
	 */
	public void js_checkTextComponent(Object textComponent)
	{
		try
		{
			if (textComponent instanceof IDelegate)
			{
				textComponent = ((IDelegate)textComponent).getDelegate();
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


	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	public String[] getParameterNames(String methodName)
	{
		if ("checkTextComponent".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "textComponent", "[language]" }; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("checkTextComponent".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("// "); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("// The desired spellcheck provider and language are set via the SpellCheck Preference Page, in the Client Preferences.\n"); //$NON-NLS-1$
			retval.append("// Spellchecking currently works in SmartClient only.\n"); //$NON-NLS-1$
			retval.append("plugins.spellcheck.checkTextComponent(forms.actionDetails.elements.actionText);\n"); //$NON-NLS-1$
			retval.append("// Optionally, the language can be sent as an argument to the function.\n"); //$NON-NLS-1$
			retval.append("// The language string is provided from the language constants class, as in the sample below\n"); //$NON-NLS-1$
			retval.append("// NOTE: the optional language, if provided, overrides the Preference Panel page setting, of the current SpellCheck provider (RapidSpell/Google).\n"); //$NON-NLS-1$
			retval.append("// plugins.spellcheck.checkTextComponent(textInDutch, SpellCheck_Languages.DUTCH);\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else
		{
			return null;
		}
	}


	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(String)
	 */
	public String getToolTip(String method)
	{
		if ("checkTextComponent".equals(method)) //$NON-NLS-1$
		{
			return "Spellcheck the form element/component."; //$NON-NLS-1$
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class[] getAllReturnedTypes()
	{
		return new Class[] { SpellCheck_Languages.class };
	}
}
