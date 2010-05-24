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
package com.servoy.extensions.plugins.scheduler;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import com.servoy.j2db.plugins.IClientPluginAccess;

/**
 * @author jcompagner
 */
public class ExecuteScriptMethodJob implements StatefulJob
{
	public void execute(JobExecutionContext jobContext) throws JobExecutionException
	{
		final String name = jobContext.getJobDetail().getName();
		JobDataMap jdm = jobContext.getJobDetail().getJobDataMap();
		Object o = jdm.get("formname"); //$NON-NLS-1$
		final String formname;
		if(o instanceof String)
		{
			formname = (String)o;
		}
		else
		{
			formname = null;
		}
		final String methodname = (String) jdm.get("methodname"); //$NON-NLS-1$
		final Object[] args = (Object[]) jdm.get("args"); //$NON-NLS-1$
		final IClientPluginAccess access = (IClientPluginAccess) jdm.get("access"); //$NON-NLS-1$
		final SchedulerProvider provider = (SchedulerProvider) jdm.get("scheduler"); //$NON-NLS-1$
		
		provider.setLastRunJobName(name);
		try
		{
			access.executeMethod(formname, methodname, args,true);
		}
		catch (Exception e1)
		{
			access.handleException(null, e1);
		}
	}
}
