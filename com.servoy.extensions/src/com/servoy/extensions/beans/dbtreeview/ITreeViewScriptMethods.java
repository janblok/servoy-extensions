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

import org.mozilla.javascript.Function;

import com.servoy.j2db.scripting.IScriptObject;

/**
 * Interface to enforce the swing and web bean instance to be the same
 * 
 * @author jblok
 */
public interface ITreeViewScriptMethods extends IScriptObject, ITreeView
{
	public String js_getBgcolor();

	public void js_setBgcolor(String bg);

	public String js_getFgcolor();

	public void js_setFgcolor(String fg);

	public int js_getLocationX();

	public int js_getLocationY();

	public void js_setLocation(int x, int y);

	public int js_getWidth();

	public int js_getHeight();

	public void js_setSize(int w, int h);

	public String js_getName();

	public Object[] js_getSelectionPath();

	public void js_setSelectionPath(Object[] path);

	public String js_getToolTipText();

	public void js_setToolTipText(String tip);

	public boolean js_isEnabled();

	public void js_setEnabled(boolean enabled);

	public boolean js_isTransparent();

	public void js_setTransparent(boolean transparent);

	public boolean js_isVisible();

	public void js_setVisible(boolean visible);

	public void js_setFont(String font);

	public void js_setExpandNode(Object[] path, boolean expand_collapse);

	public boolean js_isNodeExpanded(Object[] path);

	public void js_setNodeLevelVisible(int level, boolean visible);

	public void js_refresh();

	public int js_addRoots(Object foundSet);

	public void js_removeAllRoots();

	//servername_tablename or datasource
	public Binding js_createBinding(String... args);

//	public void js_activateBinding(Binding binding);
//	public void js_removeBinding(Binding binding);

	public RelationInfo js_createRelationInfo();

	public void js_setRowHeight(int height);

	public void js_setOnDrag(Function fOnDrag);

	public void js_setOnDragEnd(Function fOnDragEnd);

	public void js_setOnDragOver(Function fOnDragOver);

	public void js_setOnDrop(Function fOnDrop);

	/**
	 * for compatibility, just call addRoots,createBinding (store in compatibility Bindings instance) with passed info
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void js_setRoots(Object[] vargs);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void js_setMRelationName(String name);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void js_setNRelationName(String name);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void js_bindNodeFontTypeDataProvider(String dp);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void js_bindNodeImageMediaDataProvider(String dp);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void js_bindNodeImageURLDataProvider(String dp);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void js_bindNodeTooltipTextDataProvider(String dp);

	/**
	 * for compatibility, store this in a compatibility Binding instance
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void js_setCallBackInfo(Function f, String returndp);

	/**
	 * @deprecated
	 */
	@Deprecated
	public void js_bindNodeChildSortDataProvider(String dp);

}
