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

import java.awt.event.ActionListener;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.protocol.http.WebRequest;

import com.servoy.extensions.plugins.window.menu.IMenuItem;
import com.servoy.extensions.plugins.window.popup.wicket.PopupPanel;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.server.headlessclient.dataui.AbstractServoyDefaultAjaxBehavior;
import com.servoy.j2db.server.headlessclient.yui.YUILoader;
import com.servoy.j2db.util.Utils;

/**
 * @author jblok
 */
public final class MenuExecutorBehavior extends AbstractServoyDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;
	private Map<Integer, IMenuItem> callableMenuItems;
	private final IWebClientPluginAccess access;

	private static final ResourceReference servoy_menu_css = new CompressedResourceReference(MenuExecutorBehavior.class, "res/servoy-menu.css");
	private static final ResourceReference map_gif = new ResourceReference(MenuExecutorBehavior.class, "res/map.gif");

	/**
	 * @param webDataHtmlView
	 */
	MenuExecutorBehavior(IWebClientPluginAccess access)
	{
		this.access = access;
		map_gif.bind(Application.get());
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.renderCSSReference(YUILoader.CSS_FONTS);
		response.renderCSSReference(YUILoader.CSS_MENU);
		response.renderCSSReference(servoy_menu_css);

		if (((WebRequest)RequestCycle.get().getRequest()).isAjax())
		{
			response.renderJavascript("if (typeof(YAHOO_config) == \"undefined\") {YAHOO_config = {};}YAHOO_config.injecting = true;", "yahoo_config");
		}

		response.renderJavascriptReference(YUILoader.JS_YAHOO_DOM_EVENT);
		response.renderJavascriptReference(YUILoader.JS_CONTAINER_CORE);
		response.renderJavascriptReference(YUILoader.JS_MENU);
		response.renderJavascript(
			"function svy_popmenu_click(psType,psArgs,psValue){wicketAjaxGet(psValue)};var oMenu = new YAHOO.widget.Menu('basicmenu',{zIndex : " + (PopupPanel.ZINDEX + 1) + "});", "yahoomenu");
	}

	@Override
	protected void respond(AjaxRequestTarget target)
	{
		if (callableMenuItems != null)
		{
			int mhash = Utils.getAsInteger(RequestCycle.get().getRequest().getParameter("m"));
			IMenuItem mi = callableMenuItems.get(new Integer(mhash));
			// free callableMenuItems memory, are to be used only in 1 respond.
			callableMenuItems = null;
			if (mi != null)
			{
				ActionListener[] als = mi.getActionListeners();
				if (als != null)
				{
					for (ActionListener element : als)
					{
						element.actionPerformed(null);
					}
					access.generateAjaxResponse(target);
				}
			}
		}
	}

	public CharSequence getUrlForMenuItem(int hashcode)
	{
		CharSequence url = getCallbackUrl();
		StringBuilder asb = new StringBuilder(url.length() + 30);
//		asb.append("wicketAjaxGet(\"");
		asb.append(url);
		asb.append("&m=");
		asb.append(hashcode);
//		asb.append("\")");
		return asb;
	}

	void setCallableMenuItems(Map<Integer, IMenuItem> callableMenuItems)
	{
		this.callableMenuItems = callableMenuItems;
	}
}