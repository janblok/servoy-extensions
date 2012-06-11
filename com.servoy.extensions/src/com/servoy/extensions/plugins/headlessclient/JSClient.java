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
package com.servoy.extensions.plugins.headlessclient;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Undefined;

import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IConstantsObject;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.scripting.JSEvent;
import com.servoy.j2db.util.Debug;

@ServoyDocumented
public class JSClient implements IScriptable, IConstantsObject
{
	private final IHeadlessServer headlessServer;
	private final HeadlessClientPlugin plugin;
	private final String clientID;

	/**
	 * Constant that is returned as a JSEvent type when in the callback method when it executed normally.
	 */
	public static final String CALLBACK_EVENT = "headlessCallback"; //$NON-NLS-1$

	/**
	 * Constant that is returned as a JSEvent type when in the callback method when an exception occured.
	 */
	public static final String CALLBACK_EXCEPTION_EVENT = "headlessExceptionCallback"; //$NON-NLS-1$

	// for doc
	public JSClient()
	{
		this(null, null, null);
	}

	public JSClient(String clientID, IHeadlessServer server, HeadlessClientPlugin plugin)
	{
		this.clientID = clientID;
		this.headlessServer = server;
		this.plugin = plugin;
	}

	/**
	 * gets the id of the client
	 *
	 * @sample
	 * if (jsclient && jsclient.isValid())
	 * {
	 * 	/*Queue a method where the callback can do something like this
	 * 	if (event.getType() == JSClient.CALLBACK_EVENT)
	 * 	{
	 * 		application.output("callback data, name: " + event.data);
	 * 	}
	 * 	else if (event.getType() == JSClient.CALLBACK_EXCEPTION_EVENT)
	 * 	{
	 * 		application.output("exception callback, name: " + event.data);
	 * 	}*&#47;
	 * 	var x = new Object();
	 * 	x.name = 'remote1';
	 * 	x.number = 10;
	 * 	// this calls a 'remoteMethod' on the server as a global method, because the context (first argument is set to null), you can use a formname to call a form method
	 * 	jsclient.queueMethod(null, "remoteMethod", [x], callback);
	 * }
	 */
	public String js_getClientID()
	{
		return clientID;
	}

	private final List<Runnable> methodCalls = new ArrayList<Runnable>();

