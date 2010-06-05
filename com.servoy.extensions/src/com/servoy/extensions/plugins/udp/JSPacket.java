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

import java.io.UTFDataFormatException;
import java.net.DatagramPacket;

import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

public class JSPacket implements IScriptObject
{
	static final int MAX_PACKET_LENGTH = 64000;
	private final DatagramPacket enclosed;
	private int index = 0;
	private int writtenBytes = 0;

	public JSPacket()
	{
		enclosed = new DatagramPacket(new byte[MAX_PACKET_LENGTH], MAX_PACKET_LENGTH);
	}

	public JSPacket(DatagramPacket dp)
	{
		enclosed = dp;
		writtenBytes = dp.getLength();
	}

	public int js_getIndex()
	{
		return getIndex();
	}

	public void js_setIndex(int i)
	{
		setIndex(i);
	}

	public String js_getHost()
	{
		return getHost();
	}

	public byte[] js_getByteArray()
	{
		return getByteArray();
	}

	public int js_getLength()
	{
		return getLength();
	}

	public int js_getPort()
	{
		return getPort();
	}

	public int js_readByte()
	{
		return readByte();
	}

	public int js_readInt()
	{
		return readInt();
	}

	public int js_readShort()
	{
		return readShort();
	}

	public String js_readUTF(Object[] vargs)
	{
		return readUTF(vargs);
	}

	public void js_writeByte(int v)
	{
		writeByte(v);
	}

	public void js_writeBytes(byte[] bytes)
	{
		writeBytes(bytes);
	}

	public void js_writeInt(int v)
	{
		writeInt(v);
	}

	public void js_writeShort(int v)
	{
		writeShort(v);
	}

	public int js_writeUTF(String str)
	{
		return writeUTF(str);
	}

	DatagramPacket getRealPacket()
	{
		enclosed.setLength(index);
		return enclosed;
	}

	public void writeInt(int v)
	{
		byte[] buffer = enclosed.getData();
		buffer[index++] = (byte)(v >>> 24);
		buffer[index++] = (byte)(v >>> 16);
		buffer[index++] = (byte)(v >>> 8);
		buffer[index++] = (byte)(v >>> 0);
		if (index > writtenBytes) writtenBytes = index;
	}

	public int readInt()
	{
		byte[] buffer = enclosed.getData();
		return (((buffer[index++] & 0xFF) << 24) + ((buffer[index++] & 0xFF) << 16) + ((buffer[index++] & 0xFF) << 8) + ((buffer[index++] & 0xFF) << 0));
	}

	public byte[] getByteArray()
	{
		byte[] retval = new byte[writtenBytes];
		System.arraycopy(enclosed.getData(), 0, retval, 0, writtenBytes);
		return retval;
	}

	public void writeBytes(byte[] bytes)
	{
		byte[] buffer = enclosed.getData();
		if (bytes == null || (index + bytes.length) > buffer.length) return; //safety

		System.arraycopy(bytes, 0, buffer, index, bytes.length);
		index += bytes.length;
		if (index > writtenBytes) writtenBytes = index;
	}

	public void writeByte(int v)
	{
		byte[] buffer = enclosed.getData();
		buffer[index++] = (byte)(v >>> 0);
		if (index > writtenBytes) writtenBytes = index;
	}

	public int readByte()
	{
		byte[] buffer = enclosed.getData();
		return ((buffer[index++] & 0xFF) << 0);
	}

	public int readShort()
	{
		byte[] buffer = enclosed.getData();
		return ((buffer[index++] & 0xFF) << 8) + ((buffer[index++] & 0xFF) << 0);
	}

	public void writeShort(int v)
	{
		byte[] buffer = enclosed.getData();
		buffer[index++] = (byte)(v >>> 8);
		buffer[index++] = (byte)(v >>> 0);
		if (index > writtenBytes) writtenBytes = index;
	}

