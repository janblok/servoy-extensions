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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.border.Border;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.ITreeStateListener;
import org.apache.wicket.model.IModel;
import org.mozilla.javascript.Function;

import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.server.headlessclient.dataui.StyleAttributeModifierModel;
import com.servoy.j2db.ui.IStylePropertyChanges;
import com.servoy.j2db.util.DataSourceUtils;


public class WicketDBTreeView extends BaseTree implements IWicketTree, IHeaderContributor
{
	private static final long serialVersionUID = 1L;

	private final WicketTree wicketTree;
	private final BindingInfo bindingInfo;

	private static final ResourceReference IMAGES = new ResourceReference(WicketDBTreeView.class, "res/base-tree-images.png"); //$NON-NLS-1$

	private final IClientPluginAccess application;


	protected WicketDBTreeView(String id, IClientPluginAccess application)
	{
		super(id);
		this.application = application;
		bindingInfo = new BindingInfo(application);
		getTreeState().addTreeStateListener(new ITreeStateListener()
		{

			public void allNodesCollapsed()
			{
				// ignore
			}

			public void allNodesExpanded()
			{
				// ignore
			}

			public void nodeCollapsed(Object node)
			{
				// ignore
			}

			public void nodeExpanded(Object node)
			{
				// ignore
			}

			public void nodeSelected(Object node)
			{
				WicketDBTreeView.this.onNodeLinkClicked(node);
			}

			public void nodeUnselected(Object node)
			{
				// ignore
			}

		});
		wicketTree = new WicketTree(this, bindingInfo, application);

		setRootLess(true);
		add(StyleAttributeModifierModel.INSTANCE);

		IMAGES.bind(Application.get());
	}

	/**
	 * @see com.servoy.j2db.ui.IComponent#setCursor(java.awt.Cursor)
	 */
	public void setCursor(Cursor cursor)
	{
		wicketTree.setCursor(cursor);
	}

