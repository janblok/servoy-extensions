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

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.BaseTree.ILinkCallback;
import org.apache.wicket.markup.html.tree.BaseTree.LinkType;
import org.apache.wicket.markup.html.tree.LabelIconPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.inmethod.grid.column.tree.AbstractTreeColumn;
import com.servoy.extensions.beans.dbtreeview.FoundSetTreeModel;
import com.servoy.extensions.beans.dbtreeview.OnlyTargetAjaxLink;
import com.servoy.extensions.beans.dbtreeview.WicketTreeNodeStyleAdapter;

/**
 * Class representing a tree node in the web based tree table
 * 
 * @author gboros
 */
public abstract class DBTreeTableTreeNode extends Panel
{
	private static final long serialVersionUID = 1L;

	private final AbstractTreeColumn treeColumn;
	private Label nodeLabel;
	private MarkupContainer contentLink;


	/**
	 * Constructs the panel.
	 * 
	 * @param id component id
	 * @param model model that is used to access the TreeNode
	 * @param tree
	 */
	public DBTreeTableTreeNode(String id, IModel model, AbstractTreeColumn treeColumn)
	{
		super(id, model);
		this.treeColumn = treeColumn;

		addComponents(model, treeColumn.getTreeGrid().getTree());
	}

	public Label getTreeNodeLabel()
	{
		return nodeLabel;
	}

	protected void addComponents(final IModel model, final AbstractTree tree)
	{
		BaseTree.ILinkCallback callback = new BaseTree.ILinkCallback()
		{
			private static final long serialVersionUID = 1L;
			private long lastClickTime;

			public void onClick(AjaxRequestTarget target)
			{
				long clickTimeout = (System.currentTimeMillis() - lastClickTime);
				lastClickTime = System.currentTimeMillis();
				TreeNode node = (TreeNode)model.getObject();
				if (clickTimeout < 1000) tree.getTreeState().selectNode(node, false); // simulate dblclick
				if (!tree.getTreeState().getSelectedNodes().contains(node)) onNodeLinkClicked(node, tree, target);
			}
		};

		add(newCheckboxComponent("chBox", tree, model));

		MarkupContainer link = newLink("iconLink", callback);
		add(link);

		link.add(newImageComponent("icon", tree, model));

		contentLink = newLink("contentLink", callback);
		add(contentLink);
		contentLink.add(newContentComponent("content", tree, model));
	}

	protected Component newContentComponent(String componentId, AbstractTree tree, IModel model)
	{
		if (model.getObject() instanceof FoundSetTreeModel.UserNode)
		{
			String nodeText = ((FoundSetTreeModel.UserNode)model.getObject()).toString();

			if (nodeText != null)
			{
				nodeText = nodeText.trim();
				if ((nodeText.startsWith("<html>") || nodeText.startsWith("<HTML>")) && (nodeText.endsWith("</html>") || nodeText.endsWith("</HTML>")))
				{
					nodeText = nodeText.substring(6, nodeText.length() - 7);
					model = new Model(nodeText);
				}
			}
		}

		nodeLabel = new Label(componentId, model);
		nodeLabel.setEscapeModelStrings(false);
		return nodeLabel;
	}

	protected Component newCheckboxComponent(String componentId, final AbstractTree tree, final IModel model)
	{

		AjaxCheckBox cb = new AjaxCheckBox(componentId)
		{

			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				if (model.getObject() instanceof FoundSetTreeModel.UserNode)
				{
					onNodeCheckboxClicked((TreeNode)model.getObject(), tree, target);
				}
			}

			@Override
			public String getModelValue()
			{
				if (model.getObject() instanceof FoundSetTreeModel.UserNode)
				{
					return ((FoundSetTreeModel.UserNode)model.getObject()).isCheckBoxChecked() ? "true" : "false";
				}

				return "false";
			}

		};


		if (model.getObject() instanceof FoundSetTreeModel.UserNode)
		{
			cb.setVisible(((FoundSetTreeModel.UserNode)model.getObject()).hasCheckBox());
			if (!((FoundSetTreeModel.UserNode)model.getObject()).isCheckBoxEnabled()) cb.add(new AttributeModifier("disabled", true, new Model("disabled")));
		}
		else
		{
			cb.setVisible(false);
		}

