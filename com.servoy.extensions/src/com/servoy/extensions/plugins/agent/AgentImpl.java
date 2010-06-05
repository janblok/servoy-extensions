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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.servoy.j2db.Messages;
import com.servoy.j2db.util.Debug;

/**
 * @author jblok
 */
public class AgentImpl extends JLabel implements IJSAgent, MouseListener, MouseMotionListener, ActionListener, ComponentListener
{
	public static final String imagesDir = "images"; //$NON-NLS-1$
	public static final String soundsDir = "audio"; //$NON-NLS-1$
	public static final int STARTUP_ID = 0;
	public static final int ANIMATION_ID = 1;

	private final Hashtable animations = new Hashtable();
	private Animation currentAnimation = null;
	private final Properties animationData = new Properties();

	// baloon related
	private JPanel balloon;
	private Dimension balloonSize = new Dimension(120, 100);
	private javax.swing.Timer baloonHider = null;
	private final JPopupMenu agentContextMenu;

	public AgentImpl()
	{
		super();
		setVisible(false);
		loadAnimations();
		currentAnimation = (Animation)animations.get("showing"); //$NON-NLS-1$
		Icon icon = new ImageIcon(currentAnimation.getImage());
		setIcon(icon);

		Dimension d = new Dimension(icon.getIconWidth(), icon.getIconHeight());
		setPreferredSize(d); // setBounds(300, 300, 160, 128); //size 160x128!
		setSize(d);

		addMouseListener(this);
		addMouseMotionListener(this);

		agentContextMenu = new JPopupMenu();
		JMenuItem mi = new JMenuItem(Messages.getString("servoy.plugin.agent.menu.hide")); //$NON-NLS-1$
		mi.addActionListener(this);
		mi.setActionCommand("hide"); //$NON-NLS-1$
		agentContextMenu.add(mi);
		agentContextMenu.addSeparator();
		mi = new JMenuItem(Messages.getString("servoy.plugin.agent.menu.animate")); //$NON-NLS-1$
		mi.addActionListener(this);
		agentContextMenu.add(mi);
		mi.setActionCommand("animate"); //$NON-NLS-1$

		baloonHider = new javax.swing.Timer(3000, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (balloon != null) balloon.setVisible(false);
			}
		});
		baloonHider.setRepeats(false);
	}

	private void loadAnimations()
	{
		try
		{
			InputStream fis = this.getClass().getResourceAsStream("animation.data"); //$NON-NLS-1$
			java.io.BufferedInputStream bis = new BufferedInputStream(fis);
			animationData.load(bis);
			{
				String imageFileName = animationData.getProperty("showing.imageFileName"); //$NON-NLS-1$
				String audioFileName = animationData.getProperty("showing.audioFileName"); //$NON-NLS-1$
				Animation animation = new Animation(this, imageFileName, audioFileName);
				animations.put("showing", animation); //$NON-NLS-1$
			}
			/*
			 * { String imageFileName = properties.getProperty("showing.imageFileName"); String numberOfImages =
			 * properties.getProperty("showing.numberOfImages"); int lenght = Integer.parseInt(numberOfImages); Animation animation = new
			 * Animation(this,imageFileName,lenght); animations.put("showing",animation); }
			 */
		}
		catch (Exception ex)
		{
			Debug.error(ex);
		}
	}

	public void showing()// at last position
	{
		this.setVisible(true);
	}

	public void hiding()
	{
		this.setVisible(false);
	}

