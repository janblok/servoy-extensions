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

package com.servoy.extensions.beans.dbtreeview.table;

import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.mozilla.javascript.Function;

import com.servoy.extensions.beans.dbtreeview.FoundSetTreeModel.UserNode;
import com.servoy.extensions.beans.dbtreeview.SwingDBTree;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.dataprocessing.Record;
import com.servoy.j2db.dnd.CompositeTransferHandler;
import com.servoy.j2db.dnd.DRAGNDROP;
import com.servoy.j2db.dnd.ICompositeDragNDrop;
import com.servoy.j2db.dnd.JSDNDEvent;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;

/**
 * Class representing the smart client tree table
 * 
 * @author gboros
 */
public class SwingDBTreeTable extends JTable implements ICompositeDragNDrop
{
	private static final long serialVersionUID = 1L;
	private final SwingDBTreeTableView parent;
	private final IClientPluginAccess application;

	private FunctionDefinition fOnDrag;
	private FunctionDefinition fOnDragEnd;
	private FunctionDefinition fOnDragOver;
	private FunctionDefinition fOnDrop;
	private boolean dragEnabled;

	public SwingDBTreeTable(SwingDBTreeTableView parent, IClientPluginAccess application)
	{
		super();
		this.parent = parent;
		this.application = application;
	}

	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed)
	{
		TreePath selectedPath;

		switch (e.getKeyCode())
		{
			case KeyEvent.VK_LEFT :
				selectedPath = parent.getTree().getSelectionPath();
				if (selectedPath != null)
				{
					parent.getTree().collapsePath(selectedPath);
					parent.repaint();
				}
				return true;
			case KeyEvent.VK_RIGHT :
				selectedPath = parent.getTree().getSelectionPath();
				if (selectedPath != null)
				{
					int selectedRow = getSelectedRow();
					parent.getTree().expandPath(selectedPath);
					resizeAndRepaint();
					if (selectedRow != -1)
					{
						parent.getTree().setSelectionRow(selectedRow);
					}
				}
				return true;
			case KeyEvent.VK_ENTER :
				return true;
			default :
				return super.processKeyBinding(ks, e, condition, pressed);
		}
	}

	/**
	 * Overridden to message super and forward the method to the tree. Since the tree is not actually in the component hieachy it will never receive this
	 * unless we forward it in this manner.
	 */
	@Override
	public void updateUI()
	{
		super.updateUI();
		if (parent != null)
		{
			JTree tree = parent.getTree();
			if (tree != null)
			{
				tree.updateUI();
			}
			// Use the tree's default foreground and background colors in the
			// table.
			LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
		}
	}


	/*
	 * Workaround for BasicTableUI anomaly. Make sure the UI never tries to paint the editor. The UI currently uses different techniques to paint the renderers
	 * and editors and overriding setBounds() below is not the right thing to do for an editor. Returning -1 for the editing row in this case, ensures the
	 * editor is never painted.
	 */
	@Override
	public int getEditingRow()
	{
		return (getColumnClass(editingColumn) == SwingDBTree.class) ? -1 : editingRow;
	}

	/**
	 * Overridden to pass the new rowHeight to the tree.
	 */
	@Override
	public void setRowHeight(int rowHeight)
	{
		super.setRowHeight(rowHeight);
		if (parent != null)
		{
			JTree tree = parent.getTree();
			if (tree != null && tree.getRowHeight() != rowHeight)
			{
				tree.setRowHeight(getRowHeight());
			}
		}
	}

	public Object getDragSource(Point xy)
	{
		TreePath p = parent.getTree().getPathForRow(rowAtPoint(xy));
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

		return true;
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

	@Override
	public void createDefaultColumnsFromModel()
	{
		super.createDefaultColumnsFromModel();
		if (parent != null) parent.updateColumnsWidth();
	}
}
