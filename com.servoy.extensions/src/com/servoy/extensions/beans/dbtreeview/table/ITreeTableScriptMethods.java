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
package com.servoy.extensions.beans.dbtreeview.table;

import com.servoy.extensions.beans.dbtreeview.ITreeViewScriptMethods;
import com.servoy.j2db.documentation.ServoyDocumented;

/**
 * Interface to which both the smart and webclient tree table need to conform
 * 
 * @author gboros
 */
@ServoyDocumented(category = ServoyDocumented.RUNTIME, publicName = "DBTreeTableView", extendsComponent = "DBTreeView")
public interface ITreeTableScriptMethods extends ITreeViewScriptMethods
{
	/**
	 * Set the header text for the tree column
	 * 
	 * @sample
	 * %%elementName%%.setTreeColumnHeader('Tree Column Header');
	 * 
	 * @param treeColumnHeader
	 */
	public void js_setTreeColumnHeader(String treeColumnHeader);

	/**
	 * Set the preferred width in pixels for the tree column
	 * 
	 * @sample
	 * %%elementName%%.setTreeColumnPreferredWidth(200);
	 * 
	 * @param preferredWidth
	 */
	public void js_setTreeColumnPreferredWidth(int preferredWidth);

	/**
	 * @sameas js_createColumn(String, String, String, String, int)
	 */
	public Column js_createColumn(String servername, String tablename, String header, String fieldname);

	/**
	 * Create and add new column to the tree table
	 * 
	 * @sample
	 * %%elementName%%.createColumn('servername', 'tablename', 'header text', 'tablefieldname', 150);
	 * 
	 * @param servername
	 * @param tablename
	 * @param header
	 * @param fieldname
	 * @param preferredWidth (optional)
	 *   
	 * @return Column object
	 */
	public Column js_createColumn(String servername, String tablename, String header, String fieldname, int preferredWidth);

	/**
	 * Remove all columns but the tree column from the tree table
	 * 
	 * @sample
	 * %%elementName%%.removeAllColumns();
	 * 
	 */
	public void js_removeAllColumns();
}
