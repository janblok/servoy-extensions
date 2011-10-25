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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
			this.window = new JWindow((Window)parent);

			if (parent instanceof RootPaneContainer)
			{
				glassPane = ((RootPaneContainer)parent).getGlassPane();
				glassPane.setVisible(true);
				mouseListener = new PopupMouseListener();
				glassPane.addMouseListener(mouseListener);
			}

			IFormUI formUI = form.getFormUI();

			window.getContentPane().setLayout(new BorderLayout(0, 0));
			window.getContentPane().add((Component)formUI, BorderLayout.CENTER);
			window.pack();

			Point locationOnScreen = elementToShowRelatedTo.getLocationOnScreen();
			locationOnScreen.y += elementToShowRelatedTo.getHeight();
			window.setLocation(locationOnScreen);
			window.setVisible(true);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#close(java.lang.Object)
	 */
	public void close(Object retval)
	{
		record.startEditing();
		record.setValue(dataprovider, retval);

		closeWindow();
		glassPane.removeMouseListener(mouseListener);
	}

	/**
	 * 
	 */
	private void closeWindow()
	{
		glassPane.setVisible(false);
		window.setVisible(false);
		window.getContentPane().removeAll();
		window.dispose();
	}

	private class PopupMouseListener extends MouseAdapter
	{
		private Component dispatchComponent;

		@Override
		public void mousePressed(MouseEvent e)
		{
			closeWindow();
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
