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

import java.util.ArrayList;

import javax.swing.tree.TreeNode;

import org.apache.wicket.Response;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.inmethod.grid.IRenderable;
import com.inmethod.grid.column.AbstractLightWeightColumn;
import com.servoy.extensions.beans.dbtreeview.BindingInfo;
import com.servoy.extensions.beans.dbtreeview.FoundSetTreeModel;

/**
 * Class representing a column in the web based tree table
 * 
 * @author gboros
 */
public class DBTreeTableColumn extends AbstractLightWeightColumn
{
	private static final long serialVersionUID = 1L;

	private final BindingInfo bindingInfo;
	private final ArrayList<Column> columns;

	public DBTreeTableColumn(String columnId, BindingInfo bindingInfo, ArrayList<Column> columns)
	{
		super(columnId, new Model(columns.get(0).getHeader()));

		this.bindingInfo = bindingInfo;
		this.columns = columns;

		int preferredWidth = columns.get(0).getPreferredWidth();
		if (preferredWidth != -1) setInitialSize(preferredWidth);
	}

	public String getNodeValue(TreeNode node)
	{
		String nodeValue = "";

		if (node instanceof FoundSetTreeModel.UserNode)
		{
			FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)node;
			Column column;
			for (int i = 0; i < columns.size(); i++)
			{
				column = columns.get(i);
				nodeValue = bindingInfo.getText(un, column.getDataprovider(), column.getTableName());
				if (!"".equals(nodeValue)) break;
			}
		}

		return nodeValue;
	}

	@Override
	public IRenderable newCell(IModel rowModel)
	{
		return new IRenderable()
		{
			public void render(IModel rowModel, Response response)
			{
				CharSequence value = getNodeValue((TreeNode)rowModel.getObject());
				response.write(value);
			}
		};
	}
}
