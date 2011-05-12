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
import org.apache.wicket.ajax.calldecorator.AjaxPostprocessingCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;

import com.servoy.extensions.beans.dbtreeview.FoundSetTreeModel.UserNode;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.util.DataSourceUtils;

/**
 * Web mouse action.
 * @author gboros
 */
public abstract class MouseAction
{
	private final IWicketTree tree;

	public MouseAction(IWicketTree tree)
	{
		this.tree = tree;
	}

	public abstract String getName();

	public abstract String getReturnProvider(UserNode userNode);

	public abstract FunctionDefinition getMethodToCall(UserNode userNode);

	public abstract Object getModelObject();

	public AjaxPostprocessingCallDecorator getPostprocessingCallDecorator()
	{
		return new CancelEventIfNoAjaxDecorator();
	}

	public void execute(AjaxRequestTarget target, int mx, int my)
	{
		Object tn = getModelObject();
		if (tn instanceof FoundSetTreeModel.UserNode)
		{
			FoundSetTreeModel.UserNode un = (FoundSetTreeModel.UserNode)tn;
			IRecord r = un.getRecord();
			if (r != null)
			{
				String[] server_table = DataSourceUtils.getDBServernameTablename(un.getFoundSet().getDataSource());
				Object[] args = new Object[] { r.getValue(getReturnProvider(un)), (server_table == null ? null : server_table[1]), new Integer(mx), new Integer(
					my), null };

				FunctionDefinition f = getMethodToCall(un);
				if (f != null)
				{
					f.execute(tree.getClientPluginAccess(), args, false);
					tree.generateAjaxResponse(target);
				}
			}
		}
	}
}