//	public void listening()
//	{
//	}
//
//	public void hearing()
//	{
//	}

	// show balloon
	public void speak(String s)
	{
		Container parent = getParent();
		if (s == null || parent == null) return;

		if (s.toLowerCase().trim().startsWith("<html>")) //$NON-NLS-1$
		{
			if (balloon == null || balloon instanceof PlainTextBalloon)
			{
				if (balloon != null) parent.remove(balloon);// remove old
				balloon = new HTMLBalloon();
				parent.add(balloon, JLayeredPane.MODAL_LAYER, 0);
			}
		}
		else
		{
			if (balloon == null || balloon instanceof HTMLBalloon)
			{
				if (balloon != null) parent.remove(balloon);// remove old
				balloon = new PlainTextBalloon();
				parent.add(balloon, JLayeredPane.MODAL_LAYER, 0);
			}
		}

		balloon.setVisible(false);
		((IShowString)balloon).setText(s);

		Rectangle r = this.getBounds();
		balloon.setBounds(r.x - (balloonSize.width - 40), r.y - (balloonSize.height - 55), balloonSize.width, balloonSize.height);
		balloon.setVisible(isVisible());

		baloonHider.setInitialDelay(3000 + (s.length() * 100));
		baloonHider.restart();
	}

	private static class PlainTextBalloon extends JPanel implements IShowString
	{
		private final JTextArea ta;

		PlainTextBalloon()
		{
			super(new BorderLayout());
			setOpaque(false);
			ta = new JTextArea();
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);
			ta.setBorder(null);
			ta.setOpaque(false);
			ta.setEditable(false);
			add(ta, BorderLayout.CENTER);
			setBorder(new BaloonBorder());
		}

		public void setText(String s)
		{
			ta.setText(s);
		}
	}

	private static class HTMLBalloon extends JPanel implements IShowString
	{
		private final JLabel ta;

		HTMLBalloon()
		{
			super(new BorderLayout());
			setOpaque(false);
			ta = new JLabel("", SwingConstants.LEFT); //$NON-NLS-1$
			ta.setVerticalAlignment(SwingConstants.TOP);
			ta.setBorder(null);
			ta.setOpaque(false);
			add(ta, BorderLayout.CENTER);
			setBorder(new BaloonBorder());
		}

		public void setText(String s)
		{
			ta.setText(s);
		}
	}

	private interface IShowString
	{
		public void setText(String s);
	}

	// set new agent image
	public Image getImage(String name)
	{
		java.net.URL iconUrl = AgentImpl.class.getResource(name);
		if (iconUrl != null)
		{
			return new javax.swing.ImageIcon(iconUrl).getImage();
		}
		return null;
	}

	/*
	 * _______________________________________________________________________________________________________ Context menumouse handling
	 */
	private Point oldMouseLocation;

	public void mousePressed(MouseEvent e)
	{
		maybeShowPopup(e);
		if (balloon != null) balloon.setVisible(false);
		Point point = getLocation();
		e.translatePoint(point.x, point.y);
		oldMouseLocation = new Point(e.getX(), e.getY());
	}

	public void mouseReleased(MouseEvent e)
	{
		maybeShowPopup(e);
	}

	public void mouseEntered(MouseEvent e)
	{
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public void mouseExited(MouseEvent e)
	{
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void mouseClicked(MouseEvent e)
	{
		if (e.getClickCount() > 1)
		{
			speak(Messages.getString("servoy.plugin.agent.text.hello")); //$NON-NLS-1$
		}
	}

	public void mouseDragged(MouseEvent e)
	{
		Point point = getLocation();
		e.translatePoint(point.x, point.y);
		int x = e.getX();
		int y = e.getY();
		int lx = x - oldMouseLocation.x;
		int ly = y - oldMouseLocation.y;
		setLocation(point.x + lx, point.y + ly);
		oldMouseLocation = new Point(x, y);
	}

	public void mouseMoved(MouseEvent e)
	{
	}

	private void maybeShowPopup(MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			agentContextMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/*
	 * _______________________________________________________________________________________________________ Context menu handling
	 */

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();
		if (command.equals("hide")) //$NON-NLS-1$
		this.setVisible(false);
		else if (command.equals("animate")) //$NON-NLS-1$
		speak(Messages.getString("servoy.plugin.agent.text.animate")); //$NON-NLS-1$
	}

	/*
	 * _______________________________________________________________________________________________________ If parent resized we want to know, so we to
	 * listen to window resizes
	 */

	public void componentResized(ComponentEvent e)
	{
		if (balloon != null) balloon.setVisible(false);
		Component c = e.getComponent();
		Dimension d = c.getSize();
		Dimension own_d = getSize();
		setLocation(d.width - own_d.width - 20, d.height - own_d.height - 40);
	}

	public void componentHidden(ComponentEvent e)
	{
	}

	public void componentMoved(ComponentEvent e)
	{
	}

	public void componentShown(ComponentEvent e)
	{
	}

	/*
	 * _______________________________________________________________________________________________________ Methods made availeble to scripting
	 */

	@Deprecated
	public void js_hide()
	{
		js_setVisible(false);
	}

	@Deprecated
	public void js_show()
	{
		js_setVisible(true);
	}

	public void js_setVisible(boolean b)
	{
		if (b)
		{
			showing();
		}
		else
		{
			hiding();
		}
	}

	public void js_speak(String str)
	{
		speak(str);
	}

	public void js_setImageURL(String text_url)
	{
		try
		{
			Icon icon = null;
			if (text_url == null || "".equals(text_url)) //$NON-NLS-1$
			{
				currentAnimation = (Animation)animations.get("showing"); //$NON-NLS-1$
				icon = new ImageIcon(currentAnimation.getImage());
			}
			else
			{
				URL url = new URL(text_url);
				icon = new ImageIcon(url);
			}
			Icon oldIcon = getIcon();
			setIcon(icon);
			Dimension d = new Dimension(icon.getIconWidth(), icon.getIconHeight());
			setPreferredSize(d); // setBounds(300, 300, 160, 128); //size 160x128!
			setSize(d);

			Point p = getLocation();
			if (oldIcon != null)// correct location for new size
			{
				p.translate(oldIcon.getIconWidth() - icon.getIconWidth(), oldIcon.getIconHeight() - icon.getIconHeight());
				setLocation(p);
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
	}

	public int js_getX()
	{
		return getX();
	}

	public void js_setX(int x)
	{
		setLocation(x, getLocation().y);
	}

	public int js_getY()
	{
		return getY();
	}

	public void js_setY(int y)
	{
		setLocation(getLocation().x, y);
	}

	public void js_setLocation(int x, int y)
	{
		setLocation(x, y);
	}

	public void js_setBalloonSize(int w, int h)
	{
		balloonSize = new Dimension(w, h);
	}

	/*
	 * _______________________________________________________________________________________________________ Methods needed for IScriptObject
	 */
	public String[] getParameterNames(String methodName)
	{
		return getParameterNamesEx(methodName);
	}

	public static String[] getParameterNamesEx(String methodName)
	{
		if ("speak".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "message" };
		}
		else if ("setVisible".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "visible" };
		}
		else if ("setLocation".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "x", "y" };
		}
		else if ("setImageURL".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "url" };
		}
		else if ("setBalloonSize".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "width", "height" };
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return isDeprecatedEx(methodName);
	}

	public static boolean isDeprecatedEx(String methodName)
	{
		if ("hide".equals(methodName)) return true; //$NON-NLS-1$
		if ("show".equals(methodName)) return true; //$NON-NLS-1$

		return false;
	}

	public String getSample(String methodName)
	{
		return getSampleEx(methodName);
	}

	public static String getSampleEx(String methodName)
	{
		if ("speak".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTipEx(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.agent.speak('hello nerd');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("setVisible".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTipEx(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.agent.setVisible(true);\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("setLocation".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTipEx(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.agent.setLocation(100,100);\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("show".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTipEx(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.agent.show();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("hide".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTipEx(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.agent.hide();\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("setImageURL".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTipEx(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.agent.setImageURL(\"url\")\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if ("setBalloonSize".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTipEx(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.agent.setBalloonSize(width,height)\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//move the agent diagonaly\n"); //$NON-NLS-1$
			retval.append("plugins.agent.x = plugins.agent.x-10;\n"); //$NON-NLS-1$
			retval.append("plugins.agent.y = plugins.agent.y-10;\n"); //$NON-NLS-1$
			return retval.toString();
		}
	}

	public String getToolTip(String methodName)
	{
		return getToolTipEx(methodName);
	}

	public static String getToolTipEx(String methodName)
	{
		if ("speak".equals(methodName)) //$NON-NLS-1$
		{
			return "Makes the agent speak."; //$NON-NLS-1$
		}
		else if ("setVisible".equals(methodName)) //$NON-NLS-1$
		{
			return "Show/hides the agent."; //$NON-NLS-1$
		}
		else if ("show".equals(methodName)) //$NON-NLS-1$
		{
			return "Shows the agent."; //$NON-NLS-1$
		}
		else if ("hide".equals(methodName)) //$NON-NLS-1$
		{
			return "Hides the agent."; //$NON-NLS-1$
		}
		else if ("x".equals(methodName)) //$NON-NLS-1$
		{
			return "Gets or sets the x position of the agent."; //$NON-NLS-1$
		}
		else if ("y".equals(methodName)) //$NON-NLS-1$
		{
			return "Gets or sets the y position of the agent."; //$NON-NLS-1$
		}
		else if ("setImageURL".equals(methodName)) //$NON-NLS-1$
		{
			return "Sets a new image for the agent."; //$NON-NLS-1$
		}
		else if ("setLocation".equals(methodName)) //$NON-NLS-1$
		{
			return "Sets the location of the agent."; //$NON-NLS-1$
		}
		else if ("setBalloonSize".equals(methodName)) //$NON-NLS-1$
		{
			return "Sets the size of the balloon."; //$NON-NLS-1$
		}
		else
		{
			return "Moves the agent diagonaly."; //$NON-NLS-1$
		}
	}

	public Class[] getAllReturnedTypes()
	{
		return getAllReturnedTypesEx();
	}

	public static Class[] getAllReturnedTypesEx()
	{
		return null;
	}
}
