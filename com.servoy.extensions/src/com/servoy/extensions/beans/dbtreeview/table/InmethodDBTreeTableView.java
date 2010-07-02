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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.Model;
import org.mozilla.javascript.Function;

import com.inmethod.grid.SizeUnit;
import com.inmethod.grid.common.ColumnsState;
import com.inmethod.grid.treegrid.TreeGrid;
import com.servoy.extensions.beans.dbtreeview.Binding;
import com.servoy.extensions.beans.dbtreeview.BindingInfo;
import com.servoy.extensions.beans.dbtreeview.FoundSetTreeModel;
import com.servoy.extensions.beans.dbtreeview.IWicketTree;
import com.servoy.extensions.beans.dbtreeview.RelationInfo;
import com.servoy.extensions.beans.dbtreeview.WicketTree;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.server.headlessclient.dataui.StyleAttributeModifierModel;
import com.servoy.j2db.ui.IStylePropertyChanges;
import com.servoy.j2db.util.DataSourceUtils;


/**
 * Class representing the web client db tree table view
 * 
 * @author gboros
 */
public class InmethodDBTreeTableView extends TreeGrid implements IWicketTree, ITreeTableScriptMethods
{
	private static final long serialVersionUID = 1L;

	private final WicketTree wicketTree;
	private final BindingInfo bindingInfo;

	private final IClientPluginAccess application;
	private final DBTreeTableView dbTreeTableView;
	private final Model dbTreeTableTreeColumnHeaderModel = new Model("");
	private final DBTreeTableTreeColumn dbTreeTableTreeColumn;

	private final List columns;

	public InmethodDBTreeTableView(String id, IClientPluginAccess application, List columns, DBTreeTableView dbTreeTableView)
	{
		super(id, new Model((Serializable)(new JTree()).getModel()), columns);
		setOutputMarkupId(true);
		setVersioned(false);

		this.application = application;
		this.dbTreeTableView = dbTreeTableView;
		bindingInfo = new BindingInfo(application);
		this.columns = columns;

		dbTreeTableTreeColumn = new DBTreeTableTreeColumn("treeColumn", dbTreeTableTreeColumnHeaderModel);
		columns.add(dbTreeTableTreeColumn);
		setColumnState(new ColumnsState(columns));

		getTree().setRootLess(true);
		wicketTree = new WicketTree(getTree(), bindingInfo, application);

		add(StyleAttributeModifierModel.INSTANCE);
	}

	/**
	 * @see com.servoy.j2db.ui.IComponent#setCursor(java.awt.Cursor)
	 */
	public void setCursor(Cursor cursor)
	{
		wicketTree.setCursor(cursor);
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

	/**
	 * @see wicket.Component#getMarkupId()
	 */
	@Override
	public String getMarkupId()
	{
		if (getParent() instanceof ListItem)
		{
			return getParent().getId() + Component.PATH_SEPARATOR + getId();
		}
		else
		{
			return getId();
		}
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

	protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode tn)
	{
		if (tn instanceof FoundSetTreeModel.UserNode)
		{
			wicketTree.getTreeState().selectNode(tn, true);

			FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)tn;
			IRecord r = un.getRecord();
			if (r != null)
			{
				String returnProvider = bindingInfo.getReturnDataprovider(un);

				String[] server_table = DataSourceUtils.getDBServernameTablename(un.getFoundSet().getDataSource());
				Object[] args = new Object[] { r.getValue(returnProvider), (server_table == null ? null : server_table[1]) };

				FunctionDefinition f = wicketTree.bindingInfo.getMethodToCallOnClick((FoundSetTreeModel.UserNode)tn);
				if (f != null)
				{
					f.executeSync(application, args);
				}
			}
		}

		generateAjaxResponse(target);
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

				String[] server_table = DataSourceUtils.getDBServernameTablename(un.getFoundSet().getDataSource());
				Object[] args = new Object[] { r.getValue(returnProvider), (server_table == null ? null : server_table[1]) };


				FunctionDefinition f = wicketTree.bindingInfo.getMethodToCallOnCheckBoxChange((FoundSetTreeModel.UserNode)tn);
				if (f != null)
				{
					f.executeSync(application, args);
				}
			}
		}

		generateAjaxResponse(target);
	}

	public WicketTree getWicketTree()
	{
		return this.wicketTree;
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
		setEnabled(enabled);
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
		setContentHeight(height - 30, SizeUnit.PX);
		wicketTree.js_setSize(width, -1);
	}

	public void setSize(Dimension size)
	{
		setContentHeight(size.height - 30, SizeUnit.PX);
		size.height = -1;
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

	public Class[] getAllReturnedTypes()
	{
		return DBTreeTableView.getAllReturnedTypes();
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

	public Column js_createColumn(String servername, String tablename, String header, String fieldname)
	{
		Column column = new Column();
		column.setDBTreeTableView(dbTreeTableView);
		column.setServerName(servername);
		column.setTableName(tablename);
		column.setHeader(header);
		column.setDataprovider(fieldname);

		dbTreeTableView.addColumn(column);
		while (columns.size() > 1)
			columns.remove(1);
		ArrayList<ArrayList<Column>> sameHeaderColumns = dbTreeTableView.getColumns();
		for (int i = 0; i < sameHeaderColumns.size(); i++)
		{
			columns.add(new DBTreeTableColumn(Integer.toString(columns.size()), bindingInfo, sameHeaderColumns.get(i)));
		}
		setColumnState(new ColumnsState(columns));

		return column;
	}

	public void js_removeAllColumns()
	{
		dbTreeTableView.removeAllColumns();
		Object theTree = columns.get(0);
		columns.clear();
		columns.add(theTree);
		setColumnState(new ColumnsState(columns));
	}

	public void js_setTreeColumnHeader(String treeColumnHeader)
	{
		dbTreeTableView.setTreeColumnHeader(treeColumnHeader);
		dbTreeTableTreeColumnHeaderModel.setObject(treeColumnHeader);
	}

	public void js_setRowHeight(int height)
	{
	}

	private static final CompressedResourceReference CSS = new CompressedResourceReference(InmethodDBTreeTableView.class, "res/style.css"); //$NON-NLS-1$

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.renderCSSReference(CSS);
	}


	@Override
	public void onBeforeRender()
	{
		wicketTree.onBeforeRender();
		super.onBeforeRender();
	}

	public RelationInfo js_createRelationInfo()
	{
		return wicketTree.js_createRelationInfo();
	}
}