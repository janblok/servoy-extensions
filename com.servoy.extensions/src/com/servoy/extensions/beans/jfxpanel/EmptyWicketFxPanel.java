/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2013 Servoy BV

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

package com.servoy.extensions.beans.jfxpanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

import javax.swing.border.Border;

import org.apache.wicket.markup.html.basic.Label;

import com.servoy.j2db.ui.IComponent;

/**
 * @author lvostinar
 *
 */
public class EmptyWicketFxPanel extends Label implements IComponent, IJFXPanel
{
	private final String id;

	public EmptyWicketFxPanel(String id)
	{
		super(id, "JFXPanel is not supported in web client");
		this.id = id;
	}

	@Override
	public boolean isJavaFXAvailable()
	{
		return false;
	}

	@Override
	public void setComponentEnabled(boolean enabled)
	{
	}

	@Override
	public void setComponentVisible(boolean visible)
	{

	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public void setLocation(Point location)
	{

	}

	@Override
	public Point getLocation()
	{
		return null;
	}

	@Override
	public void setSize(Dimension size)
	{

	}

	@Override
	public Dimension getSize()
	{
		return null;
	}

	@Override
	public void setForeground(Color foreground)
	{

	}

	@Override
	public Color getForeground()
	{
		return null;
	}

	@Override
	public void setBackground(Color background)
	{

	}

	@Override
	public Color getBackground()
	{
		return null;
	}

	@Override
	public void setFont(Font font)
	{

	}

	@Override
	public Font getFont()
	{
		return null;
	}

	@Override
	public void setBorder(Border border)
	{

	}

	@Override
	public Border getBorder()
	{
		return null;
	}

	@Override
	public void setName(String name)
	{

	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public void setOpaque(boolean opaque)
	{

	}

	@Override
	public boolean isOpaque()
	{
		return false;
	}

	@Override
	public void setCursor(Cursor cursor)
	{

	}

	@Override
	public void setToolTipText(String tooltip)
	{

	}

	@Override
	public String getToolTipText()
	{
		return null;
	}
}
