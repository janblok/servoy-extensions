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
package com.servoy.extensions.plugins.window.shortcut.wicket;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.KeyStroke;

import org.apache.wicket.Component;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.protocol.http.WebRequest;

import com.servoy.extensions.plugins.window.WindowProvider;
import com.servoy.extensions.plugins.window.shortcut.IShortcutHandler;
import com.servoy.j2db.server.headlessclient.IPageContributor;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.server.headlessclient.dataui.AbstractServoyDefaultAjaxBehavior;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.ui.IFormUI;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.TreeBidiMap;

/**
 * This class handles (de)registering of shortcuts in the Web client (Wicket).
 * 
 * @author rgansevles
 * 
 */
public class WicketShortcutHandler implements IShortcutHandler
{
	private static final ResourceReference shortcut_js = new JavascriptResourceReference(WicketShortcutHandler.class, "res/shortcut.js"); //$NON-NLS-1$
	public static final String BEHAVIOUR = "ShortcutBehaviour"; //$NON-NLS-1$

	private static final Map<Integer, String> specialKeys = new HashMap<Integer, String>();
	static
	{
		specialKeys.put(new Integer(KeyEvent.VK_BACK_QUOTE), "`"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_MINUS), "-"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_EQUALS), "="); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_SEMICOLON), ";"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_QUOTE), "'"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_COMMA), ","); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_PERIOD), "."); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_SLASH), "/"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_BACK_SLASH), "\\"); //$NON-NLS-1$

		specialKeys.put(new Integer(KeyEvent.VK_OPEN_BRACKET), "["); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_CLOSE_BRACKET), "]"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_AMPERSAND), "&"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_ASTERISK), "*"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_QUOTEDBL), "\""); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_LESS), "<"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_GREATER), ">"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_BRACELEFT), "{"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_BRACERIGHT), "}"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_AT), "@"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_COLON), "@"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_CIRCUMFLEX), "^"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_DOLLAR), "$"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_EXCLAMATION_MARK), "!"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_LEFT_PARENTHESIS), "("); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_RIGHT_PARENTHESIS), ")"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_NUMBER_SIGN), "#"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_PLUS), "+"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_UNDERSCORE), "_"); //$NON-NLS-1$
	}

	private final TreeBidiMap<ComparableKeyStroke, String> shortcuts = new TreeBidiMap<ComparableKeyStroke, String>();

	private final IWebClientPluginAccess access;
	private final WindowProvider windowProvider;

	public WicketShortcutHandler(IWebClientPluginAccess access, WindowProvider windowProvider)
	{
		this.windowProvider = windowProvider;
		this.access = access;
	}

	public boolean addShortcut(KeyStroke key)
	{
		String shortcut = convertToJSShortcut(key);
		if (shortcut == null)
		{
			return false;
		}

		HandleShortcutbehaviour behavior = (HandleShortcutbehaviour)access.getPageContributor().getBehavior(BEHAVIOUR);
		if (access.getPageContributor().getBehavior(BEHAVIOUR) == null)
		{
			access.getPageContributor().addBehavior(BEHAVIOUR, behavior = new HandleShortcutbehaviour());
		}

		shortcuts.put(new ComparableKeyStroke(key), shortcut);
		// always call registerShortcut() (also when shortcut is already in the set), needed when called after opening new dialog
		CharSequence js = behavior.getRegisterShortcutJS(shortcut);
		if (js != null)
		{
			access.getPageContributor().addDynamicJavaScript(js.toString());
		}
		return true;
	}

	private String convertToJSShortcut(KeyStroke key)
	{
		StringBuilder sb = new StringBuilder();
		int modifiers = key.getModifiers();
		if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) sb.append("SHIFT+"); //$NON-NLS-1$
		if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) sb.append("CTRL+"); //$NON-NLS-1$
		if ((modifiers & InputEvent.META_DOWN_MASK) != 0) sb.append("META+"); //$NON-NLS-1$
		if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) sb.append("ALT+"); //$NON-NLS-1$

		int supportedModifiers = InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | InputEvent.ALT_DOWN_MASK;
		if ((modifiers | supportedModifiers) != supportedModifiers)
		{
			Debug.log("WindowPlugin: shortcut contains unsopported modifiers"); //$NON-NLS-1$
		}

		String specialKey = specialKeys.get(new Integer(key.getKeyCode()));
		if (specialKey == null)
		{
			sb.append(KeyEvent.getKeyText(key.getKeyCode()));
		}
		else
		{
			sb.append(specialKey);
		}
		return sb.toString();
	}

	public boolean removeShortcut(KeyStroke key)
	{
		String shortcut = shortcuts.remove(new ComparableKeyStroke(key));
		if (shortcut == null)
		{
			return false;
		}
		HandleShortcutbehaviour behavior = (HandleShortcutbehaviour)access.getPageContributor().getBehavior(BEHAVIOUR);
		if (behavior != null)
		{
			CharSequence js = behavior.getRemoveShortcutJS(shortcut);
			if (js != null)
			{
				access.getPageContributor().addDynamicJavaScript(js.toString());
			}
		}
		if (shortcuts.size() == 0)
		{
			// no shortcuts left
			access.getPageContributor().removeBehavior(BEHAVIOUR);
		}
		return true;
	}

	private class HandleShortcutbehaviour extends AbstractServoyDefaultAjaxBehavior
	{
		boolean headRendered = false;
		Set<String> renderedShortcuts = new HashSet<String>();

		@Override
		public void renderHead(IHeaderResponse response)
		{
			super.renderHead(response);

			boolean isAjax = ((WebRequest)RequestCycle.get().getRequest()).isAjax();
			Debug.trace("WicketShortcutHandler: renderHead headRendered=" + headRendered + " isAjax=" + isAjax); //$NON-NLS-1$ //$NON-NLS-2$
			if (!headRendered || !isAjax)
			{
				response.renderJavascriptReference(shortcut_js);
				response.renderJavascript(new StringBuilder().append(//
					"function registerShortcut(sc){").append( //$NON-NLS-1$
					"shortcut.add(sc,function(e){").append( //$NON-NLS-1$
					"var element;").append( //$NON-NLS-1$
					"if(e.target) element=e.target;").append( //$NON-NLS-1$
					"else if(e.srcElement) element=e.srcElement;").append( //$NON-NLS-1$
					"if(element.nodeType==3) element=element.parentNode;").append(// defeat Safari bug //$NON-NLS-1$
					getCallbackScript()).append(//
					"},{'propagate':true,'disable_in_input':false})}"), "registerShortcut"); //$NON-NLS-1$ //$NON-NLS-2$

				if (headRendered && !isAjax)
				{
					// page refresh, re-register all existing shortcuts
					renderedShortcuts.clear();
					StringBuilder sb = new StringBuilder();
					for (String sc : shortcuts.values())
					{
						CharSequence js = getRegisterShortcutJS(sc);
						if (js != null)
						{
							sb.append(js);
						}
					}
					if (sb.length() > 0)
					{
						response.renderJavascript(sb, null);
					}
				}
				headRendered = true;
			}
		}

		@Override
		protected CharSequence generateCallbackScript(final CharSequence partialCall)
		{
			return super.generateCallbackScript(partialCall + "+'&shortcut='+encodeURIComponent(sc)+'&elementId='+element.id"); //$NON-NLS-1$
		}

		public CharSequence getRegisterShortcutJS(String sc)
		{
			if (renderedShortcuts.add(sc))
			{
				return new StringBuilder().append("registerShortcut('").append(sc).append("');"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		}

		public CharSequence getRemoveShortcutJS(String sc)
		{
			if (renderedShortcuts.remove(sc))
			{
				return new StringBuilder().append("shortcut.remove('").append(sc).append("');"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		}

		@Override
		protected void respond(AjaxRequestTarget target)
		{
			if (Debug.tracing())
			{
				Debug.trace("WicketShortcutHandler: respond to URL " + RequestCycle.get().getRequest().getURL()); //$NON-NLS-1$
			}
			final String markupId = RequestCycle.get().getRequest().getParameter("elementId"); //$NON-NLS-1$
			if (markupId == null || markupId.length() == 0)
			{
				// unknown element or outside dialog clicked -- ignore
				Debug.trace("WicketShortcutHandler: called for unknown element"); //$NON-NLS-1$
				return;
			}
			String sc = RequestCycle.get().getRequest().getParameter("shortcut"); //$NON-NLS-1$
			ComparableKeyStroke cks = sc == null ? null : shortcuts.getKey(sc);
			if (cks == null)
			{
				Debug.trace("WicketShortcutHandler: called for unknown or removed shortcut"); //$NON-NLS-1$
				return;
			}

			IPageContributor pageContributor = access.getPageContributor();
			// find the component via the page
			IComponent component = null;
			if (pageContributor instanceof Component)
			{
				MarkupContainer page = ((Component)pageContributor).findParent(Page.class);
				if (page != null)
				{
					Component comp = (Component)page.visitChildren(Component.class, new IVisitor()
					{
						public Object component(Component c)
						{
							if (c.getMarkupId().equals(markupId))
							{
								return c;
							}
							return IVisitor.CONTINUE_TRAVERSAL;
						}
					});
					// find the first IComponent parent
					while (comp != null && !(comp instanceof IComponent))
					{
						comp = comp.getParent();
					}
					component = (IComponent)comp;
				}
			}

			// find the form name
			String formName = null;
			Component form = (Component)component;
			while (form != null)
			{
				if (form instanceof IFormUI)
				{
					formName = ((IFormUI)form).getController().getName();
					break;
				}
				form = form.getParent();
			}

			// shortcut is hit
			windowProvider.shortcutHit(cks.keyStroke, component, formName);
			access.generateAjaxResponse(target);
		}
	}

	public static class ComparableKeyStroke implements Comparable<ComparableKeyStroke>
	{
		public final KeyStroke keyStroke;

		public ComparableKeyStroke(KeyStroke keyStroke)
		{
			assert keyStroke != null;
			this.keyStroke = keyStroke;
		}

		public int compareTo(ComparableKeyStroke cks)
		{
			int thisVal = this.hashCode();
			int anotherVal = cks.hashCode();
			return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
		}

		@Override
		public int hashCode()
		{
			return keyStroke.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj != null && obj.getClass() == getClass())
			{
				return keyStroke.equals(((ComparableKeyStroke)obj).keyStroke);
			}
			return false;
		}
	}

}
