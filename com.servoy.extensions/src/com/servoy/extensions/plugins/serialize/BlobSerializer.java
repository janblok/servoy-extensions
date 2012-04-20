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

import java.util.Map;

import com.servoy.j2db.dataprocessing.ITypedColumnConverter;
import com.servoy.j2db.persistence.IColumnTypes;

public class BlobSerializer implements ITypedColumnConverter
{
	private final SerializePlugin plugin;

	public BlobSerializer(SerializePlugin p)
	{
		plugin = p;
	}

	public Object convertFromObject(Map props, int column_type, Object obj) throws Exception
	{
		if (obj == null) return null;
		return plugin.getJSONSerializer().toJSON(obj).toString().getBytes("UTF-8");
	}

	public Object convertToObject(Map props, int column_type, Object dbvalue) throws Exception
	{
		if (dbvalue == null) return null;
		return plugin.getJSONSerializer().fromJSON(plugin.getClientPluginAccess().getDatabaseManager(), new String((byte[])dbvalue, "UTF-8"));
	}

	public Map getDefaultProperties()
	{
		return null;
	}

	public String getName()
	{
		return "BlobSerializer";
	}

	public int[] getSupportedColumnTypes()
	{
		return new int[] { IColumnTypes.MEDIA };
	}

	/**
	 * @see com.servoy.j2db.dataprocessing.IColumnConverter#getToObjectType(java.util.Map)
	 */
	public int getToObjectType(Map props)
	{
		return IColumnTypes.MEDIA;
	}
}
