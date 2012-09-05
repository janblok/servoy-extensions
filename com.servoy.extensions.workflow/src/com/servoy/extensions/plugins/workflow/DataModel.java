/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2011 Servoy BV

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

package com.servoy.extensions.plugins.workflow;

import java.sql.Connection;
import java.sql.Types;

import com.servoy.j2db.persistence.Column;
import com.servoy.j2db.persistence.DummyValidator;
import com.servoy.j2db.persistence.IServerInternal;
import com.servoy.j2db.persistence.IValidateName;
import com.servoy.j2db.persistence.Table;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

//this class is likely obsolete, latest hibernate version can do all of below 
public class DataModel 
{
	private static final boolean NULL = true;
	private static final boolean NOT_NULL = false;

	private IServerInternal server;
	public DataModel(IServerInternal s)
	{
		server = s;
	}
	
	public void create() throws Exception
	{
		IValidateName v = DummyValidator.INSTANCE;
		
		// Create tables
		Table table = null;

		table = server.createNewTable(v, "JBPM4_DEPLOYMENT");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "NAME_", Types.VARCHAR, 0 , NULL);
		table.createNewColumn(v, "TIMESTAMP_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "STATE_", Types.VARCHAR, 255, NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_DEPLOYPROP");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "DEPLOYMENT_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "OBJNAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "KEY_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "STRINGVAL_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "LONGVAL_", Types.BIGINT, 0 , NULL);
		server.syncTableObjWithDB(table, true, true, null);
	    
	    table = server.createNewTable(v, "JBPM4_EXECUTION");
	    table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
	    table.createNewColumn(v, "CLASS_", Types.VARCHAR, 255, NOT_NULL);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "ACTIVITYNAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "PROCDEFID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "HASVARS_", Types.BOOLEAN, 0, NULL);
		table.createNewColumn(v, "NAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "KEY_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "ID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "STATE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "SUSPHISTSTATE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "PRIORITY_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "HISACTINST_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "PARENT_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "INSTANCE_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "SUPEREXEC_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "SUBPROCINST_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "PARENT_IDX_", Types.INTEGER, 0, NULL);
		server.syncTableObjWithDB(table, true, true, null);
	    
	    table = server.createNewTable(v, "JBPM4_HIST_ACTINST");
	    table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
	    table.createNewColumn(v, "CLASS_", Types.VARCHAR, 255, NOT_NULL);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "HPROCI_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "TYPE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "EXECUTION_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "ACTIVITY_NAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "START_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "END_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "DURATION_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "TRANSITION_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "NEXTIDX_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "HTASK_", Types.BIGINT, 0 , NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_HIST_DETAIL");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "CLASS_", Types.VARCHAR, 255, NOT_NULL);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "USERID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "TIME_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "HPROCI_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "HPROCIIDX_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "HACTI_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "HACTIIDX_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "HTASK_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "HTASKIDX_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "HVAR_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "HVARIDX_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "MESSAGE_", Types.VARCHAR, 0, NULL);
		table.createNewColumn(v, "OLD_STR_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "NEW_STR_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "OLD_INT_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "NEW_INT_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "OLD_TIME_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "NEW_TIME_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "PARENT_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "PARENT_IDX_", Types.INTEGER, 0, NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_HIST_PROCINST");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "ID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "PROCDEFID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "KEY_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "START_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "END_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "DURATION_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "STATE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "ENDACTIVITY_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "NEXTIDX_", Types.INTEGER, 0, NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_HIST_TASK");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "EXECUTION_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "OUTCOME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "ASSIGNEE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "PRIORITY_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "STATE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "CREATE_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "END_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "DURATION_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "NEXTIDX_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "SUPERTASK_", Types.BIGINT, 0 , NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_HIST_VAR");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "PROCINSTID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "EXECUTIONID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "VARNAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "VALUE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "HPROCI_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "HTASK_", Types.BIGINT, 0 , NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_ID_GROUP");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "ID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "NAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "TYPE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "PARENT_", Types.BIGINT, 0 , NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_ID_MEMBERSHIP");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "USER_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "GROUP_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "NAME_", Types.VARCHAR, 255, NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_ID_USER");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "ID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "PASSWORD_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "GIVENNAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "FAMILYNAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "BUSINESSEMAIL_", Types.VARCHAR, 255, NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_JOB");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "CLASS_", Types.VARCHAR, 255, NOT_NULL);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "DUEDATE_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "STATE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "ISEXCLUSIVE_", Types.BOOLEAN, 0, NULL);
		table.createNewColumn(v, "LOCKOWNER_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "LOCKEXPTIME_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "EXCEPTION_", Types.VARCHAR, 0, NULL);
		table.createNewColumn(v, "RETRIES_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "PROCESSINSTANCE_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "EXECUTION_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "CFG_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "SIGNAL_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "EVENT_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "REPEAT_", Types.VARCHAR, 255, NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_LOB");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "BLOB_VALUE_", Types.LONGVARBINARY, 0 , NULL);
		table.createNewColumn(v, "DEPLOYMENT_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "NAME_", Types.VARCHAR, 0, NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_PARTICIPATION");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "GROUPID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "USERID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "TYPE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "TASK_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "SWIMLANE_", Types.BIGINT, 0 , NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_PROPERTY");
		table.createNewColumn(v, "KEY_", Types.VARCHAR, 255, NOT_NULL);
		table.createNewColumn(v, "VERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "VALUE_", Types.VARCHAR, 255, NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_SWIMLANE");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "NAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "ASSIGNEE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "EXECUTION_", Types.BIGINT, 0 , NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_TASK");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "CLASS_", Types.VARCHAR, 255, NOT_NULL);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "NAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "DESCR_", Types.VARCHAR, 0, NULL);
		table.createNewColumn(v, "STATE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "SUSPHISTSTATE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "ASSIGNEE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "FORM_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "PRIORITY_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "CREATE_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "DUEDATE_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "PROGRESS_", Types.INTEGER, 0, NULL);
		table.createNewColumn(v, "SIGNALLING_", Types.BOOLEAN, 0, NULL);
		table.createNewColumn(v, "EXECUTION_ID_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "ACTIVITY_NAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "HASVARS_", Types.BOOLEAN, 0, NULL);
		table.createNewColumn(v, "SUPERTASK_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "EXECUTION_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "PROCINST_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "SWIMLANE_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "TASKDEFNAME_", Types.VARCHAR, 255, NULL);
		server.syncTableObjWithDB(table, true, true, null);
		
		table = server.createNewTable(v, "JBPM4_VARIABLE");
		table.createNewColumn(v, "DBID_", Types.BIGINT, 0, NOT_NULL, true);
		table.createNewColumn(v, "CLASS_", Types.VARCHAR, 255, NOT_NULL);
		table.createNewColumn(v, "DBVERSION_", Types.INTEGER, 0, NOT_NULL);
		table.createNewColumn(v, "KEY_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "CONVERTER_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "HIST_", Types.BOOLEAN, 0, NULL);
		table.createNewColumn(v, "EXECUTION_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "TASK_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "LOB_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "DATE_VALUE_", Types.TIMESTAMP, 0, NULL);
		table.createNewColumn(v, "DOUBLE_VALUE_", Types.FLOAT, 0 , NULL);
		table.createNewColumn(v, "CLASSNAME_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "LONG_VALUE_", Types.BIGINT, 0 , NULL);
		table.createNewColumn(v, "STRING_VALUE_", Types.VARCHAR, 255, NULL);
		table.createNewColumn(v, "TEXT_VALUE_", Types.VARCHAR, 0, NULL);
		table.createNewColumn(v, "EXESYS_", Types.BIGINT, 0 , NULL);
		server.syncTableObjWithDB(table, true, true, null);
	    
	    createIndex(server,"IDX_DEPLPROP_DEPL","JBPM4_DEPLOYPROP","DEPLOYMENT_");

	    createIndex(server,"IDX_EXEC_SUPEREXEC","JBPM4_EXECUTION","SUPEREXEC_");

	    createIndex(server,"IDX_EXEC_INSTANCE","JBPM4_EXECUTION","INSTANCE_");

	    createIndex(server,"IDX_EXEC_SUBPI","JBPM4_EXECUTION","SUBPROCINST_");

	    createIndex(server,"IDX_EXEC_PARENT","JBPM4_EXECUTION","PARENT_");

	    createIndex(server,"IDX_HACTI_HPROCI","JBPM4_HIST_ACTINST","HPROCI_");

	    createIndex(server,"IDX_HTI_HTASK","JBPM4_HIST_ACTINST","HTASK_");

	    createIndex(server,"IDX_HDET_HACTI","JBPM4_HIST_DETAIL","HACTI_");

	    createIndex(server,"IDX_HDET_HPROCI","JBPM4_HIST_DETAIL","HPROCI_");

	    createIndex(server,"IDX_HDET_HVAR","JBPM4_HIST_DETAIL","HVAR_");

	    createIndex(server,"IDX_HDET_HTASK","JBPM4_HIST_DETAIL","HTASK_");

	    createIndex(server,"IDX_HSUPERT_SUB","JBPM4_HIST_TASK","SUPERTASK_");

	    createIndex(server,"IDX_HVAR_HPROCI","JBPM4_HIST_VAR","HPROCI_");

	    createIndex(server,"IDX_HVAR_HTASK","JBPM4_HIST_VAR","HTASK_");

	    createIndex(server,"IDX_GROUP_PARENT","JBPM4_ID_GROUP","PARENT_");

	    createIndex(server,"IDX_MEM_USER","JBPM4_ID_MEMBERSHIP","USER_");

	    createIndex(server,"IDX_MEM_GROUP","JBPM4_ID_MEMBERSHIP","GROUP_");

	    createIndex(server,"IDX_JOBRETRIES","JBPM4_JOB","RETRIES_");

	    createIndex(server,"IDX_JOB_CFG","JBPM4_JOB","CFG_");

	    createIndex(server,"IDX_JOB_PRINST","JBPM4_JOB","PROCESSINSTANCE_");

	    createIndex(server,"IDX_JOB_EXE","JBPM4_JOB","EXECUTION_");

	    createIndex(server,"IDX_JOBLOCKEXP","JBPM4_JOB","LOCKEXPTIME_");

	    createIndex(server,"IDX_JOBDUEDATE","JBPM4_JOB","DUEDATE_");

	    createIndex(server,"IDX_LOB_DEPLOYMENT","JBPM4_LOB","DEPLOYMENT_");

	    createIndex(server,"IDX_PART_TASK","JBPM4_PARTICIPATION","TASK_");

	    createIndex(server,"IDX_SWIMLANE_EXEC","JBPM4_SWIMLANE","EXECUTION_");

	    createIndex(server,"IDX_TASK_SUPERTASK","JBPM4_TASK","SUPERTASK_");

	    createIndex(server,"IDX_VAR_EXESYS","JBPM4_VARIABLE","EXESYS_");

	    createIndex(server,"IDX_VAR_TASK","JBPM4_VARIABLE","TASK_");

	    createIndex(server,"IDX_VAR_EXECUTION","JBPM4_VARIABLE","EXECUTION_");

	    createIndex(server,"IDX_VAR_LOB","JBPM4_VARIABLE","LOB_");

	    addForeignKeyConstraint(server,"JBPM4_DEPLOYPROP","FK_DEPLPROP_DEPL","DEPLOYMENT_","JBPM4_DEPLOYMENT");

	    addForeignKeyConstraint(server,"JBPM4_EXECUTION","FK_EXEC_PARENT","PARENT_","JBPM4_EXECUTION");

	    addForeignKeyConstraint(server,"JBPM4_EXECUTION","FK_EXEC_SUBPI","SUBPROCINST_","JBPM4_EXECUTION");

	    addForeignKeyConstraint(server,"JBPM4_EXECUTION","FK_EXEC_INSTANCE","INSTANCE_","JBPM4_EXECUTION");

	    addForeignKeyConstraint(server,"JBPM4_EXECUTION","FK_EXEC_SUPEREXEC","SUPEREXEC_","JBPM4_EXECUTION");

	    addForeignKeyConstraint(server,"JBPM4_HIST_ACTINST","FK_HACTI_HPROCI","HPROCI_","JBPM4_HIST_PROCINST");

	    addForeignKeyConstraint(server,"JBPM4_HIST_ACTINST","FK_HTI_HTASK","HTASK_","JBPM4_HIST_TASK");

	    addForeignKeyConstraint(server,"JBPM4_HIST_DETAIL","FK_HDETAIL_HPROCI","HPROCI_","JBPM4_HIST_PROCINST");

	    addForeignKeyConstraint(server,"JBPM4_HIST_DETAIL","FK_HDETAIL_HACTI","HACTI_","JBPM4_HIST_ACTINST");

	    addForeignKeyConstraint(server,"JBPM4_HIST_DETAIL","FK_HDETAIL_HTASK","HTASK_","JBPM4_HIST_TASK");

	    addForeignKeyConstraint(server,"JBPM4_HIST_DETAIL","FK_HDETAIL_HVAR","HVAR_","JBPM4_HIST_VAR");

	    addForeignKeyConstraint(server,"JBPM4_HIST_TASK","FK_HSUPERT_SUB","SUPERTASK_","JBPM4_HIST_TASK");

	    addForeignKeyConstraint(server,"JBPM4_HIST_VAR","FK_HVAR_HPROCI","HPROCI_","JBPM4_HIST_PROCINST");

	    addForeignKeyConstraint(server,"JBPM4_HIST_VAR","FK_HVAR_HTASK","HTASK_","JBPM4_HIST_TASK");

	    addForeignKeyConstraint(server,"JBPM4_ID_GROUP","FK_GROUP_PARENT","PARENT_","JBPM4_ID_GROUP");

	    addForeignKeyConstraint(server,"JBPM4_ID_MEMBERSHIP","FK_MEM_GROUP","GROUP_","JBPM4_ID_GROUP");

	    addForeignKeyConstraint(server,"JBPM4_ID_MEMBERSHIP","FK_MEM_USER","USER_","JBPM4_ID_USER");

	    addForeignKeyConstraint(server,"JBPM4_JOB","FK_JOB_CFG","CFG_","JBPM4_LOB");

	    addForeignKeyConstraint(server,"JBPM4_LOB","FK_LOB_DEPLOYMENT","DEPLOYMENT_","JBPM4_DEPLOYMENT");

	    addForeignKeyConstraint(server,"JBPM4_PARTICIPATION","FK_PART_SWIMLANE","SWIMLANE_","JBPM4_SWIMLANE");

	    addForeignKeyConstraint(server,"JBPM4_PARTICIPATION","FK_PART_TASK","TASK_","JBPM4_TASK");

	    addForeignKeyConstraint(server,"JBPM4_SWIMLANE","FK_SWIMLANE_EXEC","EXECUTION_","JBPM4_EXECUTION");

	    addForeignKeyConstraint(server,"JBPM4_TASK","FK_TASK_SWIML","SWIMLANE_","JBPM4_SWIMLANE");

	    addForeignKeyConstraint(server,"JBPM4_TASK","FK_TASK_SUPERTASK","SUPERTASK_","JBPM4_TASK");

	    addForeignKeyConstraint(server,"JBPM4_VARIABLE","FK_VAR_LOB","LOB_","JBPM4_LOB");

	    addForeignKeyConstraint(server,"JBPM4_VARIABLE","FK_VAR_EXECUTION","EXECUTION_","JBPM4_EXECUTION");

	    addForeignKeyConstraint(server,"JBPM4_VARIABLE","FK_VAR_EXESYS","EXESYS_","JBPM4_EXECUTION");

	    addForeignKeyConstraint(server,"JBPM4_VARIABLE","FK_VAR_TASK","TASK_","JBPM4_TASK");

	}

	public boolean checkIFModelExists() throws Exception
	{
		return (server.getTable("jbpm4_variable") != null);
	}
	
	public static void createIndex(IServerInternal server, String indexName, String tableName, String singleColumnName) throws Exception 
	{
		createIndex(server, indexName, tableName,  new String[]{singleColumnName});
	}
	
	public static void createIndex(IServerInternal server, String indexName, String tableName, String[] columnNames) throws Exception 
	{
//		StringBuffer sb = new StringBuffer("CREATE INDEX ");
//		sb.append(indexName);
//		sb.append(" ON ");
//		sb.append(tableName);
//		sb.append(" (");
//		for (int i = 0; i < columnNames.length; i++) 
//		{
//			sb.append(columnNames[i]);
//			if (i < columnNames.length-1) sb.append(",");
//		}
//		sb.append(");");
//		Debug.trace(sb);

		Table t = (Table) server.getTable(tableName);
		Column[] cols = new Column[columnNames.length];
		for (int i = 0; i < columnNames.length; i++) 
		{
			cols[i] = t.getColumn(columnNames[i]);
		}
		Connection c = server.getRawConnection();
		try
		{
			String ci = server.getIndexCreateString(c, t, indexName, cols, false);
			c.createStatement().execute(ci);
		}
		finally
		{
			Utils.closeConnection(c);
		}
	}
	
	public static void addForeignKeyConstraint(IServerInternal server,String table, String name, String key, String reftable) throws Exception
	{
		StringBuffer sb = new StringBuffer("alter table ");
		sb.append("\"");
		sb.append(table);
		sb.append("\"");
		sb.append(" add constraint ");
		sb.append(name);
		sb.append(" foreign key (");
		sb.append(key);
		sb.append(") references ");
		sb.append("\"");
		sb.append(reftable);
		sb.append("\"");
		sb.append(" (DBID_)");
		Debug.trace(sb);
		
		Connection c = server.getRawConnection();
		try
		{
			c.createStatement().execute(sb.toString());
		}
		finally
		{
			Utils.closeConnection(c);
		}
	}
}
