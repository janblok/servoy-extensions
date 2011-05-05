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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.model.Model;
import org.mozilla.javascript.Function;

import com.servoy.j2db.dataprocessing.IFoundSet;
import com.servoy.j2db.dataprocessing.IFoundSetInternal;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.dataprocessing.ISwingFoundSet;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.server.headlessclient.dataui.ChangesRecorder;
import com.servoy.j2db.server.headlessclient.dataui.TemplateGenerator;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.ui.ISupportWebBounds;
import com.servoy.j2db.util.ComponentFactoryHelper;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.HtmlUtils;
import com.servoy.j2db.util.PersistHelper;
import com.servoy.j2db.util.Utils;

/**
 * Class representing the web client tree
 * 
 * @author gboros
 */
public class WicketTree implements IComponent, ITreeViewScriptMethods, TableModelListener, ISupportWebBounds
{
	private final AbstractTree abstractTree;
	public BindingInfo bindingInfo;
	private final Binding defaultBinding = new Binding();
	private transient FoundSetTreeModel treemodel;

	private Cursor cursor;

	public ChangesRecorder jsChangeRecorder = new ChangesRecorder(TemplateGenerator.DEFAULT_FIELD_BORDER_SIZE, TemplateGenerator.DEFAULT_FIELD_PADDING);

	private Color background;
	private Color foreground;
	private Dimension size;
	private Font font;
	private Point location;
	private Border border;
	private String tooltip;
	private String name;
	private boolean editable;
	private boolean editState;
	private boolean opaque;
	private boolean jsVisible = true;


	private final boolean accessible = true;


	// need to rebuild the tree because its structure has been changed
	public boolean needRebuild;

	// tree data has been changed
	public boolean hasChanged;

	public WicketTree(AbstractTree abstractTree, BindingInfo bindingInfo, IClientPluginAccess application)
	{
		this.abstractTree = abstractTree;
		this.bindingInfo = bindingInfo;

		if (abstractTree != null)
		{
			treemodel = new FoundSetTreeModel(application, bindingInfo, this);
			abstractTree.setModel(new Model(treemodel));
		}
	}

	public void onBeforeRender()
	{
		synchronized (this)
		{
			if (needRebuild)
			{
				needRebuild = false;
				hasChanged = false;
				js_refresh();
			}
		}
	}

	public void js_bindNodeChildSortDataProvider(String dp)
	{
		defaultBinding.js_setChildSortDataprovider(dp);
	}

	public void js_bindNodeFontTypeDataProvider(String dp)
	{
		defaultBinding.js_setFontTypeDataprovider(dp);
	}

	public void js_bindNodeImageMediaDataProvider(String dp)
	{
		defaultBinding.js_setImageMediaDataprovider(dp);
	}

	public void js_bindNodeImageURLDataProvider(String dp)
	{
		defaultBinding.js_setImageURLDataprovider(dp);
	}

	public void js_bindNodeTooltipTextDataProvider(String dp)
	{
		defaultBinding.js_setToolTipTextDataprovider(dp);
	}

	public String js_getBgcolor()
	{
		return PersistHelper.createColorString(background);
	}

	public String js_getFgcolor()
	{
		return PersistHelper.createColorString(foreground);
	}

	public int js_getHeight()
	{
		return size.height;
	}

	public int js_getLocationX()
	{
		return location.x;
	}

	public int js_getLocationY()
	{
		return location.y;
	}

	public String js_getName()
	{
		return getName();
	}

	public Object[] js_getSelectionPath()
	{
		Object[] path = null;

		Collection selectedNodes = abstractTree.getTreeState().getSelectedNodes();
		Iterator selectedNodesIte = selectedNodes.iterator();

		if (selectedNodesIte.hasNext())
		{
			path = ((DefaultMutableTreeNode)selectedNodesIte.next()).getPath();
		}

		return treePathToArray(path);
	}

	public String js_getToolTipText()
	{
		return tooltip;
	}

	public int js_getWidth()
	{
		return size.width;
	}

	public boolean js_isReadOnly()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean js_isEditable()
	{
		return editable;
	}

	public boolean js_isEnabled()
	{
		return abstractTree.isEnabled();
	}

