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

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import com.servoy.j2db.dataui.PropertyEditorClass;
import com.servoy.j2db.dataui.PropertyEditorHint;
import com.servoy.j2db.dataui.PropertyEditorOption;
import com.servoy.j2db.util.Debug;


/**
 * DBTreeView bean info
 * 
 * @author gboros
 */
public class DBTreeViewBeanInfo extends SimpleBeanInfo
{
	public DBTreeViewBeanInfo()
	{
		super();
		loadIcons();
	}

	@Override
	public PropertyDescriptor[] getPropertyDescriptors()
	{
		try
		{
			PropertyDescriptor name = new PropertyDescriptor("name", DBTreeView.class); //$NON-NLS-1$
			PropertyDescriptor border = new PropertyDescriptor("border", DBTreeView.class); //$NON-NLS-1$
			PropertyDescriptor foreground = new PropertyDescriptor("foreground", DBTreeView.class); //$NON-NLS-1$
			PropertyDescriptor background = new PropertyDescriptor("background", DBTreeView.class); //$NON-NLS-1$
			PropertyDescriptor opaque = new PropertyDescriptor("opaque", DBTreeView.class); //$NON-NLS-1$
			PropertyDescriptor font = new PropertyDescriptor("font", DBTreeView.class); //$NON-NLS-1$
			PropertyDescriptor loc = new PropertyDescriptor("location", DBTreeView.class); //$NON-NLS-1$
			PropertyDescriptor size = new PropertyDescriptor("size", DBTreeView.class); //$NON-NLS-1$
			PropertyDescriptor styleClass = new PropertyDescriptor("styleClass", DBTreeView.class); //$NON-NLS-1$
			PropertyEditorHint styleClassEditorHint = new PropertyEditorHint(PropertyEditorClass.styleclass);
			styleClassEditorHint.setOption(PropertyEditorOption.styleLookupName, DBTreeView.ELEMENT_TYPE.toLowerCase());
			styleClass.setValue(PropertyEditorHint.PROPERTY_EDITOR_HINT, styleClassEditorHint);
			PropertyDescriptor result[] = { name, border, foreground, background, opaque, font, loc, size, styleClass };
			return result;
		}
		catch (Exception ex)
		{
			Debug.error("DBTreeViewBeanInfo: unexpected exeption: " + ex); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	public Image getIcon(int iconKind)
	{
		switch (iconKind)
		{
			case BeanInfo.ICON_COLOR_16x16 :
				return icon16;
			case BeanInfo.ICON_COLOR_32x32 :
				return icon32;
		}
		return null;
	}

	protected Image icon16, icon32;

	protected void loadIcons()
	{
		icon16 = loadImage("res/icon/DBTreeView16.gif"); //$NON-NLS-1$
		icon32 = loadImage("res/icon/DBTreeView32.gif"); //$NON-NLS-1$
	}
}
