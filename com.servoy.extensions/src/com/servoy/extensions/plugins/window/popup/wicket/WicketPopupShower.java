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
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.mozilla.javascript.Scriptable;

import com.servoy.extensions.plugins.window.popup.IPopupShower;
import com.servoy.j2db.IForm;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.server.headlessclient.IPageContributor;
import com.servoy.j2db.server.headlessclient.IRepeatingView;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
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

	private static final ResourceReference TRANSPARENT_GIF = new ResourceReference(WicketPopupShower.class, "res/transparent.gif"); //$NON-NLS-1$

	/**
	 * @param elementToShowRelatedTo
	 * @param form
	 * @param scope
	 * @param dataprovider
	 * @param clientPluginAccess 
	 */
	@SuppressWarnings("nls")
	public WicketPopupShower(IClientPluginAccess clientPluginAccess, IComponent elementToShowRelatedTo, IForm form, Scriptable scope, String dataprovider)
	{
		this.clientPluginAccess = clientPluginAccess;
		if (elementToShowRelatedTo instanceof Component)
		{
			this.elementToShowRelatedTo = (Component)elementToShowRelatedTo;
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
	@SuppressWarnings("nls")
	public void show()
	{
		IPageContributor pageContributor = ((IWebClientPluginAccess)clientPluginAccess).getPageContributor();
		IRepeatingView repeatingView = pageContributor.getRepeatingView();

		if (repeatingView.getComponent("popup") != null)
		{
			repeatingView.removeComponent("popup");
		}
		repeatingView.addComponent("popup", new PopupPanel(repeatingView.newChildId(), form, elementToShowRelatedTo, clientPluginAccess));

		final WebMarkupContainer container = new WebMarkupContainer(repeatingView.newChildId());
		StringBuilder containerStyle = new StringBuilder("position:absolute;z-index:990;top:0px;right:0px;bottom:0px;left:0px;");
		// for IE we need to set a transparent image, else input fields under the div will get the click, not the div (case: SVY-2700)
		ClientProperties clp = ((WebClientInfo)Session.get().getClientInfo()).getProperties();
		if (clp.isBrowserInternetExplorer())
		{
			containerStyle.append("background-image:url(").append(container.urlFor(TRANSPARENT_GIF)).append(");background-size: contain;");
		}
		container.add(new SimpleAttributeModifier("style", containerStyle.toString()));
		container.add(new AjaxEventBehavior("onclick")
		{
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				Page page = container.getPage();
				close();
				WebEventExecutor.generateResponse(target, page);
			}
		});
		repeatingView.addComponent("blocker", container);
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
		close();
	}

	/**
	 * 
	 */
	private void close()
	{
		IPageContributor pageContributor = ((IWebClientPluginAccess)clientPluginAccess).getPageContributor();
		IRepeatingView repeatingView = pageContributor.getRepeatingView();
		repeatingView.removeComponent("popup");
		repeatingView.removeComponent("blocker");
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
