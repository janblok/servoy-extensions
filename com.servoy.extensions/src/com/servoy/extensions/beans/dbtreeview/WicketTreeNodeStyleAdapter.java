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

import java.awt.Font;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.resource.BufferedDynamicImageResource;

import com.servoy.j2db.util.ImageLoader;
import com.servoy.j2db.util.Pair;
import com.servoy.j2db.util.PersistHelper;

public class WicketTreeNodeStyleAdapter
{
	private final MarkupContainer treeNode;

	private String color;
	private String fontFamily;
	private String fontSize;
	private String fontStyle;
	private String fontWeight;


	public WicketTreeNodeStyleAdapter(MarkupContainer treeNode)
	{
		this.treeNode = treeNode;
	}

	public void setContentColor(String contentColor)
	{
		this.color = contentColor;
		updateStyle();
	}

	public void setContentFont(Font font)
	{
		this.fontFamily = null;
		this.fontSize = null;
		this.fontStyle = null;
		this.fontWeight = null;
		if (font != null)
		{
			Pair[] fontPropetiesPair = PersistHelper.createFontCSSProperties(PersistHelper.createFontString(font));

			String fontProperty;
			for (Pair element : fontPropetiesPair)
			{
				if (element != null)
				{
					fontProperty = (String)element.getLeft();
					if (fontProperty != null)
					{
						if (fontProperty.equals("font-family"))
						{
							this.fontFamily = (String)element.getRight();
						}
						else if (fontProperty.equals("font-size"))
						{
							this.fontSize = (String)element.getRight();
						}
						else if (fontProperty.equals("font-style"))
						{
							this.fontStyle = (String)element.getRight();
						}
						else if (fontProperty.equals("font-weight"))
						{
							this.fontWeight = (String)element.getRight();
						}
					}
				}
			}

		}

		updateStyle();
	}

	public void setContentFont(String fontFamily, String fontSize, String fontStyle, String fontWeight)
	{

		this.fontFamily = fontFamily;
		this.fontSize = fontSize;
		this.fontStyle = fontStyle;
		this.fontWeight = fontWeight;

		updateStyle();
	}

	private void updateStyle()
	{
		StringBuffer styleString = new StringBuffer();

		Label nodeLabel = null;

		if (treeNode instanceof WicketTreeNode)
		{
			nodeLabel = ((WicketTreeNode)treeNode).getTreeNodeLabel();
		}

		if (nodeLabel != null)
		{
			if (color != null)
			{
				styleString.append("color:" + color);
				styleString.append(";");
			}

			if (fontFamily != null)
			{
				styleString.append("font-family:" + fontFamily);
				styleString.append(";");
			}

			if (fontSize != null)
			{
				styleString.append("font-size:" + fontSize);
				styleString.append(";");
			}

			if (fontStyle != null)
			{
				styleString.append("font-style:" + fontStyle);
				styleString.append(";");
			}

			if (fontWeight != null)
			{
				styleString.append("font-weight:" + fontWeight);
				styleString.append(";");
			}

			String style = styleString.toString();

			if (style.length() > 0)
			{
				if (treeNode instanceof WicketDBTreeViewNode) ((WicketDBTreeViewNode)treeNode).getContentLink().add(new SimpleAttributeModifier("style", style));
				else treeNode.add(new SimpleAttributeModifier("style", style));
			}
		}
	}

	public void setNodeEnabled(boolean enabled)
	{
		treeNode.setEnabled(enabled);

		Iterator childIte = treeNode.iterator();
		while (childIte.hasNext())
		{
			((Component)childIte.next()).setEnabled(enabled);
		}

	}

	public void setTooltip(String tooltip)
	{
		Label nodeLabel = null;

		if (treeNode instanceof WicketTreeNode)
		{
			nodeLabel = ((WicketTreeNode)treeNode).getTreeNodeLabel();
		}

		if (nodeLabel != null && tooltip != null && !"".equals(tooltip)) nodeLabel.add(new SimpleAttributeModifier("title", tooltip));
	}

	/**
	 * Creates the node image resource ref
	 * 
	 * @param treeNode
	 * @return image resource ref
	 */
	public static ResourceReference imageResource(final Icon nodeIcon)
	{
		if (nodeIcon != null)
		{
			return new ResourceReference(nodeIcon.toString())
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected Resource newResource()
				{
					BufferedDynamicImageResource imgRes = new BufferedDynamicImageResource();

					imgRes.setImage(ImageLoader.imageToBufferedImage(((ImageIcon)nodeIcon).getImage()));

					return imgRes;
				}
			};
		}

		return null;
	}

	/**
	 * Creates the node image resource ref
	 * 
	 * @param treeNode
	 * @return image resource ref
	 */
	public static ResourceReference imageResource(final TreeNode treeNode)
	{
		if (treeNode != null && treeNode instanceof FoundSetTreeModel.UserNode)
		{
			final Icon nodeIcon = ((FoundSetTreeModel.UserNode)treeNode).getIcon();

			if (nodeIcon != null)
			{
				return imageResource(nodeIcon);
			}
		}

		return null;
	}
}
