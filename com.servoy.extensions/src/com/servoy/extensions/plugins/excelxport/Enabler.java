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
package com.servoy.extensions.plugins.excelxport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.swing.JMenuItem;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.servoy.j2db.dataprocessing.IFoundSet;
import com.servoy.j2db.scripting.IScriptObject;

/**
 * Xport menu enabler
 * @author jblok
 */
public class Enabler implements IScriptObject
{
	private JMenuItem imp;
	private JMenuItem exp;

	Enabler()
	{
		//only for use in eclipse
	}

	Enabler(JMenuItem imp, JMenuItem exp)
	{
		this.imp = imp;
		this.exp = exp;
	}

	public String getSample(String methodName)
	{
		if (methodName == null) return null;
		if (methodName.endsWith("xportEnabled")) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("%%elementName%%.exportEnabled = true;\n"); //$NON-NLS-1$
			retval.append("var isEnabled = %%elementName%%.exportEnabled;\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("mportEnabled")) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("%%elementName%%.importEnabled = true;\n"); //$NON-NLS-1$
			retval.append("var isEnabled = %%elementName%%.importEnabled;\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (methodName.endsWith("excelExport"))
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//");
			retval.append(getToolTip(methodName));
			retval.append("\n");
			retval.append("//export in new byte array\n");
			retval.append("var bytes = %%elementName%%.excelExport(forms.form1.foundset, ['id','name']);\n");
			retval.append("//export by adding to templateXLS in default (new) 'Servoy Data' worksheet\n");
			retval.append("var bytes = %%elementName%%.excelExport(forms.form1.foundset, ['id','name'],templateXLS);\n");
			retval.append("//export by adding to templateXLS, in 'mySheet' worksheet, starting at default(1/1) row/column\n");
			retval.append("var bytes = %%elementName%%.excelExport(forms.form1.foundset, ['id','name'],templateXLS, 'mySheet');\n");
			retval.append("//export by adding to templateXLS, in 'mySheet' worksheet, starting at 3rd row and 5th column\n");
			retval.append("var bytes = %%elementName%%.excelExport(forms.form1.foundset, ['id','name'],templateXLS, 'mySheet',3,5);\n");
			return retval.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if (methodName == null) return null;
		if (methodName.endsWith("xportEnabled")) //$NON-NLS-1$
		{
			return "Enable the export feature of this plugin."; //$NON-NLS-1$
		}
		else if (methodName.endsWith("mportEnabled")) //$NON-NLS-1$
		{
			return "Enable the import feature of this plugin."; //$NON-NLS-1$
		}
		else if (methodName.endsWith("excelExport"))
		{
			return "Export to Excel data";
		}
		return null;
	}

	public String[] getParameterNames(String methodName)
	{
		if (methodName.endsWith("excelExport")) return new String[] { "foundSet", "dataProviderIds", "[templateXLS]", "[sheetName]", "[startRow]", "[startColumn]" };
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	public Class[] getAllReturnedTypes()
	{
		return null;
	}

	/**
	 * Enable the export feature of this plugin
	 *
	 * @sample
	 * %%elementName%%.exportEnabled = true;
	 * var isEnabled = %%elementName%%.exportEnabled;
	 */
	public void js_setExportEnabled(boolean b)
	{
		if (exp != null) exp.setEnabled(b);
	}

	public boolean js_getExportEnabled()
	{
		if (exp != null)
		{
			return exp.isEnabled();
		}
		else
		{
			return false;
		}
	}

	/**
	 * Enable the import feature of this plugin
	 *
	 * @sample
	 * %%elementName%%.importEnabled = true;
	 * var isEnabled = %%elementName%%.importEnabled;
	 */
	public void js_setImportEnabled(boolean b)
	{
		if (imp != null)
		{
			imp.setEnabled(b);
		}
	}

	public boolean js_getImportEnabled()
	{
		if (imp != null)
		{
			return imp.isEnabled();
		}
		else
		{
			return false;
		}
	}

	public void setEnabled(boolean b)
	{
		if (exp != null)
		{
			exp.setEnabled(b);
		}
		if (imp != null)
		{
			imp.setEnabled(b);
		}
	}

	public byte[] js_excelExport(Object[] args) throws IOException
	{
		if ((args != null) && (args.length >= 2) && (args[0] instanceof IFoundSet) && ((Object[])args[1]).length != 0)
		{
			IFoundSet foundSet = (IFoundSet)args[0];
			Object[] dataProvidersArray = (Object[])args[1];
			String[] dps = new String[dataProvidersArray.length];
			for (int i = 0; i < dataProvidersArray.length; i++)
				dps[i] = (String)dataProvidersArray[i];
			byte[] templateXLS = null;
			String sheetName = "Servoy Data";
			int startRow = 0;
			int startColumn = 0;
			if (args.length > 2 && args[2] instanceof byte[])
			{
				templateXLS = (byte[])args[2];
			}
			if (args.length > 3 && args[3] instanceof String)
			{
				sheetName = (String)args[3];
			}
			if (args.length > 4 && args[4] instanceof Double)
			{
				double i = (Double)args[4];
				startRow = (int)(i - 1);
			}
			if (args.length > 5 && args[5] instanceof Double)
			{
				double i = (Double)args[5];
				startColumn = (int)(i - 1);
			}

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			HSSFWorkbook wb = ExportSpecifyFilePanel.populateWb(foundSet, dps, templateXLS, sheetName, startRow, startColumn);
			wb.write(buffer);
			return buffer.toByteArray();
		}
		return null;
	}

}
