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

import com.servoy.j2db.plugins.PluginException;

/**
 * @version v1.0 3-jan-2005 marceltrapman
 * @author marceltrapman
 */
public interface IToolBar
{
	public String getSample(String methodName);

	public String getToolTip(String methodName);

	public String[] getParameterNames(String methodName);

	public boolean isDeprecated(String methodName);

	public Class< ? >[] getAllReturnedTypes();

	public void js_addButton(String text, Function method) throws PluginException;

	public void js_addButton(String text, Function method, Object[] arguments) throws PluginException;

	public void js_addButton(String text, Function method, Object[] arguments, Object icon) throws PluginException;

	public void js_addButton(String text, Function method, Object[] arguments, Object icon, String tooltip) throws PluginException;

	public void js_addButton(String text, Function method, Object[] arguments, Object icon, String tooltip, boolean enabled) throws PluginException;

	public void js_addButton(String text, Function method, Object[] arguments, Object icon, String tooltip, boolean enabled, boolean visible)
		throws PluginException;

	public void js_addComboBox(Function method, int index, String[] arguments) throws PluginException;

	public void js_addComboBox(Function method, int index, String[] arguments, String tooltip) throws PluginException;

	public void js_addComboBox(Function method, int index, String[] arguments, String tooltip, boolean enabled) throws PluginException;

	public void js_addComboBox(Function method, int index, String[] arguments, String tooltip, boolean enabled, boolean visible) throws PluginException;

	public void js_addCheckBox(String text, Function method) throws PluginException;

	public void js_addCheckBox(String text, Function method, boolean selected) throws PluginException;

	public void js_addCheckBox(String text, Function method, boolean selected, String tooltip) throws PluginException;

	public void js_addCheckBox(String text, Function method, boolean selected, String tooltip, boolean enabled) throws PluginException;

	public void js_addCheckBox(String text, Function method, boolean selected, String tooltip, boolean enabled, boolean visible) throws PluginException;

	public void js_addField(Function method, String text) throws PluginException;

	public void js_addField(Function method, String text, int length) throws PluginException;

	public void js_addField(Function method, String text, int length, String tooltip) throws PluginException;

	public void js_addField(Function method, String text, int length, String tooltip, boolean enabled) throws PluginException;

	public void js_addField(Function method, String text, int length, String tooltip, boolean enabled, boolean visible) throws PluginException;

	public void js_addSeparator();

	public void js_removeItem(int index) throws PluginException;

	public void js_enableItem(int index, boolean enabled) throws PluginException;

	public void js_visibleItem(int index, boolean visible) throws PluginException;

	public void js_selectCheckBox(int index, boolean selected) throws PluginException;

	public void js_selectComboBox(int index, int selection) throws PluginException;

	public void js_setFieldText(int index, String text) throws PluginException;

	public void js_removeAllItems();

	public void js_validate();
}