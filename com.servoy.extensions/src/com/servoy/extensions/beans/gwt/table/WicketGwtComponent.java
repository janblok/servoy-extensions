/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2012 Servoy BV

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

package com.servoy.extensions.beans.gwt.table;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.border.Border;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;

import com.servoy.j2db.dataprocessing.IFoundSet;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.util.Pair;

/**
 * @author jcompagner
 *
 */
public class WicketGwtComponent extends WebComponent implements IComponent, IScriptMethods
{

	private final IClientPluginAccess access;
	private Dimension size;
	private Point location;
	private Border border;
	private Font font;
	private String name;
	private String tooltip;
	private Color foreground;
	private Color background;
	private IFoundSet foundset;
	private final List<Pair<String, String>> columns = new ArrayList<Pair<String, String>>();

	/**
	 * @param id
	 * @param access 
	 */
	public WicketGwtComponent(String id, IClientPluginAccess access)
	{
		super(id);
		this.access = access;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.Component#renderHead(org.apache.wicket.markup.html.internal.HtmlHeaderContainer)
	 */
	@Override
	public void renderHead(HtmlHeaderContainer container)
	{
		super.renderHead(container);
		StringBuilder columnsArray = new StringBuilder();
		StringBuilder dataprovidersArray = new StringBuilder();
		columnsArray.append("[");
		dataprovidersArray.append("[");
		for (Pair<String, String> pair : columns)
		{
			dataprovidersArray.append("'");
			dataprovidersArray.append(pair.getLeft());
			dataprovidersArray.append("',");
			columnsArray.append("'");
			columnsArray.append(pair.getRight());
			columnsArray.append("',");
		}
		if (dataprovidersArray.length() > 0) dataprovidersArray.setLength(dataprovidersArray.length() - 1);
		if (columnsArray.length() > 0) columnsArray.setLength(columnsArray.length() - 1);
		columnsArray.append("]");
		dataprovidersArray.append("]");
		container.getHeaderResponse().renderOnLoadJavascript(
			"window.frames['gwtbean'].createTable(" + columnsArray.toString() + "," + dataprovidersArray.toString() + ", null" + ")");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.MarkupContainer#onComponentTagBody(org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		replaceComponentTagBody(markupStream, openTag, "<iframe frameborder='0' src='/DynaTable.html' name='gwtbean' id='gwtbean' tabIndex='-1' width='" +
			getSize().width + "px' height='" + getSize().height + "px'></iframe>");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setComponentEnabled(boolean)
	 */
	public void setComponentEnabled(boolean enabled)
	{
		setEnabled(enabled);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setComponentVisible(boolean)
	 */
	public void setComponentVisible(boolean visible)
	{
		setVisible(visible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setLocation(java.awt.Point)
	 */
	public void setLocation(Point location)
	{
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#getLocation()
	 */
	public Point getLocation()
	{
		return location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setSize(java.awt.Dimension)
	 */
	public void setSize(Dimension size)
	{
		this.size = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#getSize()
	 */
	public Dimension getSize()
	{
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setForeground(java.awt.Color)
	 */
	public void setForeground(Color foreground)
	{
		this.foreground = foreground;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#getForeground()
	 */
	public Color getForeground()
	{
		return foreground;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setBackground(java.awt.Color)
	 */
	public void setBackground(Color background)
	{
		this.background = background;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#getBackground()
	 */
	public Color getBackground()
	{
		return background;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setFont(java.awt.Font)
	 */
	public void setFont(Font font)
	{
		this.font = font;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#getFont()
	 */
	public Font getFont()
	{
		return font;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setBorder(javax.swing.border.Border)
	 */
	public void setBorder(Border border)
	{
		this.border = border;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#getBorder()
	 */
	public Border getBorder()
	{
		return border;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setName(java.lang.String)
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#getName()
	 */
	public String getName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setOpaque(boolean)
	 */
	public void setOpaque(boolean opaque)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#isOpaque()
	 */
	public boolean isOpaque()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setCursor(java.awt.Cursor)
	 */
	public void setCursor(Cursor cursor)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#setToolTipText(java.lang.String)
	 */
	public void setToolTipText(String tooltip)
	{
		this.tooltip = tooltip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.ui.IComponent#getToolTipText()
	 */
	public String getToolTipText()
	{
		return tooltip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.gwt.table.IScriptMethods#js_addColumn(java.lang.String, java.lang.String)
	 */
	public void js_addColumn(String dataprovider, String columnName)
	{
		columns.add(new Pair<String, String>(dataprovider, columnName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.gwt.table.IScriptMethods#js_showFoundSet(com.servoy.j2db.dataprocessing.IFoundSet)
	 */
	public void js_showFoundSet(IFoundSet foundset)
	{
		this.foundset = foundset;
	}

}
