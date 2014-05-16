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

import java.beans.PropertyDescriptor;

import com.servoy.extensions.beans.dbtreeview.DBTreeView;
import com.servoy.extensions.beans.dbtreeview.DBTreeViewBeanInfo;
import com.servoy.j2db.dataui.PropertyEditorClass;
import com.servoy.j2db.dataui.PropertyEditorHint;
import com.servoy.j2db.dataui.PropertyEditorOption;
import com.servoy.j2db.util.Debug;

/**
 * DBTreeTableView bean info
 * 
 * @author gboros
 */
public class DBTreeTableViewBeanInfo extends DBTreeViewBeanInfo
{
	@Override
	public PropertyDescriptor[] getPropertyDescriptors()
	{
		try
		{
			PropertyDescriptor name = new PropertyDescriptor("name", DBTreeTableView.class);
			PropertyDescriptor border = new PropertyDescriptor("border", DBTreeTableView.class);
			PropertyDescriptor foreground = new PropertyDescriptor("foreground", DBTreeTableView.class);
			PropertyDescriptor background = new PropertyDescriptor("background", DBTreeTableView.class);
			PropertyDescriptor opaque = new PropertyDescriptor("opaque", DBTreeTableView.class);
			PropertyDescriptor font = new PropertyDescriptor("font", DBTreeTableView.class);
			PropertyDescriptor loc = new PropertyDescriptor("location", DBTreeTableView.class);
			PropertyDescriptor size = new PropertyDescriptor("size", DBTreeTableView.class);
			PropertyDescriptor styleClass = new PropertyDescriptor("styleClass", DBTreeTableView.class);
			PropertyEditorHint styleClassEditorHint = new PropertyEditorHint(PropertyEditorClass.styleclass);
			styleClassEditorHint.setOption(PropertyEditorOption.styleLookupName, DBTreeView.ELEMENT_TYPE.toLowerCase());
			styleClass.setValue(PropertyEditorHint.PROPERTY_EDITOR_HINT, styleClassEditorHint);
			PropertyDescriptor result[] = { name, border, foreground, background, opaque, font, loc, size, styleClass };
			return result;
		}
		catch (Exception ex)
		{
			Debug.error("DBTreeViewBeanInfo: unexpected exeption: " + ex);
			return null;
		}
	}

	@Override
	protected void loadIcons()
	{
		icon16 = loadImage("res/icon/DBTreeTableView16.gif");
		icon32 = loadImage("res/icon/DBTreeTableView32.gif");
	}
}
