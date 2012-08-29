/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2012 Servoy BV

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

package com.servoy.extensions.plugins.headlessclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.servoy.j2db.server.annotations.TerracottaAutolockRead;
import com.servoy.j2db.server.annotations.TerracottaAutolockWrite;
import com.servoy.j2db.server.annotations.TerracottaInstrumentedClass;
import com.servoy.j2db.server.annotations.TerracottaRoot;
import com.servoy.j2db.util.Debug;
import com.tc.cluster.DsoCluster;
import com.tc.cluster.DsoClusterEvent;
import com.tc.cluster.DsoClusterListener;
import com.tc.injection.annotations.InjectedDsoInstance;
import com.tcclient.cluster.DsoNode;

/**
 * Class that dispatches a call to the correct instance of server plugin (in case of clustering).
 * @author acostescu
 */
@TerracottaInstrumentedClass
public class ServerPluginDispatcher<E> implements Runnable
{

	/**
	 * Classes that implement this interface will be in cluster shared memory - so instrument them with Terracotta correctly.
	 * @author acostescu
	 *
	 * @param <E> the object of target server that a call needs to use in order to execute.
	 */
	@TerracottaInstrumentedClass
	public static interface Call<E, R>
	{
		/**
		 * Return type, and the exception
		 */
		R executeCall(E correctServerObject) throws Exception;
	}

	/**
	 * Children this class will be in cluster shared memory.
	 * @author acostescu
	 */
	@TerracottaInstrumentedClass
	private static class CallWrapper<E, R>
	{
		private boolean done = false;
		private final Call<E, R> call;
		private String exceptionMsg = null;
		private RuntimeException exception = null;
		private R result = null;

		public CallWrapper(Call<E, R> call)
		{
			this.call = call;
		}

		@TerracottaAutolockWrite
		public void executeCall(E correctServerObject)
		{
			synchronized (this) // Terracotta WRITE lock
			{
				try
				{
					result = call.executeCall(correctServerObject);
				}
				catch (ExceptionWrapper e)
				{
					exception = e;
				}
				catch (ClientNotFoundException e)
				{
					exception = e;
				}
				catch (Throwable e)
				{
					// we can't be sure that this exception class is instrumented and can be shared with terracotta DSO
					Debug.error(e);
					exceptionMsg = e.getClass().getCanonicalName() + ": " + e.getMessage(); //$NON-NLS-1$
				}
				finally
				{
					done = true;
					notifyAll(); // tell calling server plugin that he's got a value
				}
			}
		}

		@TerracottaAutolockWrite
		public void waitForCall() throws InterruptedException
		{
			synchronized (this) // Terracotta WRITE lock (for wait)
			{
				while (!done)
					wait();
			}
		}

	}

	// serverPluginId -> calls to be executed by that server plugin
	@TerracottaRoot
	private final HashMap<String, List<CallWrapper<E, ? >>> clusterWideCallQueue = new HashMap<String, List<CallWrapper<E, ? >>>();
	private final List<CallWrapper<E, ? >> callQueueOfThisPlugin; // cache to avoid one more unnecessary readLock on clusterWideCallQueue when dispatching messages
	private boolean runningInCluster; // true if the plugin is running inside a terracotta cluster; false if it's a stand-alone Servoy app. server
	private final String thisServerPluginId;
	private volatile boolean stop = false;
	private final E thisServerObject;