	public boolean js_isNodeExpanded(Object[] nodePath)
	{
		List path = pathToTreeNodeList(nodePath, true);

		if (path != null && path.size() > 0 && path.size() == nodePath.length)
		{
			TreeNode lastNode = (TreeNode)path.get(path.size() - 1);

			ITreeState treeState = abstractTree.getTreeState();

			return treeState.isNodeExpanded(lastNode);
		}

		return false;
	}

	public boolean js_isTransparent()
	{
		return !opaque;
	}

	public boolean js_isVisible()
	{
		return jsVisible;
	}

	public void js_refresh()
	{
		ArrayList expandedNodes = new ArrayList();
		getExpandedNodes((DefaultMutableTreeNode)treemodel.getRoot(), expandedNodes);

//		Object[] selectionPath = js_getSelectionPath();

		treemodel.resetRoot();
		abstractTree.setModel(new Model(treemodel));
		abstractTree.invalidateAll();

		ITreeState treeState = abstractTree.getTreeState();
		treeState.collapseAll();

		Object[] expandedPath;
		for (int i = 0; i < expandedNodes.size(); i++)
		{
			expandedPath = (Object[])expandedNodes.get(i);
			if (expandedPath != null && expandedPath.length > 0) setExpandNode(expandedPath, true);
		}

//		js_setSelectionPath(selectionPath);

		updateTree();
	}


	public void js_setRowHeight(int rowHeight)
	{
	}

	public void js_setBgcolor(String bg)
	{
		background = PersistHelper.createColor(bg);
		jsChangeRecorder.setBgcolor(bg);
		jsChangeRecorder.setTransparent(!opaque);
	}

	public void js_setCallBackInfo(Function f, String returndp)
	{
		defaultBinding.js_setMethodToCallOnClick(f, returndp);
	}


	public void js_setReadOnly(boolean b)
	{
		if (b && !editable) return;
		if (b)
		{
			setEditable(false);
			editState = true;
		}
		else
		{
			setEditable(editState);
		}
		jsChangeRecorder.setChanged();
	}

	public void js_setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public void js_setEnabled(boolean enabled)
	{
		setComponentEnabled(enabled);
		jsChangeRecorder.setChanged();
	}

	public void js_setExpandNode(Object[] path, boolean expand_collapse)
	{
		setExpandNode(path, expand_collapse);
		updateTree();
	}

	public void js_setFgcolor(String fg)
	{
		foreground = PersistHelper.createColor(fg);
		jsChangeRecorder.setFgcolor(fg);
	}

	public void js_setFont(String spec)
	{
		font = PersistHelper.createFont(spec);
		jsChangeRecorder.setFont(spec);
	}

	public void js_setLocation(int x, int y)
	{
		location = new Point(x, y);
		jsChangeRecorder.setLocation(x, y);
	}

	public void js_setMRelationName(String m_relationName)
	{
		defaultBinding.js_setMRelationName(m_relationName);
	}

	public void js_setNRelationName(String n_relationName)
	{
		defaultBinding.js_setNRelationName(n_relationName);
	}

	public void js_setNodeLevelVisible(int level, boolean visible)
	{
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)treemodel.getRoot();

