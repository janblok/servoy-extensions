package com.servoy.extensions.plugins.spellcheck;

import java.awt.Component;

import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.ui.IComponentProvider;
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
	public void js_checkTextComponent(Object c)
	{
		try
		{
			if (c instanceof IComponentProvider)
			{
				c = ((IComponentProvider)c).getComponent();
			}
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
