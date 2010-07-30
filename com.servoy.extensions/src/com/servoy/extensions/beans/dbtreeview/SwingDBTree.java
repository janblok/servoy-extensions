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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.servoy.extensions.beans.dbtreeview.SwingDBTreeView.UserNodeTreeCellRenderer;
import com.servoy.j2db.dnd.CompositeTransferHandler;
import com.servoy.j2db.dnd.ICompositeDragNDrop;
import com.servoy.j2db.dnd.JSDNDEvent;
import com.servoy.j2db.plugins.IClientPluginAccess;

/**
 * Class representing the smart client tree
 * 
 * @author gboros
 */
public class SwingDBTree extends JTree implements TableCellRenderer, ICompositeDragNDrop
{
	private static final long serialVersionUID = 1L;
	private JTable table;
	private int visibleRow;
	private final IClientPluginAccess application;

	SwingDBTree(IClientPluginAccess application)
	{
		this.application = application;
		setDragEnabled(true);
		// TODO: create custom drag gesture recognizer
		//DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl)
		TransferHandler treeTransferHandler = new CompositeTransferHandler();
		setTransferHandler(treeTransferHandler);
		new DropTarget(this, (DropTargetListener)treeTransferHandler);
	}

	public void setVisibleRow(int visibleRow)
	{
		this.visibleRow = visibleRow;
	}

	public void setTable(JTable table)
	{
		this.table = table;
	}

	@Override
	public void setBounds(int x, int y, int w, int h)
	{
		if (table != null)
		{
			super.setBounds(x, 0, w, table.getHeight());
		}
		else
		{
			super.setBounds(x, y, w, h);
		}
	}

	@Override
	public void paint(Graphics g)
	{
		if (table != null)
		{
			g.translate(0, -visibleRow * getRowHeight());
		}
		super.paint(g);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		visibleRow = row;
		((UserNodeTreeCellRenderer)getCellRenderer()).setRowFocus(hasFocus ? row : -1);
		return this;
	}

	/**
	 * updateUI is overridden to set the colors of the Tree's renderer to
	 * match that of the table.
	 */
	@Override
	public void updateUI()
	{
		super.updateUI();

		if (table != null)
		{
			// Make the tree's cell renderer use the table's cell selection
			// colors.
			TreeCellRenderer tcr = getCellRenderer();
			if (tcr instanceof DefaultTreeCellRenderer)
			{
				DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer)tcr);
				// For 1.1 uncomment this, 1.2 has a bug that will cause an
				// exception to be thrown if the border selection color is
				// null.
				// dtcr.setBorderSelectionColor(null);
				dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
				dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
			}
		}
	}

	/**
	 * Sets the row height of the tree, and forwards the row height to the
	 * table.
	 */
	@Override
	public void setRowHeight(int rowHeight)
	{
		super.setRowHeight(rowHeight);

		if (table != null && rowHeight > 0)
		{

			if (table.getRowHeight() != rowHeight)
			{
				table.setRowHeight(getRowHeight());
			}
		}
	}

	@Override
	protected void processMouseEvent(MouseEvent e)
	{
		if (e.getID() == MouseEvent.MOUSE_PRESSED) application.getDatabaseManager().saveData();
		super.processMouseEvent(e);

	}

	public Object getDragSource(Point xy)
	{
		return this;
	}

	public int onDrag(JSDNDEvent event)
	{
		//System.out.println("ON DRAG");
		return 1;
	}

	public void onDragEnd(JSDNDEvent event)
	{
		//System.out.println("ON DRAG END");
	}

	public boolean onDragOver(JSDNDEvent event)
	{
		//System.out.println("ON DRAG OVER");
		return true;
	}

	public boolean onDrop(JSDNDEvent event)
	{
		//System.out.println("ON DROP");
		return true;
	}
}
