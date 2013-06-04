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
package com.servoy.extensions.beans.dbtreeview;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.LinkIconPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * Class representing a tree node in the web based tree
 * 
 * @author gboros
 */
public abstract class WicketDBTreeViewNode extends LinkIconPanel implements WicketTreeNode
{
	private static final long serialVersionUID = 1L;

	private Label nodeLabel;
	private MarkupContainer contentLink;


	public WicketDBTreeViewNode(String id, IModel model, BaseTree tree)
	{
		super(id, model, tree);
	}


	public Label getTreeNodeLabel()
	{
		return nodeLabel;
	}

	public MarkupContainer getContentLink()
	{
		return contentLink;
	}

	/**
	 * @see org.apache.wicket.markup.html.tree.LabelIconPanel#addComponents(org.apache.wicket.model.IModel, org.apache.wicket.markup.html.tree.BaseTree)
	 */
	@Override
	protected void addComponents(final IModel model, final BaseTree tree)
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

		MarkupContainer link = tree.newLink("iconLink", callback);
		add(link);

		link.add(newImageComponent("icon", tree, model));

		contentLink = tree.newLink("contentLink", callback);
		add(contentLink);
		contentLink.add(newContentComponent("content", tree, model));
	}

	protected Component newCheckboxComponent(String componentId, final BaseTree tree, final IModel model)
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

	@Override
	protected Component newContentComponent(String componentId, BaseTree tree, IModel model)
	{

		if (model.getObject() instanceof FoundSetTreeModel.UserNode)
		{
			((FoundSetTreeModel.UserNode)model.getObject()).addModificationListener();
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

		nodeLabel = (Label)super.newContentComponent(componentId, tree, model);
		nodeLabel.setEscapeModelStrings(false);
		return nodeLabel;
	}

	static ConcurrentMap<String, ResourceReference> mediaUrlToResource = new ConcurrentHashMap<String, ResourceReference>();

	/**
	 * Creates the icon component for the node
	 * 
	 * @param componentId
	 * @param tree
	 * @param model
	 * @return icon image component
	 */
	@Override
	protected Component newImageComponent(String componentId, final BaseTree tree, final IModel model)
	{
		final Object treeNode = model.getObject();
		Component imgComp = null;

		if (treeNode != null && treeNode instanceof FoundSetTreeModel.UserNode)
		{
			Icon nodeIcon = ((FoundSetTreeModel.UserNode)treeNode).getIcon();

			if (nodeIcon != null)
			{
				final Object mediaUrl = ((FoundSetTreeModel.UserNode)treeNode).getUserObject();
				final ResourceReference imageResource = WicketTreeNodeStyleAdapter.imageResource(nodeIcon);
				if (imageResource != null)
				{
					imgComp = new Image(componentId)
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected ResourceReference getImageResourceReference()
						{
							String key = ((FoundSetTreeModel.UserNode)treeNode).getUserObject().toString();
							if (mediaUrlToResource.containsKey(key))
							{
								return mediaUrlToResource.get(key);
							}
							else
							{
								ResourceReference resRefference = WicketTreeNodeStyleAdapter.imageResource(((FoundSetTreeModel.UserNode)treeNode).getIcon());
								resRefference.bind(getApplication());
								resRefference.getResource().setCacheable(true);
								mediaUrlToResource.putIfAbsent(((FoundSetTreeModel.UserNode)treeNode).getUserObject().toString(), resRefference);

								return resRefference;
							}
						}
					};
					imgComp.add(new SimpleAttributeModifier("width", "" + nodeIcon.getIconWidth()));
					imgComp.add(new SimpleAttributeModifier("height", "" + nodeIcon.getIconHeight()));
				}
			}

		}

		if (imgComp == null) imgComp = super.newImageComponent(componentId, tree, model);


		return imgComp;
	}

	protected abstract void onNodeCheckboxClicked(TreeNode node, BaseTree tree, AjaxRequestTarget target);
}
