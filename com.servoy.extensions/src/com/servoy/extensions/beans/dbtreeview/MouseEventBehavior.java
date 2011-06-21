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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;


/**
 * Wicket mouse event behavior.
 * @author gboros
 *
 */
public class MouseEventBehavior extends AjaxEventBehavior
{
	private static final long serialVersionUID = 1L;
	private final MouseAction mouseAction;

	public static final String MOUSE_POSITION_SCRIPT = "var mx=(event.pageX ? event.pageX : event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft); var my=(event.pageY ? event.pageY : event.clientY + document.body.scrollLeft + document.documentElement.scrollLeft);";

	public MouseEventBehavior(MouseAction mouseAction)
	{
		super(mouseAction.getName());
		this.mouseAction = mouseAction;
	}

	@Override
	protected void onEvent(AjaxRequestTarget target)
	{
		String mx = getComponent().getRequest().getParameter("mx");
		String my = getComponent().getRequest().getParameter("my");
		mouseAction.execute(target, Integer.parseInt(mx), Integer.parseInt(my));
	}

	@Override
	public CharSequence getCallbackUrl(final boolean onlyTargetActivePage)
	{
		CharSequence callbackURL = super.getCallbackUrl(onlyTargetActivePage);
		return callbackURL.toString() + "&mx=' + mx + '&my=' + my + '";
	}

	@Override
	protected CharSequence getCallbackScript()
	{
		return getCallbackScript(true);
	}

	@Override
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return mouseAction.getPostprocessingCallDecorator();
	}

	@Override
	public boolean isEnabled(Component component)
	{
		Object tn = mouseAction.getModelObject();
		if (tn instanceof FoundSetTreeModel.UserNode)
		{
			return mouseAction.getMethodToCall((FoundSetTreeModel.UserNode)tn) != null;
		}

		return false;
	}
}