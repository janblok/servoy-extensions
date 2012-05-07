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

import java.text.ParseException;
import java.util.Date;

import org.mozilla.javascript.Function;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import com.servoy.j2db.Messages;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;

/**
 * @author jcompagner
 */
public class SchedulerProvider implements IScriptObject
{
	private static Object schedulerLock = new Object();
	private static Scheduler scheduler;
	private final SchedulerPlugin plugin;

	private String lastRunned;

	/**
	 * @param app
	 */
	public SchedulerProvider(SchedulerPlugin plugin)
	{
		this.plugin = plugin;
	}

	public void stopScheduler()
	{
		if (scheduler != null)
		{
			try
			{
				int type = plugin.getClientPluginAccess().getApplicationType();
				if (type == IClientPluginAccess.CLIENT || type == IClientPluginAccess.RUNTIME)
				{
					scheduler.shutdown(false);
					scheduler = null;
				}
				else
				{
					String id = plugin.getClientPluginAccess().getClientID();
					String[] jobNames = scheduler.getJobNames(id);
					if (jobNames != null && jobNames.length > 0)
					{
						for (String element : jobNames)
						{
							scheduler.deleteJob(element, id);
						}
					}
				}
			}
			catch (SchedulerException e)
			{
				Debug.error(e);
			}
		}
	}

	public boolean isDeprecated(String methodName)
	{
		if ("getLastRunnedJobName".equals(methodName)) //$NON-NLS-1$
		{
			return true;
		}
		return false;
	}

