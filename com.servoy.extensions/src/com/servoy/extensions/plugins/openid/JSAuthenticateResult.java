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

package com.servoy.extensions.plugins.openid;

import org.openid4java.message.ax.FetchResponse;

import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptObject;

public class JSAuthenticateResult implements IJavaScriptType, IScriptObject
{
	private final FetchResponse fetchResp;

	public JSAuthenticateResult()
	{
		//for developer scripting introspection only
		this(null);
	}

	public JSAuthenticateResult(FetchResponse fetchResp)
	{
		this.fetchResp = fetchResp;
	}

	public String js_getAttributeValue(String alias)
	{
		if (fetchResp != null) return fetchResp.getAttributeValue(alias);
		return null;
	}

	@SuppressWarnings("unchecked")
	public String[] js_getAttributeValues(String alias)
	{
		if (fetchResp != null) return (String[])fetchResp.getAttributeValues(alias).toArray(new String[0]);
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	public String[] getParameterNames(String methodName)
	{
		if ("getAttributeValue".equals(methodName))
		{
			return new String[] { "alias" };
		}
		else if ("getAttributeValues".equals(methodName))
		{
			return new String[] { "alias" };
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("getAttributeValue".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("var email = authenticateResult.getAttributeValue('email')\n");
			return retval.toString();
		}
		else if ("getAttributeValues".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("var namesArray = authenticateResult.getAttributeValues('names')\n");
			retval.append("for (var i = 0; i < namesArray.length; i++) { \n");
			retval.append("	application.output(namesArray[i]); \n");
			retval.append("} \n");
			return retval.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("getAttributeValue".equals(methodName))
		{
			return "Get attibute value";
		}
		else if ("getAttributeValues".equals(methodName))
		{
			return "Get an array of attibute values";
		}
		return null;
	}

	public Class[] getAllReturnedTypes()
	{
		return null;
	}
}
