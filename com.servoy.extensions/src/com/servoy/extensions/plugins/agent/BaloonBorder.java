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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Polygon;

import javax.swing.border.Border;

/**
 * @author		jblok
 */
public class BaloonBorder implements Border
{
	private Color background;
	public BaloonBorder()
	{
		background = new Color(255, 255, 204);
	}

	public Insets getBorderInsets(Component c)
	{
		return new Insets(10, 10, 10, 20);
	}
	
	public boolean isBorderOpaque()
	{
		return false;
	}
	
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
	{
		Polygon p = new Polygon();
		p.addPoint(0, 0);
		p.addPoint(10, 10);
		p.addPoint(0, 10);
		p.translate(width - 11, height - 21);

		g.setColor(background);
		g.fillRoundRect(x, y, width - 11, height - 1, 10, 10);
		g.fillPolygon(p);
		g.setColor(Color.black);
		g.drawRoundRect(x, y, width - 11, height - 1, 10, 10);
		g.drawPolygon(p);
		g.setColor(background);
		g.fillPolygon(p);
	}
}
