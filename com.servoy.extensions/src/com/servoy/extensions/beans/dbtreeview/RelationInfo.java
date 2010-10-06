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

import com.servoy.j2db.scripting.IScriptObject;

/**
 * Script object holding informations for relation nodes 
 * 
 * @author gboros
 */
public class RelationInfo implements IScriptObject
{
	private String label;
	private String nRelationName;

	public RelationInfo()
	{
	}

	public String getLabel()
	{
		return label;
	}

	public void js_setLabel(String label)
	{
		this.label = label;
	}

	public String getNRelationName()
	{
		return nRelationName;
	}

	public void js_setNRelationName(String relationName)
	{
		nRelationName = relationName;
	}

	public Class[] getAllReturnedTypes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getParameterNames(String methodName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getSample(String methodName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolTip(String methodName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
