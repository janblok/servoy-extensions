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
package com.servoy.extensions.beans.dbtreeview.table;

import com.servoy.j2db.scripting.IScriptObject;

/**
 * Script object holding informations needed to display a tree table column
 * 
 * @author gboros
 */
public class Column implements IScriptObject
{
	private String serverName;
	private String tableName;

	private String header;
	private String dataprovider;
	private int preferredWidth;

	private DBTreeTableView dbTreeTableView;

	public void setDBTreeTableView(DBTreeTableView dbTreeTableView)
	{
		this.dbTreeTableView = dbTreeTableView;
	}

	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public void setPreferredWidth(int preferredWidth)
	{
		this.preferredWidth = preferredWidth;
	}

	public int getPreferredWidth()
	{
		return preferredWidth;
	}

	public String getDataprovider()
	{
		return dataprovider;
	}

	public void js_setDataprovider(String fieldName)
	{
		this.dataprovider = fieldName;
	}

	public String getHeader()
	{
		return header;
	}

	public void js_setHeader(String header)
	{
		this.header = header;
		if (dbTreeTableView != null) dbTreeTableView.flagColumnsChanged();
	}

	public String getServerName()
	{
		return serverName;
	}

	public String getTableName()
	{
		return tableName;
	}

	public Class[] getAllReturnedTypes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getParameterNames(String methodName)
	{
		if (methodName.endsWith("Header"))
		{
			return new String[] { "headerText" };
		}
		else if (methodName.endsWith("Dataprovider"))
		{
			return new String[] { "fieldName" };
		}

		return null;
	}

	public String getSample(String methodName)
	{
		if (methodName.endsWith("Header"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("column.setHeader('header text');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("Dataprovider"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("column.setDataprovider('fieldName');\n"); //$NON-NLS-1$
			return retval.toString();
		}

		return null;
	}

	public String getToolTip(String methodName)
	{
		if (methodName.endsWith("Header"))
		{
			return "Set column header text";
		}
		else if (methodName.endsWith("Dataprovider"))
		{
			return "Set column dataprovider";
		}

		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
