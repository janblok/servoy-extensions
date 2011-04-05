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

	@SuppressWarnings("nls")
	public String[] getParameterNames(String methodName)
	{
		if ("setLabel".equals(methodName))
		{
			return new String[] { "label" };
		}
		else if ("setNRelationName".equals(methodName))
		{
			return new String[] { "relationName" };
		}
		return null;
	}

	@SuppressWarnings("nls")
	public String getSample(String methodName)
	{
		if ("setLabel".equals(methodName) || "setNRelationName".equals(methodName))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("var companies_binding = elements.myDbTreeView.createBinding('example_data', 'companies');\n");
			retval.append("var company_relations = new Array();\n");
			retval.append("company_relations[0] = elements.myDbTreeView.createRelationInfo();\n");
			retval.append("company_relations[0].setLabel('Employees');\n");
			retval.append("company_relations[0].setNRelationName('companies_to_employees');\n");
			retval.append("companies_binding.setNRelationInfos(company_relations);\n");
			return retval.toString();
		}
		return null;
	}

	@SuppressWarnings("nls")
	public String getToolTip(String methodName)
	{
		if ("setLabel".equals(methodName))
		{
			return "Sets the label of a relation info object used to set multiple child relations for a tree node.";
		}
		else if ("setNRelationName".equals(methodName))
		{
			return "Sets the name of a relation info object used to set multiple child relations for a tree node.";
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("RelationInfo { relationName : ").append(nRelationName).append(" }").toString();
	}
}
