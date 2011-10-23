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
package com.servoy.extensions.plugins.agent;

import com.servoy.j2db.documentation.ServoyDocumented;

@ServoyDocumented(publicName = AgentPlugin.PLUGIN_NAME, scriptingName = "plugins." + AgentPlugin.PLUGIN_NAME)
public class DummyAgentImpl implements IJSAgent
{

	public int js_getX()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int js_getY()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Deprecated
	public void js_hide()
	{
		// TODO Auto-generated method stub

	}

	public void js_setBalloonSize(int width, int height)
	{
		// TODO Auto-generated method stub

	}

	public void js_setImageURL(String url)
	{
		// TODO Auto-generated method stub

	}

	public void js_setLocation(int x, int y)
	{
		// TODO Auto-generated method stub

	}

	@Deprecated
	public void js_setX(int x)
	{
		// TODO Auto-generated method stub

	}

	@Deprecated
	public void js_setY(int y)
	{
		// TODO Auto-generated method stub

	}

	@Deprecated
	public void js_show()
	{
		// TODO Auto-generated method stub

	}

	public void js_speak(String message)
	{
		// TODO Auto-generated method stub

	}

	public void js_setVisible(boolean visible)
	{
		// TODO Auto-generated method stub

	}
}
