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

import com.servoy.j2db.scripting.IScriptObject;

public interface IJSAgent extends IScriptObject
{
	/**
	 * get or set the x position of the agent
	 *
	 * @sample
	 * //move the agent diagonaly
	 * plugins.agent.x = plugins.agent.x-10;
	 * plugins.agent.y = plugins.agent.y-10;
	 */
	public int js_getX();

	/**
	 * get or set the y position of the agent
	 *
	 * @sample
	 * //move the agent diagonaly
	 * plugins.agent.x = plugins.agent.x-10;
	 * plugins.agent.y = plugins.agent.y-10;
	 */
	public int js_getY();

	/**
	 * set the size of the balloon
	 *
	 * @sample plugins.agent.setBalloonSize(width,height)
	 *
	 * @param width 
	 *
	 * @param height 
	 */
	public void js_setBalloonSize(int w, int h);

	/**
	 * set a new image for this agent
	 *
	 * @sample plugins.agent.setImageURL("url")
	 *
	 * @param url 
	 */
	public void js_setImageURL(String url);

	/**
	 * set the agent location
	 *
	 * @sample plugins.agent.setLocation(100,100);
	 *
	 * @param x 
	 *
	 * @param y 
	 */
	public void js_setLocation(int x, int y);

	/**
	 * let the agent speak
	 *
	 * @sample plugins.agent.speak('hello nerd');
	 *
	 * @param message 
	 */
	public void js_speak(String s);

	/**
	 * show/hide the agent
	 *
	 * @sample plugins.agent.setVisible(true);
	 *
	 * @param visible 
	 */
	public void js_setVisible(boolean b);

	/** @deprecated */
	@Deprecated
	public void js_setX(int x);

	/** @deprecated */
	@Deprecated
	public void js_setY(int y);

	/**
	 * hide the agent
	 *
	 * @sample plugins.agent.hide();
	 */
	@Deprecated
	public void js_hide();

	/**
	 * show the agent
	 *
	 * @sample plugins.agent.show();
	 */
	@Deprecated
	public void js_show();
}
