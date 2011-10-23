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
package com.servoy.extensions.plugins.tabxport;

import javax.swing.JMenuItem;

import com.servoy.j2db.dataprocessing.IFoundSet;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Utils;

/**
 * Xport menu enabler
 * @author jblok
 */
@ServoyDocumented(publicName = "textxport")
public class Enabler implements IScriptable
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

	public void js_setExportEnabled(boolean b)
	{
		if (exp != null) exp.setEnabled(b);
	}

	/**
	 * Enable the export feature of this plugin.
	 *
	 * @sample
	 * plugins.textxport.exportEnabled = true;
	 * var isEnabled = plugins.textxport.exportEnabled;
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
	 * plugins.textxport.importEnabled = true;
	 * var isEnabled = plugins.textxport.importEnabled;
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
	 * Export to text 'separated value' data (*.tab/*.csv)
	 *
	 * @sample
	 * //export with default separator(tab) and no header
	 * var dataToBeWritten = plugins.textxport.textExport(forms.form1.foundset,['id','name']);
	 * //export with ';' separator and no header
	 * var dataToBeWritten = plugins.textxport.textExport(forms.form1.foundset,['id','name'],';');
	 * //export with ';' separator and header
	 * var dataToBeWritten = plugins.textxport.textExport(forms.form1.foundset,['id','name'],';',true);
	 *
	 * @param foundSet 
	 * @param dataProviderIds 
	 * @param separator optional
	 * @param exportHeader optional
	 */
	public String js_textExport(Object[] args)
	{
		if ((args.length >= 2) && (args[0] != null) && (args[1] instanceof Object[]) && ((Object[])args[1]).length != 0)
		{
			IFoundSet foundSet = ((IFoundSet)args[0]);
			Object[] data = (Object[])args[1];
			String[] dataProviders = new String[data.length];
			for (int i = 0; i < data.length; i++)
				dataProviders[i] = (String)data[i];
			String sep = "\t";
			boolean exportHeader = false;
			if (args.length > 2)
			{
				sep = (String)args[2];
				if (args.length > 3)
				{
					exportHeader = Utils.getAsBoolean(args[3]);
				}
			}
			StringBuffer fileData = new StringBuffer();
			if (exportHeader) fileData.insert(0, ExportSpecifyFilePanel.createHeader(dataProviders, sep));
			fileData.append(ExportSpecifyFilePanel.populateFileData(foundSet, dataProviders, sep));

			return fileData.toString();
		}
		return null;
	}
}
