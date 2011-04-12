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
package com.servoy.extensions.plugins.rest_ws;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import com.servoy.extensions.plugins.rest_ws.servlets.RestWSServlet;
import com.servoy.j2db.plugins.IServerAccess;
import com.servoy.j2db.plugins.IServerPlugin;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.preference.PreferencePanel;
import com.servoy.j2db.server.headlessclient.HeadlessClientFactory;
import com.servoy.j2db.server.headlessclient.IHeadlessClient;
import com.servoy.j2db.util.serialize.JSONSerializerWrapper;
import com.servoy.j2db.util.serialize.NativeObjectSerializer;

/**
 * Servoy Server plugin for the RESTfull webservices servlet.
 * <p>
 * Configuration:
 * <ul>
 * <li>rest_ws_plugin_client_pool_size, default 5
 * <li>rest_ws_plugin_client_pool_exhausted_action [block/fail/grow], default block
 * </ul>
 * 
 * @see RestWSServlet
 * 
 * @author rgansevles
 * 
 */
public class RestWSPlugin implements IServerPlugin
{
	private static final String CLIENT_POOL_SIZE_PROPERTY = "rest_ws_plugin_client_pool_size"; //$NON-NLS-1$
	private static final int CLIENT_POOL_SIZE_DEFAULT = 5;
	private static final String CLIENT_POOL_EXCHAUSTED_ACTION_PROPERTY = "rest_ws_plugin_client_pool_exhausted_action"; //$NON-NLS-1$
	private static final String ACTION_BLOCK = "block"; //$NON-NLS-1$
	private static final String ACTION_FAIL = "fail"; //$NON-NLS-1$
	private static final String ACTION_GROW = "grow"; //$NON-NLS-1$
	private static final String AUTHORIZED_GROUPS_PROPERTY = "rest_ws_plugin_authorized_groups"; //$NON-NLS-1$

	private static final String WEBSERVICE_NAME = "rest_ws"; //$NON-NLS-1$
	private static final String[] SOLUTION_OPEN_METHOD_ARGS = new String[] { "rest_ws_server" }; //$NON-NLS-1$

	public final Log log = LogFactory.getLog(RestWSPlugin.class);

	private JSONSerializerWrapper serializerWrapper;
	private GenericKeyedObjectPool clientPool = null;
	private IServerAccess application;


	public void initialize(IServerAccess app) throws PluginException
	{
		this.application = app;
		app.registerWebService(WEBSERVICE_NAME, new RestWSServlet(WEBSERVICE_NAME, this));
	}

	public PreferencePanel[] getPreferencePanels()
	{
		return null;
	}

	public Map<String, String> getRequiredPropertyNames()
	{
		Map<String, String> req = new HashMap<String, String>();
		req.put(CLIENT_POOL_SIZE_PROPERTY,
			"Max number of clients used (this defines the number of concurrent requests and licences used), default = " + CLIENT_POOL_SIZE_DEFAULT); //$NON-NLS-1$ 
		req.put(CLIENT_POOL_EXCHAUSTED_ACTION_PROPERTY, "The following values are supported for this property:\n" + //$NON-NLS-1$
			//
			ACTION_BLOCK +
			" (default): requests will wait untill a client becomes available\n" + //$NON-NLS-1$
			//
			ACTION_FAIL + ": the request will fail. The API will generate a SERVICE_UNAVAILABLE response (HTTP " + //$NON-NLS-1$
			HttpServletResponse.SC_SERVICE_UNAVAILABLE +
			")\n" + //$NON-NLS-1$
			//
			ACTION_GROW +
			": allows the pool to temporarily grow, by starting additional clients. These will be automatically removed when not required anymore."); //$NON-NLS-1$ 
		req.put(AUTHORIZED_GROUPS_PROPERTY,
			"Only authenticated users in the listed groups (comma-separated) have access, when left empty unauthorised access is allowed"); //$NON-NLS-1$ 
		return req;
	}

	public void load() throws PluginException
	{
	}

	public void unload() throws PluginException
	{
	}

