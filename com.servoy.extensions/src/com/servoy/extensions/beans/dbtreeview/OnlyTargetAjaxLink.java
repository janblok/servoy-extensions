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
package com.servoy.extensions.beans.dbtreeview;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

import com.servoy.j2db.server.headlessclient.dataui.ServoyAjaxEventBehavior;

public abstract class OnlyTargetAjaxLink extends AbstractLink implements IAjaxLink
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public OnlyTargetAjaxLink(final String id)
	{
		this(id, null);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 */
	public OnlyTargetAjaxLink(final String id, final IModel model)
	{
		super(id, model);

		add(new ServoyAjaxEventBehavior("onclick")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				onClick(target);
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new CancelEventIfNoAjaxDecorator(OnlyTargetAjaxLink.this.getAjaxCallDecorator());
			}

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				// add the onclick handler only if link is enabled
				if (isLinkEnabled())
				{
					super.onComponentTag(tag);
				}
			}

			@Override
			protected CharSequence getCallbackScript()
			{
				return getCallbackScript(true);
			}
		});
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);

		if (isLinkEnabled())
		{
			// disable any href attr in markup
			if (tag.getName().equalsIgnoreCase("a") || tag.getName().equalsIgnoreCase("link") || tag.getName().equalsIgnoreCase("area"))
			{
				tag.put("href", "#");
			}
		}
		else
		{
			disableLink(tag);
		}

	}

	/**
	 * Returns ajax call decorator that will be used to decorate the ajax call.
	 * 
	 * @return ajax call decorator
	 */
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return null;
	}

	/**
	 * Listener method invoked on the ajax request generated when the user clicks the link
	 * 
	 * @param target
	 */
	public abstract void onClick(final AjaxRequestTarget target);
}