	@Override
	public void onBeforeRender()
	{
		wicketTree.onBeforeRender();
		super.onBeforeRender();
	}

	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		super.onRender(markupStream);
		synchronized (wicketTree)
		{
			if (!wicketTree.hasChanged)
			{
				wicketTree.jsChangeRecorder.setRendered();
			}
			wicketTree.hasChanged = false;
		}
	}

	@Override
	public String getMarkupId()
	{
		if (getParent() instanceof ListItem)
		{
			return getParent().getId() + Component.PATH_SEPARATOR + getId();
		}
		/*
		 * a compilable version (with obfuscation within the plugins project) of the following if (getParent() instanceof CellContainer)
		 */
		else if (getParent() != null && getParent().getParent() instanceof ListItem)
		{
			ListItem li = (ListItem)getParent().getParent();
			return li.getId() + Component.PATH_SEPARATOR + getId();
		}
		else
		{
			return getId();
		}
	}

	private static final ResourceReference MY_CSS = new CompressedResourceReference(WicketDBTreeView.class, "res/base-tree.css");

	@Override
	protected ResourceReference getCSS()
	{
		return MY_CSS;
	}

	@Override
	protected Component newJunctionLink(MarkupContainer parent, final String id, final Object node)
	{
		final MarkupContainer junctionLink;

		if (isLeaf(node) == false)
		{
			final ILinkCallback junctionLinkCallback = new ILinkCallback()
			{
				private static final long serialVersionUID = 1L;

				public void onClick(AjaxRequestTarget target)
				{
					if (WicketDBTreeView.this.isEnabled())
					{
						if (isNodeExpanded(node))
						{
							getTreeState().collapseNode(node);
						}
						else
						{
							getChildCount(node);
							getTreeState().expandNode(node);
						}
						onJunctionLinkClicked(target, node);

						updateTree(target);
					}
				}
			};

			if (getLinkType() == LinkType.AJAX)
			{
				junctionLink = new AjaxLinkWithIndicator(id)
				{
					private static final long serialVersionUID = 1L;

					/**
					 * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
					 */
					@Override
					public void onClick(AjaxRequestTarget target)
					{
						junctionLinkCallback.onClick(target);
					}

					public String getAjaxIndicatorMarkupId()
					{
						return isNodeExpanded(node) ? null : "indicator";
					}
				};
			}
			else junctionLink = newLink(id, junctionLinkCallback);


			junctionLink.add(new AbstractBehavior()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onComponentTag(Component component, ComponentTag tag)
				{
					if (isNodeExpanded(node))
					{
						tag.put("class", "junction-open");
					}
					else
					{
						tag.put("class", "junction-closed");
					}
				}
			});

			junctionLink.setEnabled(wicketTree.isEnabled());
		}
		else
		{
			junctionLink = new WebMarkupContainer(id)
			{
				private static final long serialVersionUID = 1L;

				/**
				 * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
				 */
				@Override
				protected void onComponentTag(ComponentTag tag)
				{
					super.onComponentTag(tag);
					tag.setName("span");
					tag.put("class", "junction-corner");
				}
			};

		}

		return junctionLink;
	}

	@Override
	protected Component newNodeComponent(String id, IModel model)
	{

		WicketDBTreeViewNode nodeComp = new WicketDBTreeViewNode(id, model, this)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target)
			{
				tree.getTreeState().selectNode(node, !tree.getTreeState().isNodeSelected(node));
				generateAjaxResponse(target);
				((WicketDBTreeView)tree).updateTree(target);
			}

			@Override
			protected void onNodeCheckboxClicked(TreeNode node, BaseTree tree, AjaxRequestTarget target)
			{
				WicketDBTreeView.this.onNodeCheckboxClicked(target, node);
			}

		};

		WicketTreeNodeStyleAdapter treeNodeStyleAdapter = new WicketTreeNodeStyleAdapter(nodeComp);

		Properties prop = getStylePropertyChanges().getChanges();

		if (prop.get("color") != null)
		{
			treeNodeStyleAdapter.setContentColor((String)prop.get("color"));
		}


		treeNodeStyleAdapter.setContentFont((String)prop.get("font-family"), (String)prop.get("font-size"), (String)prop.get("font-style"),
			(String)prop.get("font-weight"));

		treeNodeStyleAdapter.setNodeEnabled(isEnabled());
		if (model.getObject() instanceof FoundSetTreeModel.UserNode)
		{
			FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)model.getObject();
			treeNodeStyleAdapter.setTooltip(bindingInfo.getToolTipText(un));
			Font unFont = bindingInfo.getFont(un);
			if (unFont != null) treeNodeStyleAdapter.setContentFont(unFont);

		}

		return nodeComp;

	}

	protected void generateAjaxResponse(AjaxRequestTarget target)
	{
		synchronized (wicketTree)
		{
			boolean isChanged = wicketTree.jsChangeRecorder.isChanged();
			wicketTree.jsChangeRecorder.setRendered();
			if (application instanceof IWebClientPluginAccess) ((IWebClientPluginAccess)application).generateAjaxResponse(target);
			if (isChanged) wicketTree.jsChangeRecorder.setChanged();
		}
	}

	protected void onNodeLinkClicked(Object tn)
	{
		if (tn instanceof FoundSetTreeModel.UserNode)
		{
			wicketTree.getTreeState().selectNode(tn, true);

			FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)tn;
			IRecord r = un.getRecord();
			if (r != null)
			{
				String returnProvider = bindingInfo.getReturnDataprovider(un);
				if (returnProvider == null)
				{
					returnProvider = wicketTree.bindingInfo.getReturnDataprovider((FoundSetTreeModel.UserNode)tn);
				}

				String[] server_table = DataSourceUtils.getDBServernameTablename(un.getFoundSet().getDataSource());
				Object[] args = new Object[] { r.getValue(returnProvider), (server_table == null ? null : server_table[1]) };

				FunctionDefinition f = wicketTree.bindingInfo.getMethodToCallOnClick((FoundSetTreeModel.UserNode)tn);
				if (f != null)
				{
					f.execute(application, args, false);
				}
			}
		}
	}

	protected void onNodeCheckboxClicked(AjaxRequestTarget target, TreeNode tn)
	{
		if (tn instanceof FoundSetTreeModel.UserNode)
		{
			FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)tn;
			IRecord r = un.getRecord();
			if (r != null)
			{
				bindingInfo.setCheckBox(un, !bindingInfo.isCheckBoxChecked(un));
				String returnProvider = bindingInfo.getReturnDataproviderOnCheckBoxChange(un);
				if (returnProvider == null)
				{
					returnProvider = wicketTree.bindingInfo.getReturnDataproviderOnCheckBoxChange((FoundSetTreeModel.UserNode)tn);
				}

				String[] server_table = DataSourceUtils.getDBServernameTablename(un.getFoundSet().getDataSource());
				Object[] args = new Object[] { r.getValue(returnProvider), (server_table == null ? null : server_table[1]) };


				FunctionDefinition f = wicketTree.bindingInfo.getMethodToCallOnCheckBoxChange((FoundSetTreeModel.UserNode)tn);
				if (f != null)
				{
					f.execute(application, args, false);
				}
			}
		}
		generateAjaxResponse(target);
	}

	public void js_setRoots(Object[] vargs)
	{
		wicketTree.js_setRoots(vargs);
	}

	public void js_setCallBackInfo(Function methodToCallOnClick, String returndp)//can be related dp, when clicked and passed as argument to method
	{
		wicketTree.js_setCallBackInfo(methodToCallOnClick, returndp);
	}

	public void js_bindNodeTooltipTextDataProvider(String dp)//can be related dp
	{
		wicketTree.js_bindNodeTooltipTextDataProvider(dp);
	}

	public void js_bindNodeChildSortDataProvider(String dp)//can be related dp
	{
		wicketTree.js_bindNodeChildSortDataProvider(dp);
	}

	public void js_bindNodeFontTypeDataProvider(String dp)//can be related dp
	{
		wicketTree.js_bindNodeFontTypeDataProvider(dp);
	}

	public void js_bindNodeImageURLDataProvider(String dp)//can be related dp
	{
		wicketTree.js_bindNodeImageURLDataProvider(dp);
	}

	public void js_bindNodeImageMediaDataProvider(String dp)//can be related dp
	{
		wicketTree.js_bindNodeImageMediaDataProvider(dp);
	}

	public void js_setNRelationName(String n_relationName)//normally self join
	{
		wicketTree.js_setNRelationName(n_relationName);
	}

	public void js_setMRelationName(String m_relationName)//incase of n-m inbetween table
	{
		wicketTree.js_setMRelationName(m_relationName);
	}

	/*
	 * readonly/editable---------------------------------------------------
	 */
	public boolean js_isEditable()
	{
		return wicketTree.js_isEditable();
	}

	public void js_setEditable(boolean editable)
	{
		wicketTree.js_setEditable(editable);
	}

	public boolean js_isReadOnly()
	{
		return wicketTree.js_isReadOnly();
	}

	public void js_setReadOnly(boolean b)
	{
		wicketTree.js_setReadOnly(b);
	}

	/*
	 * name---------------------------------------------------
	 */
	public String js_getName()
	{
		return wicketTree.js_getName();
	}

	public void setName(String name)
	{
		wicketTree.setName(name);
	}

	public String getName()
	{
		return wicketTree.getName();
	}

	/*
	 * border---------------------------------------------------
	 */
	public void setBorder(Border border)
	{
		wicketTree.setBorder(border);
	}

	public Border getBorder()
	{
		return wicketTree.getBorder();
	}


	/*
	 * opaque---------------------------------------------------
	 */
	public void setOpaque(boolean opaque)
	{
		wicketTree.setOpaque(opaque);
	}

	public boolean js_isTransparent()
	{
		return wicketTree.js_isTransparent();
	}

	public void js_setTransparent(boolean b)
	{
		wicketTree.js_setTransparent(b);
	}

	public boolean isOpaque()
	{
		return wicketTree.isOpaque();
	}


	/*
	 * tooltip---------------------------------------------------
	 */
	public String js_getToolTipText()
	{
		return wicketTree.js_getToolTipText();
	}

	public void setToolTipText(String tooltip)
	{
		wicketTree.setToolTipText(tooltip);
	}

	public void js_setToolTipText(String tooltip)
	{
		wicketTree.js_setToolTipText(tooltip);
	}

	/**
	 * @see com.servoy.j2db.ui.IComponent#getToolTipText()
	 */
	public String getToolTipText()
	{
		return wicketTree.getToolTipText();
	}

	/*
	 * font---------------------------------------------------
	 */
	public void setFont(Font font)
	{
		wicketTree.setFont(font);
	}

	public void js_setFont(String spec)
	{
		wicketTree.js_setFont(spec);
	}

	public Font getFont()
	{
		return wicketTree.getFont();
	}


	/*
	 * bgcolor---------------------------------------------------
	 */
	public String js_getBgcolor()
	{
		return wicketTree.js_getBgcolor();
	}

	public void js_setBgcolor(String bgcolor)
	{
		wicketTree.js_setBgcolor(bgcolor);
	}

	public void setBackground(Color cbg)
	{
		wicketTree.setBackground(cbg);
	}

	public Color getBackground()
	{
		return wicketTree.getBackground();
	}


	/*
	 * fgcolor---------------------------------------------------
	 */
	public String js_getFgcolor()
	{
		return wicketTree.js_getFgcolor();
	}

	public void js_setFgcolor(String fgcolor)
	{
		wicketTree.js_setFgcolor(fgcolor);
	}

	public void setForeground(Color cfg)
	{
		wicketTree.setForeground(cfg);
	}

	public Color getForeground()
	{
		return wicketTree.getForeground();
	}


	/*
	 * visible---------------------------------------------------
	 */
	public void setComponentVisible(boolean visible)
	{
		wicketTree.setComponentVisible(visible);
	}

	public boolean js_isVisible()
	{
		return wicketTree.js_isVisible();
	}

	public void js_setVisible(boolean visible)
	{
		wicketTree.js_setVisible(visible);
	}

	/*
	 * enabled---------------------------------------------------
	 */
	public void js_setEnabled(boolean enabled)
	{
		wicketTree.js_setEnabled(enabled);
	}

	public void setComponentEnabled(boolean enabled)
	{
		wicketTree.setComponentEnabled(enabled);
	}


	public boolean js_isEnabled()
	{
		return wicketTree.js_isEnabled();
	}

	/*
	 * location---------------------------------------------------
	 */
	public int js_getLocationX()
	{
		return wicketTree.js_getLocationX();
	}

	public int js_getLocationY()
	{
		return wicketTree.js_getLocationY();
	}

	public void js_setLocation(int x, int y)
	{
		wicketTree.js_setLocation(x, y);
	}

	public void setLocation(Point location)
	{
		wicketTree.setLocation(location);
	}

	public Point getLocation()
	{
		return wicketTree.getLocation();
	}


	/*
	 * size---------------------------------------------------
	 */
	public Dimension getSize()
	{
		return wicketTree.getSize();
	}

	public void js_setSize(int width, int height)
	{
		wicketTree.js_setSize(width, height);
	}

	public void setSize(Dimension size)
	{
		wicketTree.setSize(size);
	}

	public int js_getWidth()
	{
		return wicketTree.js_getWidth();
	}

	public int js_getHeight()
	{
		return wicketTree.js_getHeight();
	}

	/*
	 * jsmethods---------------------------------------------------
	 */
	public void js_setNodeLevelVisible(int level, boolean visible)
	{
		wicketTree.js_setNodeLevelVisible(level, visible);
	}


	public Object[] js_getSelectionPath()
	{
		return wicketTree.js_getSelectionPath();
	}

	public void js_setSelectionPath(Object[] selectionPath)
	{
		wicketTree.js_setSelectionPath(selectionPath);
	}


	public void js_setExpandNode(Object[] nodePath, boolean expand_collapse)
	{
		wicketTree.js_setExpandNode(nodePath, expand_collapse);
	}

	public boolean js_isNodeExpanded(Object[] nodePath)
	{
		return wicketTree.js_isNodeExpanded(nodePath);
	}

	public void js_refresh()
	{
		wicketTree.js_refresh();
	}

	public void js_setRowHeight(int rowHeight)
	{
	}

	public Class[] getAllReturnedTypes()
	{
		return wicketTree.getAllReturnedTypes();
	}

	public String[] getParameterNames(String methodName)
	{
		return wicketTree.getParameterNames(methodName);
	}

	public String getSample(String methodName)
	{
		return wicketTree.getSample(methodName);
	}

	public String getToolTip(String methodName)
	{
		return wicketTree.getToolTip(methodName);
	}

	public boolean isDeprecated(String methodName)
	{
		return wicketTree.isDeprecated(methodName);
	}

	public IStylePropertyChanges getStylePropertyChanges()
	{
		return wicketTree.jsChangeRecorder;
	}

	public Binding js_createBinding(String... args)
	{
		Binding binding = new Binding();
		if (args.length == 2)
		{
			binding.setServerName(args[0]);
			binding.setTableName(args[1]);
		}
		else
		{
			binding.setDataSource(args[0]);
		}

		bindingInfo.addBinding(binding);

		return binding;
	}


	public void js_addRoots(Object foundSet)
	{
		wicketTree.js_addRoots(foundSet);
	}

	public void js_removeAllRoots()
	{
		wicketTree.js_removeAllRoots();
	}

	public RelationInfo js_createRelationInfo()
	{
		return wicketTree.js_createRelationInfo();
	}

	/**
	 * Creates a link of type specified by current linkType. When the links is clicked it calls the specified callback.
	 * 
	 * @param id The component id
	 * @param callback The link call back
	 * @return The link component
	 */
	@Override
	public MarkupContainer newLink(String id, final ILinkCallback callback)
	{
		if (getLinkType() == LinkType.AJAX)
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
		else return super.newLink(id, callback);
	}

	private abstract class AjaxLinkWithIndicator extends OnlyTargetAjaxLink implements IAjaxIndicatorAware
	{
		public AjaxLinkWithIndicator(String id)
		{
			super(id);
		}

	}

	public void renderHead(IHeaderResponse response)
	{
		Iterator selectedNodesIte = getTreeState().getSelectedNodes().iterator();
		if (selectedNodesIte.hasNext())
		{
			TreeNode firstSelectedNode = (TreeNode)selectedNodesIte.next();
			Component nodeComponent = getNodeComponent(firstSelectedNode);
			if (nodeComponent != null)
			{
				String treeId = getMarkupId();
				String nodeId = nodeComponent.getMarkupId();
				response.renderOnDomReadyJavascript("document.getElementById('" + treeId + "').scrollTop = document.getElementById('" + nodeId +
					"').offsetTop;\n");
			}
		}
	}
}
