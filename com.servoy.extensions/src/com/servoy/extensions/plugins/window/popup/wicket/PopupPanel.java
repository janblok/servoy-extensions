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

package com.servoy.extensions.plugins.window.popup.wicket;

import java.awt.Dimension;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;

import com.servoy.j2db.FormController;
import com.servoy.j2db.IForm;
import com.servoy.j2db.server.headlessclient.yui.YUILoader;

/**
 * @author jcompagner
 *
 */
public class PopupPanel extends Panel
{
	private final Component elementToShowRelatedTo;

	/**
	 * @param id
	 */
	public PopupPanel(String id, IForm form, Component elementToShowRelatedTo)
	{
		super(id);
		this.elementToShowRelatedTo = elementToShowRelatedTo;
		add((Component)form.getFormUI());
		setOutputMarkupId(true);
		Dimension size = ((FormController)form).getForm().getSize();
		add(new SimpleAttributeModifier("style", "position:absolute;z-index:999;width:" + size.width + "px;height:" + size.height + "px"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.markup.html.panel.Panel#renderHead(org.apache.wicket.markup.html.internal.HtmlHeaderContainer)
	 */
	@Override
	public void renderHead(HtmlHeaderContainer container)
	{
		super.renderHead(container);
		StringBuilder js = new StringBuilder();
		if (elementToShowRelatedTo != null)
		{
			container.getHeaderResponse().renderJavascriptReference(YUILoader.JS_YAHOO_DOM_EVENT);
		
			js.append("positionPopup('");
			js.append(getMarkupId());
			js.append("','");
			js.append(elementToShowRelatedTo.getMarkupId());
			js.append("');");
		}
		else
		{
			js.append("centerPopup('");
			js.append(getMarkupId());
			js.append("');");
		}
		container.getHeaderResponse().renderOnDomReadyJavascript(js.toString());
	}
}
