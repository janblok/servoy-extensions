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

package com.servoy.extensions.plugins.file;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.mozilla.javascript.Function;

import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IJavaScriptType;
import com.servoy.j2db.scripting.IScriptObject;

/**
 * This class is returned to the Servoy client from the {@link FileProvider} js_streamFilesFromServer or js_streamFilesToServer method<br/>
 * It is used to monitor the uploads/downloads calling back a Servoy method that can then get the status of the transfer(s)<br/>
 * It is called repeatedly by a {@link Timer} when scheduled with a fixed interval to callback the Servoy method provided.
 * 
 * @since 5.2.1
 * 
 * @author jcompagner
 * @author Servoy Stuff
 */
public class JSProgressMonitor extends TimerTask implements IScriptObject, IJavaScriptType
{
	private final FileProvider provider;
	private FunctionDefinition callback;

	private final AtomicLong totalBytes = new AtomicLong(0);
	private final AtomicLong totalTransferred = new AtomicLong(0);
	private final AtomicLong currentBytes = new AtomicLong(0);
	private final AtomicLong currentTransferred = new AtomicLong(0);
	private final AtomicInteger totalFiles = new AtomicInteger(0);
	private final AtomicInteger currentFileIndex = new AtomicInteger(0);
	private final AtomicInteger testDelay = new AtomicInteger(0);
	private final AtomicBoolean finished = new AtomicBoolean(false);
	private final AtomicBoolean canceled = new AtomicBoolean(false);
	private String currentFileName;

	/**
	 * For developer scripting introspection only
	 */
	public JSProgressMonitor()
	{
		this.provider = null;
	}

	/**
	 * Constructor
	 * 
	 * @param provider the parent {@link FileProvider}
	 * @param totalBytes the total number of bytes to transfer, calculated by the parent
	 * @param totalFiles the total number of files to transfer, calculated by the parent
	 */
	public JSProgressMonitor(final FileProvider provider, final long totalBytes, final int totalFiles)
	{
		this.provider = provider;
		this.totalBytes.set(totalBytes);
		this.totalFiles.set(totalFiles);
	}

	/**
	 * Schedules a callback to a Servoy method
	 * 
	 * @param function the {@link Function} to call back at the specified interval
	 * @param interval the interval (in seconds) to use
	 * @return this for chaining
	 */
	public JSProgressMonitor js_setProgressCallBack(final Function function, final float interval)
	{
		return js_setProgressCallBack(function, interval, 0);
	}

	/**
	 * Schedules a callback to a Servoy method
	 * 
	 * @param function the {@link Function} to call back at the specified interval
	 * @param interval the interval (in seconds) to use
	 * @param delay adds a delay for testing purpose in Developer
	 * @return this for chaining
	 */
	public JSProgressMonitor js_setProgressCallBack(final Function function, final float interval, final int delay)
	{
		if (function != null && interval >= 1f)
		{
			synchronized (this)
			{
				if (callback == null) // callback and interval cannot be changed once set
				{
					callback = new FunctionDefinition(function);
					provider.scheduleMonitor(this, interval);
				}
			}
		}
		this.testDelay.set(delay);
		return this;
	}


	public long js_getTotalBytesToTransfer()
	{
		return totalBytes.get();
	}

	public long js_getTotalTransferredBytes()
	{
		return totalTransferred.get();
	}

	public long js_getCurrentBytesToTransfer()
	{
		return currentBytes.get();
	}

	public long js_getCurrentTransferredBytes()
	{
		return currentTransferred.get();
	}

	public int js_getTotalFilesToTransfer()
	{
		return totalFiles.get();
	}

	public int js_getCurrentFileIndex()
	{
		return currentFileIndex.get();
	}

	public synchronized String js_getCurrentTransferredFileName()
	{
		return currentFileName;
	}

	public boolean js_isFinished()
	{
		return finished.get();
	}

	public boolean js_isCanceled()
	{
		return canceled.get();
	}

	public boolean js_cancel()
	{
		this.canceled.set(true);
		return super.cancel();
	}

