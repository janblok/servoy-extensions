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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.mozilla.javascript.Scriptable;

import com.servoy.extensions.plugins.window.popup.IPopupShower;
import com.servoy.j2db.IForm;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.ui.IFormUI;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.ServoyException;

/**
 * @author jcompagner
 *
 */
public class SwingPopupShower implements IPopupShower
{
	private final IClientPluginAccess clientPluginAccess;
	private JComponent elementToShowRelatedTo;
	private final IForm form;
	private final Scriptable scope;
	private final String dataprovider;
	private Container parent;
	private JDialog window;
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
	public SwingPopupShower(IClientPluginAccess clientPluginAccess, IComponent elementToShowRelatedTo, IForm form, Scriptable scope, String dataprovider)
	{
		this.clientPluginAccess = clientPluginAccess;
		if (elementToShowRelatedTo instanceof JComponent)
		{
			this.elementToShowRelatedTo = (JComponent)elementToShowRelatedTo;
		}
		this.form = form;
		this.scope = scope;
		this.dataprovider = dataprovider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#show()
	 */
	public void show()
	{
		parent = null;
		if (elementToShowRelatedTo != null)
		{
			parent = elementToShowRelatedTo.getParent();
		}
		else
		{
			parent = ((ISmartRuntimeWindow)clientPluginAccess.getCurrentRuntimeWindow()).getWindow();
		}

		while (parent != null && !(parent instanceof Dialog) && !(parent instanceof Frame))
		{
			parent = parent.getParent();
		}

		if (parent instanceof Dialog)
		{
			window = new JDialog((Dialog)parent);
		}
		else if (parent instanceof Frame)
		{
			window = new JDialog((Frame)parent);
		}

		if (window != null)
		{
			windowListener = new WindowListener();
			((Window)parent).addComponentListener(windowListener);
			((Window)parent).addWindowStateListener(windowListener);
			this.window.setFocusableWindowState(true);
			this.window.setFocusable(true);
			this.window.setUndecorated(true);
			this.window.getRootPane().setWindowDecorationStyle(JRootPane.NONE);

			IFormUI formUI = form.getFormUI();

			window.getContentPane().setLayout(new BorderLayout(0, 0));
			window.getContentPane().add((Component)formUI, BorderLayout.CENTER);
			window.pack();
			if (elementToShowRelatedTo != null)
			{
				Point locationOnScreen = elementToShowRelatedTo.getLocationOnScreen();
				locationOnScreen.y += elementToShowRelatedTo.getHeight();

				Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());
				Rectangle bounds = window.getGraphicsConfiguration().getBounds();

				int screenWidth = (bounds.width + bounds.x - screenInsets.left - screenInsets.right);
				//if necessary right align popup on related component
				if ((locationOnScreen.x + window.getSize().width) > screenWidth)
				{
					locationOnScreen.x = screenWidth - window.getSize().width;
				}

				int screenHeight = (bounds.height + bounds.y - screenInsets.top - screenInsets.bottom);
				//if necessary popup on the top of the related component
				if (window.getSize().height + locationOnScreen.y > screenHeight)
				{
					if (locationOnScreen.y - elementToShowRelatedTo.getHeight() - window.getSize().height > screenInsets.top)
					{
						locationOnScreen.y = locationOnScreen.y - elementToShowRelatedTo.getHeight() - window.getSize().height;
					}
					else
					{
						locationOnScreen.y = screenHeight - window.getSize().height;
					}
				}

				if (locationOnScreen.x < bounds.x) locationOnScreen.x = bounds.x;

				if (locationOnScreen.y < bounds.y) locationOnScreen.y = bounds.y;

				window.setLocation(locationOnScreen);
			}
			else
			{
				window.setLocationRelativeTo(parent);
			}
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
		scope.put(dataprovider, scope, retval);
		cancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#cancel()
	 */
	public void cancel()
	{
		closeWindow(true);
	}

	/**
	 * 
	 */
	private void closeWindow(boolean removeMouseListener)
	{
		glassPane.setVisible(false);
		window.setVisible(false);
		if (parent != null) parent.removeComponentListener(windowListener);
		window.getContentPane().removeAll();
		window.dispose();
		if (removeMouseListener) glassPane.removeMouseListener(mouseListener);
		try
		{
			form.setUsingAsExternalComponent(false);
		}
		catch (ServoyException e)
		{
			Debug.error(e);
		}
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
		private boolean mousePressed;

		@Override
		public void mousePressed(MouseEvent e)
		{
			mousePressed = true;
			closeWindow(false);
			Point p2 = SwingUtilities.convertPoint(glassPane, e.getPoint(), parent);
			dispatchComponent = parent.findComponentAt(p2.x, p2.y);
			if (dispatchComponent != null)
			{
				Point p3 = SwingUtilities.convertPoint(parent, p2, dispatchComponent);
				MouseEvent e2 = new MouseEvent(dispatchComponent, e.getID(), e.getWhen(), e.getModifiers(), p3.x, p3.y, e.getClickCount(), e.isPopupTrigger());
				dispatchComponent.dispatchEvent(e2);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (mousePressed)
			{
				glassPane.removeMouseListener(mouseListener);
				if (dispatchComponent != null)
				{
					Point p2 = SwingUtilities.convertPoint(glassPane, e.getPoint(), parent);
					Point p3 = SwingUtilities.convertPoint(parent, p2, dispatchComponent);
					MouseEvent e2 = new MouseEvent(dispatchComponent, e.getID(), e.getWhen(), e.getModifiers(), p3.x, p3.y, e.getClickCount(),
						e.isPopupTrigger());
					dispatchComponent.dispatchEvent(e2);
				}
			}
		}
	}
}
