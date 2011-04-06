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

package com.servoy.extensions.plugins.http;

import org.apache.http.client.methods.HttpOptions;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author pbakker
 *
 */
public class OptionsRequest extends BaseRequest
{
	public static String OPTIONS_HEADER = "Allow"; //$NON-NLS-1$

	//only used by script engine
	public OptionsRequest()
	{
		super();
	}

	public OptionsRequest(String url, DefaultHttpClient hc)
	{
		super(url, hc);
		method = new HttpOptions(url);
	}

	public String[] js_getAllowedMethods(Response res)
	{
		return res.getAllowedMethods();
	}

	@Override
	public String getSample(String methodName)
	{
		if ("getAllowedMethods".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var supportedOperations = request.getAllowedMethods()"); //$NON-NLS-1$
			retval.append("application.output(supportedOperations.join(',');"); //$NON-NLS-1$
			return retval.toString();
		}
		return super.getSample(methodName);
	}

	@Override
	public String getToolTip(String methodName)
	{
		if ("getAllowedMethods".equals(methodName)) //$NON-NLS-1$
		{
			return "Returns the supported HTTP Request operations as a String Array"; //$NON-NLS-1$
		}
		return super.getToolTip(methodName);
	}
}
