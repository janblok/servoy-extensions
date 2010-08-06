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
package com.servoy.extensions.plugins.mail;

import java.io.Serializable;

import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.ImageLoader;

/**
 * @author jblok
 */
public class Attachment implements Serializable, IScriptObject
{
	private final String name;
	private String mimeType;
	private final byte[] data;
	private boolean embedded;

	public Attachment()
	{
		//for developer script introspection only
		this(null, null, null);
	}

	public Attachment(String name, byte[] data, String mimeType)
	{
		this.name = name;
		this.mimeType = mimeType;
		this.data = data;
		this.embedded = false;

		if (mimeType == null && data != null && data.length != 0)
		{
			this.mimeType = ImageLoader.getContentType(data, name);
		}
	}

	public Attachment(String name, byte[] data)
	{
		this(name, data, null);
	}

	public byte[] js_getData()
	{
		return getData();
	}

	public String js_getName()
	{
		return getName();
	}

	public String js_getMimeType()
	{
		return getMimeType();
	}

	public boolean js_isEmbedded()
	{
		return isEmbedded();
	}

	/**
	 * @return
	 */
	public byte[] getData()
	{
		return data;
	}

	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setEmbedded(boolean embedded)
	{
		this.embedded = embedded;
	}

	public boolean isEmbedded()
	{
		return embedded;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("name: "); //$NON-NLS-1$
		sb.append(getName());
		sb.append("\n"); //$NON-NLS-1$
		sb.append("mime type: "); //$NON-NLS-1$
		sb.append(getMimeType());
		sb.append("\n"); //$NON-NLS-1$
		sb.append("datalength: "); //$NON-NLS-1$
		sb.append((getData() != null ? getData().length : 0));
		sb.append("\n"); //$NON-NLS-1$
		sb.append("embedded: ");
		sb.append(embedded);
		sb.append("\n");
		return sb.toString();
	}

	public String[] getParameterNames(String methodName)
	{
		return null;
	}

	public String getSample(String methodName)
	{
		if ("getData".equals(methodName) || "getMimeType".equals(methodName) || "getName".equals(methodName) || "isEmbedded".equals(methodName))
		{
			StringBuffer sb = new StringBuffer();
			sb.append("var logo = plugins.mail.createBinaryAttachment('logo.jpg', plugins.file.readFile('d:/logo.jpg'));\n");
			sb.append("var invoice = plugins.mail.createTextAttachment('invoice.txt', plugins.file.readTXTFile('d:/invoice.txt'));\n");
			sb.append("var attachments = new Array(logo, invoice);\n");
			sb.append("var success = plugins.mail.sendMail(toAddress, fromAddress, 'subject line', 'message text', null, null, attachments, properties);\n");
			sb.append("if (!success)\n");
			sb.append("{\n");
			sb.append("\tplugins.dialogs.showWarningDialog('Alert', 'Failed to send mail', 'OK');\n");
			sb.append("}\n");
			sb.append("else\n");
			sb.append("{\n");
			sb.append("\tplugins.dialogs.showInfoDialog('Success', 'Mail sent', 'OK');\n");
			sb.append("\tapplication.output('logo attachment name: ' + logo.getName());\n");
			sb.append("\tapplication.output('logo attachment mime type: ' + logo.getMimeType());\n");
			sb.append("\tapplication.output('logo attachment size: ' + logo.getData().length);\n");
			sb.append("\tapplication.output('logo attachment embedded state: ' + logo.isEmbedded());\n");
			sb.append("\tapplication.output('invoice attachment name: ' + invoice.getName());\n");
			sb.append("\tapplication.output('invoice attachment mime type: ' + invoice.getMimeType());\n");
			sb.append("\tapplication.output('invoice attachment size: ' + invoice.getData().length);\n");
			sb.append("\tapplication.output('invoice attachment embedded state: ' + invoice.isEmbedded());\n");
			sb.append("}\n");
			return sb.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if ("getData".equals(methodName))
		{
			return "Returns a byte array with the content of this attachment.";
		}
		if ("getMimeType".equals(methodName))
		{
			return "Returns the Mime type of this attachment.";
		}
		if ("getName".equals(methodName))
		{
			return "Returns the name of this attachment.";
		}
		if ("isEmbedded".equals(methodName))
		{
			return "Returns true if this attachment is embedded, false otherwise. Attachments become embedded if they are references through tags from the body text of the message.";
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
