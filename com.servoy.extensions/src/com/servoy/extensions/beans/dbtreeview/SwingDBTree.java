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
import javax.swing.tree.TreePath;

import org.mozilla.javascript.Function;

import com.servoy.extensions.beans.dbtreeview.FoundSetTreeModel.UserNode;
import com.servoy.extensions.beans.dbtreeview.SwingDBTreeView.UserNodeTreeCellRenderer;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.dataprocessing.Record;
import com.servoy.j2db.dnd.CompositeTransferHandler;
import com.servoy.j2db.dnd.DRAGNDROP;
import com.servoy.j2db.dnd.ICompositeDragNDrop;
import com.servoy.j2db.dnd.JSDNDEvent;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;

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
	private final SwingDBTreeView parent;
	private final IClientPluginAccess application;

	private FunctionDefinition fOnDrag;
	private FunctionDefinition fOnDragEnd;
	private FunctionDefinition fOnDragOver;
	private FunctionDefinition fOnDrop;
	private boolean dragEnabled;

	SwingDBTree(SwingDBTreeView parent, IClientPluginAccess application)
	{
		this.parent = parent;
		this.application = application;
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
		TreePath p = this.getPathForLocation(xy.x, xy.y);
		return p != null ? p.getLastPathComponent() : null;
	}

	public int onDrag(JSDNDEvent event)
	{
		if (fOnDrag != null)
		{
			fillDragEvent(event);
			Object dragReturn = fOnDrag.executeSync(application, new Object[] { event });
			if (dragReturn instanceof Number) return ((Number)dragReturn).intValue();
		}

		return DRAGNDROP.NONE;
	}

	public void onDragEnd(JSDNDEvent event)
	{
		if (fOnDragEnd != null)
		{
			fillDragEvent(event);
			fOnDragEnd.executeSync(application, new Object[] { event });
		}
	}

	public boolean onDragOver(JSDNDEvent event)
	{
		if (fOnDragOver != null)
		{
			fillDragEvent(event);
			Object dragOverReturn = fOnDragOver.executeSync(application, new Object[] { event });
			if (dragOverReturn instanceof Boolean) return ((Boolean)dragOverReturn).booleanValue();
		}

		return false;
	}

	public boolean onDrop(JSDNDEvent event)
	{
		if (fOnDrop != null)
		{
			fillDragEvent(event);
			Object dropHappened = fOnDrop.executeSync(application, new Object[] { event });
			if (dropHappened instanceof Boolean) return ((Boolean)dropHappened).booleanValue();
		}
		return false;
	}

	private void fillDragEvent(JSDNDEvent event)
	{
		event.setSource(parent);
		String dragSourceName = parent.getName();
		if (dragSourceName == null) dragSourceName = parent.getId();
		event.setElementName(dragSourceName);

		Object dragSource = getDragSource(new Point(event.js_getX(), event.js_getY()));
		if (dragSource instanceof UserNode)
		{
			IRecord dragRecord = ((UserNode)dragSource).getRecord();
			if (dragRecord instanceof Record) event.setRecord((Record)dragRecord);
		}
	}

	void setOnDragCallback(Function fOnDrag)
	{
		this.fOnDrag = new FunctionDefinition(fOnDrag);
		initDragAndDrop();
	}

	void setOnDragEndCallback(Function fOnDragEnd)
	{
		this.fOnDragEnd = new FunctionDefinition(fOnDragEnd);
		initDragAndDrop();
	}

	void setOnDragOverCallback(Function fOnDragOver)
	{
		this.fOnDragOver = new FunctionDefinition(fOnDragOver);
		initDragAndDrop();
	}

	void setOnDropCallback(Function fOnDrop)
	{
		this.fOnDrop = new FunctionDefinition(fOnDrop);
		initDragAndDrop();
	}

	private void initDragAndDrop()
	{
		if (!dragEnabled)
		{
			setDragEnabled(true);

			TransferHandler treeTransferHandler = new CompositeTransferHandler();
			setTransferHandler(treeTransferHandler);
			new DropTarget(this, (DropTargetListener)treeTransferHandler);
			dragEnabled = true;
		}
	}
}
