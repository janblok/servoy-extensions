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

import java.awt.Component;

import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.IDelegate;

/**
 * @author		jblok
 */
public class SpellCheckProvider implements IScriptObject
{
	private final SpellCheckerPlugin plugin;

	public SpellCheckProvider(SpellCheckerPlugin app)
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
	public void js_checkTextComponent(Object c)
	{
		try
		{
			if (c instanceof IDelegate)
			{
				c = ((IDelegate)c).getDelegate();
			}
			if (c instanceof Component)
			{
				plugin.check((Component)c);
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
			return new String[] { "textComponent" };
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("checkTextComponent".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.spellcheck.checkTextComponent(forms.actionDetails.elements.actionText);\n"); //$NON-NLS-1$
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
		return null;
	}
}
