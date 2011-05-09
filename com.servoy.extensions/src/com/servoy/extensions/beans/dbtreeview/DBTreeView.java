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
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.CSS;

import com.servoy.j2db.IApplication;
import com.servoy.j2db.IServoyBeanFactory;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.IStyleSheet;

/**
 * DB Tree View bean main class
 * 
 * @author jblok
 */
@SuppressWarnings("nls")
public class DBTreeView implements IServoyBeanFactory, Serializable, ITreeView
{
	private static final long serialVersionUID = 1L;
	public static final String ELEMENT_TYPE = "DBTREEVIEW";

	private String name;
	private Border border;
	private Color foreground;
	private Color background;
	private boolean opaque;
	private Font font;
	private Point location;
	private Dimension size;
	private String styleSheet;
	private String styleClass;

	public DBTreeView()
	{
	}

	public IComponent getBeanInstance(int servoy_application_type, IClientPluginAccess application, Object[] cargs)
	{
		IComponent t = null;
		if (servoy_application_type == IApplication.WEB_CLIENT || servoy_application_type == IApplication.HEADLESS_CLIENT)
		{
			t = getWicketDBTreeView(cargs, application);
		}
		else
		{
			t = getSwingDBTreeView(cargs, application);
		}
		styleSheet = (String)cargs[2];
		t.setName(name);
		applyStyleSheet(t, application.getStyleSheet(styleSheet), styleClass);
		if (font != null) t.setFont(font);
		if (background != null) t.setBackground(background);
		if (foreground != null) t.setForeground(foreground);
		if (border != null) t.setBorder(border);
		t.setOpaque(opaque);
		if (location != null) t.setLocation(location);
		if (size != null) t.setSize(size);
		return t;
	}

	protected IWicketTree getWicketDBTreeView(Object[] cargs, IClientPluginAccess application)
	{
		return new WicketDBTreeView(cargs[0].toString(), application);
	}

	protected SwingDBTreeView getSwingDBTreeView(Object[] cargs, IClientPluginAccess application)
	{
		return new SwingDBTreeView(application);
	}

	public static Class[] getAllReturnedTypes()
	{
		return new Class[] { Binding.class, RelationInfo.class };
	}

	public static String[] getParameterNames(String methodName)
	{

		if ("setNodeLevelVisible".equals(methodName))
		{
			return new String[] { "level", "visible" };
		}
		else if (methodName.startsWith("setExpandNode"))
		{
			return new String[] { "path", "expand_collapse" };
		}
		else if (methodName.startsWith("isNodeExpanded"))
		{
			return new String[] { "path" };
		}
		else if (methodName.startsWith("addRoots"))
		{
			return new String[] { "foundset" };
		}
		else if (methodName.startsWith("createBinding"))
		{
			return new String[] { "datasource/servername", "[tablename]" };
		}
		else if ("setTreeColumnHeader".equals(methodName))
		{
			return new String[] { "treeColumnHeader" };
		}
		else if ("setTreeColumnPreferredWidth".equals(methodName))
		{
			return new String[] { "treeColumnPreferredWidth" };
		}
		else if ("createColumn".equals(methodName))
		{
			return new String[] { "servername", "tablename", "header", "fieldname", "[preferredWidth]" };
		}
		else if ("setRowHeight".equals(methodName))
		{
			return new String[] { "rowHeight" };
		}
		else if ("setOnDrag".equals(methodName) || "setOnDragEnd".equals(methodName) || "setOnDragOver".equals(methodName) || "setOnDrop".equals(methodName))
		{
			return new String[] { "callback" };
		}
		else if ("setFont".equals(methodName))
		{
			return new String[] { "spec" };
		}
		else if ("setLocation".equals(methodName))
		{
			return new String[] { "x", "y" };
		}
		else if ("setSize".equals(methodName))
		{
			return new String[] { "x", "y" };
		}
		return null;
	}

