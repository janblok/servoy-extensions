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

import org.mozilla.javascript.Function;

import com.servoy.base.scripting.annotations.ServoyClientSupport;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.DataSourceUtils;

/**
 * Script object holding informations needed to display a tree node that is binded
 * to the same data source
 * 
 * @author gboros
 */
@ServoyDocumented(category = ServoyDocumented.BEANS)
@ServoyClientSupport(ng = false, wc = true, sc = true)
public class Binding implements IScriptable
{
	private String dataSource;

	private String textDataprovider;
	private String toolTipTextDataprovider;
	private String fontTypeDataprovider;
	private String imageURLDataprovider;
	private String imageMediaDataprovider;
	private String nRelationName;
	private String mRelationName;
	private String childSortDataprovider;
	private String checkBoxValueDataprovider;
	private String hasCheckBoxDataprovider;
	private String nRelationDataprovider;
	private String mRelationDataprovider;


	private FunctionDefinition callBack;
	private String returnDataprovider;
	private FunctionDefinition methodToCallOnCheckBoxChange;
	private String returnDataproviderOnCheckBoxChange;
	private FunctionDefinition methodToCallOnRightClick;
	private String returnDataproviderOnRightClick;
	private FunctionDefinition methodToCallOnDoubleClick;
	private String returnDataproviderOnDoubleClick;
	private FunctionDefinition methodToCallOnClick;
	private String returnDataproviderOnClick;

	private RelationInfo[] nRelationInfos;

	// dataprovider that return an object with dataproviders for all attributes
	private String configurationDataprovider;


	public String getConfigurationDataprovider()
	{
		return configurationDataprovider;
	}

	/**
	 * Set configuration dataprovider. Dataprovider must be MEDIA type and returns a configuration object
	 * 
	 * @sample
	 * var config = new Object();
	 * config.text = 'my_text';
	 * config.nRelation = 'my_n_relation';
	 * config.mRelation = 'my_m_relation';
	 * config.childSort = 'my_sort_field';
	 * config.fontType = 'my_font';
	 * config.toolTipText = 'my_tooltip';
	 * config.hasCheckBox = 'true';
	 * config.checkBoxValue = 'true';
	 * 
	 * @param configurationDataprovider
	 * 
	 */
	public void js_setConfigurationDataprovider(String configurationDataprovider)
	{
		this.configurationDataprovider = configurationDataprovider;
	}

	public String getHasCheckBoxDataprovider()
	{
		return hasCheckBoxDataprovider;
	}

	/**
	 * Set has checkbox flag dataprovider. Dataprovider returns INTEGER (0 / 1 / 2) or STRING (false / true / disabled) for (does not have / have / have but disabled)
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), controller.getTableName());
	 * binding.setHasCheckBoxDataprovider('hasCheckBox');
	 * 
	 * @param hasCheckBoxDataprovider
	 * 
	 */
	public void js_setHasCheckBoxDataprovider(String hasCheckBoxDataprovider)
	{
		this.hasCheckBoxDataprovider = hasCheckBoxDataprovider;
	}


	public String getCheckBoxValueDataprovider()
	{
		return checkBoxValueDataprovider;
	}

	/**
	 * Set checkbox value dataprovider. Dataprovider returns INTEGER (0 or 1) or STRING (false or true)
	 *  
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), controller.getTableName());
	 * binding.setCheckBoxValueDataprovider('comment_text');
	 * 
	 * @param checkBoxValueDataprovider
	 * 
	 */
	public void js_setCheckBoxValueDataprovider(String checkBoxValueDataprovider)
	{
		this.checkBoxValueDataprovider = checkBoxValueDataprovider;
	}

	public String getChildSortDataprovider()
	{
		return childSortDataprovider;
	}

	/**
	 * Set the dataprovider name to retrieve column name and sort order for the child nodes.\nThe provided data must be a string of form : column_name_used_for_sort sort_order(asc or desc)
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), 'customers');
	 * binding.setChildSortDataprovider('company_sort');
	 * 
	 * @param childSortDataprovider
	 * 
	 */
	public void js_setChildSortDataprovider(String childSortDataprovider)
	{
		this.childSortDataprovider = childSortDataprovider;
	}

	public String getFontTypeDataprovider()
	{
		return fontTypeDataprovider;
	}

	/**
	 * Set the dataprovider name to retrieve the node font from
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), controller.getTableName());
	 * binding.setFontTypeDataprovider('bean_font');
	 * 
	 * @param fontTypeDataprovider
	 * 
	 */
	public void js_setFontTypeDataprovider(String fontTypeDataprovider)
	{
		this.fontTypeDataprovider = fontTypeDataprovider;
	}

	public String getImageMediaDataprovider()
	{
		return imageMediaDataprovider;
	}

