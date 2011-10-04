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
package com.servoy.extensions.plugins.rawSQL;

import com.servoy.j2db.dataprocessing.IDataSet;
import com.servoy.j2db.dataprocessing.JSDataSet;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.ServoyException;
import com.servoy.j2db.util.UUID;

/**
 * Rawsql plugin scriptable.
 * 
 * @author jblok
 */


public class RawSQLProvider implements IScriptObject
{
	private final RawSQLPlugin plugin;
	private ISQLService sqlService;
	private Exception exception;

	RawSQLProvider(RawSQLPlugin plugin)
	{
		this.plugin = plugin;
	}

	private ISQLService getSQLService() throws Exception
	{
		exception = null;
		if (sqlService == null)
		{
			sqlService = (ISQLService)plugin.getClientPluginAccess().getServerService("servoy.ISQLService"); //$NON-NLS-1$
		}
		return sqlService;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public String js_getExceptionMsg()
	{
		return (exception == null ? null : exception.toString() + ' ' + exception.getMessage());
	}

	public Exception js_getException()
	{
		return exception;
	}

	public boolean js_executeSQL(Object[] args)
	{
		if (args.length < 3)
		{
			return false;
		}

		String serverName = "" + args[0]; //$NON-NLS-1$
		String table = "" + args[1]; //$NON-NLS-1$
		String sql = "" + args[2]; //$NON-NLS-1$
		Object[] sql_args = null;
		if (args.length >= 4)
		{
			sql_args = (Object[])args[3];
		}

		try
		{
			String tid = plugin.getClientPluginAccess().getDatabaseManager().getTransactionID(serverName);
			return getSQLService().executeSQL(plugin.getClientPluginAccess().getClientID(), serverName, table, sql, sql_args, tid);
		}
		catch (Exception ex)
		{
			exception = ex;
			Debug.error(ex);
			return false;
		}
	}

	public JSDataSet js_executeStoredProcedure(String serverName, String procedureDeclaration, Object[] jsargs, int[] jsinOutType, int maxNumberOfRowsToRetrieve)
	{
		Object[] args;
		int[] inOutType;
		if (jsargs == null || jsinOutType == null)
		{
			args = new Object[0];
			inOutType = new int[0];
		}
		else if (jsargs.length != jsinOutType.length)
		{
			throw new RuntimeException("In/Out Arguments should be same size as directionality array"); //$NON-NLS-1$
		}
		else
		{
			args = jsargs;
			inOutType = jsinOutType;
		}

		try
		{
			String tid = plugin.getClientPluginAccess().getDatabaseManager().getTransactionID(serverName);
			String cid = plugin.getClientPluginAccess().getClientID();
			// TODO HOW TO HANDLE ARGS WITH NULL?? sHOULD BE CONVERTED TO NullValue?????
			IDataSet set = getSQLService().executeStoredProcedure(cid, serverName, tid, procedureDeclaration, args, inOutType, 0, maxNumberOfRowsToRetrieve);
			return new JSDataSet(set);
		}
		catch (ServoyException ex)
		{
			exception = ex;
			Debug.error(ex);
			return new JSDataSet(ex);
		}
		catch (Exception e)
		{
			exception = e;
			Debug.error(e);
			return null;
		}
	}

	@Deprecated
	public byte[] js_convertUUIDToBytes(String uuid)
	{
		return UUID.fromString(uuid).toBytes();
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public String js_convertUUIDToString(byte[] data)
	{
		return new UUID(data).toString();
	}

	public boolean js_flushAllClientsCache(String serverName, String tableName)
	{
		try
		{
			return getSQLService().flushAllClientsCache(plugin.getClientPluginAccess().getClientID(), true, serverName, tableName,
				plugin.getClientPluginAccess().getDatabaseManager().getTransactionID(serverName));
		}
		catch (Exception ex)
		{
			exception = ex;
			Debug.error(ex);
			return false;
		}
	}

	public boolean js_notifyDataChange(String serverName, String tableName, IDataSet pks, int action)
	{
		if (pks == null || pks.getRowCount() == 0) return false; //make sure developer does not call this without knowing this would be the same as flushAllClientsCache function

		try
		{
			return getSQLService().notifyDataChange(plugin.getClientPluginAccess().getClientID(), true, serverName, tableName, pks, action,
				plugin.getClientPluginAccess().getDatabaseManager().getTransactionID(serverName));
		}
		catch (Exception ex)
		{
			exception = ex;
			Debug.error(ex);
			return false;
		}
	}

	public String[] getParameterNames(String methodName)
	{
		if ("executeSQL".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "serverName", "tableName", "SQL", "[arguments]" }; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		if ("executeStoredProcedure".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "serverName", "procedureDeclaration", "arguments[]", "IODirectionality[]", "maxNrReturnedRows" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		if ("flushAllClientsCache".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "serverName", "tableName" }; //$NON-NLS-1$  //$NON-NLS-2$
		}
		if ("notifyDataChange".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "serverName", "tableName", "pksDataset", "action" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		if ("getExceptionMsg".equals(methodName)) //$NON-NLS-1$
		{
			return true;
		}
		if (methodName.startsWith("convertUUIDTo")) //$NON-NLS-1$
		{
			return true;
		}
		return false;
	}

	public String getSample(String methodName)
	{
		StringBuilder retval = new StringBuilder();
		retval.append("/****************************************************************************\n"); //$NON-NLS-1$
		retval.append("WARNING! You can cause data loss or serious data integrity compromises!\n"); //$NON-NLS-1$
		retval.append("You should have a THOROUGH understanding of both SQL and your backend\n"); //$NON-NLS-1$
		retval.append("database (and other interfaces that may use that backend) BEFORE YOU USE\n"); //$NON-NLS-1$
		retval.append("ANY OF THESE COMMANDS.\n"); //$NON-NLS-1$
		retval.append("You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS\n"); //$NON-NLS-1$
		retval.append("\nNote that when server names have been switched (databasemanager.switchServer),the \n"); //$NON-NLS-1$
		retval.append("real server names must be used here, plugins.rawSQL is not transparent to switched servers.\n"); //$NON-NLS-1$
		retval.append("****************************************************************************/\n"); //$NON-NLS-1$

		retval.append("\n// ").append(getToolTip(methodName)).append('\n'); //$NON-NLS-1$

		if ("executeSQL".equals(methodName) || "flushAllClientsCache".equals(methodName) || "getException".equals(methodName)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			retval.append("var country = 'NL'\n"); //$NON-NLS-1$
			retval.append("var done = plugins.rawSQL.executeSQL(\"example_data\",\"employees\",\"update employees set country = ?\", [country])\n"); //$NON-NLS-1$
			retval.append("if (done)\n{\n\t//flush is required when changes are made in db\n"); //$NON-NLS-1$
			retval.append("\tplugins.rawSQL.flushAllClientsCache(\"example_data\",\"employees\")\n"); //$NON-NLS-1$
			retval.append("}\nelse\n{\n"); //$NON-NLS-1$
			retval.append("\tvar msg = plugins.rawSQL.getException().getMessage(); //see exception node for more info about the exception obj\n"); //$NON-NLS-1$
			retval.append("\tplugins.dialogs.showErrorDialog('Error',  'SQL exception: '+msg,  'Ok')\n"); //$NON-NLS-1$
			retval.append("}\n"); //$NON-NLS-1$
			retval.append("\n// Note that when this function is used to create a new table in the database, this table will only be seen by\n"); //$NON-NLS-1$
			retval.append("// the Servoy Application Server when the table name starts with 'temp_', otherwise a server restart is needed.\n"); //$NON-NLS-1$
		}
		else if ("executeStoredProcedure".equals(methodName)) //$NON-NLS-1$
		{
			retval.append("var maxReturnedRows = 10; //useful to limit number of rows\n"); //$NON-NLS-1$
			retval.append("var procedure_declaration = '{?=calculate_interest_rate(?)}'\n"); //$NON-NLS-1$
			retval.append("// define the direction, a 0 for input data, a 1 for output data\n"); //$NON-NLS-1$
			retval.append("var typesArray = [1, 0];\n"); //$NON-NLS-1$
			retval.append("// define the types and values, a value for input data, a sql-type for output data\n"); //$NON-NLS-1$
			retval.append("var args = [java.sql.Types.NUMERIC, 3000]\n"); //$NON-NLS-1$
			retval.append("// A dataset is returned, when no output-parameters defined, the last select-result in the procedure will be returned.\n"); //$NON-NLS-1$
			retval.append("// When one or more output-parameters are defined, the dataset will contain 1 row with the output data.\n"); //$NON-NLS-1$
			retval.append("var dataset = plugins.rawSQL.executeStoredProcedure(controller.getServerName(), procedure_declaration, args, typesArray, maxReturnedRows);\n"); //$NON-NLS-1$
			retval.append("var interest_rate = dataset.getValue(1, 1);\n"); //$NON-NLS-1$
		}
		else if ("convertUUIDToBytes".equals(methodName)) //$NON-NLS-1$
		{
			retval.append("var uuid = application.getNewUUID();\n"); //$NON-NLS-1$
			retval.append("plugins.rawSQL.executeSQL(controller.getServerName(), 'employees', 'insert into employees (employees_id, creation_date) values (?, ?)', [plugins.rawSQL.convertUUIDToBytes(uuid), new Date()]);\n"); //$NON-NLS-1$
		}
		else if ("notifyDataChange".equals(methodName)) //$NON-NLS-1$
		{
			retval.append("var action = SQL_ACTION_TYPES.DELETE_ACTION //pks deleted\n"); //$NON-NLS-1$
			retval.append("//var action = SQL_ACTION_TYPES.INSERT_ACTION //pks inserted\n"); //$NON-NLS-1$
			retval.append("//var action = SQL_ACTION_TYPES.UPDATE_ACTION //pks updates\n"); //$NON-NLS-1$
			retval.append("var pksdataset = databaseManager.convertToDataSet(new Array(12,15,16,21))\n"); //$NON-NLS-1$
			retval.append("var ok = plugins.rawSQL.notifyDataChange(controller.getServerName(), 'employees', pksdataset,action)\n"); //$NON-NLS-1$
		}
		else return null;

		return retval.toString();
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(String)
	 */
	public String getToolTip(String methodName)
	{
		if ("executeSQL".equals(methodName)) //$NON-NLS-1$
		{
			return "Execute any SQL, returns true if successful."; //$NON-NLS-1$
		}
		if ("executeStoredProcedure".equals(methodName)) //$NON-NLS-1$
		{
			return "Execute a stored procedure."; //$NON-NLS-1$
		}
		if ("getException".equals(methodName)) //$NON-NLS-1$
		{
			return "If the result from a function was false, it will return the exception object."; //$NON-NLS-1$
		}
		if ("flushAllClientsCache".equals(methodName)) //$NON-NLS-1$
		{
			return "Flush cached database data. Use with extreme care, its affecting the performance of clients!"; //$NON-NLS-1$
		}
		if ("notifyDataChange".equals(methodName)) //$NON-NLS-1$
		{
			return "Notify clients about changes in records, based on pk(s). Use with extreme care, its affecting the performance of clients!"; //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}
}
