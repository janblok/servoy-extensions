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

import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.DataSourceUtils;

/**
 * Script object holding informations needed to display a tree node that is binded
 * to the same data source
 * 
 * @author gboros
 */
public class Binding implements IScriptObject
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


	private Object[] nRelationInfos;

	// dataprovider that return an object with dataproviders for all attributes
	private String configurationDataprovider;


	public String getConfigurationDataprovider()
	{
		return configurationDataprovider;
	}

	public void setConfigurationDataprovider(String configurationDataprovider)
	{
		this.configurationDataprovider = configurationDataprovider;
	}

	public void js_setConfigurationDataprovider(String configurationDataprovider)
	{
	}

	public String getHasCheckBoxDataprovider()
	{
		return hasCheckBoxDataprovider;
	}

	public void setHasCheckBoxDataprovider(String hasCheckBoxDataprovider)
	{
		this.hasCheckBoxDataprovider = hasCheckBoxDataprovider;
	}

	public void js_setHasCheckBoxDataprovider(String hasCheckBoxDataprovider)
	{
	}

	public String getCheckBoxValueDataprovider()
	{
		return checkBoxValueDataprovider;
	}

	public void setCheckBoxValueDataprovider(String checkBoxValueDataprovider)
	{
		this.checkBoxValueDataprovider = checkBoxValueDataprovider;
	}

	public void js_setCheckBoxValueDataprovider(String checkBoxValueDataprovider)
	{
	}

	public String getChildSortDataprovider()
	{
		return childSortDataprovider;
	}

	public void setChildSortDataprovider(String childSortDataprovider)
	{
		this.childSortDataprovider = childSortDataprovider;
	}

	public void js_setChildSortDataprovider(String childSortDataprovider)
	{
	}

	public String getFontTypeDataprovider()
	{
		return fontTypeDataprovider;
	}

	public void setFontTypeDataprovider(String fontTypeDataprovider)
	{
		this.fontTypeDataprovider = fontTypeDataprovider;
	}

	public void js_setFontTypeDataprovider(String fontTypeDataprovider)
	{
	}

	public String getImageMediaDataprovider()
	{
		return imageMediaDataprovider;
	}

	public void setImageMediaDataprovider(String imageMediaDataprovider)
	{
		this.imageMediaDataprovider = imageMediaDataprovider;
	}

	public void js_setImageMediaDataprovider(String imageMediaDataprovider)
	{
	}

	public String getImageURLDataprovider()
	{
		return imageURLDataprovider;
	}

	public void setImageURLDataprovider(String imageURLDataprovider)
	{
		this.imageURLDataprovider = imageURLDataprovider;
	}

	public void js_setImageURLDataprovider(String imageURLDataprovider)
	{
	}

	public String getMRelationName()
	{
		return mRelationName;
	}

	public void setMRelationName(String name)
	{
		mRelationName = name;
	}

	public void js_setMRelationName(String name)
	{
	}

	public String getNRelationName()
	{
		return nRelationName;
	}

	public void setNRelationName(String name)
	{
		nRelationName = name;
	}

	public void js_setNRelationName(String name)
	{
	}

	public Object[] getNRelationInfos()
	{
		return nRelationInfos;
	}

	public void setNRelationInfos(Object[] relationInfos)
	{
		nRelationInfos = relationInfos;
	}

	public void js_setNRelationInfos(Object[] relationInfos)
	{
	}

	public String getReturnDataprovider()
	{
		return returnDataprovider;
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

	public void setTextDataprovider(String textDataprovider)
	{
		this.textDataprovider = textDataprovider;
	}

	public void js_setTextDataprovider(String textDataprovider)
	{
	}

	public String getToolTipTextDataprovider()
	{
		return toolTipTextDataprovider;
	}

	public void setToolTipTextDataprovider(String toolTipTextDataprovider)
	{
		this.toolTipTextDataprovider = toolTipTextDataprovider;
	}

	public void js_setToolTipTextDataprovider(String toolTipTextDataprovider)
	{
	}

	public FunctionDefinition getMethodToCallOnCheckBoxChange()
	{
		return methodToCallOnCheckBoxChange;
	}

	public void setMethodToCallOnCheckBoxChange(Function methodToCallOnCheckBoxChange, String returnDataproviderOnCheckBoxChange)
	{
		if (methodToCallOnCheckBoxChange != null)
		{
			this.methodToCallOnCheckBoxChange = new FunctionDefinition(methodToCallOnCheckBoxChange);
			this.returnDataproviderOnCheckBoxChange = returnDataproviderOnCheckBoxChange;
		}
	}

	public void js_setMethodToCallOnCheckBoxChange(Function methodToCallOnCheckBoxChange, String returnDataproviderOnCheckBoxChange)
	{
	}

	public FunctionDefinition getCallBack()
	{
		return callBack;
	}

	/**
	 * for compatibility
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void setMethodToCallOnClick(Function methodToCallOnClick, String returnDataprovider)
	{
		setCallBackInfo(methodToCallOnClick, returnDataprovider);
	}

	/**
	 * for compatibility
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void js_setMethodToCallOnClick(Function methodToCallOnClick, String returnDataprovider)
	{
	}

	public void setCallBackInfo(Function f, String returnDataprovider)
	{
		if (f != null)
		{
			this.callBack = new FunctionDefinition(f);
			this.returnDataprovider = returnDataprovider;
		}
	}

	public void js_setCallBackInfo(Function f, String returnDataprovider)
	{

	}


	public String getMRelationDataprovider()
	{
		return mRelationDataprovider;
	}

	public void setMRelationDataprovider(String dataprovider)
	{
		mRelationDataprovider = dataprovider;
	}

	public void js_setMRelationDataprovider(String dataprovider)
	{
	}

	public String getNRelationDataprovider()
	{
		return nRelationDataprovider;
	}

	public void setNRelationDataprovider(String dataprovider)
	{
		nRelationDataprovider = dataprovider;
	}

	public void js_setNRelationDataprovider(String dataprovider)
	{
	}

	public Class[] getAllReturnedTypes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getParameterNames(String methodName)
	{
		if (methodName.endsWith("provider"))
		{
			return new String[] { "dataprovider" };
		}
		else if (methodName.startsWith("setMethod"))
		{
			return new String[] { "function", "returnDataprovider" };
		}
		else if (methodName.endsWith("elationName"))
		{
			return new String[] { "relation" };
		}

		return null;
	}

	public String getSample(String methodName)
	{
		if (methodName.endsWith("NRelationInfos"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("var company_relations = new Array();\n");
			retval.append("company_relations[0] = tree.createRelationInfo();\n");
			retval.append("company_relations[0].setLabel('Employees');\n");
			retval.append("company_relations[0].setNRelationName('companies_to_employees');\n");
			retval.append("company_relations[1] = tree.createRelationInfo();\n");
			retval.append("company_relations[1].setLabel('Customers');\n");
			retval.append("company_relations[1].setNRelationName('companies_to_customers');\n");
			retval.append("binding.setNRelationInfos(company_relations);\n");

			return retval.toString();
		}
		else if (methodName.endsWith("provider"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("binding.");
			retval.append(methodName);
			retval.append("('dataprovider');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.startsWith("setMethod") || methodName.startsWith("setCallBackInfo"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("binding.");
			retval.append(methodName);
			retval.append("(callbackFunction, 'returnDataprovider');\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("elationName"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("binding.");
			retval.append(methodName);
			retval.append("('relation');\n"); //$NON-NLS-1$
			return retval.toString();
		}

		return null;
	}

	public String getToolTip(String methodName)
	{
		if (methodName.endsWith("CheckBoxValueDataprovider"))
		{
			return "Set checkbox value dataprovider. Dataprovider returns INTEGER (0 or 1) or STRING (false or true)";
		}
		else if (methodName.endsWith("ChildSortDataprovider"))
		{
			return "Set the dataprovider name to retrieve column name and sort order for the child nodes.\nThe provided data must be a string of form : column_name_used_for_sort sort_order(asc or desc)";
		}
		else if (methodName.endsWith("FontTypeDataprovider"))
		{
			return "Set the dataprovider name to retrieve the node font from";
		}
		else if (methodName.endsWith("HasCheckBoxDataprovider"))
		{
			return "Set has checkbox flag dataprovider. Dataprovider returns INTEGER (0 / 1 / 2) or STRING (false / true / disabled) for (does not have / have / have but disabled)";
		}
		else if (methodName.endsWith("ImageMediaDataprovider"))
		{
			return "Set the dataprovider name to retrieve the node image from (blob column)";
		}
		else if (methodName.endsWith("ImageURLDataprovider"))
		{
			return "Set the dataprovider name to retrieve the node image from (via url)";
		}
		else if (methodName.endsWith("MRelationDataprovider"))
		{
			return "Set m-relation dataprovider. Dataprovider returns the name of the m-relation";
		}
		else if (methodName.endsWith("NRelationDataprovider"))
		{
			return "Set n-relation dataprovider. Dataprovider returns the name of the n-relation";
		}
		else if (methodName.endsWith("TextDataprovider"))
		{
			return "Set text dataprovider";
		}
		else if (methodName.endsWith("ToolTipTextDataprovider"))
		{
			return "Set the dataprovider name to retrieve the node tooltiptext from";
		}
		else if (methodName.endsWith("MRelationName"))
		{
			return "Set m-relation name";
		}
		else if (methodName.endsWith("NRelationName"))
		{
			return "Set n-relation name";
		}
		else if (methodName.endsWith("MethodToCallOnCheckBoxChange"))
		{
			return "Set method to call on check box status change";
		}
		else if (methodName.endsWith("CallBackInfo"))
		{
			return "Set callback method for node selection and double click";
		}
		else if (methodName.endsWith("ConfigurationDataprovider"))
		{
			StringBuffer example = new StringBuffer();
			example.append("Ex.:\n");
			example.append("var config = new Object();\n");
			example.append("config.text = 'my_text';\n");
			example.append("config.nRelation = 'my_n_relation';\n");
			example.append("config.mRelation = 'my_m_relation';\n");
			example.append("config.childSort = 'my_sort_field';\n");
			example.append("config.fontType = 'my_font';\n");
			example.append("config.toolTipText = 'my_tooltip';\n");
			example.append("config.hasCheckBox = 'true';\n");
			example.append("config.checkBoxValue = 'true';\n");

			return "Set configuration dataprovider. Dataprovider must be MEDIA type and returns a configuration object\n" + example.toString();
		}
		else if (methodName.endsWith("NRelationInfos"))
		{
			return "Set n-relation infos (array of RelationInfo objects created using tree.createRelationInfo() for having multiple child relations for one node)";
		}


		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