		setNodeLevelVisible(root, level, visible);
		updateTree();
	}

	public void js_setRoots(Object[] vargs)
	{
		IFoundSet fs = (IFoundSet)((vargs.length >= 1 && vargs[0] instanceof IFoundSet) ? vargs[0] : null);

		if (fs != null)
		{
			bindingInfo.removeRoots();
			bindingInfo.addRoots(fs);
			defaultBinding.setDataSource(fs.getDataSource());
			bindingInfo.addBinding(defaultBinding);
			defaultBinding.js_setTextDataprovider(((vargs.length >= 2 && vargs[1] != null) ? vargs[1].toString() : null));
			defaultBinding.js_setNRelationName(((vargs.length >= 3 && vargs[2] != null) ? vargs[2].toString() : defaultBinding.getNRelationName()));
			defaultBinding.js_setMRelationName(((vargs.length >= 4 && vargs[3] != null) ? vargs[3].toString() : defaultBinding.getMRelationName()));

			try
			{
				fs = ((IFoundSetInternal)fs).copy(false);
				ITreeState treeState = abstractTree.getTreeState();
				treeState.collapseAll();
			}
			catch (Exception ex)
			{
				Debug.error(ex);
			}

			js_refresh();
		}
	}

	public void js_setSelectionPath(Object[] path)
	{
		List selection = pathToTreeNodeList(path, true);
		if (selection != null && selection.size() > 0)
		{
			setSelectionPath(selection, true);
		}
		else
		{
			selection = pathToTreeNodeList(js_getSelectionPath(), false);
			setSelectionPath(selection, false);
		}
	}

	private void setSelectionPath(List selection, boolean isSelected)
	{
		if (selection != null && selection.size() > 0)
		{
			//selection.add(0, treemodel.getRoot());
			abstractTree.getTreeState().selectNode(selection.get(selection.size() - 1), isSelected);
			updateTree();
		}
	}

	public void js_setSize(int w, int h)
	{
		size = new Dimension(w, h);
		jsChangeRecorder.setSize(w, h, border, null, 0);
	}

	public Rectangle getWebBounds()
	{
		Dimension d = jsChangeRecorder.calculateWebSize(size.width, size.height, border, null, 0, null);
		return new Rectangle(location, d);
	}

	/**
	 * @see com.servoy.j2db.ui.ISupportWebBounds#getPaddingAndBorder()
	 */
	public Insets getPaddingAndBorder()
	{
		return jsChangeRecorder.getPaddingAndBorder(size.height, border, null, 0, null);
	}


	public void js_setToolTipText(String tip)
	{
		setToolTipText(tip);
		jsChangeRecorder.setChanged();
	}

	public void js_setTransparent(boolean transparent)
	{
		opaque = !transparent;
		jsChangeRecorder.setTransparent(transparent);
	}

	public void js_setVisible(boolean visible)
	{
		jsVisible = visible;
		jsChangeRecorder.setVisible(visible);
	}

	public Class[] getAllReturnedTypes()
	{
		return DBTreeView.getAllReturnedTypes();
	}

	public String[] getParameterNames(String methodName)
	{
		return DBTreeView.getParameterNames(methodName);
	}

	public String getSample(String methodName)
	{
		return DBTreeView.getSample(methodName);
	}

	public String getToolTip(String methodName)
	{
		return DBTreeView.getToolTip(methodName);
	}

	public boolean isDeprecated(String methodName)
	{
		return DBTreeView.isDeprecated(methodName);
	}

	public Color getBackground()
	{
		return background;
	}

	public Border getBorder()
	{
		return border;
	}

	public Font getFont()
	{
		return font;
	}

	public Color getForeground()
	{
		return foreground;
	}

	public Point getLocation()
	{
		return location;
	}

	public String getName()
	{
		return name;
	}

	public Dimension getSize()
	{
		return size;
	}

	public boolean isOpaque()
	{
		return opaque;
	}

	public void setBackground(Color background)
	{
		this.background = background;
		if (background != null) jsChangeRecorder.setBgcolor(PersistHelper.createColorString(background));
	}

	public void setBorder(Border border)
	{
		this.border = border;
		if (border != null)
		{
			ComponentFactoryHelper.createBorderCSSProperties(ComponentFactoryHelper.createBorderString(border), jsChangeRecorder.getChanges());
		}
	}

	public void setComponentEnabled(boolean enabled)
	{
		if (accessible)
		{
			abstractTree.setEnabled(enabled);
			abstractTree.invalidateAll();
		}
	}

	public void setFont(Font font)
	{
		this.font = font;
		if (font != null) jsChangeRecorder.setFont(PersistHelper.createFontString(font));
	}

	public void setForeground(Color foreground)
	{
		this.foreground = foreground;
		if (foreground != null) jsChangeRecorder.setFgcolor(PersistHelper.createColorString(foreground));
	}

	public void setLocation(Point location)
	{
		this.location = location;
	}

	public void setOpaque(boolean opaque)
	{
		this.opaque = opaque;
		jsChangeRecorder.setTransparent(!opaque);
	}

	public void setSize(Dimension size)
	{
		this.size = size;
	}

	public void tableChanged(TableModelEvent e)
	{
		Object foundSet = e.getSource();

		if (foundSet instanceof ISwingFoundSet && treemodel != null)
		{
			int changeType = e.getType();

			synchronized (this)
			{
				switch (changeType)
				{
					case TableModelEvent.INSERT :
					case TableModelEvent.DELETE :
						needRebuild = true;
						hasChanged = true;
						jsChangeRecorder.setChanged();
						break;
					case TableModelEvent.UPDATE :
						hasChanged = true;
						jsChangeRecorder.setChanged();
						break;
				}
			}
		}
	}

	public void setEditable(boolean b)
	{
		editState = b;
		editable = b;
	}

	private Object[] treePathToArray(Object[] path)
	{
		if (path != null)
		{
			Object[] retval = new Object[path.length - 1];
			for (int i = 1; i < path.length; i++)
			{
				if (path[i] instanceof FoundSetTreeModel.UserNode)
				{
					FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)path[i];
					IRecord rec = un.getRecord();
					if (rec != null && rec.getPK() != null)
					{
						retval[i - 1] = rec.getPK()[0];
					}
				}
				else if (path[i] instanceof FoundSetTreeModel.RelationNode)
				{
					retval[i - 1] = new Integer(((FoundSetTreeModel.RelationNode)path[i]).getId());
				}
			}
			return retval;
		}
		else
		{
			return new Object[0];
		}
	}

	private List pathToTreeNodeList(Object[] nodePath, boolean expand)
	{
		List path = null;

		if (nodePath != null)
		{
			path = new ArrayList();

			DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode)treemodel.getRoot();

			for (int i = 0; i < nodePath.length; i++)
			{
				boolean found = false;
				Object pk = nodePath[i];

				for (int j = 0; j < lastNode.getChildCount(); j++)
				{
					DefaultMutableTreeNode child = (DefaultMutableTreeNode)lastNode.getChildAt(j);
					if (child instanceof FoundSetTreeModel.UserNode)
					{
						FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)child;
						IRecord rec = un.getRecord();
						if (rec != null && rec.getPK() != null)
						{
							if (Utils.equalObjects(rec.getPK()[0], pk))
							{
								path.add(child);
								lastNode = child;
								found = true;
								if (!lastNode.isLeaf())
								{
									treemodel.lazyLoadChilderenIfNeeded(lastNode);
									if (expand && i < nodePath.length - 1) abstractTree.getTreeState().expandNode(lastNode);
								}
								break;
							}
						}
					}
					else if (child instanceof FoundSetTreeModel.RelationNode)
					{
						FoundSetTreeModel.RelationNode un = (FoundSetTreeModel.RelationNode)child;
						if (new Integer(un.getId()).equals(pk))
						{
							path.add(child);
							lastNode = child;
							found = true;
							if (!lastNode.isLeaf())
							{
								treemodel.lazyLoadChilderenIfNeeded(lastNode);
								if (expand && i < nodePath.length - 1) abstractTree.getTreeState().expandNode(lastNode);
							}
							break;
						}
					}
				}
				if (!found) break;
			}
		}

		return path;
	}

	private void setExpandNode(Object[] nodePath, boolean expand_collapse)
	{
		List path = pathToTreeNodeList(nodePath, true);
		if (path != null && path.size() == nodePath.length)
		{
			TreeNode lastNode = (TreeNode)path.get(path.size() - 1);

			ITreeState treeState = abstractTree.getTreeState();
			if (expand_collapse)
			{
				treeState.expandNode(lastNode);
			}
			else
			{
				treeState.collapseNode(lastNode);
			}
		}
	}

	private void getExpandedNodes(DefaultMutableTreeNode treeNode, ArrayList expandedNodes)
	{
		if (abstractTree.getTreeState().isNodeExpanded(treeNode) || treemodel.getRoot().equals(treeNode))
		{
			expandedNodes.add(treePathToArray(treeNode.getPath()));
			int childCount = treeNode.getChildCount();
			for (int i = 0; i < childCount; i++)
			{
				getExpandedNodes((DefaultMutableTreeNode)treeNode.getChildAt(i), expandedNodes);
			}
		}
	}

	public void updateTree()
	{
		jsChangeRecorder.setChanged();
	}

	/**
	 * Set node level visibility
	 * 
	 * @param node starting node
	 * @param level of visibility
	 * @param visible
	 */
	private void setNodeLevelVisible(DefaultMutableTreeNode node, int level, boolean visible)
	{
		int nodeLevel = node.getLevel();

		if (!node.isLeaf())
		{
			ITreeState treeState = abstractTree.getTreeState();

			if (visible)
			{
				if (nodeLevel <= level)
				{
					if (nodeLevel > 0) // not root
					{
						treeState.expandNode(node);
					}

					int childCount = node.getChildCount();

					for (int i = 0; i < childCount; i++)
					{
						setNodeLevelVisible((DefaultMutableTreeNode)node.getChildAt(i), level, visible);
					}
				}
			}
			else
			{
				if (treeState.isNodeExpanded(node) || nodeLevel < 1) // node expanded or root
				{
					int childCount = node.getChildCount();

					for (int i = 0; i < childCount; i++)
					{
						setNodeLevelVisible((DefaultMutableTreeNode)node.getChildAt(i), level, visible);
					}

					if (nodeLevel >= level && nodeLevel > 0) // nodeLevel > 0 (not root)
					{
						treeState.collapseNode(node);
					}
				}
			}

		}
	}

	public String getId()
	{
		return abstractTree.getId();
	}

	public String getToolTipText()
	{
		return tooltip;
	}

	public boolean isEnabled()
	{
		return abstractTree.isEnabled();
	}

	public boolean isVisible()
	{
		return abstractTree.isVisible();
	}

	public void setComponentVisible(boolean visible)
	{
		jsVisible = visible;
		abstractTree.setVisible(visible);
	}

	public void setCursor(Cursor cursor)
	{
		this.cursor = cursor;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setToolTipText(String tip)
	{
		if (!Utils.stringIsEmpty(tip))
		{
			if (!Utils.stringContainsIgnoreCase(tip, "<html")) //$NON-NLS-1$
			{
				tooltip = tip;
			}
			else if (HtmlUtils.hasUsefulHtmlContent(tip))
			{
				tooltip = tip;
			}
		}
		else
		{
			tooltip = null;
		}
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

	public int js_addRoots(Object foundSet)
	{
		int addedRootNodes = 0;
		if (foundSet instanceof IFoundSet)
		{
			bindingInfo.addRoots((IFoundSet)foundSet);
			js_refresh();
			addedRootNodes = ((IFoundSet)foundSet).getSize();
		}
		return addedRootNodes;
	}

	public void js_removeAllRoots()
	{
		bindingInfo.removeRoots();
	}

	public ITreeState getTreeState()
	{
		return abstractTree.getTreeState();
	}

	public void updateTree(AjaxRequestTarget target)
	{
		abstractTree.updateTree(target);
	}

	public RelationInfo js_createRelationInfo()
	{
		return new RelationInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeViewScriptMethods#js_setOnDrag(org.mozilla.javascript.Function)
	 */
	public void js_setOnDrag(Function fOnDrag)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeViewScriptMethods#js_setOnDragEnd(org.mozilla.javascript.Function)
	 */
	public void js_setOnDragEnd(Function fOnDragEnd)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeViewScriptMethods#js_setOnDragOver(org.mozilla.javascript.Function)
	 */
	public void js_setOnDragOver(Function fOnDragOver)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeViewScriptMethods#js_setOnDrop(org.mozilla.javascript.Function)
	 */
	public void js_setOnDrop(Function fOnDrop)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#setStyleClass(java.lang.String)
	 */
	public void setStyleClass(String styleClass)
	{
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#getStyleClass()
	 */
	public String getStyleClass()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#isTransparent()
	 */
	public boolean isTransparent()
	{
		return !isOpaque();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#setTransparent(boolean)
	 */
	public void setTransparent(boolean transparent)
	{
		setOpaque(!transparent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#getBorderType()
	 */
	public Border getBorderType()
	{
		return getBorder();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#setBorderType(javax.swing.border.Border)
	 */
	public void setBorderType(Border border)
	{
		setBorder(border);
	}
}