	public static String getSample(String methodName)
	{

		if ("setNodeLevelVisible".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("%%elementName%%.setNodeLevelVisible(globals.g_treeview_level, (globals.g_treeview_expand == 1 ? true : false));\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("electionPath"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("%%elementName%%.selectionPath = new Array(14,24,45,67);\n"); //$NON-NLS-1$
			retval.append("var currentSelectionArray = %%elementName%%.selectionPath;\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("setExpandNode"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var pathArray = new Array(14,24,45,67);\n"); //$NON-NLS-1$
			retval.append("%%elementName%%.setExpandNode(pathArray, true);\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("isNodeExpanded"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var pathArray = new Array(14,24,45,67);\n"); //$NON-NLS-1$
			retval.append("%%elementName%%.isNodeExpanded(pathArray);\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("refresh"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("%%elementName%%.refresh();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("addRoots"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var addedRootNodes = %%elementName%%.addRoots(foundset);\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("removeAllRoots"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("%%elementName%%.removeAllRoots();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("createBinding"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var companies_binding = %%elementName%%.createBinding('example_data', 'companies');\n");
			retval.append("companies_binding.setTextDataprovider('company_name');\n");
			retval.append("companies_binding.setNRelationName('companies_to_companies');\n");
			retval.append("companies_binding.setImageURLDataprovider('type_icon');\n");
			retval.append("companies_binding.setChildSortDataprovider('company_sort');\n");

			return retval.toString();
		}
		else if ("createRelationInfo".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("var company_relations = new Array();\n");
			retval.append("company_relations[0] = %%elementName%%.createRelationInfo();\n");
			retval.append("company_relations[0].setLabel('Employees');\n");
			retval.append("company_relations[0].setNRelationName('companies_to_employees');\n");
			retval.append("company_relations[1] = %%elementName%%.createRelationInfo();\n");
			retval.append("company_relations[1].setLabel('Customers');\n");
			retval.append("company_relations[1].setNRelationName('companies_to_customers');\n");
			retval.append("companies_binding.setNRelationInfos(company_relations);\n");

			return retval.toString();
		}
		else if ("setTreeColumnHeader".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.setTreeColumnHeader('Tree Column Header');\n");
			return retval.toString();
		}
		else if ("setTreeColumnPreferredWidth".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.setTreeColumnPreferredWidth(200);\n");
			return retval.toString();
		}
		else if ("createColumn".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.createColumn('servername', 'tablename', 'header text', 'tablefieldname', 150);\n");
			return retval.toString();
		}
		else if ("removeAllColumns".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.removeAllColumns();\n");
			return retval.toString();
		}
		else if ("setRowHeight".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.setRowHeight(40);\n");
			return retval.toString();
		}
		else if ("setOnDrag".equals(methodName) || "setOnDragEnd".equals(methodName) || "setOnDragOver".equals(methodName) || "setOnDrop".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.").append(methodName).append("(on").append(methodName.substring(5)).append(");");
			return retval.toString();
		}
		else if ("getHeight".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.getHeight();\n");
			return retval.toString();
		}
		else if ("getLocationX".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.getLocationX();\n");
			return retval.toString();
		}
		else if ("getLocationY".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.getLocationY();\n");
			return retval.toString();
		}
		else if ("getName".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.getName();\n");
			return retval.toString();
		}
		else if ("getWidth".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.getWidth();\n");
			return retval.toString();
		}
		else if ("setFont".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.setFont('Times New Roman, 1, 22');\n");
			return retval.toString();
		}
		else if ("setLocation".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.setLocation(120,80);\n");
			return retval.toString();
		}
		else if ("setSize".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("%%elementName%%.setSize(400,300);\n");
			return retval.toString();
		}
		return null;
	}

	public static String getToolTip(String methodName)
	{
		if ("setNodeLevelVisible".equals(methodName))
		{
			return "Set the level of visible nodes (expand or collapse to certain level)";
		}
		else if (methodName.endsWith("electionPath"))
		{
			return "Get/Set the selection (path), array with pk records values (only single pk key supported)";
		}
		else if (methodName.endsWith("setExpandNode"))
		{
			return "Expand/collapse the path, array with pk records values (only single pk key supported)";
		}
		else if (methodName.endsWith("isNodeExpanded"))
		{
			return "Check the path (array with pk records values (only single pk key supported)) expanded status";
		}
		else if (methodName.endsWith("refresh"))
		{
			return "Refresh the tree display";
		}
		else if (methodName.endsWith("addRoots"))
		{
			return "Add foundset to the list of foundsets used to create the tree's root nodes.\nReturns the number of added root nodes.\nNote: the bean will use a clone of the foundset, so any changes on the foundset parameter will be ignored in the tree.";
		}
		else if (methodName.endsWith("removeAllRoots"))
		{
			return "Remove all root foundsets";
		}
		else if (methodName.endsWith("createBinding"))
		{
			return "Create and add binding object for a database table used to set data bindings for nodes.";
		}
		else if (methodName.equals("createRelationInfo"))
		{
			return "Create relation info object used to set multiple child relations for a tree node";
		}
		else if ("setTreeColumnHeader".equals(methodName))
		{
			return "Set the header text for the tree column";
		}
		else if ("setTreeColumnPreferredWidth".equals(methodName))
		{
			return "Set the preferred width in pixels for the tree column";
		}
		else if ("createColumn".equals(methodName))
		{
			return "Create and add new column to the tree table";
		}
		else if ("removeAllColumns".equals(methodName))
		{
			return "Remove all columns but the tree column from the tree table";
		}
		else if ("setRowHeight".equals(methodName))
		{
			return "Set row height";
		}
		else if ("setOnDrag".equals(methodName))
		{
			return "Set method to be called when a drag is started on the tree. For more details about the method arguments and return value check the same property of a form";
		}
		else if ("setOnDragEnd".equals(methodName))
		{
			return "Set method to be called when a drag of on the tree is ended. For more details about the method arguments and return value check the same property of a form";
		}
		else if ("setOnDragOver".equals(methodName))
		{
			return "Set method to be called during a drag over the tree. For more details about the method arguments and return value check the same property of a form";
		}
		else if ("setOnDrop".equals(methodName))
		{
			return "Set method to be called on a drop on the tree. For more details about the method arguments and return value check the same property of a form";
		}
		else if ("getHeight".equals(methodName))
		{
			return "Returns the height of the tree.";
		}
		else if ("getLocationX".equals(methodName))
		{
			return "Get the x coordinate of the location of the tree.";
		}
		else if ("getLocationY".equals(methodName))
		{
			return "Get the y coordinate of the location of the tree.";
		}
		else if ("getName".equals(methodName))
		{
			return "Returns the name of the tree.";
		}
		else if ("getWidth".equals(methodName))
		{
			return "Returns the width of the tree.";
		}
		else if ("setFont".equals(methodName))
		{
			return "Sets the specified font as the font of the tree.";
		}
		else if ("setLocation".equals(methodName))
		{
			return "Sets the location of the tree.";
		}
		else if ("setSize".equals(methodName))
		{
			return "Sets the size of the tree.";
		}
		return null;
	}

	public static boolean isDeprecated(String methodName)
	{
		if ("setNRelationName".equals(methodName))
		{
			return true;
		}
		else if ("setMRelationName".equals(methodName))
		{
			return true;
		}
		else if ("setRoots".equals(methodName))
		{
			return true;
		}
		else if ("bindNodeFontTypeDataProvider".equals(methodName))
		{
			return true;
		}
		else if ("bindNodeImageMediaDataProvider".equals(methodName))
		{
			return true;
		}
		else if ("bindNodeImageURLDataProvider".equals(methodName))
		{
			return true;
		}
		else if ("bindNodeTooltipTextDataProvider".equals(methodName))
		{
			return true;
		}
		else if ("setCallBackInfo".equals(methodName))
		{
			return true;
		}
		else if ("bindNodeChildSortDataProvider".equals(methodName))
		{
			return true;
		}
		return false;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Border getBorder()
	{
		return border;
	}

	public void setBorder(Border border)
	{
		this.border = border;
	}

	public Color getForeground()
	{
		return foreground;
	}

	public void setForeground(Color foreground)
	{
		this.foreground = foreground;
	}

	public Color getBackground()
	{
		return background;
	}

	public void setBackground(Color background)
	{
		this.background = background;
	}

	public boolean isOpaque()
	{
		return opaque;
	}

	public void setOpaque(boolean opaque)
	{
		this.opaque = opaque;
	}

	public Font getFont()
	{
		return font;
	}

	public void setFont(Font font)
	{
		this.font = font;
	}

	public Point getLocation()
	{
		return location;
	}

	public void setLocation(Point location)
	{
		this.location = location;
	}

	public Dimension getSize()
	{
		return size;
	}

	public void setSize(Dimension size)
	{
		this.size = size;
	}

	public void setBounds(Rectangle r)
	{
		setLocation(r.getLocation());
		setSize(r.getSize());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#setStyleClass(java.lang.String)
	 */
	public void setStyleClass(String styleClass)
	{
		this.styleClass = styleClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#getStyleClass()
	 */
	public String getStyleClass()
	{
		return styleClass;
	}

	public static void applyStyleSheet(IComponent component, IStyleSheet ss, String rule)
	{
		if (ss != null)
		{
			try
			{
				String treeViewRule = DBTreeView.ELEMENT_TYPE.toLowerCase();
				if (rule != null) treeViewRule = new StringBuffer(treeViewRule).append('.').append(rule).toString();
				AttributeSet style = ss.getRule(treeViewRule);
				if (style != null)
				{
					if (style.getAttribute(CSS.Attribute.COLOR) != null)
					{
						Color cfg = ss.getForeground(style);
						if (cfg != null) component.setForeground(cfg);
					}
					Object sbackground_color = style.getAttribute(CSS.Attribute.BACKGROUND_COLOR);
					if (sbackground_color != null)
					{
						if ("transparent".equals(sbackground_color.toString()))
						{
							component.setOpaque(false);
						}
						else
						{
							Color cbg = ss.getBackground(style);
							if (cbg != null) component.setBackground(cbg);
						}
					}
					if (ss.hasFont(style))
					{
						Font f = ss.getFont(style);
						if (f != null) component.setFont(f);
					}
					if (ss.hasBorder(style))
					{
						Border b = ss.getBorder(style);
						if (b != null)
						{
							component.setBorder(b);
						}
					}
				}
			}
			catch (Exception e)
			{
				Debug.error(e);//parsing can fail in java 1.5
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#isTransparent()
	 */
	public boolean isTransparent()
	{
		return !isOpaque();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#setTransparent(boolean)
	 */
	public void setTransparent(boolean transparent)
	{
		setOpaque(!transparent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#getBorderType()
	 */
	public Border getBorderType()
	{
		return getBorder();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.beans.dbtreeview.ITreeView#setBorderType(javax.swing.border.Border)
	 */
	public void setBorderType(Border border)
	{
		setBorder(border);
	}
}
