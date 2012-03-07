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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;

import com.servoy.j2db.plugins.IServerAccess;
import com.servoy.j2db.plugins.IServerPlugin;
import com.servoy.j2db.preference.PreferencePanel;
import com.servoy.j2db.server.headlessclient.HeadlessClientFactory;
import com.servoy.j2db.server.headlessclient.IHeadlessClient;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.UUID;
import com.servoy.j2db.util.serialize.JSONConverter;

@SuppressWarnings("nls")
public class HeadlessServerPlugin implements IHeadlessServer, IServerPlugin
{
	private final Map<String, WeakReference<IHeadlessClient>> clients = new ConcurrentHashMap<String, WeakReference<IHeadlessClient>>();
	private final Map<String, MethodCall> methodCalls = new ConcurrentHashMap<String, MethodCall>();

	private final JSONConverter jsonConverter = new JSONConverter();
	private IServerAccess application;

	public HeadlessServerPlugin()//must have default constructor
	{
	}

	public Properties getProperties()
	{
		Properties props = new Properties();
		props.put(DISPLAY_NAME, "HeadlessServerPlugin");
		return props;
	}

	public void load()
	{
	}

	public void initialize(IServerAccess app)
	{
		this.application = app;
		try
		{
			app.registerRMIService(IHeadlessServer.SERVICE_NAME, this);
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
	}

	public void unload()
	{
	}

	public Map<String, String> getRequiredPropertyNames()
	{
		return Collections.emptyMap();
	}

	public PreferencePanel[] getPreferencePanels()
	{
		return null;
	}

	public String createClient(String solutionname, String username, String password, Object[] solutionOpenMethodArgs, String callingClientId) throws Exception
	{
		if (!application.isServerProcess(callingClientId) && !application.isAuthenticated(callingClientId))
		{
			throw new SecurityException("Rejected unauthenticated access");
		}
		Iterator<Entry<String, WeakReference<IHeadlessClient>>> clientsIterator = clients.entrySet().iterator();
		while (clientsIterator.hasNext())
		{
			Entry<String, WeakReference<IHeadlessClient>> entry = clientsIterator.next();
			if (entry.getValue().get() == null)
			{
				clientsIterator.remove();
			}
		}
		IHeadlessClient c = HeadlessClientFactory.createHeadlessClient(solutionname, username, password, solutionOpenMethodArgs);
		WeakReference<IHeadlessClient> clientRef = new WeakReference<IHeadlessClient>(c);
		String newClientKey = UUID.randomUUID().toString();
		clients.put(newClientKey, clientRef);
		return newClientKey;
	}

	private IHeadlessClient getClient(String clientKey) throws ClientNotFoundException
	{
		WeakReference<IHeadlessClient> clientRef = clients.get(clientKey);
		if (clientRef != null)
		{
			IHeadlessClient c = clientRef.get();
			if (c != null && c.isValid())
			{
				return c;
			}
		}
		throw new ClientNotFoundException(clientKey);
	}

	public Object executeMethod(String clientKey, String contextName, String methodName, Object[] args, String callingClientId) throws Exception
	{
		MethodCall call = new MethodCall(callingClientId, methodName);

		synchronized (methodCalls)
		{
			while (methodCalls.containsKey(clientKey))
				methodCalls.wait();
			methodCalls.put(clientKey, call);
		}
		try
		{
			Object[] convertedArgs = null;
			if (args != null)
			{
				convertedArgs = new Object[args.length];
				for (int i = 0; i < args.length; i++)
				{
					convertedArgs[i] = getJSONConverter().convertFromJSON(args[i]);
				}
			}
			IHeadlessClient c = getClient(clientKey);
			return getJSONConverter().convertToJSON(c.getPluginAccess().executeMethod(contextName, methodName, convertedArgs, false));
		}
		catch (JavaScriptException jse)
		{
			throw new Exception(getJSONConverter().convertToJSON(jse.getValue()));
		}
		catch (RhinoException e)
		{
			// wrap it in a normal exception, else serializeable exceptions will happen.
			throw new Exception(getJSONConverter().convertToJSON(e.details()));
		}
		finally
		{
			synchronized (methodCalls)
			{
				methodCalls.remove(clientKey);
				methodCalls.notifyAll();
			}
		}
	}

	private JSONConverter getJSONConverter()
	{
		return jsonConverter;
	}

	public Object getDataProviderValue(String clientKey, String contextName, String dataprovider, String callingClientId, String methodName)
	{
		if (methodName != null)
		{
			synchronized (methodCalls)
			{
				MethodCall methodCall = methodCalls.get(clientKey);
				if (methodCall == null || !(methodCall.callingClientId.equals(callingClientId) && methodCall.methodName.equals(methodName)))
				{
					return UndefinedMarker.INSTANCE;
				}
			}
		}
		IHeadlessClient c = getClient(clientKey);
		Object dataProviderValue = c.getDataProviderValue(contextName, dataprovider);
		try
		{
			return getJSONConverter().convertToJSON(dataProviderValue);
		}
		catch (Exception e)
		{
			throw new RuntimeException("exception when serializing value " + dataProviderValue, e);
		}
	}

	public boolean isValid(String clientKey)
	{
		try
		{
			IHeadlessClient c = getClient(clientKey);
			return c.isValid();
		}
		catch (RuntimeException re)
		{
			Debug.trace("client not found for " + clientKey, re);
		}
		return false;
	}

	public Object setDataProviderValue(String clientKey, String contextName, String dataprovider, Object value, String callingClientId, String methodName)
	{
		if (methodName != null)
		{
			synchronized (methodCalls)
			{
				MethodCall methodCall = methodCalls.get(clientKey);
				if (methodCall == null || !(methodCall.callingClientId.equals(callingClientId) && methodCall.methodName.equals(methodName)))
				{
					return UndefinedMarker.INSTANCE;
				}
			}
		}
		IHeadlessClient c = getClient(clientKey);
		Object retValue;
		try
		{
			retValue = c.setDataProviderValue(contextName, dataprovider, getJSONConverter().convertFromJSON(value));
		}
		catch (Exception e)
		{
			throw new RuntimeException("exception when deserializing value " + value, e);
		}

		try
		{
			return getJSONConverter().convertToJSON(retValue);
		}
		catch (Exception e)
		{
			throw new RuntimeException("exception when serializing value " + retValue, e);
		}

	}

	public void shutDown(String clientKey, boolean force)
	{
		IHeadlessClient c = getClient(clientKey);
		try
		{
			c.shutDown(force);
		}
		finally
		{
			clients.remove(clientKey);
		}
	}

	private static class MethodCall
	{

		private final String callingClientId;
		private final String methodName;

		/**
		 * @param callingClientId
		 * @param methodName
		 */
		public MethodCall(String callingClientId, String methodName)
		{
			this.callingClientId = callingClientId;
			this.methodName = methodName;
		}

	}

}
