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

import java.util.Properties;

import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.inmethod.grid.column.tree.AbstractTreeColumn;
import com.inmethod.icon.Icon;
import com.servoy.extensions.beans.dbtreeview.FoundSetTreeModel;
import com.servoy.extensions.beans.dbtreeview.WicketTree;
import com.servoy.extensions.beans.dbtreeview.WicketTreeNodeStyleAdapter;
import com.servoy.j2db.server.headlessclient.dataui.StyleAppendingModifier;

/**
 * Class representing the tree column in the web based tree table
 * 
 * @author gboros
 */
public class DBTreeTableTreeColumn extends AbstractTreeColumn
{
	public DBTreeTableTreeColumn(String columnId, IModel headerModel)
	{
		super(columnId, headerModel);
	}

	@Override
	protected Component newNodeComponent(String id, IModel model)
	{
		DBTreeTableTreeNode nodeComp = new DBTreeTableTreeNode(id, model, this)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onNodeLinkClicked(TreeNode node, AbstractTree tree, AjaxRequestTarget target)
			{
				InmethodDBTreeTableView treeGrid = (InmethodDBTreeTableView)getTreeGrid();
				treeGrid.onNodeLinkClicked(target, node);
			}

			@Override
			protected void onNodeCheckboxClicked(TreeNode node, AbstractTree tree, AjaxRequestTarget target)
			{
				InmethodDBTreeTableView treeGrid = (InmethodDBTreeTableView)getTreeGrid();
				treeGrid.onNodeCheckboxClicked(target, node);
			}

		};


		InmethodDBTreeTableView treeGrid = (InmethodDBTreeTableView)getTreeGrid();
		WicketTree wicketTree = treeGrid.getWicketTree();

		if (wicketTree != null)
		{
			WicketTreeNodeStyleAdapter treeNodeStyleAdapter = new WicketTreeNodeStyleAdapter(nodeComp);

			Properties prop = wicketTree.jsChangeRecorder.getChanges();

			if (prop.get("color") != null)
			{
				treeNodeStyleAdapter.setContentColor((String)prop.get("color"));
			}


			treeNodeStyleAdapter.setContentFont((String)prop.get("font-family"), (String)prop.get("font-size"), (String)prop.get("font-style"),
				(String)prop.get("font-weight"));

			treeNodeStyleAdapter.setNodeEnabled(treeGrid.isEnabled());
			if (model.getObject() instanceof FoundSetTreeModel.UserNode)
			{
				FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)model.getObject();
				treeNodeStyleAdapter.setTooltip(wicketTree.bindingInfo.getToolTipText(un));
				treeNodeStyleAdapter.setContentFont(wicketTree.bindingInfo.getFont(un));

			}

			int rowHeight = wicketTree.getRowHeight();
			if (rowHeight > 0)
			{
				nodeComp.getTreeNodeLabel().add(new StyleAppendingModifier(new Model<String>("line-height: " + rowHeight + "px;")));
			}
		}

		return nodeComp;
	}

	@Override
	protected Icon getIcon(IModel model)
	{
		return null;
	}

}