	/**
	 * Queues a method call on the remote server. The callback method will be called when the method is executed on the server
	 * and the return value is given as the JSEvent.data object with the JSEvent.getType() value of JSClient.CALLBACK_EVENT. 
	 * If an exception is thrown somewhere then the callback method will be called with
	 * the exception as the JSEvent data object with the JSEvent.getType() value of JSClient.CALLBACK_EXCEPTION_EVENT
	 * The second argument that is give back is the JSClient instance that did the call.
	 * 
	 * @sampleas js_getClientID()
	 * 
	 * @param contextName The context of the given method, null if it is global method or a form name for a form method.
	 * @param methodName The method name.
	 * @param args The arguments that should be passed to the method.
	 * @param notifyCallBackMethod The callback method that is called when the execution is finished.
	 */
	public void js_queueMethod(final String contextName, final String methodName, final Object[] args, Function notifyCallBackMethod)
	{
		final FunctionDefinition functionDef = new FunctionDefinition(notifyCallBackMethod);
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				try
				{
					Object retval = null;
					try
					{
						String[] convertedArgs = null;
						if (args != null)
						{
							convertedArgs = new String[args.length];

							for (int i = 0; i < args.length; i++)
							{
								convertedArgs[i] = plugin.getJSONConverter().convertToJSON(args[i]);
							}

						}
						retval = plugin.getJSONConverter().convertFromJSON(
							headlessServer.executeMethod(clientID, contextName, methodName, convertedArgs, plugin.getPluginAccess().getClientID()));
						JSEvent event = new JSEvent();
						event.setType(CALLBACK_EVENT);
						event.setData(retval);
						// function def will not throw an exception.
						functionDef.executeAsync(plugin.getPluginAccess(), new Object[] { event, JSClient.this });
					}
					catch (ExceptionWrapper ex)
					{
						Debug.log("Error calling method " + methodName + ", context: " + contextName + " on client " + clientID, ex); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						JSEvent event = new JSEvent();
						event.setType(CALLBACK_EXCEPTION_EVENT);
						Object data = ex.getMessage();
						try
						{
							// see catch JavaScriptException in headlessServer.executeMethod
							data = plugin.getJSONConverter().convertFromJSON(data);
						}
						catch (Exception e)
						{
							Debug.error(e);
						}
						event.setData(data);
						functionDef.executeAsync(plugin.getPluginAccess(), new Object[] { event, JSClient.this });
					}
					catch (Exception ex)
					{
						Debug.log("Error calling method " + methodName + ", context: " + contextName + " on client " + clientID, ex); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						JSEvent event = new JSEvent();
						event.setType(CALLBACK_EXCEPTION_EVENT);
						Object data = ex.getMessage();
						event.setData(data);
						functionDef.executeAsync(plugin.getPluginAccess(), new Object[] { event, JSClient.this });
					}
				}
				finally
				{
					synchronized (methodCalls)
					{
						methodCalls.remove(this);
						if (methodCalls.size() > 0)
						{
							Executor exe = plugin.getPluginAccess().getExecutor();
							exe.execute(methodCalls.get(0));
						}
					}
				}
			}
		};
		synchronized (methodCalls)
		{
			methodCalls.add(runnable);
			if (methodCalls.size() == 1)
			{
				Executor exe = plugin.getPluginAccess().getExecutor();
				exe.execute(runnable);
			}
		}
	}

	/**
	 * returns true if this client is still valid/usable.
	 *
	 * @sampleas js_getClientID()
	 */
	public boolean js_isValid()
	{
		try
		{
			return headlessServer.isValid(clientID);
		}
		catch (Exception ex)
		{
			Debug.trace(ex);
			return false;
		}
	}

	/**
	 * @deprecated Replaced by {@link #shutdown(boolean)}
	 * 
	 * @sameas js_shutdown(boolean)
	 */
	@Deprecated
	public void js_shutDown(boolean force)
	{
		try
		{
			headlessServer.shutDown(clientID, force);
		}
		catch (Exception ex)
		{
			Debug.error(ex);
		}
	}

	/**
	 * closes the client.
	 *
	 * @sampleas js_getClientID()
	 */
	public void js_shutdown()
	{
		js_shutdown(false);
	}

	/**
	 * @clonedesc js_shutdown()
	 * @sampleas js_shutdown()
	 *
	 * @param force 
	 */
	public void js_shutdown(boolean force)
	{
		try
		{
			headlessServer.shutDown(clientID, force);
		}
		catch (Exception ex)
		{
			Debug.error(ex);
		}
	}

	/**
	 * Get a data-provider value.
	 *
	 * @sample
	 * if (jsclient && jsclient.isValid())
	 * {
	 * 	// only gets the globals.media when the 'remoteMethod' is currently executing for this client
	 * 	var value = jsclient.getDataProviderValue(null, "scopes.globals.number", 'remoteMethod');
	 * 	if (value != null)
	 * 	{
	 * 		application.output("value get from scopes.globals.number :: "+ value);
	 * 		scopes.globals.value = value+10;
	 * 		var returnValue = jsclient.setDataProviderValue(null, "scopes.globals.number", scopes.globals.value, 'remoteMethod');
	 * 		application.output("value set to scopes.globals.number previous value "+ returnValue);
	 * 	}
	 * 	else
	 * 	{
	 * 		application.output("value get from scopes.globals.number :: " + null);
	 * 	}
	 * }
	 * 
	 * @param contextName The context of the given method, null if it is global method or a form name for a form method
	 * @param dataprovider the data-provider name as seen in Servoy
	 * @return the value for the data-provider.
	 */
	public Object js_getDataProviderValue(String contextName, String dataprovider)
	{
		return js_getDataProviderValue(contextName, dataprovider, null);
	}

	/**
	 * @clonedesc js_getDataProviderValue(String, String)
	 * @sampleas js_getDataProviderValue(String, String)
	 *
	 * @param contextName The context of the given method; null if it is global method or a form name for a form method.
	 * @param dataprovider the data-provider name as seen in Servoy.
	 * @param methodName if this is specified, the data-provider's value will only be returned if the specified method is running in this headless client because the currently running client requested it to. Otherwise undefined is returned.
	 * @return the value of the data-provider.
	 */
	public Object js_getDataProviderValue(String contextName, String dataprovider, String methodName)
	{
		Object retval = null;
		try
		{
			retval = headlessServer.getDataProviderValue(clientID, contextName, dataprovider, plugin.getPluginAccess().getClientID(), methodName);
		}
		catch (RemoteException re)
		{
			Debug.error(re);
			throw new RuntimeException(re.getCause());
		}
		catch (Exception ex)
		{
			Debug.error(ex);
			throw new RuntimeException(ex);
		}
		if (UndefinedMarker.INSTANCE.equals(retval)) return Undefined.instance;

		try
		{
			return plugin.getJSONConverter().convertFromJSON(retval);
		}
		catch (Exception e)
		{
			Debug.error(e);
			return retval;
		}
	}


	/**
	 * Set a data-provider value.
	 * 
	 * @sampleas js_getDataProviderValue(String, String)
	 * 
	 * @param contextName The context of the given method, null if it is global method or a form name for a form method.
	 * @param dataprovider the data-provider name as seen in Servoy.
	 * @param value the value to set.
	 * @return the old value or null if no change.
	 */
	public Object js_setDataProviderValue(String contextName, String dataprovider, Object value)
	{
		return js_setDataProviderValue(contextName, dataprovider, value, null);
	}

	/**
	 * @clonedesc js_setDataProviderValue(String, String, Object)
	 * @sampleas js_setDataProviderValue(String, String, Object)
	 * 
	 * @param contextName The context of the given method, null if it is global method or a form name for a form method
	 * @param dataprovider the data-provider name as seen in Servoy
	 * @param value the value to set
	 * @param methodName if this is specified, the data-provider's value will only be set if the specified method is running in this headless client because the currently running client requested it to. Otherwise the value is not set into the data-provider and undefined is returned.
	 * @return the old value or null if no change
	 */
	public Object js_setDataProviderValue(String contextName, String dataprovider, Object value, String methodName)
	{
		Object retval = null;
		try
		{
			retval = headlessServer.setDataProviderValue(clientID, contextName, dataprovider, plugin.getJSONConverter().convertToJSON(value),
				plugin.getPluginAccess().getClientID(), methodName);
		}
		catch (RemoteException re)
		{
			Debug.error(re);
			throw new RuntimeException(re.getCause());
		}
		catch (Exception ex)
		{
			Debug.error(ex);
			throw new RuntimeException(ex);
		}
		if (UndefinedMarker.INSTANCE.equals(retval)) return Undefined.instance;
		try
		{
			return plugin.getJSONConverter().convertFromJSON(retval);
		}
		catch (Exception e)
		{
			Debug.error(e);
			return retval;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return "JSClient[" + clientID + "]";
	}

}