	public Properties getProperties()
	{
		Properties props = new Properties();
		props.put(DISPLAY_NAME, "RESTful Web Services Plugin"); //$NON-NLS-1$
		return props;
	}

	public IServerAccess getServerAccess()
	{
		return application;
	}

	public JSONSerializerWrapper getJSONSerializer()
	{
		if (serializerWrapper == null)
		{
			serializerWrapper = new JSONSerializerWrapper(new NativeObjectSerializer(false, false, true), true);
		}
		return serializerWrapper;
	}

	public String[] getAuthorizedGroups()
	{
		String property = application.getSettings().getProperty(AUTHORIZED_GROUPS_PROPERTY);
		if (property == null || property.trim().length() == 0)
		{
			return null;
		}
		return property.split(","); //$NON-NLS-1$
	}

	synchronized KeyedObjectPool getClientPool()
	{
		if (clientPool == null)
		{
			int poolSize;
			try
			{
				poolSize = Integer.parseInt(application.getSettings().getProperty(CLIENT_POOL_SIZE_PROPERTY));
			}
			catch (NumberFormatException nfe)
			{
				poolSize = CLIENT_POOL_SIZE_DEFAULT;
			}
			String exchaustedActionCode = application.getSettings().getProperty(CLIENT_POOL_EXCHAUSTED_ACTION_PROPERTY);
			if (exchaustedActionCode != null) exchaustedActionCode = exchaustedActionCode.trim();
			byte exchaustedAction;
			if (ACTION_FAIL.equalsIgnoreCase(exchaustedActionCode))
			{
				exchaustedAction = GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL;
				log.debug("Client pool, exchaustedAction=" + ACTION_FAIL); //$NON-NLS-1$
			}
			else if (ACTION_GROW.equalsIgnoreCase(exchaustedActionCode))
			{
				exchaustedAction = GenericKeyedObjectPool.WHEN_EXHAUSTED_GROW;
				log.debug("Client pool, exchaustedAction=" + ACTION_GROW); //$NON-NLS-1$
			}
			else
			{
				exchaustedAction = GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK;
				log.debug("Client pool, exchaustedAction=" + ACTION_BLOCK); //$NON-NLS-1$
			}
			log.debug("Creating client pool, maxSize=" + poolSize); //$NON-NLS-1$
			clientPool = new GenericKeyedObjectPool(new BaseKeyedPoolableObjectFactory()
			{
				@Override
				public Object makeObject(Object key) throws Exception
				{
					log.debug("creating new session client for solution '" + key + '\''); //$NON-NLS-1$
					return HeadlessClientFactory.createHeadlessClient((String)key, SOLUTION_OPEN_METHOD_ARGS);
				}

				@Override
				public boolean validateObject(Object key, Object obj)
				{
					boolean valid = ((IHeadlessClient)obj).isValid();
					log.debug("Validated session client for solution '" + key + "', valid = " + valid); //$NON-NLS-1$ //$NON-NLS-2$
					return valid;
				}
			}, poolSize);
			clientPool.setTestOnBorrow(true);
			clientPool.setWhenExhaustedAction(exchaustedAction);
			clientPool.setMaxIdle(poolSize); // destroy objects when pool has grown
		}
		return clientPool;
	}

	public IHeadlessClient getClient(String solutionName) throws Exception
	{
		try
		{
			return (IHeadlessClient)getClientPool().borrowObject(solutionName);
		}
		catch (NoSuchElementException e)
		{
			// no more licenses
			throw new NoClientsException();
		}
	}

	public void releaseClient(String solutionName, IHeadlessClient client) throws Exception
	{
		getClientPool().returnObject(solutionName, client);
	}

	public static class NoClientsException extends Exception
	{
	}
	public static class NotAuthorizedException extends Exception
	{
		public NotAuthorizedException(String message)
		{
			super(message);
		}
	}
	public static class NotAuthenticatedException extends Exception
	{
		private final String realm;

		public NotAuthenticatedException(String realm)
		{
			this.realm = realm;
		}

		public String getRealm()
		{
			return realm;
		}
	}
}