	public String getSample(String methodName)
	{
		if (methodName.equals("addCronJob")) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// see: http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html for more info\n"); //$NON-NLS-1$
			sb.append("// add a job that runs every 20 minutes after the hour (0,20,40)\n"); //$NON-NLS-1$
			sb.append("plugins.scheduler.addCronJob('20mins','0 0/20 * * * ?',method)\n"); //$NON-NLS-1$
			sb.append("// add a job that runs every day at 23:30 between now and 5 days from now\n"); //$NON-NLS-1$
			sb.append("var dateNow = new Date();\n"); //$NON-NLS-1$
			sb.append("var date5Days = new Date(dateNow.getTime()+5*24*60*60*1000);\n"); //$NON-NLS-1$
			sb.append("plugins.scheduler.addCronJob('23:30','0 30 23 ? * *',method,dateNow,date5Days)\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if (methodName.equals("addJob")) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// add a job that runs at the given date (20 seconds in the future)\n"); //$NON-NLS-1$
			sb.append("// and repeats that every 20 seconds for 40 times or the enddate is reached (0 for no repeats = just one call)\n"); //$NON-NLS-1$
			sb.append("var startDate = new Date();\n"); //$NON-NLS-1$
			sb.append("startDate.setTime(startDate.getTime()+20000);\n"); //$NON-NLS-1$
			sb.append("var endDate = new Date(startDate.getTime()+100000);\n"); //$NON-NLS-1$
			sb.append("plugins.scheduler.addJob('in20seconds',startDate,method,20000,40,endDate)\n"); //$NON-NLS-1$

			return sb.toString();
		}
		else if (methodName.equals("removeJob")) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// removes a job 'myjob' from the scheduler\n"); //$NON-NLS-1$
			sb.append("plugins.scheduler.removeJob('myjob')\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if (methodName.equals("getCurrentJobNames")) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Returns an array of current jobnames\n"); //$NON-NLS-1$
			sb.append("plugins.scheduler.getCurrentJobNames()\n"); //$NON-NLS-1$
			return sb.toString();
		}
		else if ("getLastRunJobName".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer sb = new StringBuffer();
			sb.append("// Returns the last job run from the scheduler\n"); //$NON-NLS-1$
			sb.append("plugins.scheduler.getLastRunJobName();\n"); //$NON-NLS-1$
			return sb.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("addCronJob".equals(methodName)) //$NON-NLS-1$
		{
			return "Adds a cron job to the scheduler."; //$NON-NLS-1$
		}
		else if ("addJob".equals(methodName)) //$NON-NLS-1$
		{
			return "Adds a job to the scheduler."; //$NON-NLS-1$
		}
		else if ("removeJob".equals(methodName)) //$NON-NLS-1$
		{
			return "Removes a job from the scheduler."; //$NON-NLS-1$
		}
		else if ("getCurrentJobNames".equals(methodName)) //$NON-NLS-1$
		{
			return "Returns an array with the current jobs."; //$NON-NLS-1$
		}
		else if ("getLastRunJobName".equals(methodName)) //$NON-NLS-1$
		{
			return "Returns the last job run from the scheduler."; //$NON-NLS-1$
		}
		return null;
	}

	public String[] getParameterNames(String methodName)
	{
		if (methodName.equals("addCronJob")) //$NON-NLS-1$
		{
			return new String[] { "jobname", "cronTimings", "method", "[startDate]", "[endDate]", "[arguments]" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		}
		else if (methodName.equals("addJob")) //$NON-NLS-1$
		{
			return new String[] { "jobname", "startDate", "method", "[repeatInterval(ms)]", "[repeatCount]", "[endDate]", "[arguments]" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		}
		else if (methodName.equals("removeJob")) //$NON-NLS-1$
		{
			return new String[] { "jobname" }; //$NON-NLS-1$
		}
		return null;
	}

	public Class[] getAllReturnedTypes()
	{
		return null;
	}

	public void js_addJob(String name, Date time, Function method)
	{
		js_addJob(name, time, method, 0, 0, null, null);
	}

	public void js_addJob(String name, Date time, Function method, Object[] args)
	{
		js_addJob(name, time, method, 0, 0, null, args);
	}

	public void js_addJob(String name, Date time, Function method, int repeatInterval)
	{
		js_addJob(name, time, method, repeatInterval, -1, null, null);
	}

	public void js_addJob(String name, Date time, Function method, int repeatInterval, int repeatCount)
	{
		js_addJob(name, time, method, repeatInterval, repeatCount, null, null);
	}

	public void js_addJob(String name, Date time, Function method, int repeatInterval, int repeatCount, Date endDate)
	{
		js_addJob(name, time, method, repeatInterval, repeatCount, endDate, null);
	}

	/**
	 * Adds a job to the scheduler (parameters: jobname,triggerDate,method,[repeatInterval(ms),repeatCount,endDate,arguments]/[arguments])
	 *
	 * @sample
	 * // add a job that runs at the given date (20 seconds in the future)
	 * // and repeats that every 20 seconds for 40 times or the enddate is reached (0 for no repeats = just one call)
	 * var startDate = new Date();
	 * startDate.setTime(startDate.getTime()+20000);
	 * var endDate = new Date(startDate.getTime()+100000);
	 * plugins.scheduler.addJob('in20seconds',startDate,method,20000,40,endDate)
	 *
	 * @param jobname 
	 *
	 * @param startDate 
	 *
	 * @param method 
	 *
	 * @param repeatInterval(ms) optional 
	 *
	 * @param repeatCount optional 
	 *
	 * @param endDate optional 
	 *
	 * @param arguments optional 
	 */
	public void js_addJob(String name, Date time, Function method, int repeatInterval, int repeatCount, Date endDate, Object[] args)
	{
		testScheduler();

		synchronized (schedulerLock)
		{
			String id = plugin.getClientPluginAccess().getClientID();
			JobDetail jobDetail = new JobDetail(name, id, ExecuteScriptMethodJob.class);
			jobDetail.getJobDataMap().put("scheduler", this); //$NON-NLS-1$
			FunctionDefinition functionDef = new FunctionDefinition(method);
			jobDetail.getJobDataMap().put("methodname", functionDef.getMethodName()); //$NON-NLS-1$
			jobDetail.getJobDataMap().put("formname", functionDef.getFormName()); //$NON-NLS-1$
			jobDetail.getJobDataMap().put("args", args); //$NON-NLS-1$
			jobDetail.getJobDataMap().put("access", plugin.getClientPluginAccess()); //$NON-NLS-1$
			if (repeatCount == -1)
			{
				repeatCount = SimpleTrigger.REPEAT_INDEFINITELY;
			}

			SimpleTrigger trigger = new SimpleTrigger(name, id, time, endDate, repeatCount, repeatInterval);
			try
			{
				scheduler.scheduleJob(jobDetail, trigger);
			}
			catch (SchedulerException e)
			{
				Debug.error(e);
				throw new RuntimeException(Messages.getString("servoy.plugin.scheduler.cannotStart") + e.getMessage()); //$NON-NLS-1$
			}
		}
	}

	public String js_getLastRunnedJobName()
	{
		return lastRunned;
	}

	public String js_getLastRunJobName()
	{
		return lastRunned;
	}

	void setLastRunJobName(String lastRunned)
	{
		this.lastRunned = lastRunned;
	}

	/**
	 * Returns an array with the current jobs
	 *
	 * @sample
	 * // Returns an array of current jobnames
	 * plugins.scheduler.getCurrentJobNames()
	 */
	public String[] js_getCurrentJobNames()
	{
		testScheduler();
		try
		{
			String id = plugin.getClientPluginAccess().getClientID();
			return scheduler.getJobNames(id);
		}
		catch (SchedulerException e)
		{
			Debug.error(e);
		}
		return new String[0];
	}

	public void js_addCronJob(String name, String timings, Function method)
	{
		js_addCronJob(name, timings, method, null, null, null);
	}

	public void js_addCronJob(String name, String timings, Function method, Date startDate)
	{
		js_addCronJob(name, timings, method, startDate, null, null);
	}


	public void js_addCronJob(String name, String timings, Function method, Date startDate, Date endDate)
	{
		js_addCronJob(name, timings, method, startDate, endDate, null);
	}

	/**
	 * Adds a cron job to the scheduler (parameter: jobname,cronTiming (s m h D/M M D/W),method,[startDate],[endDate]). A cron job must have at least one minute between each execution (otherwise it won't execute).
	 *
	 * @sample
	 * // add a job that runs every 20 minutes after the hour (0,20,40)
	 * plugins.scheduler.addCronJob('20mins','0 0/20 * * * ?',method)
	 * // add a job that runs every day at 23:30 between now and 5 days from now
	 * var dateNow = new Date();
	 * var date5Days = new Date(dateNow.getTime()+5*24*60*60*1000);
	 * plugins.scheduler.addCronJob('23:30','0 30 23 ? * *',method,dateNow,date5Days)
	 *
	 * @param jobname 
	 *
	 * @param cronTimings 
	 *
	 * @param method 
	 *
	 * @param [startDate] optional 
	 *
	 * @param [endDate] optional 
	 *
	 * @param [arguments] optional 
	 * 
	 * @link http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html
	 */
	public void js_addCronJob(String name, String timings, Function method, Date startDate, Date endDate, Object[] args)
	{
		testScheduler();

		synchronized (schedulerLock)
		{
			String id = plugin.getClientPluginAccess().getClientID();
			JobDetail jobDetail = new JobDetail(name, id, ExecuteScriptMethodJob.class);
			jobDetail.getJobDataMap().put("scheduler", this); //$NON-NLS-1$
			FunctionDefinition functionDef = new FunctionDefinition(method);
			jobDetail.getJobDataMap().put("methodname", functionDef.getMethodName()); //$NON-NLS-1$
			jobDetail.getJobDataMap().put("formname", functionDef.getFormName()); //$NON-NLS-1$
			jobDetail.getJobDataMap().put("args", args); //$NON-NLS-1$
			jobDetail.getJobDataMap().put("access", plugin.getClientPluginAccess()); //$NON-NLS-1$
			try
			{

				CronTrigger cronTrigger = new CronTrigger(name, id, name, id, startDate, endDate, timings);
				scheduler.scheduleJob(jobDetail, cronTrigger);
			}
			catch (ParseException e)
			{
				Debug.error(e);
				throw new RuntimeException(Messages.getString("servoy.plugin.scheduler.invalidTimings", new Object[] { timings, e.getMessage() })); //$NON-NLS-1$
			}
			catch (SchedulerException e)
			{
				Debug.error(e);
				throw new RuntimeException(Messages.getString("servoy.plugin.scheduler.cannotScheduleJob", new Object[] { name, e.getMessage() })); //$NON-NLS-1$
			}
		}
	}


	private void testScheduler()
	{
		synchronized (schedulerLock)
		{
			if (scheduler == null)
			{
				try
				{
					//				SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
					//				scheduler = schedFact.getScheduler();
					scheduler = org.quartz.impl.StdSchedulerFactory.getDefaultScheduler();
					scheduler.start();
				}
				catch (Exception e)
				{
					Debug.error(e);
					throw new RuntimeException(Messages.getString("servoy.plugin.scheduler.cannotStart") + e.getMessage()); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Removes a job from the scheduler (parameter: jobname)
	 *
	 * @sample
	 * // removes a job 'myjob' from the scheduler
	 * plugins.scheduler.removeJob('myjob')
	 *
	 * @param jobname 
	 */
	public boolean js_removeJob(String name)
	{
		if (scheduler != null)
		{
			try
			{
				String id = plugin.getClientPluginAccess().getClientID();
				return scheduler.deleteJob(name, id);
			}
			catch (SchedulerException e)
			{
				Debug.error("Error removing scheduler job: " + e.getMessage()); //$NON-NLS-1$
			}
		}
		return false;
	}
}
