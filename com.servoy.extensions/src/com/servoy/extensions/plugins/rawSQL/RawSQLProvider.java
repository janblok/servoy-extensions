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
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.ClientPluginAccessProvider;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.DataSourceUtils;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.ServoyException;
import com.servoy.j2db.util.UUID;

/**
 * Rawsql plugin scriptable.
 * 
 * @author jblok
 */
@ServoyDocumented(publicName = RawSQLPlugin.PLUGIN_NAME, scriptingName = "plugins." + RawSQLPlugin.PLUGIN_NAME)
public class RawSQLProvider implements IScriptable
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
			sqlService = (ISQLService)plugin.getClientPluginAccess().getRemoteService("servoy.ISQLService");
		}
		return sqlService;
	}

	/**
	 * @deprecated Replaced by {@link #getException()}.
	 */
	@Deprecated
	public String js_getExceptionMsg()
	{
		return (exception == null ? null : exception.toString() + ' ' + exception.getMessage());
	}

	/**
	 * If the result from a function was false, it will return the exception object.
	 *
	 * @sampleas js_executeSQL(String,String,String)
	 */
	public Exception js_getException()
	{
		return exception;
	}

	/**
	 * Execute any SQL, returns true if successful.
	 *
	 * @sample
	 * /**************************************************************************** 
	 * WARNING! You can cause data loss or serious data integrity compromises!
	 * You should have a THOROUGH understanding of both SQL and your backend
	 * database (and other interfaces that may use that backend) BEFORE YOU USE
	 * ANY OF THESE COMMANDS.
	 * You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS
	 * 
	 * Note that when server names have been switched (databasemanager.switchServer),the 
	 * real server names must be used here, plugins.rawSQL is not transparent to switched servers.
	 * ****************************************************************************&#47;
	 * 
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
	 * // Note that when this function is used to create a new table in the database, this table will only be seen by
	 * // the Servoy Application Server when the table name starts with 'temp_', otherwise a server restart is needed.
	 * 
	 * @param serverName the name of the server 
	 * @param tableName the name of the table
	 * @param sql the sql query to execute
	 */
	public boolean js_executeSQL(String serverName, String tableName, String sql)
	{
		return js_executeSQL(serverName, tableName, sql, null);
	}

	/**
	 * @clonedesc js_executeSQL(String,String,String)
	 *
	 * @sampleas js_executeSQL(String,String,String)
	 * 
	 * @param serverName the name of the server 
	 * @param tableName the name of the table
	 * @param sql the sql query to execute
	 * @param sql_args the arguments for the query
	 */
	public boolean js_executeSQL(String serverName, String tableName, String sql, Object[] sql_args)
	{
		try
		{
			String originalServerName = plugin.getClientPluginAccess().getDatabaseManager().getOriginalServerName(serverName);
			String tid = plugin.getClientPluginAccess().getDatabaseManager().getTransactionID(originalServerName);
			return getSQLService().executeSQL(plugin.getClientPluginAccess().getClientID(), serverName, tableName, sql, sql_args, tid);
		}
		catch (Exception ex)
		{
			exception = ex;
			Debug.error(ex);
			return false;
		}
	}

	/**
	 * Execute a stored procedure.
	 *
	 * @sample
	 * /**************************************************************************** 
	 * WARNING! You can cause data loss or serious data integrity compromises!
	 * You should have a THOROUGH understanding of both SQL and your backend
	 * database (and other interfaces that may use that backend) BEFORE YOU USE
	 * ANY OF THESE COMMANDS.
	 * You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS
	 * 
	 * Note that when server names have been switched (databasemanager.switchServer),the 
	 * real server names must be used here, plugins.rawSQL is not transparent to switched servers.
	 * ****************************************************************************&#47;
	 * 
	 * var maxReturnedRows = 10; //useful to limit number of rows
	 * var procedure_declaration = '{?=calculate_interest_rate(?)}'
	 * // define the direction, a 0 for input data, a 1 for output data
	 * var typesArray = [1, 0];
	 * // define the types and values, a value for input data, a sql-type for output data
	 * var args = [java.sql.Types.NUMERIC, 3000]
	 * // A dataset is returned, when no output-parameters defined, the last select-result in the procedure will be returned.
	 * // When one or more output-parameters are defined, the dataset will contain 1 row with the output data.
	 * var dataset = plugins.rawSQL.executeStoredProcedure(databaseManager.getDataSourceServerName(controller.getDataSource()), procedure_declaration, args, typesArray, maxReturnedRows);
	 * var interest_rate = dataset.getValue(1, 1);
	 *
	 * @param serverName 
	 * @param procedureDeclaration 
	 * @param arguments 
	 * @param inOutDirectionality 
	 * @param maxNumberOfRowsToRetrieve 
	 * 
	 * @return a dataset with output (in case of output data) or the last result set executed by the procedure.
	 */
	public JSDataSet js_executeStoredProcedure(String serverName, String procedureDeclaration, Object[] arguments, int[] inOutDirectionality,
		int maxNumberOfRowsToRetrieve)
	{
		if (arguments != null && inOutDirectionality != null && arguments.length != inOutDirectionality.length)
		{
			throw new RuntimeException("In/Out Arguments should be same size as directionality array");
		}

		try
		{
			String tid = plugin.getClientPluginAccess().getDatabaseManager().getTransactionID(
				plugin.getClientPluginAccess().getDatabaseManager().getOriginalServerName(serverName));
			String cid = plugin.getClientPluginAccess().getClientID();
			// TODO HOW TO HANDLE ARGS WITH NULL?? sHOULD BE CONVERTED TO NullValue?????
			IDataSet set = getSQLService().executeStoredProcedure(cid, serverName, tid, procedureDeclaration, arguments, inOutDirectionality, 0,
				maxNumberOfRowsToRetrieve);
			return new JSDataSet(((ClientPluginAccessProvider)plugin.getClientPluginAccess()).getApplication(), set);
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
	 * Execute a stored procedure, return all created result sets.
	 *
	 * @sample
	 * /**************************************************************************** 
	 * WARNING! You can cause data loss or serious data integrity compromises!
	 * You should have a THOROUGH understanding of both SQL and your backend
	 * database (and other interfaces that may use that backend) BEFORE YOU USE
	 * ANY OF THESE COMMANDS.
	 * You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS
	 * 
	 * Note that when server names have been switched (databasemanager.switchServer),the 
	 * real server names must be used here, plugins.rawSQL is not transparent to switched servers.
	 * ****************************************************************************&#47;
	 * 
	 * var maxReturnedRows = 10; //useful to limit number of rows
	 * var procedure_declaration = '{ get_unpaid_orders_and_their_customers(?) }'
	 * var args = [42]
	 * var datasets = plugins.rawSQL.executeStoredProcedure(databaseManager.getDataSourceServerName(controller.getDataSource()), procedure_declaration, args, maxReturnedRows);
	 * for (var i = 0; i < datasets.length; i++) {
	 * 	var ds = datasets[i]
	 * 	// process dataset
	 * }
	 * 
	 * @param serverName 
	 * @param procedureDeclaration 
	 * @param arguments 
	 * @param maxNumberOfRowsToRetrieve 
	 * 
	 * @return the result sets created by the procedure.
	 */
	public IDataSet[] js_executeStoredProcedure(String serverName, String procedureDeclaration, Object[] arguments, int maxNumberOfRowsToRetrieve)
		throws Exception
	{
		String tid = plugin.getClientPluginAccess().getDatabaseManager().getTransactionID(
			plugin.getClientPluginAccess().getDatabaseManager().getOriginalServerName(serverName));
		// TODO HOW TO HANDLE ARGS WITH NULL?? sHOULD BE CONVERTED TO NullValue?????
		return getSQLService().executeStoredProcedure(plugin.getClientPluginAccess().getClientID(), serverName, tid, procedureDeclaration, arguments, 0,
			maxNumberOfRowsToRetrieve);
	}

	/**
	 * @deprecated Replaced by {@link application#getUUID(String)}
	 *
	 * @sample
	 * /**************************************************************************** 
	 * WARNING! You can cause data loss or serious data integrity compromises!
	 * You should have a THOROUGH understanding of both SQL and your backend
	 * database (and other interfaces that may use that backend) BEFORE YOU USE
	 * ANY OF THESE COMMANDS.
	 * You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS
	 * 
	 * Note that when server names have been switched (databasemanager.switchServer),the 
	 * real server names must be used here, plugins.rawSQL is not transparent to switched servers.
	 * ****************************************************************************&#47;
	 * 
	 * var uuid = application.getNewUUID();
	 * plugins.rawSQL.executeSQL(databaseManager.getDataSourceServerName(controller.getDataSource()), 'employees', 'insert into employees (employees_id, creation_date) values (?, ?)', [plugins.rawSQL.convertUUIDToBytes(uuid), new Date()]);
	 */
	@Deprecated
	public byte[] js_convertUUIDToBytes(String uuid)
	{
		return UUID.fromString(uuid).toBytes();
	}

	/**
	 * @deprecated Replaced by {@link application#getUUID(byte[])}
	 * 
	 * @param data
	 * @return
	 */
	@Deprecated
	public String js_convertUUIDToString(byte[] data)
	{
		return new UUID(data).toString();
	}

	/**
	 * Flush cached database data. Use with extreme care, its affecting the performance of clients!
	 *
	 * @sampleas js_executeSQL(String,String,String)
	 * 
	 * @param serverName
	 * @param tableName
	 */
	public boolean js_flushAllClientsCache(String serverName, String tableName)
	{
		try
		{
			String originalServerName = plugin.getClientPluginAccess().getDatabaseManager().getOriginalServerName(serverName);
			String transactionID = plugin.getClientPluginAccess().getDatabaseManager().getTransactionID(originalServerName);
			if (transactionID != null)
			{
				// flush data locally within the transaction
				plugin.getClientPluginAccess().getDatabaseManager().notifyDataChange(DataSourceUtils.createDBTableDataSource(originalServerName, tableName),
					null, 0);
			}
			return getSQLService().flushAllClientsCache(plugin.getClientPluginAccess().getClientID(), true, serverName, tableName, transactionID);
		}
		catch (Exception ex)
		{
			exception = ex;
			Debug.error(ex);
			return false;
		}
	}

	/**
	 * Notify clients about changes in records, based on pk(s). Use with extreme care, its affecting the performance of clients!
	 *
	 * @sample
	 * /**************************************************************************** 
	 * WARNING! You can cause data loss or serious data integrity compromises!
	 * You should have a THOROUGH understanding of both SQL and your backend
	 * database (and other interfaces that may use that backend) BEFORE YOU USE
	 * ANY OF THESE COMMANDS.
	 * You should also READ THE DOCUMENTATION BEFORE USING ANY OF THESE COMMANDS
	 * 
	 * Note that when server names have been switched (databasemanager.switchServer),the 
	 * real server names must be used here, plugins.rawSQL is not transparent to switched servers.
	 * ****************************************************************************&#47;
	 * 
	 * var action = SQL_ACTION_TYPES.DELETE_ACTION //pks deleted
	 * //var action = SQL_ACTION_TYPES.INSERT_ACTION //pks inserted
	 * //var action = SQL_ACTION_TYPES.UPDATE_ACTION //pks updates
	 * var pksdataset = databaseManager.convertToDataSet(new Array(12,15,16,21))
	 * var ok = plugins.rawSQL.notifyDataChange(databaseManager.getDataSourceServerName(controller.getDataSource()), 'employees', pksdataset,action)
	 *
	 * @param serverName 
	 * @param tableName 
	 * @param pksDataset 
	 * @param action 
	 */
	public boolean js_notifyDataChange(String serverName, String tableName, IDataSet pksDataset, int action)
	{
		if (pksDataset == null || pksDataset.getRowCount() == 0) return false; //make sure developer does not call this without knowing this would be the same as flushAllClientsCache function

		try
		{
			String originalServerName = plugin.getClientPluginAccess().getDatabaseManager().getOriginalServerName(serverName);
			String transactionID = plugin.getClientPluginAccess().getDatabaseManager().getTransactionID(originalServerName);
			if (transactionID != null)
			{
				// flush data locally within the transaction
				plugin.getClientPluginAccess().getDatabaseManager().notifyDataChange(DataSourceUtils.createDBTableDataSource(originalServerName, tableName),
					pksDataset, action);
			}
			return getSQLService().notifyDataChange(plugin.getClientPluginAccess().getClientID(), true, serverName, tableName, pksDataset, action,
				transactionID);
		}
		catch (Exception ex)
		{
			exception = ex;
			Debug.error(ex);
			return false;
		}
	}

}
