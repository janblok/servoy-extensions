/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2011 Servoy BV

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

package com.servoy.extensions.plugins.window.popup.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import com.servoy.extensions.plugins.window.popup.IPopupShower;
import com.servoy.j2db.IForm;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.ui.IFormUI;

/**
 * @author jcompagner
 *
 */
public class SwingPopupShower implements IPopupShower
{

	private final JComponent elementToShowRelatedTo;
	private final IForm form;
	private final IRecord record;
	private final String dataprovider;
	private JWindow window;
	private Component glassPane;
	private PopupMouseListener mouseListener;
	private WindowListener windowListener;

	/**
	 * @param elementToShowRelatedTo
	 * @param form
	 * @param record
	 * @param dataprovider
	 */
	@SuppressWarnings("nls")
	public SwingPopupShower(IComponent elementToShowRelatedTo, IForm form, IRecord record, String dataprovider)
	{
		if (!(elementToShowRelatedTo instanceof JComponent)) throw new IllegalArgumentException("element to show the popup on is not a JComponent: " +
			elementToShowRelatedTo);
		this.elementToShowRelatedTo = (JComponent)elementToShowRelatedTo;
		this.form = form;
		this.record = record;
		this.dataprovider = dataprovider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#show()
	 */
	public void show()
	{
		Container parent = elementToShowRelatedTo.getParent();
		while (parent != null && !(parent instanceof Window))
		{
			parent = parent.getParent();
		}
		if (parent != null)
		{
			windowListener = new WindowListener();
			((Window)parent).addComponentListener(windowListener);
			((Window)parent).addWindowStateListener(windowListener);
			this.window = new JWindow((Window)parent);
			this.window.setFocusableWindowState(true);
			this.window.setFocusable(true);

			IFormUI formUI = form.getFormUI();

			window.getContentPane().setLayout(new BorderLayout(0, 0));
			window.getContentPane().add((Component)formUI, BorderLayout.CENTER);
			window.pack();
			Point locationOnScreen = elementToShowRelatedTo.getLocationOnScreen();
			locationOnScreen.y += elementToShowRelatedTo.getHeight();
			window.setLocation(locationOnScreen);
			window.setVisible(true);

			if (parent instanceof RootPaneContainer)
			{
				glassPane = ((RootPaneContainer)parent).getGlassPane();
				glassPane.setVisible(true);
				mouseListener = new PopupMouseListener();
				glassPane.addMouseListener(mouseListener);
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#close(java.lang.Object)
	 */
	@SuppressWarnings("nls")
	public void close(Object retval)
	{
		if (record.startEditing())
		{
			record.setValue(dataprovider, retval);
		}
		else
		{
			throw new RuntimeException("record dataprovider " + dataprovider + " can't be set to " + retval +
				" in a form popup close because it can't be editted");
		}

		closeWindow(true);
	}

	/**
	 * 
	 */
	private void closeWindow(boolean removeMouseListener)
	{
		glassPane.setVisible(false);
		window.setVisible(false);
		window.getOwner().removeComponentListener(windowListener);
		window.getContentPane().removeAll();
		window.dispose();
		if (removeMouseListener) glassPane.removeMouseListener(mouseListener);

	}

	/**
	 * @author jcompagner
	 *
	 */
	private final class WindowListener implements ComponentListener, WindowStateListener
	{
		public void componentShown(ComponentEvent e)
		{
			closeWindow(true);
		}

		public void componentResized(ComponentEvent e)
		{
			closeWindow(true);
		}

		public void componentMoved(ComponentEvent e)
		{
			closeWindow(true);
		}

		public void componentHidden(ComponentEvent e)
		{
			closeWindow(true);
		}

		public void windowStateChanged(WindowEvent e)
		{
			closeWindow(true);
		}
	}

	private class PopupMouseListener extends MouseAdapter
	{
		private Component dispatchComponent;

		@Override
		public void mousePressed(MouseEvent e)
		{
			closeWindow(false);
			Point p2 = SwingUtilities.convertPoint(glassPane, e.getPoint(), window.getOwner());
			dispatchComponent = window.getOwner().findComponentAt(p2.x, p2.y);
			if (dispatchComponent != null)
			{
				Point p3 = SwingUtilities.convertPoint(window.getOwner(), p2, dispatchComponent);
				MouseEvent e2 = new MouseEvent(dispatchComponent, e.getID(), e.getWhen(), e.getModifiers(), p3.x, p3.y, e.getClickCount(), e.isPopupTrigger());
				dispatchComponent.dispatchEvent(e2);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			glassPane.removeMouseListener(mouseListener);
			if (dispatchComponent != null)
			{
				Point p2 = SwingUtilities.convertPoint(glassPane, e.getPoint(), window.getOwner());
				Point p3 = SwingUtilities.convertPoint(window.getOwner(), p2, dispatchComponent);
				MouseEvent e2 = new MouseEvent(dispatchComponent, e.getID(), e.getWhen(), e.getModifiers(), p3.x, p3.y, e.getClickCount(), e.isPopupTrigger());
				dispatchComponent.dispatchEvent(e2);
			}
		};
	}

}
