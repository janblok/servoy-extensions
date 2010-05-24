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

import com.servoy.j2db.scripting.FunctionDefinition;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

public class UDPProvider implements IScriptObject
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
	 * Start a UDP socket for a port
	 *
	 * @sample plugins.udp.startSocket(1234,my_packet_process_method)
	 *
	 * @param portnumber 
	 *
	 * @param methodToInvokeWhenPacketReceivedAndBufferIsEmpty 
	 */
	public boolean js_startSocket(int port, Object method_to_call_when_packet_received_and_buffer_is_empty)
	{
		//clear if restart
		hasSeenEmpty = true;
		buffer = Collections.synchronizedList(new LinkedList());

		this.port = port;
		if (listner == null)
		{
			try
			{
				if (!(method_to_call_when_packet_received_and_buffer_is_empty instanceof Function)) throw new IllegalArgumentException("method invalid"); //$NON-NLS-1$

				DatagramSocket socket = new DatagramSocket(port);
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
	 * Stop the UDP socket for a port
	 *
	 * @sample plugins.udp.stopSocket()
	 */
	public void js_stopSocket()
	{
		if (listner != null) listner.setListen(false);
		listner = null;
	}

	/**
	 * Create a new empty packet
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
	 * Send a packet
	 *
	 * @sample
	 * var packet = plugins.udp.createNewPacket()
	 * packet.writeUTF('hello world!')
	 * plugins.udp.sendPacket('10.0.0.1',packet)
	 *
	 * @param ip/host 
	 *
	 * @param packet 
	 *
	 * @param [port] optional 
	 */
	public boolean js_sendPacket(Object[] vargs)
	{
		Object dest_ip = null;
		Object packet = null;
		int aport = port;
		if (vargs != null && vargs.length > 0 && vargs[0] != null)
		{
			dest_ip = vargs[0];
		}
		if (vargs != null && vargs.length > 1 && vargs[1] != null)
		{
			packet = vargs[1];
		}
		if (vargs != null && vargs.length > 2 && vargs[2] != null)
		{
			int other_port = Utils.getAsInteger(vargs[2]);
			if (other_port > 0) aport = other_port;
		}
		if (dest_ip instanceof String && packet instanceof JSPacket)
		{
			try
			{
				InetAddress ip = InetAddress.getByName(dest_ip.toString());
				if (ip != null && listner != null)
				{
					DatagramPacket dp = ((JSPacket)packet).getRealPacket();
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
	 * Put a test packet in the receive buffer to test your method call and getReceivedPacket
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
	 * @deprecated
	 */
	@Deprecated
	public JSPacket js_getRecievedPacket()
	{
		return js_getReceivedPacket();
	}

	/**
	 * Get a packet from recieve buffer, read buffer until empty (null is returned)
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
				functionDef.execute(plugin.getClientPluginAccess(), null, true);
			}
		}
	}

	public String[] getParameterNames(String method)
	{
		if ("startSocket".equals(method)) //$NON-NLS-1$
		{
			return new String[] { "portnumber", "methodToInvokeWhenPacketReceivedAndBufferIsEmpty" }; //$NON-NLS-1$//$NON-NLS-2$
		}
		else if (method.equals("sendPacket")) //$NON-NLS-1$
		{
			return new String[] { "ip/host", "packet", "[port]" }; //$NON-NLS-1$//$NON-NLS-2$
		}
		else if (method.equals("testPacket")) //$NON-NLS-1$
		{
			return new String[] { "packet" }; //$NON-NLS-1$
		}
		return null;
	}

	public boolean isDeprecated(String method)
	{
		if ("getRecievedPacket".equals(method)) //$NON-NLS-1$
		{
			return true;
		}
		return false;
	}

	public String getSample(String method)
	{
		if ("startSocket".equals(method)) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(method));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.udp.startSocket(1234,my_packet_process_method)\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (method.equals("sendPacket")) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(method));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var packet = plugins.udp.createNewPacket()\n"); //$NON-NLS-1$
			retval.append("packet.writeUTF('hello world!')\n"); //$NON-NLS-1$
			retval.append("plugins.udp.sendPacket('10.0.0.1',packet)\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (method.equals("testPacket")) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(method));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var packet = plugins.udp.createNewPacket()\n"); //$NON-NLS-1$
			retval.append("packet.writeUTF('hello world!')\n"); //$NON-NLS-1$
			retval.append("plugins.udp.testPacket(packet)\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (method.equals("createNewPacket")) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(method));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var packet = plugins.udp.createNewPacket()\n"); //$NON-NLS-1$
			retval.append("packet.writeUTF('hello world!')//writes UTF\n"); //$NON-NLS-1$
			retval.append("packet.writeInt(12348293)//writes 4 bytes\n"); //$NON-NLS-1$
			retval.append("packet.writeShort(14823)//writes 2 bytes\n"); //$NON-NLS-1$
			retval.append("packet.writeByte(123)//writes 1 byte\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (method.equals("stopSocket")) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(method));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("plugins.udp.stopSocket()\n"); //$NON-NLS-1$
			return retval.toString();
		}
		else if (method.equals("getReceivedPacket")) //$NON-NLS-1$
		{
			StringBuffer retval = new StringBuffer();
			retval.append("//"); //$NON-NLS-1$
			retval.append(getToolTip(method));
			retval.append("\n"); //$NON-NLS-1$
			retval.append("var packet = null\n"); //$NON-NLS-1$
			retval.append("while( ( packet = plugins.udp.getReceivedPacket() ) != null)\n"); //$NON-NLS-1$
			retval.append("{\n"); //$NON-NLS-1$
			retval.append("\tvar text = packet.readUTF()\n"); //$NON-NLS-1$
			retval.append("\tvar count = packet.readInt()\n"); //$NON-NLS-1$
			retval.append("}\n"); //$NON-NLS-1$
			return retval.toString();
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(String)
	 */
	public String getToolTip(String method)
	{
		if ("startSocket".equals(method)) //$NON-NLS-1$
		{
			return "Start a UDP socket for a port."; //$NON-NLS-1$
		}
		else if (method.equals("sendPacket")) //$NON-NLS-1$
		{
			return "Send a packet."; //$NON-NLS-1$
		}
		else if (method.equals("testPacket")) //$NON-NLS-1$
		{
			return "Put a test packet in the receive buffer to test your method call and getReceivedPacket."; //$NON-NLS-1$
		}
		else if (method.equals("createNewPacket")) //$NON-NLS-1$
		{
			return "Create a new empty packet."; //$NON-NLS-1$
		}
		else if (method.equals("stopSocket")) //$NON-NLS-1$
		{
			return "Stop the UDP socket for a port."; //$NON-NLS-1$
		}
		else if (method.equals("getReceivedPacket")) //$NON-NLS-1$
		{
			return "Get a packet from recieve buffer, read buffer until empty (null is returned)."; //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class[] getAllReturnedTypes()
	{
		return new Class[] { JSPacket.class };
	}
}
