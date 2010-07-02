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
	private final String fieldDp;
	private final String tableName;

	public DBTreeTableColumn(String columnId, BindingInfo bindingInfo, String header, String fieldDp, String tableName)
	{
		super(columnId, new Model(header));

		this.bindingInfo = bindingInfo;
		this.fieldDp = fieldDp;
		this.tableName = tableName;
	}

	public String getNodeValue(TreeNode node)
	{
		String nodeValue = "";

		if (node instanceof FoundSetTreeModel.UserNode)
		{
			FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)node;
			nodeValue = bindingInfo.getText(un, fieldDp, tableName);
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
