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
package com.servoy.extensions.plugins.serialize;

import com.servoy.j2db.scripting.IScriptObject;

public class SerializeProvider implements IScriptObject
{
	private final SerializePlugin plugin;

	public SerializeProvider(SerializePlugin p)
	{
		plugin = p;
	}

	/**
	 * serialize an object to json text
	 *
	 * @sample
	 * var org_array = new Array('A1','F1','Paris-Dakar');
	 * var string_data = plugins.serialize.toJSON(org_array);
	 * var new_array = plugins.serialize.fromJSON(string_data);
	 * application.output(new_array.join('#'));
	 */
	public String js_toJSON(Object obj) throws Exception
	{
		return plugin.getJSONSerializer().toJSON(obj).toString();
	}

	/**
	 * deserialize from json text to an object
	 *
	 * @sample
	 * var org_array = new Array('A1','F1','Paris-Dakar');
	 * var string_data = plugins.serialize.toJSON(org_array);
	 * var new_array = plugins.serialize.fromJSON(string_data);
	 * application.output(new_array.join('#'));
	 */
	public Object js_fromJSON(String data) throws Exception
	{
		return plugin.getJSONSerializer().fromJSON(data);
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	public String[] getParameterNames(String methodName)
	{
//		if ("getPageData".equals(methodName)) //$NON-NLS-1$
//	    {
//			return new String[]{"url","[http_clientname]","[username","password]"};
//	    }
		return null;
	}

	public String getSample(String methodName)
	{
//		if ("getPageData".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var org_array = new Array('A1','F1','Paris-Dakar');\n"); //$NON-NLS-1$
			retval.append("var string_data = plugins.serialize.toJSON(org_array);\n"); //$NON-NLS-1$
			retval.append("var new_array = plugins.serialize.fromJSON(string_data);\n"); //$NON-NLS-1$
			retval.append("application.output(new_array.join('#'));\n"); //$NON-NLS-1$
			return retval.toString();
		}
//		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(String)
	 */
	public String getToolTip(String methodName)
	{
		if ("toJSON".equals(methodName)) //$NON-NLS-1$
		{
			return "Serialize an object to JSON text."; //$NON-NLS-1$
		}
		else if ("fromJSON".equals(methodName)) //$NON-NLS-1$
		{
			return "Deserialize from JSON text to an object."; //$NON-NLS-1$
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