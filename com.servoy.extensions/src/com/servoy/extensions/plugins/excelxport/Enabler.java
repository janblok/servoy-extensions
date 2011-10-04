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
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;

/**
 * Xport menu enabler
 * @author jblok
 */
@ServoyDocumented
public class Enabler implements IScriptable, IReturnedTypesProvider
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

	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}

	public void js_setExportEnabled(boolean b)
	{
		if (exp != null) exp.setEnabled(b);
	}

	/**
	 * Enable the export feature of this plugin.
	 *
	 * @sample
	 * plugins.excelxport.exportEnabled = true;
	 * var isEnabled = plugins.excelxport.exportEnabled;
	 */
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

	public void js_setImportEnabled(boolean b)
	{
		if (imp != null)
		{
			imp.setEnabled(b);
		}
	}

	/**
	 * Enable the import feature of this plugin.
	 *
	 * @sample
	 * plugins.excelxport.importEnabled = true;
	 * var isEnabled = plugins.excelxport.importEnabled;
	 */
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

	/**
	 * Export to Excel data
	 *
	 * @sample
	 * //export in new byte array
	 * var bytes = plugins.excelxport.excelExport(forms.form1.foundset, ['id','name']);
	 * //export by adding to templateXLS in default (new) 'Servoy Data' worksheet
	 * var bytes = plugins.excelxport.excelExport(forms.form1.foundset, ['id','name'],templateXLS);
	 * //export by adding to templateXLS, in 'mySheet' worksheet, starting at default(1/1) row/column
	 * var bytes = plugins.excelxport.excelExport(forms.form1.foundset, ['id','name'],templateXLS, 'mySheet');
	 * //export by adding to templateXLS, in 'mySheet' worksheet, starting at 3rd row and 5th column
	 * var bytes = plugins.excelxport.excelExport(forms.form1.foundset, ['id','name'],templateXLS, 'mySheet',3,5);
	 *
	 * @param foundSet 
	 * @param dataProviderIds 
	 * @param templateXLS optional
	 * @param sheetName optional
	 * @param startRow optional
	 * @param startColumn optional
	 */
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
