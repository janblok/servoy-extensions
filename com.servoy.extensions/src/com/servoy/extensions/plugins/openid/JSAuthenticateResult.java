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

import com.servoy.base.scripting.annotations.ServoyClientSupport;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptable;

@ServoyDocumented
@ServoyClientSupport(ng = false, wc = true, sc = true)
public class JSAuthenticateResult implements IJavaScriptType, IScriptable
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

	/**
	 * Get attribute value
	 *
	 * @sample
	 * var email = authenticateResult.getAttributeValue('email')
	 *
	 * @param alias 
	 */
	public String js_getAttributeValue(String alias)
	{
		if (fetchResp != null) return fetchResp.getAttributeValue(alias);
		return null;
	}

	/**
	 * Get an array of attribute values
	 *
	 * @sample
	 * var namesArray = authenticateResult.getAttributeValues('names')
	 * for (var i = 0; i < namesArray.length; i++) { 
	 * 	application.output(namesArray[i]); 
	 * }
	 *
	 * @param alias 
	 */
	@SuppressWarnings("unchecked")
	public String[] js_getAttributeValues(String alias)
	{
		if (fetchResp != null) return (String[])fetchResp.getAttributeValues(alias).toArray(new String[0]);
		return null;
	}
}