	/**
	 * Set the dataprovider name to retrieve the node image from (blob column)
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), 'customers');
	 * binding.setImageMediaDataprovider('company_icon');
	 * 
	 * @param imageMediaDataprovider
	 * 
	 */
	public void js_setImageMediaDataprovider(String imageMediaDataprovider)
	{
		this.imageMediaDataprovider = imageMediaDataprovider;
	}

	public String getImageURLDataprovider()
	{
		return imageURLDataprovider;
	}

	/**
	 * Set the dataprovider name to retrieve the node image from (via url)
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), 'orders');
	 * binding.setImageURLDataprovider('order_icon');
	 * 
	 * @param imageURLDataprovider
	 */
	public void js_setImageURLDataprovider(String imageURLDataprovider)
	{
		this.imageURLDataprovider = imageURLDataprovider;
	}

	public String getMRelationName()
	{
		return mRelationName;
	}

	/**
	 * Set m-relation name
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), controller.getTableName());
	 * binding.setMRelationName('m_relation_name');
	 * 
	 * @param name
	 * 
	 */
	public void js_setMRelationName(String name)
	{
		mRelationName = name;
	}

	public String getNRelationName()
	{
		return nRelationName;
	}

	/**
	 * Set n-relation name
	 * 
	 *  @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), controller.getTableName());
	 * binding.setNRelationName('n_relation_name');
	 * 
	 * @param name
	 * 
	 */
	public void js_setNRelationName(String name)
	{
		nRelationName = name;
	}

	public RelationInfo[] getNRelationInfos()
	{
		return nRelationInfos;
	}

	/**
	 * Set n-relation infos (array of RelationInfo objects created using tree.createRelationInfo() for having multiple child relations for one node)
	 * 
	 * @sample
	 * var company_relations = new Array();
	 * company_relations[0] = tree.createRelationInfo();
	 * company_relations[0].setLabel('Employees');
	 * company_relations[0].setNRelationName('companies_to_employees');
	 * company_relations[1] = tree.createRelationInfo();
	 * company_relations[1].setLabel('Customers');
	 * company_relations[1].setNRelationName('companies_to_customers');
	 * binding.setNRelationInfos(company_relations);
	 * 
	 * @param relationInfos
	 * 
	 */
	public void js_setNRelationInfos(RelationInfo[] relationInfos)
	{
		nRelationInfos = relationInfos;
	}

	public String getReturnDataprovider()
	{
		return returnDataprovider;
	}

	public String getReturnDataproviderOnRightClick()
	{
		return returnDataproviderOnRightClick;
	}

	public String getReturnDataproviderOnDoubleClick()
	{
		return returnDataproviderOnDoubleClick;
	}

	public String getReturnDataproviderOnClick()
	{
		return returnDataproviderOnClick;
	}

	public String getReturnDataproviderOnCheckBoxChange()
	{
		return returnDataproviderOnCheckBoxChange;
	}

	public String getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(String ds)
	{
		dataSource = ds;
	}

	public String getServerName()
	{
		String[] server_table = DataSourceUtils.getDBServernameTablename(dataSource);
		return (server_table == null ? null : server_table[0]);
	}

	public void setServerName(String serverName)
	{
		dataSource = DataSourceUtils.createDBTableDataSource(serverName, getTableName());
	}

	public String getTableName()
	{
		String[] server_table = DataSourceUtils.getDBServernameTablename(dataSource);
		return (server_table == null ? null : server_table[1]);
	}

	public void setTableName(String tableName)
	{
		dataSource = DataSourceUtils.createDBTableDataSource(getServerName(), tableName);
	}

	public String getTextDataprovider()
	{
		return textDataprovider;
	}

	/**
	 * Set text dataprovider
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), 'orders');
	 * binding.setTextDataprovider('orderid');
	 * 
	 * @param textDataprovider
	 * 
	 */
	public void js_setTextDataprovider(String textDataprovider)
	{
		this.textDataprovider = textDataprovider;
	}

	public String getToolTipTextDataprovider()
	{
		return toolTipTextDataprovider;
	}

	/**
	 * Set the dataprovider name to retrieve the node tooltiptext from
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), 'customers');
	 * binding.setToolTipTextDataprovider('companyname');
	 * 
	 * @param toolTipTextDataprovider
	 * 
	 */
	public void js_setToolTipTextDataprovider(String toolTipTextDataprovider)
	{
		this.toolTipTextDataprovider = toolTipTextDataprovider;
	}

	public FunctionDefinition getMethodToCallOnRightClick()
	{
		return methodToCallOnRightClick;
	}

