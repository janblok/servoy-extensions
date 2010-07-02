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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

import javax.swing.border.Border;

/**
 * Interface to which both the smart and webclient tree need to conform
 * 
 * @author gboros
 */
public interface ITreeView
{
	public Border getBorder();

	public void setBorder(Border border);

	public Color getForeground();

	public void setForeground(Color foreground);

	public Color getBackground();

	public void setBackground(Color background);

	public boolean isOpaque();

	public void setOpaque(boolean opaque);

	public Font getFont();

	public void setFont(Font font);

	public Point getLocation();

	public void setLocation(Point location);

	public Dimension getSize();

	public void setSize(Dimension size);
}