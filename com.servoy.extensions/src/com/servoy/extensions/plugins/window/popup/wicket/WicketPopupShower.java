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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.mozilla.javascript.Scriptable;

import com.servoy.extensions.plugins.window.popup.IPopupShower;
import com.servoy.j2db.IForm;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.server.headlessclient.IPageContributor;
import com.servoy.j2db.server.headlessclient.IRepeatingView;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.server.headlessclient.dataui.AbstractServoyDefaultAjaxBehavior;
import com.servoy.j2db.server.headlessclient.dataui.WebEventExecutor;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.ServoyException;

/**
 * @author jcompagner
 *
 */
public class WicketPopupShower implements IPopupShower
{
	private final IClientPluginAccess clientPluginAccess;
	private Component elementToShowRelatedTo;
	private final IForm form;
	private final Scriptable scope;
	private final String dataprovider;
	private final int width, height;

	/**
	 * @param elementToShowRelatedTo
	 * @param form
	 * @param scope
	 * @param dataprovider
	 * @param clientPluginAccess 
	 */
	public WicketPopupShower(IClientPluginAccess clientPluginAccess, IComponent elementToShowRelatedTo, IForm form, Scriptable scope, String dataprovider,
		int width, int height)
	{
		this.clientPluginAccess = clientPluginAccess;
		if (elementToShowRelatedTo instanceof Component)
		{
			this.elementToShowRelatedTo = (Component)elementToShowRelatedTo;
		}
		this.form = form;
		this.scope = scope;
		this.dataprovider = dataprovider;
		this.width = width;
		this.height = height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#show()
	 */
	public void show()
	{
		IPageContributor pageContributor = ((IWebClientPluginAccess)clientPluginAccess).getPageContributor();
		IRepeatingView repeatingView = pageContributor.getRepeatingView();

		if (repeatingView.getComponent("popup") != null)
		{
			repeatingView.removeComponent("popup");
		}
		final PopupPanel popupPanel = new PopupPanel(repeatingView.newChildId(), form, elementToShowRelatedTo, clientPluginAccess, width, height);
		popupPanel.add(new AbstractServoyDefaultAjaxBehavior()
		{
			@Override
			public void renderHead(IHeaderResponse response)
			{
				response.renderOnDomReadyJavascript("ServoyPopup.setup('" + popupPanel.getMarkupId() + "', '" + getCallbackUrl() + "');");
			}

			@Override
			protected void respond(AjaxRequestTarget target)
			{
				Page page = popupPanel.getPage();
				close();
				WebEventExecutor.generateResponse(target, page);
			}

		});
		repeatingView.addComponent("popup", popupPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#close(java.lang.Object)
	 */
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
		close();
	}

	/**
	 * 
	 */
	private void close()
	{
		IPageContributor pageContributor = ((IWebClientPluginAccess)clientPluginAccess).getPageContributor();
		IRepeatingView repeatingView = pageContributor.getRepeatingView();
		Component popupComponent;
		if ((popupComponent = repeatingView.getComponent("popup")) instanceof PopupPanel)
		{
			((PopupPanel)popupComponent).cleanup();
		}
		repeatingView.removeComponent("popup");
		try
		{
			form.setUsingAsExternalComponent(false);
		}
		catch (ServoyException e)
		{
			Debug.error(e);
		}
	}
}
