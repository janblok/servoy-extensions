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
package com.servoy.extensions.plugins.window.menu.wicket;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;

import com.servoy.extensions.plugins.window.menu.IMenuItem;
import com.servoy.extensions.plugins.window.menu.IPopupMenu;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.server.headlessclient.IPageContributor;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.util.Utils;

/**
 * Popupmenu in webclient.
 * 
 * @author jblok
 */

public class ScriptableWicketPopupMenu extends ScriptableWicketMenu implements IPopupMenu
{
	public ScriptableWicketPopupMenu(IClientPluginAccess access)
	{
		super(null, access);
	}

	private final Map<Integer, IMenuItem> callableMenuItems = new HashMap<Integer, IMenuItem>();

	public void showPopup(Object comp, int x, int y)
	{
		boolean useAJAX = Utils.getAsBoolean(app.getRuntimeProperties().get("useAJAX"));
		if (useAJAX)
		{
			IPageContributor pc = app.getPageContributor();
			if (pc != null)
			{
				String jsComp = null;
				if (comp instanceof IComponent && comp instanceof Component)
				{
					IComponent c = (IComponent)comp;
					jsComp = "document.getElementById('" + ((Component)c).getMarkupId() + "');";
				}

				StringBuilder js = new StringBuilder();

				js.append("oMenu.clearContent();");
				js.append(generateMenuJS(this, "oMenu"));				
				js.append("setTimeout(\"");

				if (jsComp != null)
				{
					js.append("var jsComp = ").append(jsComp).append(";");
					js.append("var parentReg = YAHOO.util.Dom.getRegion(jsComp.offsetParent);");
					js.append("var jsCompReg = YAHOO.util.Dom.getRegion(jsComp);");
					js.append("oMenu.render(document.body);");
					js.append("var oMenuReg = YAHOO.util.Dom.getRegion(document.getElementById(oMenu.id));");

					String popupHeight = "(oMenuReg.bottom - oMenuReg.top)";
					String render_parentY = "parentReg.top";
					String render_parentHeight = "(parentReg.bottom - parentReg.top)";
					String jsCompY = "jsCompReg.top + " + y;


					js.append("if(jsComp.offsetParent && ").append(jsCompY).append("+").append(popupHeight).append(" > ").append(render_parentY).append(" + ").append(
						render_parentHeight).append(" && ").append(jsCompY).append(" - ").append(popupHeight).append(" > ").append(render_parentY).append("){");

					js.append("oMenu.moveTo(jsCompReg.left  + ").append(x).append(",jsCompReg.top - ").append(popupHeight).append(");");

					js.append("} else {");

					js.append("oMenu.moveTo(jsCompReg.left  + ").append(x).append(",jsCompReg.top + ").append(y).append(");");

					js.append("}");
				}
				else
				{
					js.append("oMenu.render(document.body);");
					js.append("oMenu.moveTo(").append(x).append(",").append(y).append(");");
				}
				js.append("oMenu.show();\", 0);");

				MenuExecutorBehavior menuExecutor = (MenuExecutorBehavior)pc.getBehavior("PopMenuExecutor");
				if (menuExecutor != null)
				{
					menuExecutor.setCallableMenuItems(callableMenuItems);
				}

				pc.addDynamicJavaScript(js.toString());
			}
		}
	}

	public void hidePopup()
	{
		// Not implemented in webclient
		// TODO
	}

	CharSequence getCallBackUrl(IMenuItem item)
	{
		callableMenuItems.put(new Integer(item.hashCode()), item);

		boolean useAJAX = Utils.getAsBoolean(app.getRuntimeProperties().get("useAJAX"));
		if (useAJAX)
		{
			IPageContributor pc = app.getPageContributor();
			if (pc != null)
			{
				MenuExecutorBehavior menuExecutor = (MenuExecutorBehavior)pc.getBehavior("PopMenuExecutor");
				if (menuExecutor == null)
				{
					menuExecutor = new MenuExecutorBehavior(app);
					pc.addBehavior("PopMenuExecutor", menuExecutor);
				}
				return menuExecutor.getUrlForMenuItem(item.hashCode());
			}
		}
		return "";
	}
}
