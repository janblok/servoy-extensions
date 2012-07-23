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

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IHeadlessServer extends Remote
{
	public static final String SERVICE_NAME = "servoy.IHeadlessServer"; //$NON-NLS-1$


	public String getOrCreateClient(String clientKey, String solutionname, String username, String password, Object[] solutionOpenMethodArgs,
		String callingClientId) throws Exception, RemoteException;

	public String createClient(String solutionname, String username, String password, Object[] solutionOpenMethodArgs, String callingClientId)
		throws Exception, RemoteException;

	/**
	 * 
	 * @param args this is an array of serialized-through-JSON objects.
	 * @return a serialized-through-JSON (so actually a String) object.
	 */
	public Object executeMethod(String clientKey, String contextName, String methodName, String[] args, String callingClientId) throws Exception,
		RemoteException;

	public boolean isValid(String clientKey) throws RemoteException;

	/**
	 * 
	 * @return  a serialized-through-JSON (so actually a String) object or UndefinedMarker.INSTANCE.
	 */
	public Object getDataProviderValue(String clientKey, String contextName, String dataprovider, String callingClientId, String methodName)
		throws RemoteException;

	/**
	 * 
	 * @param value this is a serialized-through-JSON object.
	 * @return a serialized-through-JSON (so actually a String) object or UndefinedMarker.INSTANCE.
	 */
	public Object setDataProviderValue(String clientKey, String contextName, String dataprovider, String value, String callingClientId, String methodName)
		throws RemoteException;

	public void shutDown(String clientKey, boolean force) throws RemoteException;
}
