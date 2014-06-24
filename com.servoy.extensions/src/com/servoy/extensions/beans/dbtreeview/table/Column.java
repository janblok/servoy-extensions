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

import com.servoy.base.scripting.annotations.ServoyClientSupport;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;

/**
 * Script object holding informations needed to display a tree table column
 * 
 * @author gboros
 */
@ServoyDocumented(category = ServoyDocumented.BEANS, publicName = "Column")
@ServoyClientSupport(ng = false, wc = true, sc = true)
public class Column implements IReturnedTypesProvider, IScriptable
{
	private String datasource;

	private String header;
	private String dataprovider;
	private int preferredWidth;

	private DBTreeTableView dbTreeTableView;

	public void setDBTreeTableView(DBTreeTableView dbTreeTableView)
	{
		this.dbTreeTableView = dbTreeTableView;
	}

	public void setDatasource(String datasource)
	{
		this.datasource = datasource;
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

	/**
	 * Set column dataprovider
	 * 
	 * @sample
	 * column.setDataprovider('fieldName');
	 * 
	 * @param fieldName
	 * 
	 */
	public void js_setDataprovider(String fieldName)
	{
		this.dataprovider = fieldName;
	}

	public String getHeader()
	{
		return header;
	}

	/**
	 * Set column header text
	 * 
	 * @sample
	 * column.setHeader('header text');
	 * 
	 * @param header
	 * 
	 */
	public void js_setHeader(String header)
	{
		this.header = header;
		if (dbTreeTableView != null) dbTreeTableView.flagColumnsChanged();
	}

	public String getDatasource()
	{
		return datasource;
	}

	public Class[] getAllReturnedTypes()
	{
		return null;
	}
}
