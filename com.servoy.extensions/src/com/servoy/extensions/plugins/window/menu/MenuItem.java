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
package com.servoy.extensions.plugins.window.menu;

import org.mozilla.javascript.Function;

import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.IConstantsObject;

/**
 * Menu item scriptable.
 * 
 */

public class MenuItem extends AbstractMenuItem implements IConstantsObject
{
	public static final int ALIGN_CENTER = 0;
	public static final int ALIGN_LEFT = 2;
	public static final int ALIGN_RIGHT = 4;

	public MenuItem()
	{
		// only used by script engine
	}

	public MenuItem(IClientPluginAccess pluginAccess, IMenuHandler menuHandler, IMenuItem menuItem)
	{
		super(pluginAccess, menuHandler, menuItem);
	}

	@Override
	public MenuItem js_setIcon(Object icon)
	{
		super.js_setIcon(icon);
		return this;
	}

	@Override
	public MenuItem js_setAccelerator(String accelerator)
	{
		super.js_setAccelerator(accelerator);
		return this;
	}

	@Override
	public MenuItem js_setMethod(Function method)
	{
		super.js_setMethod(method);
		return this;
	}

	@Override
	public MenuItem js_setMethod(Function method, Object[] arguments)
	{
		super.js_setMethod(method, arguments);
		return this;
	}

	@Override
	public MenuItem js_setMnemonic(String mnemonic)
	{
		super.js_setMnemonic(mnemonic);
		return this;
	}

	@Override
	public MenuItem js_setVisible(boolean visible)
	{
		super.js_setVisible(visible);
		return this;
	}
}
