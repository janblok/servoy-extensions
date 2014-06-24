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

import com.servoy.base.scripting.annotations.ServoyClientSupport;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IScriptable;

/**
 * Script object holding informations for relation nodes 
 * 
 * @author gboros
 */
@ServoyDocumented(category = ServoyDocumented.BEANS, scriptingName = "RelationInfo")
@ServoyClientSupport(ng = false, wc = true, sc = true)
public class RelationInfo implements IScriptable
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

	/**
	 * Sets the label of a relation info object used to set multiple child relations for a tree node.
	 * 
	 * @sample
	 * var companies_binding = elements.myDbTreeView.createBinding('example_data', 'companies');
	 * var company_relations = new Array();
	 * company_relations[0] = elements.myDbTreeView.createRelationInfo();
	 * company_relations[0].setLabel('Employees');
	 * company_relations[0].setNRelationName('companies_to_employees');
	 * companies_binding.setNRelationInfos(company_relations);
	 * 
	 * @param label
	 * 
	 */
	public void js_setLabel(String label)
	{
		this.label = label;
	}

	public String getNRelationName()
	{
		return nRelationName;
	}

	/**
	 * Sets the name of a relation info object used to set multiple child relations for a tree node.
	 * 
	 * @sample
	 * var companies_binding = elements.myDbTreeView.createBinding('example_data', 'companies');
	 * var company_relations = new Array();
	 * company_relations[0] = elements.myDbTreeView.createRelationInfo();
	 * company_relations[0].setLabel('Employees');
	 * company_relations[0].setNRelationName('companies_to_employees');
	 * companies_binding.setNRelationInfos(company_relations); 
	 * 
	 * @param relationName
	 * 
	 */
	public void js_setNRelationName(String relationName)
	{
		nRelationName = relationName;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("RelationInfo { relationName : ").append(nRelationName).append(" }").toString();
	}
}