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
package com.servoy.extensions.plugins.converters;

import java.util.HashMap;
import java.util.Map;

import com.servoy.j2db.dataprocessing.IPropertyDescriptor;
import com.servoy.j2db.dataprocessing.IPropertyDescriptorProvider;
import com.servoy.j2db.dataprocessing.ITypedColumnConverter;
import com.servoy.j2db.dataprocessing.PropertyDescriptor;
import com.servoy.j2db.persistence.Column;
import com.servoy.j2db.persistence.IColumnTypes;
import com.servoy.j2db.plugins.IClientPluginAccess;

public class GlobalMethodConverter implements ITypedColumnConverter, IPropertyDescriptorProvider
{
	private IClientPluginAccess clientPluginAccess;

	public GlobalMethodConverter(IClientPluginAccess clientPluginAccess)
	{
		this.clientPluginAccess = clientPluginAccess;
	}

	private Object executeMethod(String methodName, int column_type, Object obj) throws Exception
	{
		Object value = obj;
		if (clientPluginAccess != null && methodName != null && methodName.trim().length() != 0)
		{
			value = clientPluginAccess.executeMethod(null, methodName, new Object[] { obj, Column.getDisplayTypeString(column_type) }, false);
		}
		return value;
	}

	public Object convertFromObject(Map<String, String> props, int column_type, Object obj) throws Exception
	{
		return executeMethod(props.get(FROM_OBJECT_NAME_PROPERTY), column_type, obj);
	}

	public Object convertToObject(Map<String, String> props, int column_type, Object dbvalue) throws Exception
	{
		return executeMethod(props.get(TO_OBJECT_NAME_PROPERTY), column_type, dbvalue);
	}

	public Map<String, String> getDefaultProperties()
	{
		Map<String, String> props = new HashMap<String, String>();
		props.put(FROM_OBJECT_NAME_PROPERTY, ""); //$NON-NLS-1$
		props.put(TO_OBJECT_NAME_PROPERTY, ""); //$NON-NLS-1$
		props.put(TYPE_NAME_PROPERTY, ""); //$NON-NLS-1$
		return props;
	}

	public String getName()
	{
		return "GlobalMethodConverter"; //$NON-NLS-1$
	}

	/**
	 * @see com.servoy.j2db.dataprocessing.IColumnConverter#getToObjectType(java.util.Map)
	 */
	public int getToObjectType(Map<String, String> props)
	{
		Object value = props.get(TYPE_NAME_PROPERTY);
		if (value != null)
		{
			if (value.equals("TEXT")) return IColumnTypes.TEXT; //$NON-NLS-1$
			if (value.equals("INTEGER")) return IColumnTypes.INTEGER; //$NON-NLS-1$
			if (value.equals("NUMBER")) return IColumnTypes.NUMBER; //$NON-NLS-1$
			if (value.equals("DATETIME")) return IColumnTypes.DATETIME; //$NON-NLS-1$
			if (value.equals("MEDIA")) return IColumnTypes.MEDIA; //$NON-NLS-1$
		}
		return Integer.MAX_VALUE;
	}

	public int[] getSupportedColumnTypes()
	{
		return new int[] { IColumnTypes.DATETIME, IColumnTypes.INTEGER, IColumnTypes.MEDIA, IColumnTypes.NUMBER, IColumnTypes.TEXT };
	}

	/**
	 * @see com.servoy.j2db.dataprocessing.ITypedColumnConverter#getPropertyDescriptor(java.lang.String)
	 */
	public IPropertyDescriptor getPropertyDescriptor(String property)
	{
		if (TO_OBJECT_NAME_PROPERTY.equals(property))
		{
			return new PropertyDescriptor("Global method to convert DBValue to Object, signature: (dbvalue,columntype)", IPropertyDescriptor.GLOBAL_METHOD); //$NON-NLS-1$
		}
		if (FROM_OBJECT_NAME_PROPERTY.equals(property))
		{
			return new PropertyDescriptor("Global method to convert Object to DBValue, signature: (object,columntype)", IPropertyDescriptor.GLOBAL_METHOD); //$NON-NLS-1$
		}
		if (TYPE_NAME_PROPERTY.equals(property))
		{
			String[] choices = new String[6];
			choices[0] = "<as is>"; //$NON-NLS-1$
			choices[1] = "TEXT"; //$NON-NLS-1$
			choices[2] = "INTEGER"; //$NON-NLS-1$
			choices[3] = "NUMBER"; //$NON-NLS-1$
			choices[4] = "DATETIME"; //$NON-NLS-1$
			choices[5] = "MEDIA"; //$NON-NLS-1$
			return new PropertyDescriptor("The converted object type", IPropertyDescriptor.NUMBER, choices); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.dataprocessing.IPropertyDescriptorProvider#validateProperties(java.util.Map)
	 */
	public void validateProperties(Map<String, String> properties)
	{
	}

	public void setClientPluginAccess(IClientPluginAccess clientPluginAccess)
	{
		this.clientPluginAccess = clientPluginAccess;
	}

}
