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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.string.AppendingStringBuffer;

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
		specialKeys.put(new Integer(KeyEvent.VK_ADD), "add"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_MULTIPLY), "multiply"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_DIVIDE), "divide"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_SUBTRACT), "subtract"); //$NON-NLS-1$
		specialKeys.put(new Integer(KeyEvent.VK_DECIMAL), "decimal"); //$NON-NLS-1$
	}

	private final TreeBidiMap<ComparableKeyStroke, String> shortcuts = new TreeBidiMap<ComparableKeyStroke, String>();

	private final IWebClientPluginAccess access;
	private final WindowProvider windowProvider;

	public WicketShortcutHandler(IWebClientPluginAccess access, WindowProvider windowProvider)
	{
		this.windowProvider = windowProvider;
		this.access = access;
	}

	public boolean addShortcut(KeyStroke key, boolean consumeEvent)
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

		shortcuts.put(new ComparableKeyStroke(key, consumeEvent), shortcut);
		// always call registerShortcut() (also when shortcut is already in the set), needed when called after opening new dialog
		behavior.getRegisterShortcutJS(shortcut, consumeEvent);
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
			Debug.log("WindowPlugin: shortcut contains unsupported modifiers: " + modifiers); //$NON-NLS-1$
		}

		String specialKey = specialKeys.get(Integer.valueOf(key.getKeyCode()));
		if (specialKey == null)
		{
			sb.append(getKeyText(key.getKeyCode()));
		}
		else
		{
			sb.append(specialKey);
		}
		return sb.toString();
	}

	public boolean removeShortcut(KeyStroke key)
	{
		String shortcut = shortcuts.remove(new ComparableKeyStroke(key, false));
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
				response.renderJavascript(
					new StringBuilder().append(//
						"function registerShortcut(sc, consumeEvent){").append( //$NON-NLS-1$
							"shortcut.add(sc,function(e){").append( //$NON-NLS-1$
								"var element;").append( //$NON-NLS-1$
									"if(e.target) element=e.target;").append( //$NON-NLS-1$
										"else if(e.srcElement) element=e.srcElement;").append( //$NON-NLS-1$
											"if(element.nodeType==3) element=element.parentNode;").append(// defeat Safari bug //$NON-NLS-1$
												getCallbackScript()).append(//
													"},{'propagate':!consumeEvent,'disable_in_input':false})}"), //$NON-NLS-1$
					"registerShortcut"); //$NON-NLS-1$

				if (headRendered && !isAjax)
				{
					// page refresh, re-register all existing shortcuts
					renderedShortcuts.clear();
					StringBuilder sb = new StringBuilder();
					for (Entry<ComparableKeyStroke, String> entry : shortcuts.entrySet())
					{
						CharSequence js = getRegisterShortcutJS(entry.getValue(), false, entry.getKey().shouldConsume());
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
				else
				{
					renderNewShortCuts(response);
				}

				headRendered = true;
			}
			else
			{
				renderNewShortCuts(response);
			}
		}

		private void renderNewShortCuts(IHeaderResponse response)
		{
			Iterator<CharSequence> newShortcuts = newShortCuts.iterator();
			while (newShortcuts.hasNext())
			{
				response.renderJavascript(newShortcuts.next(), null);
			}
			newShortCuts.clear();
		}

		@Override
		protected CharSequence getCallbackScript(boolean onlyTargetActivePage)
		{
			// post, serialize the component so that modified data is pushed in the FormComponent.
			return generateCallbackScript(new AppendingStringBuffer("wicketAjaxPost('").append(getCallbackUrl(onlyTargetActivePage)).append( //$NON-NLS-1$
				"'+'&shortcut='+encodeURIComponent(sc)+'&elementId='+element.id, wicketSerialize(Wicket.$(element.id))")); //$NON-NLS-1$
		}

		List<CharSequence> newShortCuts = new ArrayList<CharSequence>();

		public CharSequence getRegisterShortcutJS(String sc, boolean consumeEvent)
		{
			return getRegisterShortcutJS(sc, true, consumeEvent);
		}

		public CharSequence getRegisterShortcutJS(String sc, boolean fillNewShortCutsMap, boolean consumeEvent)
		{
			if (renderedShortcuts.add(sc))
			{
				StringBuilder newShortcutJS = new StringBuilder().append("registerShortcut('").append(sc).append("'," + consumeEvent + ");"); //$NON-NLS-1$ //$NON-NLS-2$
				if (fillNewShortCutsMap) newShortCuts.add(newShortcutJS);
				return newShortcutJS;
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
			// can be null or empty string if the shortcut target has no id
			final String markupId = RequestCycle.get().getRequest().getParameter("elementId"); //$NON-NLS-1$

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
					Component comp = (Component)page.visitChildren(Component.class, new IVisitor<Component>()
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

			// update the form component input
			if (component instanceof FormComponent)
			{
				FormComponent< ? > formComponent = (FormComponent< ? >)component;
				formComponent.inputChanged(); // reads input from request post data
				formComponent.validate();
				if (formComponent.hasErrorMessage())
				{
					formComponent.invalid();
				}
				else
				{
					formComponent.valid();
					formComponent.updateModel();
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
		private final boolean consume;

		public ComparableKeyStroke(KeyStroke keyStroke, boolean consume)
		{
			this.consume = consume;
			assert keyStroke != null;
			this.keyStroke = keyStroke;
		}

		/**
		 * @return
		 */
		public boolean shouldConsume()
		{
			return consume;
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

	@SuppressWarnings("nls")
	private static String getKeyText(int keyCode)
	{
		if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9 || keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z)
		{
			return String.valueOf((char)keyCode);
		}

		switch (keyCode)
		{
			case KeyEvent.VK_ENTER :
				return "Enter";
			case KeyEvent.VK_BACK_SPACE :
				return "Backspace";
			case KeyEvent.VK_TAB :
				return "Tab";
			case KeyEvent.VK_CANCEL :
				return "Cancel";
			case KeyEvent.VK_CLEAR :
				return "Clear";
			case KeyEvent.VK_COMPOSE :
				return "Compose";
			case KeyEvent.VK_PAUSE :
				return "Pause";
			case KeyEvent.VK_CAPS_LOCK :
				return "Caps Lock";
			case KeyEvent.VK_ESCAPE :
				return "Escape";
			case KeyEvent.VK_SPACE :
				return "Space";
			case KeyEvent.VK_PAGE_UP :
				return "pageup";
			case KeyEvent.VK_PAGE_DOWN :
				return "pagedown";
			case KeyEvent.VK_END :
				return "End";
			case KeyEvent.VK_HOME :
				return "Home";
			case KeyEvent.VK_LEFT :
				return "Left";
			case KeyEvent.VK_UP :
				return "Up";
			case KeyEvent.VK_RIGHT :
				return "Right";
			case KeyEvent.VK_DOWN :
				return "Down";
			case KeyEvent.VK_BEGIN :
				return "Begin";

			// modifiers
			case KeyEvent.VK_SHIFT :
				return "Shift";
			case KeyEvent.VK_CONTROL :
				return "Control";
			case KeyEvent.VK_ALT :
				return "Alt";
			case KeyEvent.VK_META :
				return "Meta";
			case KeyEvent.VK_ALT_GRAPH :
				return "Alt Graph";

			// punctuation
			case KeyEvent.VK_COMMA :
				return "Comma";
			case KeyEvent.VK_PERIOD :
				return "Period";
			case KeyEvent.VK_SLASH :
				return "Slash";
			case KeyEvent.VK_SEMICOLON :
				return "Semicolon";
			case KeyEvent.VK_EQUALS :
				return "Equals";
			case KeyEvent.VK_OPEN_BRACKET :
				return "Open Bracket";
			case KeyEvent.VK_BACK_SLASH :
				return "Back Slash";
			case KeyEvent.VK_CLOSE_BRACKET :
				return "Close Bracket";

			// numpad numeric keys handled below
			case KeyEvent.VK_MULTIPLY :
				return "NumPad *";
			case KeyEvent.VK_ADD :
				return "NumPad +";
			case KeyEvent.VK_SEPARATOR :
				return "NumPad ,";
			case KeyEvent.VK_SUBTRACT :
				return "NumPad -";
			case KeyEvent.VK_DECIMAL :
				return "NumPad .";
			case KeyEvent.VK_DIVIDE :
				return "NumPad /";
			case KeyEvent.VK_DELETE :
				return "Delete";
			case KeyEvent.VK_NUM_LOCK :
				return "Num Lock";
			case KeyEvent.VK_SCROLL_LOCK :
				return "Scroll Lock";

			case KeyEvent.VK_WINDOWS :
				return "Windows";
			case KeyEvent.VK_CONTEXT_MENU :
				return "Context Menu";

			case KeyEvent.VK_F1 :
				return "F1";
			case KeyEvent.VK_F2 :
				return "F2";
			case KeyEvent.VK_F3 :
				return "F3";
			case KeyEvent.VK_F4 :
				return "F4";
			case KeyEvent.VK_F5 :
				return "F5";
			case KeyEvent.VK_F6 :
				return "F6";
			case KeyEvent.VK_F7 :
				return "F7";
			case KeyEvent.VK_F8 :
				return "F8";
			case KeyEvent.VK_F9 :
				return "F9";
			case KeyEvent.VK_F10 :
				return "F10";
			case KeyEvent.VK_F11 :
				return "F11";
			case KeyEvent.VK_F12 :
				return "F12";
			case KeyEvent.VK_F13 :
				return "F13";
			case KeyEvent.VK_F14 :
				return "F14";
			case KeyEvent.VK_F15 :
				return "F15";
			case KeyEvent.VK_F16 :
				return "F16";
			case KeyEvent.VK_F17 :
				return "F17";
			case KeyEvent.VK_F18 :
				return "F18";
			case KeyEvent.VK_F19 :
				return "F19";
			case KeyEvent.VK_F20 :
				return "F20";
			case KeyEvent.VK_F21 :
				return "F21";
			case KeyEvent.VK_F22 :
				return "F22";
			case KeyEvent.VK_F23 :
				return "F23";
			case KeyEvent.VK_F24 :
				return "F24";

			case KeyEvent.VK_PRINTSCREEN :
				return "Print Screen";
			case KeyEvent.VK_INSERT :
				return "Insert";
			case KeyEvent.VK_HELP :
				return "Help";
			case KeyEvent.VK_BACK_QUOTE :
				return "Back Quote";
			case KeyEvent.VK_QUOTE :
				return "Quote";

			case KeyEvent.VK_KP_UP :
				return "Up";
			case KeyEvent.VK_KP_DOWN :
				return "Down";
			case KeyEvent.VK_KP_LEFT :
				return "Left";
			case KeyEvent.VK_KP_RIGHT :
				return "Right";

			case KeyEvent.VK_DEAD_GRAVE :
				return "Dead Grave";
			case KeyEvent.VK_DEAD_ACUTE :
				return "Dead Acute";
			case KeyEvent.VK_DEAD_CIRCUMFLEX :
				return "Dead Circumflex";
			case KeyEvent.VK_DEAD_TILDE :
				return "Dead Tilde";
			case KeyEvent.VK_DEAD_MACRON :
				return "Dead Macron";
			case KeyEvent.VK_DEAD_BREVE :
				return "Dead Breve";
			case KeyEvent.VK_DEAD_ABOVEDOT :
				return "Dead Above Dot";
			case KeyEvent.VK_DEAD_DIAERESIS :
				return "Dead Diaeresis";
			case KeyEvent.VK_DEAD_ABOVERING :
				return "Dead Above Ring";
			case KeyEvent.VK_DEAD_DOUBLEACUTE :
				return "Dead Double Acute";
			case KeyEvent.VK_DEAD_CARON :
				return "Dead Caron";
			case KeyEvent.VK_DEAD_CEDILLA :
				return "Dead Cedilla";
			case KeyEvent.VK_DEAD_OGONEK :
				return "Dead Ogonek";
			case KeyEvent.VK_DEAD_IOTA :
				return "Dead Iota";
			case KeyEvent.VK_DEAD_VOICED_SOUND :
				return "Dead Voiced Sound";
			case KeyEvent.VK_DEAD_SEMIVOICED_SOUND :
				return "Dead Semivoiced Sound";

			case KeyEvent.VK_AMPERSAND :
				return "Ampersand";
			case KeyEvent.VK_ASTERISK :
				return "Asterisk";
			case KeyEvent.VK_QUOTEDBL :
				return "Double Quote";
			case KeyEvent.VK_LESS :
				return "Less";
			case KeyEvent.VK_GREATER :
				return "Greater";
			case KeyEvent.VK_BRACELEFT :
				return "Left Brace";
			case KeyEvent.VK_BRACERIGHT :
				return "Right Brace";
			case KeyEvent.VK_AT :
				return "At";
			case KeyEvent.VK_COLON :
				return "Colon";
			case KeyEvent.VK_CIRCUMFLEX :
				return "Circumflex";
			case KeyEvent.VK_DOLLAR :
				return "Dollar";
			case KeyEvent.VK_EURO_SIGN :
				return "Euro";
			case KeyEvent.VK_EXCLAMATION_MARK :
				return "Exclamation Mark";
			case KeyEvent.VK_INVERTED_EXCLAMATION_MARK :
				return "Inverted Exclamation Mark";
			case KeyEvent.VK_LEFT_PARENTHESIS :
				return "Left Parenthesis";
			case KeyEvent.VK_NUMBER_SIGN :
				return "Number Sign";
			case KeyEvent.VK_MINUS :
				return "Minus";
			case KeyEvent.VK_PLUS :
				return "Plus";
			case KeyEvent.VK_RIGHT_PARENTHESIS :
				return "Right Parenthesis";
			case KeyEvent.VK_UNDERSCORE :
				return "Underscore";

			case KeyEvent.VK_FINAL :
				return "Final";
			case KeyEvent.VK_CONVERT :
				return "Convert";
			case KeyEvent.VK_NONCONVERT :
				return "No Convert";
			case KeyEvent.VK_ACCEPT :
				return "Accept";
			case KeyEvent.VK_MODECHANGE :
				return "Mode Change";
			case KeyEvent.VK_KANA :
				return "Kana";
			case KeyEvent.VK_KANJI :
				return "Kanji";
			case KeyEvent.VK_ALPHANUMERIC :
				return "Alphanumeric";
			case KeyEvent.VK_KATAKANA :
				return "Katakana";
			case KeyEvent.VK_HIRAGANA :
				return "Hiragana";
			case KeyEvent.VK_FULL_WIDTH :
				return "Full-Width";
			case KeyEvent.VK_HALF_WIDTH :
				return "Half-Width";
			case KeyEvent.VK_ROMAN_CHARACTERS :
				return "Roman Characters";
			case KeyEvent.VK_ALL_CANDIDATES :
				return "All Candidates";
			case KeyEvent.VK_PREVIOUS_CANDIDATE :
				return "Previous Candidate";
			case KeyEvent.VK_CODE_INPUT :
				return "Code Input";
			case KeyEvent.VK_JAPANESE_KATAKANA :
				return "Japanese Katakana";
			case KeyEvent.VK_JAPANESE_HIRAGANA :
				return "Japanese Hiragana";
			case KeyEvent.VK_JAPANESE_ROMAN :
				return "Japanese Roman";
			case KeyEvent.VK_KANA_LOCK :
				return "Kana Lock";
			case KeyEvent.VK_INPUT_METHOD_ON_OFF :
				return "Input Method On/Off";

			case KeyEvent.VK_AGAIN :
				return "Again";
			case KeyEvent.VK_UNDO :
				return "Undo";
			case KeyEvent.VK_COPY :
				return "Copy";
			case KeyEvent.VK_PASTE :
				return "Paste";
			case KeyEvent.VK_CUT :
				return "Cut";
			case KeyEvent.VK_FIND :
				return "Find";
			case KeyEvent.VK_PROPS :
				return "Props";
			case KeyEvent.VK_STOP :
				return "Stop";
		}

		if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9)
		{
			String numpad = "NumPad";
			char c = (char)(keyCode - KeyEvent.VK_NUMPAD0 + '0');
			return numpad + "-" + c;
		}

		String unknown = "Unknown";
		return unknown + " keyCode: 0x" + Integer.toString(keyCode, 16);
	}

	@Override
	public void cleanup()
	{

	}
}
