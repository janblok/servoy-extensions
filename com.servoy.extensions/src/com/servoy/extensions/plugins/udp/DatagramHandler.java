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
package com.servoy.extensions.plugins.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import com.servoy.j2db.util.Debug;

public class DatagramHandler extends Thread
{
	private boolean listen = true;
	private UDPProvider provider;
	private DatagramSocket socket;
	
	DatagramHandler(UDPProvider provider, DatagramSocket socket)
	{
		this.provider = provider;
		this.socket = socket;
	}
	
	public void run()
	{
		while(listen)
		{
			try
			{
				DatagramPacket dp = new DatagramPacket(new byte[JSPacket.MAX_PACKET_LENGTH], JSPacket.MAX_PACKET_LENGTH);
				socket.receive(dp);
				provider.addPacket(dp);
			}
			catch (Throwable e)
			{
				Debug.error(e);
			}
		}
	}

	void setListen(boolean listen)
	{
		this.listen = listen;
		try
		{
			socket.close();
		}
		catch (Throwable e)
		{
			Debug.error(e);
		}
		socket = null;
		provider = null;
	}

	boolean send(DatagramPacket dp)
	{
		try
		{
			socket.send(dp);
			return true;
		}
		catch (Throwable e)
		{
			Debug.error(e);
		}
		return false;
	}
}