	/**
	 * Set method to call on right click.\nThe callback will be called with the following arguments : returnDataprovider, tableName, mouseX, mouseY
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), 'orders');
	 * binding.setMethodToCallOnRightClick(rightClickMethod,'orderid');
	 * 
	 * @param methodToCallOnRightClick
	 * @param returnDataproviderOnRightClick
	 * 
	 */
	public void js_setMethodToCallOnRightClick(Function methodToCallOnRightClick, String returnDataproviderOnRightClick)
	{
		if (methodToCallOnRightClick != null)
		{
			this.methodToCallOnRightClick = new FunctionDefinition(methodToCallOnRightClick);
			this.returnDataproviderOnRightClick = returnDataproviderOnRightClick;
		}
	}

	public FunctionDefinition getMethodToCallOnDoubleClick()
	{
		return methodToCallOnDoubleClick;
	}

	/**
	 * Set method to call on double click.\nThe callback will be called with the following arguments : returnDataprovider, tableName, mouseX, mouseY
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), 'orders');
	 * binding.setMethodToCallOnDoubleClick(doubleClickMethod,'orderid');
	 * 
	 * @param methodToCallOnDoubleClick
	 * @param returnDataproviderOnDoubleClick
	 * 
	 */
	public void js_setMethodToCallOnDoubleClick(Function methodToCallOnDoubleClick, String returnDataproviderOnDoubleClick)
	{
		if (methodToCallOnDoubleClick != null)
		{
			this.methodToCallOnDoubleClick = new FunctionDefinition(methodToCallOnDoubleClick);
			this.returnDataproviderOnDoubleClick = returnDataproviderOnDoubleClick;
		}
	}

	public FunctionDefinition getMethodToCallOnClick()
	{
		return methodToCallOnClick;
	}

	/**
	 * Set method to call on click.\nThe callback will be called with the following arguments : returnDataprovider, tableName, mouseX, mouseY
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), 'orders');
	 * binding.setMethodToCallOnClick(onClickMethod,'orderid');
	 * 
	 * @param methodToCallOnClick
	 * @param returnDataproviderOnClick
	 * 
	 */
	public void js_setMethodToCallOnClick(Function methodToCallOnClick, String returnDataproviderOnClick)
	{
		if (methodToCallOnClick != null)
		{
			this.methodToCallOnClick = new FunctionDefinition(methodToCallOnClick);
			this.returnDataproviderOnClick = returnDataproviderOnClick;
		}
	}

	public FunctionDefinition getMethodToCallOnCheckBoxChange()
	{
		return methodToCallOnCheckBoxChange;
	}

	/**
	 * Set method to call on check box status change
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), 'orders');
	 * binding.setMethodToCallOnCheckBoxChange(onCheckBoxChangeMethod,'orderdate');
	 * 
	 * @param methodToCallOnCheckBoxChange
	 * @param returnDataproviderOnCheckBoxChange
	 * 
	 */
	public void js_setMethodToCallOnCheckBoxChange(Function methodToCallOnCheckBoxChange, String returnDataproviderOnCheckBoxChange)
	{
		if (methodToCallOnCheckBoxChange != null)
		{
			this.methodToCallOnCheckBoxChange = new FunctionDefinition(methodToCallOnCheckBoxChange);
			this.returnDataproviderOnCheckBoxChange = returnDataproviderOnCheckBoxChange;
		}
	}

	public FunctionDefinition getCallBack()
	{
		return callBack;
	}

	/**
	 * Set callback method for node selection and double click
	 * 
	 * @sample 
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), controller.getTableName());
	 * binding.setCallBackInfo(globals.node_selected, 'node_id');
	 * 
	 * @param f
	 * @param returnDataprovider
	 * 
	 */
	public void js_setCallBackInfo(Function f, String returnDataprovider)
	{
		if (f != null)
		{
			this.callBack = new FunctionDefinition(f);
			this.returnDataprovider = returnDataprovider;
		}
	}


	public String getMRelationDataprovider()
	{
		return mRelationDataprovider;
	}

	/**
	 * Set m-relation dataprovider. Dataprovider returns the name of the m-relation
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), controller.getTableName());
	 * binding.setMRelationDataprovider('m_relation');
	 * 
	 * @param dataprovider
	 * 
	 */
	public void js_setMRelationDataprovider(String dataprovider)
	{
		mRelationDataprovider = dataprovider;
	}

	public String getNRelationDataprovider()
	{
		return nRelationDataprovider;
	}

	/**
	 * Set n-relation dataprovider. Dataprovider returns the name of the n-relation
	 * 
	 * @sample
	 * var binding = elements.dbtreeview.createBinding(controller.getServerName(), controller.getTableName());
	 * binding.setNRelationDataprovider('n_relation');
	 * 
	 * @param dataprovider
	 * 
	 */
	public void js_setNRelationDataprovider(String dataprovider)
	{
		nRelationDataprovider = dataprovider;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("Binding { dataSource : ").append(dataSource).append(" }").toString();
	}
}