	public int writeUTF(String str)
	{
		int strlen = str.length();
		int utflen = 0;
		char[] charr = new char[strlen];
		int c = 0;
		str.getChars(0, strlen, charr, 0);
		for (int i = 0; i < strlen; i++)
		{
			c = charr[i];
			if ((c >= 0x0001) && (c <= 0x007F))
			{
				utflen++;
			}
			else if (c > 0x07FF)
			{
				utflen += 3;
			}
			else
			{
				utflen += 2;
			}
		}

		byte[] bytearr = enclosed.getData();
		if (utflen + index >= bytearr.length)
		{
			return 0;
		}

		writeInt(utflen);

		for (int i = 0; i < strlen; i++)
		{
			c = charr[i];
			if ((c >= 0x0001) && (c <= 0x007F))
			{
				bytearr[index++] = (byte)c;
			}
			else if (c > 0x07FF)
			{
				bytearr[index++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
				bytearr[index++] = (byte)(0x80 | ((c >> 6) & 0x3F));
				bytearr[index++] = (byte)(0x80 | ((c >> 0) & 0x3F));
			}
			else
			{
				bytearr[index++] = (byte)(0xC0 | ((c >> 6) & 0x1F));
				bytearr[index++] = (byte)(0x80 | ((c >> 0) & 0x3F));
			}
		}
		if (index > writtenBytes) writtenBytes = index;
		return utflen + 2;
	}

	public String readUTF(Object[] vargs)
	{
		try
		{
			int utflen = 0;
			if (vargs != null && vargs.length != 0 && vargs[0] != null)
			{
				utflen = Utils.getAsInteger(vargs[0]);
			}
			else
			{
				utflen = readInt();//in.readUnsignedShort();
			}

			if (utflen > 0 && utflen < enclosed.getLength())
			{
				byte[] bytearr = enclosed.getData();
				char[] chararr = new char[utflen];

				int c, char2, char3;
				int count = index;
				int chararr_count = 0;
				//		in.readFully(bytearr, 0, utflen);
				while (count < utflen + index)
				{
					c = bytearr[count] & 0xff;
					if (c > 127) break;
					count++;
					chararr[chararr_count++] = (char)c;
				}
				while (count < utflen + index)
				{
					c = bytearr[count] & 0xff;
					switch (c >> 4)
					{
						case 0 :
						case 1 :
						case 2 :
						case 3 :
						case 4 :
						case 5 :
						case 6 :
						case 7 :
							/* 0xxxxxxx */
							count++;
							chararr[chararr_count++] = (char)c;
							break;
						case 12 :
						case 13 :
							/* 110x xxxx 10xx xxxx */
							count += 2;
							if (count - index > utflen) throw new UTFDataFormatException("malformed input: partial character at end"); //$NON-NLS-1$
							char2 = bytearr[count - 1];
							if ((char2 & 0xC0) != 0x80) throw new UTFDataFormatException("malformed input around byte " + count); //$NON-NLS-1$
							chararr[chararr_count++] = (char)(((c & 0x1F) << 6) | (char2 & 0x3F));
							break;
						case 14 :
							/* 1110 xxxx 10xx xxxx 10xx xxxx */
							count += 3;
							if (count > utflen) throw new UTFDataFormatException("malformed input: partial character at end"); //$NON-NLS-1$
							char2 = bytearr[count - 2];
							char3 = bytearr[count - 1];
							if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) throw new UTFDataFormatException(
								"malformed input around byte " + (count - 1)); //$NON-NLS-1$
							chararr[chararr_count++] = (char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
							break;
						default :
							/* 10xx xxxx, 1111 xxxx */
							throw new UTFDataFormatException("malformed input around byte " + count); //$NON-NLS-1$
					}
				}
				index += utflen;
				// The number of chars produced may be less than utflen
				return new String(chararr, 0, chararr_count).trim();
			}
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return null;
	}

	public int getLength()
	{
		return writtenBytes;
	}

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int i)
	{
		if (i < writtenBytes)
		{
			index = i;
		}

		int buflen = enclosed.getData().length;
		if (index > buflen) //safety
		{
			index = buflen;
		}
	}

	public String getHost()
	{
		return enclosed.getAddress().toString().substring(1);
	}

	public int getPort()
	{
		return enclosed.getPort();
	}

	public static void main(String[] args)
	{
		JSPacket p = new JSPacket();
		System.out.println(p.index);
		p.enclosed.getData()[0] = (byte)-1;
		p.enclosed.getData()[1] = (byte)0;
		p.enclosed.getData()[2] = (byte)-1;
		p.enclosed.getData()[3] = (byte)-1;
		System.out.println(p.readInt());
		System.out.println(p.index);
	}

	public String[] getParameterNames(String methodName)
	{
		if ("readUTF".equals(methodName))
		{
			return new String[] { "length" };
		}
		else if ("writeByte".equals(methodName))
		{
			return new String[] { "number" };
		}
		else if ("writeBytes".equals(methodName))
		{
			return new String[] { "array" };
		}
		else if ("writeInt".equals(methodName))
		{
			return new String[] { "number" };
		}
		else if ("writeShort".equals(methodName))
		{
			return new String[] { "number" };
		}
		else if ("writeUTF".equals(methodName))
		{
			return new String[] { "string" };
		}
		return null;
	}

	public String getSample(String methodName)
	{
		if ("getLength".equals(methodName) || "getHost".equals(methodName) || "getPort".equals(methodName) || "readUTF".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var packet;\n");
			sb.append("while (packet = plugins.udp.getReceivedPacket()) {\n");
			sb.append("\tapplication.output('packet received from ' + packet.getHost() + ':' + packet.getPort());\n");
			sb.append("\tif (packet.getLength() > 0) {\n");
			sb.append("\t\tapplication.output('message is: ' + packet.readUTF());\n");
			sb.append("\t}\n");
			sb.append("\telse {\n");
			sb.append("\t\tapplication.output('end of communication.');\n");
			sb.append("\t\tbreak;\n");
			sb.append("\t}\n");
			sb.append("}\n");
			return sb.toString();
		}
		else if ("index".equals(methodName) || "readInt".equals(methodName) || "readShort".equals(methodName) || "readByte".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var packet;\n");
			sb.append("while (packet = plugins.udp.getReceivedPacket()) {\n");
			sb.append("\tapplication.output('packet received from ' + packet.getHost() + ':' + packet.getPort());\n");
			sb.append("\tif (packet.getLength() > 0) {\n");
			sb.append("\t\tapplication.output('an int is: ' + packet.readInt());\n");
			sb.append("\t\tapplication.output('moved to index: ' + packet.index);\n");
			sb.append("\t\tapplication.output('a short is: ' + packet.readShort());\n");
			sb.append("\t\tapplication.output('moved to index: ' + packet.index);\n");
			sb.append("\t\tapplication.output('a byte is: ' + packet.readByte());\n");
			sb.append("\t\tapplication.output('moved to index: ' + packet.index);\n");
			sb.append("\t\tapplication.output('a byte is: ' + packet.readByte());\n");
			sb.append("\t\tapplication.output('moved to index: ' + packet.index);\n");
			sb.append("\t}\n");
			sb.append("\telse {\n");
			sb.append("\t\tapplication.output('end of communication.');\n");
			sb.append("\t\tbreak;\n");
			sb.append("\t}\n");
			sb.append("}\n");
			return sb.toString();
		}
		else if ("writeByte".equals(methodName) || "writeBytes".equals(methodName) || "writeInt".equals(methodName) || "writeShort".equals(methodName) ||
			"writeUTF".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("if (!plugins.udp.startSocket('5555', packetReceived)) {\n");
			sb.append("\tapplication.output('Failed to start socket.');\n");
			sb.append("} else {\n");
			sb.append("\tvar packet = plugins.udp.createNewPacket();\n");
			sb.append("\tpacket.writeUTF('hello world!');\n");
			sb.append("\tplugins.udp.sendPacket('localhost', packet, 1234);\n");
			sb.append("\tpacket = plugins.udp.createNewPacket();\n");
			sb.append("\tpacket.writeByte(0xFF);\n");
			sb.append("\tpacket.writeShort(10001);\n");
			sb.append("\tpacket.writeInt(2000000001);\n");
			sb.append("\tplugins.udp.sendPacket('localhost', packet, 1234);\n");
			sb.append("\tvar imgBytes = plugins.file.readFile('logo.jpg', 1024);\n");
			sb.append("\tpacket = plugins.udp.createNewPacket();\n");
			sb.append("\tpacket.writeBytes(imgBytes);\n");
			sb.append("\tplugins.udp.sendPacket('localhost', packet, 1234);\n");
			sb.append("\tplugins.udp.stopSocket();\n");
			sb.append("}\n");
			return sb.toString();
		}
		else if ("getByteArray".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var packet;\n");
			sb.append("while (packet = plugins.udp.getReceivedPacket()) {\n");
			sb.append("\tapplication.output('packet received from ' + packet.getHost() + ':' + packet.getPort());\n");
			sb.append("\tif (packet.getLength() > 0) {\n");
			sb.append("\t\tvar bytes = packet.getByteArray();\n");
			sb.append("\t\tapplication.output('received a packet of length: ' + bytes.length);\n");
			sb.append("\t\tfor (var i=0; i<bytes.length; i++)\n");
			sb.append("\t\t\tapplication.output(bytes[i]);\n");
			sb.append("\t\t}\n");
			sb.append("\telse {\n");
			sb.append("\t\tapplication.output('end of communication.');\n");
			sb.append("\t\tbreak;\n");
			sb.append("\t}\n");
			sb.append("}\n");
			return sb.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("index".equals(methodName))
		{
			return "Returns the current position in the byte array of the packet. The next read/write operation will occur at this position.";
		}
		else if ("getByteArray".equals(methodName))
		{
			return "Returns the content of the package into a byte array.";
		}
		else if ("getHost".equals(methodName))
		{
			return "Returns the name of the host that sent the packet.";
		}
		else if ("getLength".equals(methodName))
		{
			return "Returns the length of the packet in bytes.";
		}
		else if ("getPort".equals(methodName))
		{
			return "Returns the port where the packet originated from.";
		}
		else if ("readByte".equals(methodName))
		{
			return "Reads an 8 bits byte value from the packet, starting from the current index. Advances the index with one position.";
		}
		else if ("readInt".equals(methodName))
		{
			return "Reads a 32 bits int value from the packet, starting from the current index. Advances the index with 4 positions.";
		}
		else if ("readShort".equals(methodName))
		{
			return "Reads a 32 bits short value from the packet, starting from the current index. Advances the index with 2 positions.";
		}
		else if ("readUTF".equals(methodName))
		{
			return "Reads a UTF string from the packet, starting from the current index. If an argument is specified, then it represents the length of the string to read. If no argument is specified, then first a 32 bits int is read from the packet and that will be the length of the string. Advances the index with a number of positions that depends on the length of the read string.";
		}
		else if ("writeByte".equals(methodName))
		{
			return "Writes one byte into the packet, at the current index. The index is advanced with one position.";
		}
		else if ("writeBytes".equals(methodName))
		{
			return "Writes an array of bytes into the packet, at the current index. The index is advanced with a number of positions equal to the length of the written array.";
		}
		else if ("writeInt".equals(methodName))
		{
			return "Writes a 32 bits int into the packet, at the current index. The index is advances with 4 positions.";
		}
		else if ("writeShort".equals(methodName))
		{
			return "Writes a 16 bits short value into the packet, at the current index. The index is advances with 2 positions.";
		}
		else if ("writeUTF".equals(methodName))
		{
			return "Writes an UTF encoded string into the packet, at the current index. First the length of the string is written on 4 bytes, then the string is written. The index is advanced with a number of positions equal to the length of the string plus 4.";
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}
}
