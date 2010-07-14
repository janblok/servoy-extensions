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

	/**
	 * If the result from a function was false, it will return the exception object
	 *
	 * @sample 
	 */
	public Exception js_getException()
	{
		return exception;
	}

	/**
	 * Execute any SQL, returns true if successful
	 *
	 * @sample
	 * //****************************************************************************
	 * // WARNING! You can cause data loss or serious data integrity compromises!
	 * // You should have a THOROUGH understanding of both SQL and your backend
	 * // database (and other interfaces that may use that backend) BEFORE YOU USE
	 * // ANY OF THESE COMMANDS.
	 * // You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS
	 * // 
	 * // Note that when server names have been switched (databasemanager.switchServer),the 
	 * // real server names must be used here, plugins.rawSQL is not transparent to switched servers.
	 * // ****************************************************************************
	 * //Execute any SQL, returns true if successful
	 * var country = 'NL'
	 * var done = plugins.rawSQL.executeSQL("example_data","employees","update employees set country = ?", [country])
	 * if (done)
	 * {
	 * 	//flush is required when changes are made in db
	 * 	plugins.rawSQL.flushAllClientsCache("example_data","employees")
	 * }
	 * else
	 * {
	 * 	var msg = plugins.rawSQL.getException().getMessage(); //see exception node for more info about the exception obj
	 * 	plugins.dialogs.showErrorDialog('Error',  'SQL exception: '+msg,  'Ok')
	 * }
	 *
	 * @param serverName 
	 *
	 * @param tableName 
	 *
	 * @param SQL 
	 *
	 * @param [arguments] optional 
	 */
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

	/**
	 * Execute a stored procedure
	 *
	 * @sample
	 * // ****************************************************************************
	 * // WARNING! You can cause data loss or serious data integrity compromises!
	 * // You should have a THOROUGH understanding of both SQL and your backend
	 * // database (and other interfaces that may use that backend) BEFORE YOU USE
	 * // ANY OF THESE COMMANDS.
	 * // You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS
	 * // 
	 * // Note that when server names have been switched (databasemanager.switchServer),the 
	 * // real server names must be used here, plugins.rawSQL is not transparent to switched servers.
	 * // ****************************************************************************
	 * //Execute a stored procedure
	 * var maxReturnedRows = 10;//useful to limit number of rows
	 * var procedure_declaration = '{?=calculate_interest_rate(?)}'
	 * var args = new Array()
	 * args[0] = java.sql.Types.NUMERIC
	 * args[1] = 3000
	 * //	define the types and direction, in this case a 0 for input data
	 * var typesArray = new Array();
	 * typesArray[0]=1;
	 * typesArray[1]=0;
	 * var dataset = plugins.rawSQL.executeStoredProcedure(controller.getServerName(), procedure_declaration, args, typesArray,maxReturnedRows);
	 * 
	 * //example to calc a strange total
	 * global_total = 0;
	 * for( var i = 1 ; i <= dataset.getMaxRowIndex() ; i++ )
	 * {
	 * 		global_total = global_total + dataset.getValue(i,1);
	 * }
	 *
	 * @param serverName 
	 *
	 * @param procedureDeclaration 
	 *
	 * @param arguments[] 
	 *
	 * @param IODirectionality[] 
	 *
	 * @param maxNrReturnedRows 
	 */
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

	/**
	 * 
	 *
	 * @sample
	 * // ****************************************************************************
	 * // WARNING! You can cause data loss or serious data integrity compromises!
	 * // You should have a THOROUGH understanding of both SQL and your backend
	 * // database (and other interfaces that may use that backend) BEFORE YOU USE
	 * // ANY OF THESE COMMANDS.
	 * // You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS
	 * // 
	 * // Note that when server names have been switched (databasemanager.switchServer),the 
	 * // real server names must be used here, plugins.rawSQL is not transparent to switched servers.
	 * // ****************************************************************************
	 * //null
	 * var uuid = application.getNewUUID();
	 * plugins.rawSQL.executeSQL(controller.getServerName(), 'employees', 'insert into employees (employees_id, creation_date) values (?, ?)', [plugins.rawSQL.convertUUIDToBytes(uuid), new Date()]);
	 */
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

	/**
	 * Flush cached database data, use with extreme care, its effecting the performance of clients!
	 *
	 * @sample
	 * // ****************************************************************************
	 * // WARNING! You can cause data loss or serious data integrity compromises!
	 * // You should have a THOROUGH understanding of both SQL and your backend
	 * // database (and other interfaces that may use that backend) BEFORE YOU USE
	 * // ANY OF THESE COMMANDS.
	 * // You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS
	 * // 
	 * // Note that when server names have been switched (databasemanager.switchServer),the 
	 * // real server names must be used here, plugins.rawSQL is not transparent to switched servers.
	 * // ****************************************************************************
	 * //Flush cached database data, use with extreme care, its effecting the performance of clients!
	 * var country = 'NL'
	 * var done = plugins.rawSQL.executeSQL("example_data","employees","update employees set country = ?", [country])
	 * if (done)
	 * {
	 * 	//flush is required when changes are made in db
	 * 	plugins.rawSQL.flushAllClientsCache("example_data","employees")
	 * }
	 * else
	 * {
	 * 	var msg = plugins.rawSQL.getException().getMessage(); //see exception node for more info about the exception obj
	 * 	plugins.dialogs.showErrorDialog('Error',  'SQL exception: '+msg,  'Ok')
	 * }
	 *
	 * @param serverName 
	 *
	 * @param tableName 
	 */
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

	/**
	 * Notify clients about changes in records, based on pk(s), use with extreme care, its effecting the performance of clients!
	 *
	 * @sample
	 * // ****************************************************************************
	 * // WARNING! You can cause data loss or serious data integrity compromises!
	 * // You should have a THOROUGH understanding of both SQL and your backend
	 * // database (and other interfaces that may use that backend) BEFORE YOU USE
	 * // ANY OF THESE COMMANDS.
	 * // You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS
	 * // 
	 * // Note that when server names have been switched (databasemanager.switchServer),the 
	 * // real server names must be used here, plugins.rawSQL is not transparent to switched servers.
	 * // ****************************************************************************
	 * //Notify clients about changes in records, based on pk(s), use with extreme care, its effecting the performance of clients!
	 * var action = 1 //pks deleted
	 * //var action = 2 //pks inserted
	 * //var action = 3 //pks updates
	 * var pksdataset = databaseManager.convertToDataSet(new Array(12,15,16,21))
	 * var ok = plugins.rawSQL.notifyDataChange(controller.getServerName(), 'employees',pksdataset,action)
	 *
	 * @param serverName 
	 *
	 * @param tableName 
	 *
	 * @param pksDataset 
	 *
	 * @param action 
	 */
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
		StringBuffer retval = new StringBuffer();
		retval.append("/****************************************************************************\n"); //$NON-NLS-1$
		retval.append("WARNING! You can cause data loss or serious data integrity compromises!\n"); //$NON-NLS-1$
		retval.append("You should have a THOROUGH understanding of both SQL and your backend\n"); //$NON-NLS-1$
		retval.append("database (and other interfaces that may use that backend) BEFORE YOU USE\n"); //$NON-NLS-1$
		retval.append("ANY OF THESE COMMANDS.\n"); //$NON-NLS-1$
		retval.append("You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS\n"); //$NON-NLS-1$
		retval.append("\nNote that when server names have been switched (databasemanager.switchServer),the \n"); //$NON-NLS-1$
		retval.append("real server names must be used here, plugins.rawSQL is not transparent to switched servers.\n"); //$NON-NLS-1$
		retval.append("****************************************************************************/\n"); //$NON-NLS-1$

		if ("executeSQL".equals(methodName) || "flushAllClientsCache".equals(methodName)) //$NON-NLS-1$ //$NON-NLS-2$
		{
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var country = 'NL'\n"); //$NON-NLS-1$
			retval.append("var done = plugins.rawSQL.executeSQL(\"example_data\",\"employees\",\"update employees set country = ?\", [country])\n"); //$NON-NLS-1$
			retval.append("if (done)\n{\n\t//flush is required when changes are made in db\n"); //$NON-NLS-1$
			retval.append("\tplugins.rawSQL.flushAllClientsCache(\"example_data\",\"employees\")\n"); //$NON-NLS-1$
			retval.append("}\nelse\n{\n"); //$NON-NLS-1$
			retval.append("\tvar msg = plugins.rawSQL.getException().getMessage(); //see exception node for more info about the exception obj\n"); //$NON-NLS-1$
			retval.append("\tplugins.dialogs.showErrorDialog('Error',  'SQL exception: '+msg,  'Ok')\n"); //$NON-NLS-1$
			retval.append("}\n"); //$NON-NLS-1$
			return retval.toString();
		}
		if ("executeStoredProcedure".equals(methodName)) //$NON-NLS-1$
		{
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var maxReturnedRows = 10;//useful to limit number of rows\n"); //$NON-NLS-1$
			retval.append("var procedure_declaration = '{?=calculate_interest_rate(?)}'\n"); //$NON-NLS-1$
			retval.append("var args = new Array()\n"); //$NON-NLS-1$
			retval.append("args[0] = java.sql.Types.NUMERIC\n"); //$NON-NLS-1$
			retval.append("args[1] = 3000\n"); //$NON-NLS-1$
			retval.append("//	define the types and direction, in this case a 0 for input data\n"); //$NON-NLS-1$
			retval.append("var typesArray = new Array();\n"); //$NON-NLS-1$
			retval.append("typesArray[0]=1;\n"); //$NON-NLS-1$
			retval.append("typesArray[1]=0;\n"); //$NON-NLS-1$
			retval.append("var dataset = plugins.rawSQL.executeStoredProcedure(controller.getServerName(), procedure_declaration, args, typesArray,maxReturnedRows);\n\n"); //$NON-NLS-1$
			retval.append("//example to calc a strange total\n"); //$NON-NLS-1$
			retval.append("global_total = 0;\n"); //$NON-NLS-1$
			retval.append("for( var i = 1 ; i <= dataset.getMaxRowIndex() ; i++ )\n"); //$NON-NLS-1$
			retval.append("{\n"); //$NON-NLS-1$
			retval.append("		global_total = global_total + dataset.getValue(i,1);\n"); //$NON-NLS-1$
			retval.append("}\n"); //$NON-NLS-1$
			return retval.toString();
		}
		if ("convertUUIDToBytes".equals(methodName)) //$NON-NLS-1$
		{
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var uuid = application.getNewUUID();\n"); //$NON-NLS-1$
			retval.append("plugins.rawSQL.executeSQL(controller.getServerName(), 'employees', 'insert into employees (employees_id, creation_date) values (?, ?)', [plugins.rawSQL.convertUUIDToBytes(uuid), new Date()]);\n"); //$NON-NLS-1$
			return retval.toString();
		}
		if ("notifyDataChange".equals(methodName)) //$NON-NLS-1$
		{
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(methodName));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var action = 1 //pks deleted\n"); //$NON-NLS-1$
			retval.append("//var action = 2 //pks inserted\n"); //$NON-NLS-1$
			retval.append("//var action = 3 //pks updates\n"); //$NON-NLS-1$
			retval.append("var pksdataset = databaseManager.convertToDataSet(new Array(12,15,16,21))\n"); //$NON-NLS-1$
			retval.append("var ok = plugins.rawSQL.notifyDataChange(controller.getServerName(), 'employees',pksdataset,action)\n"); //$NON-NLS-1$
			return retval.toString();
		}
		return null;
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
