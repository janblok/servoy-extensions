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
package com.servoy.extensions.plugins.window.util;


import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import com.servoy.j2db.util.Debug;

/**
 * Utilities
 * 
 * @author marceltrapman
 * @since 6-sep-2006
 */
public class Utilities
{
	/**
	 * Create ImageIcon of imageURL or Byte[]
	 * 
	 * @param imageName The url or byte[] representing the image
	 * @return
	 */
	public static ImageIcon getImageIcon(Object imageName)
	{
		if (imageName == null || "".equals(imageName) || " ".equals(imageName)) //$NON-NLS-1$ //$NON-NLS-2$
		{
			return new ImageIcon();
		}

		if (imageName instanceof String)
		{
			return getImageIcon((String)imageName);
		}
		else if (imageName instanceof byte[])
		{
			return getImageIcon((byte[])imageName);
		}
		return new ImageIcon();
	}

	private static ImageIcon getImageIcon(byte[] bits)
	{
		if (bits == null)
		{
			return new ImageIcon();
		}
		return new ImageIcon(bits);
	}

	private static ImageIcon getImageIcon(String imageName)
	{
		URL imageURL = null;
		ImageIcon image = new ImageIcon();

		if (imageName == null || imageName.trim().equals("")) //$NON-NLS-1$
		{
			return new ImageIcon();
		}
		else
		{
			if (imageName.startsWith("media:///")) //$NON-NLS-1$
			{
				try
				{
					imageURL = new URL(imageName);
				}
				catch (MalformedURLException e)
				{
					Debug.error(e);
				}
			}
			if (imageURL != null)
			{
				image = new ImageIcon(imageURL);
			}
			else
			{
				image = new ImageIcon(imageName);
			}
		}
		return image;
	}
}