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

import com.servoy.j2db.scripting.IScriptObject;

/**
 * gives the Servoy developer access to some Cookie methods for basic cookie operations
 * 
 * @author paul
 */
public class Cookie implements IScriptObject
{

	private org.apache.http.cookie.Cookie cookie;

	public Cookie()
	{
	}

	public void setCookie(org.apache.http.cookie.Cookie cookie)
	{
		this.cookie = cookie;
	}

	public Cookie(org.apache.http.cookie.Cookie cookie)
	{
		this.cookie = cookie;
	}

	/**
	 * returns the cookie name
	 *
	 * @sample
	 * var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')
	 * var name = cookie.getName();
	 */
	public String js_getName()
	{
		if (cookie == null) return ""; //$NON-NLS-1$
		return cookie.getName();
	}

	/**
	 * returns the cookie value
	 *
	 * @sample
	 * var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')
	 * var value = cookie.getValue();
	 */
	public String js_getValue()
	{
		if (cookie == null) return "";//$NON-NLS-1$
		return cookie.getValue();
	}

	/**
	 * returns the cookie domain
	 *
	 * @sample
	 * var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')
	 * var domain = cookie.getDomain();
	 */
	public String js_getDomain()
	{
		if (cookie == null) return "";//$NON-NLS-1$
		return cookie.getDomain();
	}

	/**
	 * returns the cookie path
	 *
	 * @sample
	 * var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')
	 * var path = cookie.getPath();
	 */
	public String js_getPath()
	{
		if (cookie == null) return "";//$NON-NLS-1$
		return cookie.getPath();
	}

	/**
	 * returns the cookie secure attribute
	 *
	 * @sample
	 * var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')
	 * var path = cookie.getSecure();
	 */
	public boolean js_getSecure()
	{
		if (cookie == null) return false;
		return cookie.isSecure();
	}

	/**
	 * returns the cookie comment
	 *
	 * @sample
	 * var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')
	 * var path = cookie.getComment();
	 */
	public String js_getComment()
	{
		if (cookie == null) return "";//$NON-NLS-1$
		return cookie.getComment();
	}

	public Class[] getAllReturnedTypes()
	{
		return null;
	}

	public String[] getParameterNames(String methodName)
	{
		return new String[] { "" };//$NON-NLS-1$
	}

	/**
	 * methods from the interface
	 */
	public String getSample(String methodName)
	{
		StringBuffer retval = new StringBuffer();
		if (methodName != null && getToolTip(methodName) != null)
		{
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
		}

		if ("getName".equals(methodName))//$NON-NLS-1$
		{
			retval.append("var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')\n");//$NON-NLS-1$
			retval.append("var name = cookie.getName();\n");//$NON-NLS-1$
			return retval.toString();
		}
		else if ("getValue".equals(methodName))//$NON-NLS-1$
		{
			retval.append("var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')\n");//$NON-NLS-1$
			retval.append("var value = cookie.getValue();\n");//$NON-NLS-1$
			return retval.toString();
		}
		else if ("getDomain".equals(methodName))//$NON-NLS-1$
		{
			retval.append("var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')\n");//$NON-NLS-1$
			retval.append("var domain = cookie.getDomain();\n");//$NON-NLS-1$
			return retval.toString();
		}
		else if ("getPath".equals(methodName))//$NON-NLS-1$
		{
			retval.append("var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')\n");//$NON-NLS-1$
			retval.append("var path = cookie.getPath();\n");//$NON-NLS-1$
			return retval.toString();
		}
		else if ("getSecure".equals(methodName))//$NON-NLS-1$
		{
			retval.append("var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')\n");//$NON-NLS-1$
			retval.append("var path = cookie.getSecure();\n");//$NON-NLS-1$
			return retval.toString();
		}
		else if ("getComment".equals(methodName))//$NON-NLS-1$
		{
			retval.append("var cookie = plugins.http.getHttpClientCookie('clientName', 'cookieName')\n");//$NON-NLS-1$
			retval.append("var path = cookie.getComment();\n");//$NON-NLS-1$
			return retval.toString();
		}

		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("getName".equals(methodName))//$NON-NLS-1$
		{
			return "Returns the cookie name.";//$NON-NLS-1$
		}
		else if ("getValue".equals(methodName))//$NON-NLS-1$
		{
			return "Returns the cookie value.";//$NON-NLS-1$
		}
		else if ("getDomain".equals(methodName))//$NON-NLS-1$
		{
			return "Returns the cookie domain.";//$NON-NLS-1$
		}
		else if ("getPath".equals(methodName))//$NON-NLS-1$
		{
			return "Returns the cookie path.";//$NON-NLS-1$
		}
		else if ("getSecure".equals(methodName))//$NON-NLS-1$
		{
			return "Returns the cookie secure attribute.";//$NON-NLS-1$
		}
		else if ("getComment".equals(methodName))//$NON-NLS-1$
		{
			return "Returns the cookie comment.";//$NON-NLS-1$
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

}
