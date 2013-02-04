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
import com.servoy.j2db.dataprocessing.IFoundSet;
import com.servoy.j2db.persistence.Part;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.server.headlessclient.WebRuntimeWindow;
import com.servoy.j2db.server.headlessclient.yui.YUILoader;
import com.servoy.j2db.util.ServoyException;

/**
 * @author jcompagner
 *
 */
public class PopupPanel extends Panel
{
	private final Component elementToShowRelatedTo;
	private final String formName;
	private final IClientPluginAccess clientPluginAccess;

	/**
	 * @param id
	 */
	public PopupPanel(String id, IForm form, Component elementToShowRelatedTo, IClientPluginAccess clientPluginAccess)
	{
		super(id);
		this.elementToShowRelatedTo = elementToShowRelatedTo;
		this.formName = form.getName();
		this.clientPluginAccess = clientPluginAccess;
		add((Component)form.getFormUI());
		setOutputMarkupId(true);
		Dimension size = ((FormController)form).getForm().getSize();
		StringBuilder style = new StringBuilder("display:none;position:absolute;z-index:999;"); //$NON-NLS-1$
		int height = size.height;
		int formView = form.getView();
		if (formView == IForm.LIST_VIEW || formView == FormController.LOCKED_LIST_VIEW)
		{
			IFoundSet formModel = ((FormController)form).getFormModel();
			if (formModel != null)
			{
				FormController fc = (FormController)form;
				int extraHeight = fc.getPartHeight(Part.HEADER) + fc.getPartHeight(Part.TITLE_HEADER) + fc.getPartHeight(Part.FOOTER) +
					fc.getPartHeight(Part.TITLE_FOOTER);
				height = (height - extraHeight) * formModel.getSize() + extraHeight;
				WebRuntimeWindow window = (WebRuntimeWindow)clientPluginAccess.getCurrentRuntimeWindow();
				if (height > window.getHeight()) height = window.getHeight();
			}
		}

		style.append("width:").append(size.width).append("px;height:").append(height).append("px"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		add(new SimpleAttributeModifier("style", style.toString())); //$NON-NLS-1$
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

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		if (size() == 0 && formName != null && clientPluginAccess != null && clientPluginAccess.getFormManager() != null)
		{
			IForm form = clientPluginAccess.getFormManager().getForm(formName);
			if (form != null)
			{
				try
				{
					form.setUsingAsExternalComponent(true);
				}
				catch (ServoyException e)
				{
					System.err.println(e.getMessage());
				}
				add((Component)form.getFormUI());
			}
		}
	}
}