	@TerracottaAutolockWrite
	public ServerPluginDispatcher(String serverPluginId, E thisServerObject)
	{
		this.thisServerPluginId = serverPluginId;

		runningInCluster = false;
		try
		{
			// if this class can be found it means application server was started under Terracotta
			Class.forName("com.tc.cluster.DsoClusterListener"); //$NON-NLS-1$
			runningInCluster = true;

			Debug.trace("Starting cluster listener for server plugin."); //$NON-NLS-1$
			new ClusterListener();
		}
		catch (ClassNotFoundException e)
		{
			// we are not running inside a Terracotta cluster, so no need to have a status monitor
		}

		this.thisServerObject = thisServerObject;

		callQueueOfThisPlugin = new ArrayList<CallWrapper<E, ? >>();

		synchronized (clusterWideCallQueue) // Terracotta WRITE lock
		{
			clusterWideCallQueue.put(serverPluginId, callQueueOfThisPlugin);
		}

		if (runningInCluster)
		{
			try
			{
				Thread callScheduler = new Thread(this, "Headless plugin Dispatcher"); //$NON-NLS-1$
				callScheduler.setDaemon(true);
				callScheduler.start();
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
		}
	}

	/**
	 * Should only be called on/by the Servoy server this object originated from. (as 'stop' is terracotta transient)
	 */
	@TerracottaAutolockWrite
	public void shutdown()
	{
		synchronized (callQueueOfThisPlugin) // Terracotta WRITE lock (for notify)
		{
			stop = true;
			callQueueOfThisPlugin.notifyAll();
		}
	}

	@TerracottaAutolockWrite
	public <R> void callOnAllServers(Call<E, R> call)
	{
		CallWrapper<E, R> callWrapper = new CallWrapper<E, R>(call);
		if (runningInCluster)
		{
			synchronized (clusterWideCallQueue) // Terracotta WRITE lock
			{
				for (List<CallWrapper<E, ? >> lst : clusterWideCallQueue.values())
				{
					synchronized (lst) // Terracotta WRITE lock
					{
						if (lst.size() == 0) lst.notifyAll();
						lst.add(callWrapper);
					}
				}
			}
		}
		else
		{
			callWrapper.executeCall(thisServerObject);
		}
	}

	/**
	 * @return if waitForExecution is false it will return null, otherwise it will wait for the method to get called and return it's result.
	 */
	@TerracottaAutolockWrite
	public <R> R callOnCorrectServer(String serverPluginId, Call<E, R> call, boolean waitForExecution)
	{
		CallWrapper<E, R> callWrapper = new CallWrapper<E, R>(call);

		if (runningInCluster)
		{
			List<CallWrapper<E, ? >> callList = getCallList(serverPluginId);
			if (callList != null)
			{
				synchronized (callList) // Terracotta WRITE lock
				{
					if (callList.size() == 0) callList.notify();
					callList.add(callWrapper);
				}

				if (waitForExecution)
				{
					try
					{
						callWrapper.waitForCall(); // after this call, the callWrapper should be up to date on the heap (clustering), as it's synchronized when waiting
						// otherwise, future references like callWrapper.result would need to acquire a terracotta read lock
					}
					catch (InterruptedException e)
					{
						throw new RuntimeException(e.getClass().getCanonicalName() + ": " + e.getMessage()); //$NON-NLS-1$
					}
				}
			}
			else
			{
				throw new RuntimeException("The client's app. server is no longer running."); //$NON-NLS-1$
			}
		}
		else
		{
			callWrapper.executeCall(thisServerObject);
		}

		if (callWrapper.exception != null)
		{
			throw callWrapper.exception;
		}
		else if (callWrapper.exceptionMsg != null)
		{
			throw new RuntimeException(callWrapper.exceptionMsg);
		}
		return callWrapper.result;
	}

	@TerracottaAutolockWrite
	public void run()
	{
		// runs in a separate thread when inside a terracotta cluster
		List<CallWrapper<E, ? >> copiedCalls = new ArrayList<CallWrapper<E, ? >>();
		while (!stop)
		{
			try
			{
				synchronized (callQueueOfThisPlugin) // Terracotta WRITE lock
				{
					while (callQueueOfThisPlugin.size() == 0 && !stop)
					{
						callQueueOfThisPlugin.wait();
					}
					if (stop) break;
					copiedCalls.addAll(callQueueOfThisPlugin);
					callQueueOfThisPlugin.clear();
				}
				for (CallWrapper<E, ? > callWrapper : copiedCalls)
				{
					callWrapper.executeCall(thisServerObject);
				}
			}
			catch (Exception e)
			{
				Debug.log(e);
				try
				{
					// Protection against 100% CPU usage in case of a fatal error.
					Thread.sleep(1000);
				}
				catch (InterruptedException i)
				{
					// Ignore.
				}
			}
			copiedCalls.clear();
		}
	}

	@TerracottaAutolockRead
	private List<CallWrapper<E, ? >> getCallList(String serverPluginId)
	{
		List<CallWrapper<E, ? >> callList;
		if (serverPluginId == this.thisServerPluginId)
		{
			// call is to be sent to this server
			callList = callQueueOfThisPlugin;
		}
		else
		{
			synchronized (clusterWideCallQueue) // Terracotta READ lock
			{
				callList = clusterWideCallQueue.get(serverPluginId);
			}
		}
		return callList;
	}

	@TerracottaAutolockWrite
	public void cleanupServer(String serverPluginId)
	{
		synchronized (clusterWideCallQueue) // Terracotta WRITE lock
		{
			clusterWideCallQueue.remove(serverPluginId);
		}
	}

	@TerracottaInstrumentedClass
	public class ClusterListener implements DsoClusterListener
	{

		@TerracottaRoot
		private final HashMap<String, String> nodeIdToServerPluginID = new HashMap<String, String>();

		@InjectedDsoInstance
		private DsoCluster cluster;

		public ClusterListener()
		{
			if (cluster == null) throw new RuntimeException(
				"Please remove the tim-api.jar from classpath if not running clustered! Classes from it are available but not functional...");

			// cluster is set by Terracotta
			cluster.addClusterListener(this);
		}

		@TerracottaAutolockWrite
		public void nodeJoined(DsoClusterEvent event)
		{
			if (event.getNode() == cluster.getCurrentNode())
			{
				// current server is joining the cluster
				synchronized (nodeIdToServerPluginID) // Terracotta WRITE lock
				{
					// when the last Servoy server from a cluster is shut-down, nodeIdToServerPluginID might not get cleaned up properly because there is no other
					// node listening for it's disconnect event; so do this cleanup when a new node connects if necessary (we could do it only if it is the first node connecting,
					// but that would not be thread-safe, cause because we can get the number of connected servers from a cluster, another server might already have been started)
					HashSet<String> allNodeIds = new HashSet<String>();
					List<String> disconnectedNodeIds = new ArrayList<String>();
					for (DsoNode x : cluster.getClusterTopology().getNodes())
					{
						allNodeIds.add(x.getId());
					}
					for (String x : nodeIdToServerPluginID.keySet())
					{
						if (!allNodeIds.contains(x))
						{
							disconnectedNodeIds.add(x);
						}
					}
					for (String x : disconnectedNodeIds)
					{
						Debug.trace("Cleaning up after another node in server plugin (at startup). Cleanup for node with Terracotta id:  " + x); //$NON-NLS-1$
						cleanupAfterOtherNode(x); // node is in nodeIdToServerPluginID but is no longer connected to the Terracotta cluster
					}

					// register this server as being part of the cluster
					nodeIdToServerPluginID.put(event.getNode().getId(), thisServerPluginId);
				}
			}
		}

		public void nodeLeft(DsoClusterEvent event)
		{
			// if an app. server (node) left the cluster - clean-up it's data (maybe it didn't close properly in order to perform cleanup
			if (event.getNode() == cluster.getCurrentNode())
			{
				// maybe this happened because node is shutting down... if app. server was not shut down properly, there isn't much we can do, because any attempt to send
				// messages to clients would need to acquire terracotta locks - which are no longer operational; so it is just going to end execution probably
			}
			else
			{
				// another node left the cluster; current node is still connected
				Debug.trace("Cleaning up after another in server plugin: " + event.getNode().getHostname() + " (" + event.getNode().getIp() + ")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				// make sure we do the cleanup for this node just in case it left before it could cleanup itself
				cleanupAfterOtherNode(event.getNode().getId());
			}
		}

		@TerracottaAutolockWrite
		private void cleanupAfterOtherNode(String id)
		{
			synchronized (nodeIdToServerPluginID) // Terracotta WRITE lock
			{
				if (nodeIdToServerPluginID.containsKey(id))
				{
					// do cleanup
					cleanupServer(nodeIdToServerPluginID.get(id));
					nodeIdToServerPluginID.remove(id);
				}
			}
		}

		// this method will only be called for the current node (info from method documentation on the site)
		public void operationsDisabled(DsoClusterEvent event)
		{
			// not interested (this app. server (node) is not doing anything, maybe due to a connection problem
			// Servoy has a timeout defined which will result in it closing after a while)
		}

		// this method will only be called for the current node (info from method documentation on the site)
		public void operationsEnabled(DsoClusterEvent event)
		{
			// not interested
		}

	}

}