		return cb;
	}

	/**
	 * Creates the icon component for the node
	 * 
	 * @param componentId
	 * @param tree
	 * @param model
	 * @return icon image component
	 */
	protected Component newImageComponent(String componentId, final AbstractTree tree, final IModel model)
	{
		Object treeNode = model.getObject();
		Component imgComp = null;

		if (treeNode != null && treeNode instanceof FoundSetTreeModel.UserNode)
		{
			Icon nodeIcon = ((FoundSetTreeModel.UserNode)treeNode).getIcon();

			if (nodeIcon != null)
			{
				final ResourceReference imageResource = WicketTreeNodeStyleAdapter.imageResource(nodeIcon);
				if (imageResource != null)
				{
					imgComp = new Image(componentId)
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected ResourceReference getImageResourceReference()
						{
							return imageResource;
						}
					};

					imgComp.add(new SimpleAttributeModifier("width", "" + nodeIcon.getIconWidth()));
					imgComp.add(new SimpleAttributeModifier("height", "" + nodeIcon.getIconHeight()));
				}
			}

		}

		if (imgComp == null) imgComp = getDefaultIcon(componentId, tree, model);


		return imgComp;
	}


	/**
	 * Creates a link of type specified by current linkType. When the links is clicked it calls the specified callback.
	 * 
	 * @param id The component id
	 * @param callback The link call back
	 * @return The link component
	 */
	private MarkupContainer newLink(String id, final ILinkCallback callback)
	{
		if (getLinkType() == LinkType.REGULAR)
		{
			return new Link(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.markup.html.link.Link#onClick()
				 */
				@Override
				public void onClick()
				{
					callback.onClick(null);
				}
			};
		}
		else if (getLinkType() == LinkType.AJAX)
		{
			return new OnlyTargetAjaxLink(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
				 */
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					callback.onClick(target);
				}
			};
		}
		else
		{
			return new AjaxFallbackLink(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.ajax.markup.html.AjaxFallbackLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
				 */
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					callback.onClick(target);
				}
			};
		}
	}

	/**
	 * Returns the current type of links on tree items.
	 * 
	 * @return The link type
	 */
	private LinkType getLinkType()
	{
		return LinkType.AJAX;
	}

	/**
	 * Creates default icon for the node
	 * 
	 * @param componentId
	 * @param tree
	 * @param model
	 * @return icon image component
	 */
	private Component getDefaultIcon(String componentId, final AbstractTree tree, final IModel model)
	{
		return new Image(componentId)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected ResourceReference getImageResourceReference()
			{
				return DBTreeTableTreeNode.this.getImageResourceReference(tree, (TreeNode)model.getObject());
			}
		};
	}

	/**
	 * Returns the image resource reference based on the give tree node type.
	 * 
	 * @param tree
	 * @param node
	 * @return image resource reference
	 */
	private ResourceReference getImageResourceReference(AbstractTree tree, TreeNode node)
	{
		if (node.isLeaf())
		{
			return RESOURCE_ITEM;
		}
		else
		{
			if (tree.getTreeState().isNodeExpanded(node))
			{
				return RESOURCE_FOLDER_OPEN;
			}
			else
			{
				return RESOURCE_FOLDER_CLOSED;
			}
		}
	}

	private static final ResourceReference RESOURCE_FOLDER_OPEN = new ResourceReference(LabelIconPanel.class, "res/folder-open.gif");
	private static final ResourceReference RESOURCE_FOLDER_CLOSED = new ResourceReference(LabelIconPanel.class, "res/folder-closed.gif");
	private static final ResourceReference RESOURCE_ITEM = new ResourceReference(LabelIconPanel.class, "res/item.gif");


	protected void onNodeLinkClicked(TreeNode node, AbstractTree tree, AjaxRequestTarget target)
	{
		tree.getTreeState().selectNode(node, !tree.getTreeState().isNodeSelected(node));
		tree.updateTree(target);
	}

	protected abstract void onNodeCheckboxClicked(TreeNode node, AbstractTree tree, AjaxRequestTarget target);
}
