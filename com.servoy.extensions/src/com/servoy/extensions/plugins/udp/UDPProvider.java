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
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.mozilla.javascript.Function;

import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Debug;

@ServoyDocumented(publicName = UDPPlugin.PLUGIN_NAME, scriptingName = "plugins." + UDPPlugin.PLUGIN_NAME)
public class UDPProvider implements IScriptable, IReturnedTypesProvider
{
	private final UDPPlugin plugin;
	private DatagramHandler listner;
	private List buffer;
	private FunctionDefinition functionDef;
	private int port;
	private boolean hasSeenEmpty;//special flag to prevent starting while lastone is processing last packet

	UDPProvider(UDPPlugin plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * Start a UDP socket for a port.
	 *
	 * @sample
	 * plugins.udp.startSocket(1234,my_packet_process_method)
	 *
	 * @param port_number 
	 * @param method_to_call_when_packet_received_and_buffer_is_empty 
	 */
	public boolean js_startSocket(int port_number, Object method_to_call_when_packet_received_and_buffer_is_empty)
	{
		//clear if restart
		hasSeenEmpty = true;
		buffer = Collections.synchronizedList(new LinkedList());

		this.port = port_number;
		if (listner == null)
		{
			try
			{
				if (!(method_to_call_when_packet_received_and_buffer_is_empty instanceof Function)) throw new IllegalArgumentException("method invalid"); //$NON-NLS-1$

				DatagramSocket socket = new DatagramSocket(port_number);
				listner = new DatagramHandler(this, socket);
				listner.start();

				functionDef = new FunctionDefinition((Function)method_to_call_when_packet_received_and_buffer_is_empty);
				return true;
			}
			catch (SocketException e)
			{
				Debug.error(e);
			}
		}
		return false;
	}

	/**
	 * Stop the UDP socket for a port.
	 *
	 * @sample
	 * plugins.udp.stopSocket()
	 */
	public void js_stopSocket()
	{
		if (listner != null) listner.setListen(false);
		listner = null;
	}

	/**
	 * Create a new empty packet.
	 *
	 * @sample
	 * var packet = plugins.udp.createNewPacket()
	 * packet.writeUTF('hello world!')//writes UTF
	 * packet.writeInt(12348293)//writes 4 bytes
	 * packet.writeShort(14823)//writes 2 bytes
	 * packet.writeByte(123)//writes 1 byte
	 */
	public JSPacket js_createNewPacket()
	{
		return new JSPacket();
	}

	/**
	 * Send a packet.
	 *
	 * @sample
	 * var packet = plugins.udp.createNewPacket()
	 * packet.writeUTF('hello world!')
	 * plugins.udp.sendPacket('10.0.0.1',packet)
	 *
	 * @param hostAddress
	 * @param packet 
	 */
	public boolean js_sendPacket(String destIpOrHostname, JSPacket packet)
	{
		return js_sendPacket(destIpOrHostname, packet, port);
	}

	/**
	 * Send a packet on another port.
	 *
	 * @sample
	 * var packet = plugins.udp.createNewPacket()
	 * packet.writeUTF('hello world!')
	 * plugins.udp.sendPacket('10.0.0.1',packet, 4321)
	 *
	 * @param hostAddress
	 * @param packet 
	 * @param port
	 */
	public boolean js_sendPacket(String destIpOrHostname, JSPacket packet, int aport)
	{
		if (destIpOrHostname != null && packet != null)
		{
			try
			{
				InetAddress ip = InetAddress.getByName(destIpOrHostname.toString());
				if (ip != null && listner != null)
				{
					DatagramPacket dp = packet.getRealPacket();
					dp.setAddress(ip);
					dp.setPort(aport);
					return listner.send(dp);
				}
			}
			catch (UnknownHostException e)
			{
				Debug.error(e);
			}
		}
		return false;
	}

	/**
	 * Put a test packet in the receive buffer to test your method call and getReceivedPacket.
	 *
	 * @sample
	 * var packet = plugins.udp.createNewPacket()
	 * packet.writeUTF('hello world!')
	 * plugins.udp.testPacket(packet)
	 *
	 * @param packet 
	 */
	public boolean js_testPacket(JSPacket packet)
	{
		if (packet != null)
		{
			addPacket(packet.getRealPacket());
		}
		return false;
	}

	/**
	 * @deprecated Replaced by {@link #getReceivedPacket()}.
	 */
	@Deprecated
	public JSPacket js_getRecievedPacket()
	{
		return js_getReceivedPacket();
	}

	/**
	 * Get a packet from receive buffer, read buffer until empty (null is returned).
	 *
	 * @sample
	 * var packet = null
	 * while( ( packet = plugins.udp.getReceivedPacket() ) != null)
	 * {
	 * 	var text = packet.readUTF()
	 * 	var count = packet.readInt()
	 * }
	 */
	public JSPacket js_getReceivedPacket()
	{
		if (buffer.size() > 0)
		{
			return (JSPacket)buffer.remove(0);
		}
		else
		{
			hasSeenEmpty = true;
			return null;
		}
	}

	void addPacket(DatagramPacket dp)
	{
		buffer.add(new JSPacket(dp));

		if (hasSeenEmpty)
		{
			hasSeenEmpty = false;
			if (functionDef != null)
			{
				functionDef.executeAsync(plugin.getClientPluginAccess(), null);
			}
		}
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { JSPacket.class };
	}
}
