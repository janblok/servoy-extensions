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

import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IConstantsObject;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.scripting.JSEvent;
import com.servoy.j2db.util.Debug;

public class JSClient implements IScriptObject, IConstantsObject
{
	private final IHeadlessServer headlessServer;
	private final HeadlessClientPlugin plugin;
	private final String clientID;

	public static final String CALLBACK_EVENT = "headlessCallback"; //$NON-NLS-1$

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
	 * @param contextName The context of the given method, null if it is global method or a form name for a form method
	 * @param methodName The method name
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
						Object[] convertedArgs = null;
						if (args != null)
						{
							convertedArgs = new Object[args.length];

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
						functionDef.executeAsync(plugin.getPluginAccess(), new Object[] { event, JSClient.this  });
					}
					catch (Exception ex)
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
						functionDef.executeAsync(plugin.getPluginAccess(), new Object[] { event, JSClient.this  });
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

	public void js_shutdown()
	{
		js_shutdown(false);
	}

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
	 * Get a dataprovider value.
	 * 
	 * @param contextName The context of the given method, null if it is global method or a form name for a form method
	 * @param dataprovider the dataprovider name as seen in Servoy
	 * @return the value for the dataprovider
	 */
	public Object js_getDataProviderValue(String contextName, String dataprovider)
	{
		return js_getDataProviderValue(contextName, dataprovider, null);
	}

	/**
	 * Get a dataprovider value.
	 * 
	 * @param contextName The context of the given method, null if it is global method or a form name for a form method
	 * @param dataprovider the dataprovider name as seen in Servoy
	 * @param methodName the methodname that should be running now for this client, if not then undefined is returned.
	 * @return the value for the dataprovider
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
	 * Set a dataprovider value.
	 * 
	 * @param contextName The context of the given method, null if it is global method or a form name for a form method
	 * @param dataprovider the dataprovider name as seen in Servoy
	 * @param value to set
	 * @return the old value or null if no change
	 */
	public Object js_setDataProviderValue(String contextName, String dataprovider, Object value)
	{
		return js_setDataProviderValue(contextName, dataprovider, value, null);
	}

	/**
	 * Set a dataprovider value.
	 * 
	 * @param contextName The context of the given method, null if it is global method or a form name for a form method
	 * @param dataprovider the dataprovider name as seen in Servoy
	 * @param value to set
	 * @param methodName the methodname that should be running now for this client, if not then value wont be set and undefined is returned.
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

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getParameterNames(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String[] getParameterNames(String methodName)
	{
		if ("getDataProviderValue".equals(methodName))
		{
			return new String[] { "context", "variableName", "[currentMethodName]" };
		}
		if ("setDataProviderValue".equals(methodName))
		{
			return new String[] { "context", "variableName", "value", "[currentMethodName]" };
		}
		if ("queueMethod".equals(methodName))
		{
			return new String[] { "context", "methodName", "args", "callbackFunction" };
		}
		if ("shutdown".equals(methodName))
		{
			return new String[] { "force" };
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getSample(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String getSample(String methodName)
	{
		if ("getDataProviderValue".equals(methodName) || "setDataProviderValue".equals(methodName))
		{
			return "if (jsclient && jsclient.isValid())\n{\n\t// only gets the globals.media when the 'remoteMethod' is currently executing for this client\n\tvar value = jsclient.getDataProviderValue(null, \"globals.number\", 'remoteMethod');\n\tif (value != null)\n\t{\n\t\tapplication.output(\"value get from globals.number :: \"+ value);\n\t\tglobals.value = value+10;\n\t\tvar returnValue = jsclient.setDataProviderValue(null, \"globals.number\", globals.value, 'remoteMethod');\n\t\tapplication.output(\"value set to globals.number previous value \"+ returnValue);\n\t}\n\telse\n\t{\n\t\tapplication.output(\"value get from globals.number :: \" + null);\n\t}\n}";
		}
		else
		{
			return "if (jsclient && jsclient.isValid())\n{\n\t/*Queue a method where the callback can do something like this\n\tif (event.getType() == JSClient.CALLBACK_EVENT)\n\t{\n\t\tapplication.output(\"callback data, name: \" + event.data);\n\t}\n\telse if (event.getType() == JSClient.CALLBACK_EXCEPTION_EVENT)\n\t{\n\t\tapplication.output(\"exception callback, name: \" + event.data);\n\t}*/\n\tvar x = new Object();\n\tx.name = 'remote1';\n\tx.number = 10;\n\t// this calls a 'remoteMethod' on the server as a global method, because the context (first argument is set to null), you can use a formname to call a form method\n\tjsclient.queueMethod(null, \"remoteMethod\", [x], callback);\n}";
		}
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String getToolTip(String methodName)
	{
		if ("getDataProviderValue".equals(methodName))
		{
			return "get a dataprovider value from the client";
		}
		if ("setDataProviderValue".equals(methodName))
		{
			return "set a dataprovider value on the client";
		}
		if ("queueMethod".equals(methodName))
		{
			return "queue a method on the client, calling the method name specified on the context, the callback method will get a JSEvent as the first and a JSClient (the this of the client that did the call) as the second parameter ";
		}
		if ("shutDown".equals(methodName))
		{
			return "closes the client";
		}
		if ("getClientID".equals(methodName))
		{
			return "gets the id of the client";
		}
		if ("CALLBACK_EVENT".equals(methodName))
		{
			return "Constant that is returned as a JSEvent type when in the callback method when it executed normally";
		}
		if ("CALLBACK_EXCEPTION_EVENT".equals(methodName))
		{
			return "Constant that is returned as a JSEvent type when in the callback method when an exception occured";
		}
		if ("isValid".equals(methodName))
		{
			return "returns true if this client is still valid/usable";
		}
		return methodName;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#isDeprecated(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public boolean isDeprecated(String methodName)
	{
		return "shutDown".equals(methodName);
	}

	/**
	 * @see com.servoy.j2db.scripting.IReturnedTypesProvider#getAllReturnedTypes()
	 */
	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}
}