	public int getDelay()
	{
		return testDelay.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.IReturnedTypesProvider#getAllReturnedTypes()
	 */
	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.IScriptObject#getSample(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String getSample(String methodName)
	{
		if ("setProgressCallBack".equals(methodName))
		{
			return "// call the progressCallbackFuntion every 2 and a half seconds (with a delay of 200ms in developer):\n\tmonitor.setProgressCallBack(progressCallbackFunction, 2.5, (application.isInDeveloper() ? 200 : 0));\n";
		}
		else if ("cancel".equals(methodName))
		{
			return "monitor.cancel();\n";
		}
		else
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append("application.output('total transferred: ' + monitor.getTotalTransferredBytes() + ' / ' + monitor.getTotalBytesToTransfer());\n");
			buffer.append("\tapplication.output('current file: ' + monitor.getCurrentTransferredFileName() + ' ( ' + monitor.getCurrentFileIndex() + ' / ' + monitor.getTotalFilesToTransfer() + ' )');\n");
			buffer.append("\tapplication.output('current bytes transferred: '+monitor.getCurrentTransferredBytes() + ' / ' + monitor.getCurrentBytesToTransfer());\n");
			buffer.append("\tif (monitor.isCanceled()) {\n");
			buffer.append("\t\tapplication.output('canceled!')\n");
			buffer.append("\t}\n");
			buffer.append("\tif (monitor.isFinished()) {\n");
			buffer.append("\t\tapplication.output('finished!'\n");
			buffer.append("\t}\n");
			return buffer.toString();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String getToolTip(String methodName)
	{
		if ("setProgressCallBack".equals(methodName))
		{
			return "Sets a method to be called repeatedly at the given interval (in seconds), the method will receive an instance of this JSProgressMonitor updated with the latest values. Can use an optional delay (for testing purpose in developer).";
		}
		else if ("cancel".equals(methodName))
		{
			return "Cancels the transfer process.";
		}
		else if ("getTotalBytesToTransfer".equals(methodName))
		{
			return "Returns the total bytes to transfer to or from the server (sum of all the files size)";
		}
		else if ("getTotalTransferredBytes".equals(methodName))
		{
			return "Returns the total bytes already transferred (for all files)";
		}
		else if ("getCurrentBytesToTransfer".equals(methodName))
		{
			return "Returns the number of bytes to transfer for the current file.";
		}
		else if ("getCurrentTransferredBytes".equals(methodName))
		{
			return "Returns the number of bytes already transferred for the current file.";
		}
		else if ("getTotalFilesToTransfer".equals(methodName))
		{
			return "Returns the total number of files to transfer.";
		}
		else if ("getCurrentFileIndex".equals(methodName))
		{
			return "Returns the index of the current file being transferred.";
		}
		else if ("getCurrentTransferredFileName".equals(methodName))
		{
			return "Returns the name of the current file being transferred.";
		}
		else if ("isFinished".equals(methodName))
		{
			return "Returns true if the process is finished.";
		}
		else if ("isCanceled".equals(methodName))
		{
			return "Returns true if the process was canceled.";
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.IScriptObject#getParameterNames(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String[] getParameterNames(String methodName)
	{
		if ("setProgressCallBack".equals(methodName))
		{
			return new String[] { "progressCallbackFunction", "interval", "[testDelay]" };
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.j2db.scripting.IScriptObject#isDeprecated(java.lang.String)
	 */
	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	/**
	 * @param totalBytes the totalBytes to set
	 */
	public void setTotalBytes(long totalBytes)
	{
		this.totalBytes.set(totalBytes);
	}

	/**
	 * @param totalTransferred the totalTransferred to set
	 */
	public void setTotalTransferred(long totalTransferred)
	{
		this.totalTransferred.set(totalTransferred);
	}

	/**
	 * @param currentBytes the currentBytes to set
	 */
	public void setCurrentBytes(long currentBytes)
	{
		this.currentBytes.set(currentBytes);
	}

	/**
	 * @param currentTransferred the currentTransferred to set
	 */
	public void setCurrentTransferred(long currentTransferred)
	{
		this.currentTransferred.set(currentTransferred);
	}

	/**
	 * @param currentFileName the currentFileName to set
	 */
	public synchronized void setCurrentFileName(String currentFileName)
	{
		this.currentFileName = currentFileName;
	}

	/**
	 * @param totalFiles the totalFiles to set
	 */
	public void setTotalFiles(int totalFiles)
	{
		this.totalFiles.set(totalFiles);
	}

	/**
	 * @param currentFileIndex the currentFileIndex to set
	 */
	public void setCurrentFileIndex(int currentFileIndex)
	{
		this.currentFileIndex.set(currentFileIndex);
	}

	/**
	 * @param finished the finished to set
	 */
	public void setFinished(boolean finished)
	{
		this.finished.set(finished);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public synchronized void run()
	{
		if (callback != null)
		{
			provider.callbackProgress(this, callback);
		}
	}

}
