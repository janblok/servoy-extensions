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

import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Debug;

@ServoyDocumented
public class HeadlessClientProvider implements IScriptable, IReturnedTypesProvider
{
	private final HeadlessClientPlugin plugin;
	private IHeadlessServer headlessServer = null;

	HeadlessClientProvider(HeadlessClientPlugin plugin)
	{
		this.plugin = plugin;
	}

	private void createService()
	{
		if (headlessServer == null)
		{
			try
			{
				IClientPluginAccess access = plugin.getPluginAccess();
				headlessServer = (IHeadlessServer)access.getServerService(IHeadlessServer.SERVICE_NAME);
			}
			catch (Exception ex)
			{
				Debug.error(ex);
			}
		}
	}

	/**
	 * Creates a headless client that will open the given solution.
	 *
	 * @sample
	 * // Creates a headless client that will open the given solution.
	 * var headlessClient = plugins.headlessclient.createClient("someSolution", "user", "pass", null);
	 * if (headlessClient != null && headlessClient.isValid()) { 
	 * 	 var x = new Object();
	 * 	 x.name = 'remote1';
	 * 	 x.number = 10;
	 * headlessClient.queueMethod(null, "remoteMethod", [x], callback);
	 * }
	 *
	 * @param solutionName 
	 * @param username 
	 * @param password 
	 * @param solutionOpenMethodArgs 
	 */
	public JSClient js_createClient(String solutionName, String username, String password, Object[] solutionOpenMethodArgs)
	{
		//create if not yet created
		createService();

		try
		{
			String clientID = headlessServer.createClient(solutionName, username, password, solutionOpenMethodArgs);
			if (clientID != null)
			{
				return new JSClient(clientID, headlessServer, plugin);
			}
		}
		catch (Exception ex)
		{
			Debug.error(ex);
		}
		return null;
	}

	/**
	 * Gets an existing headless client for the given client uuid.
	 *
	 * @sample
	 * // Gets an existing headless client for the given client uuid.
	 * var headlessClient = plugins.headlessclient.getClient("clientID");
	 * if (headlessClient != null && headlessClient.isValid()) {
	 * 	 headlessClient.queueMethod(null, "someRemoteMethod", null, callback);
	 * }
	 *
	 * @param clientID 
	 */
	public JSClient js_getClient(String clientID)
	{
		//create if not yet created
		createService();

		try
		{
			if (headlessServer.isValid(clientID))
			{
				return new JSClient(clientID, headlessServer, plugin);
			}
		}
		catch (Exception ex)
		{
			Debug.error(ex);
		}
		return null;
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { JSClient.class };
	}
}
