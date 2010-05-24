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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.servoy.j2db.scripting.IScriptObject;

/**
 * @author Jan Blok
 */
public class MailMessage implements Serializable, IScriptObject
{
	List attachments = new ArrayList();
	String fromAddresses;
	String recipientAddresses;
	String ccAddresses;
	String replyAddresses;
	String subject;
	String headers;
	Date receivedDate;
	Date sentDate;
	String plainMsg;
	String htmlMsg;

	public Attachment[] js_getAttachments()
	{
		return getAttachments();
	}

	public String js_getCCAddresses()
	{
		return getCCAddresses();
	}

	public String js_getFromAddresses()
	{
		return getFromAddresses();
	}

	public String js_getHeaders()
	{
		return getHeaders();
	}

	public String js_getHtmlMsg()
	{
		return getHtmlMsg();
	}

	public String js_getPlainMsg()
	{
		return getPlainMsg();
	}

	public Date js_getReceivedDate()
	{
		return getReceivedDate();
	}

	public String js_getRecipientAddresses()
	{
		return getRecipientAddresses();
	}

	public String js_getReplyAddresses()
	{
		return getReplyAddresses();
	}

	public Date js_getSentDate()
	{
		return getSentDate();
	}

	public String js_getSubject()
	{
		return getSubject();
	}

	public String getFromAddresses()
	{
		return fromAddresses;
	}

	public Date getReceivedDate()
	{
		return receivedDate;
	}

	public String getRecipientAddresses()
	{
		return recipientAddresses;
	}

	public String getReplyAddresses()
	{
		return replyAddresses;
	}

	public Date getSentDate()
	{
		return sentDate;
	}

	public String getSubject()
	{
		return subject;
	}

	public String getHeaders()
	{
		return headers;
	}

	public String getHtmlMsg()
	{
		return htmlMsg;
	}

	public String getPlainMsg()
	{
		return plainMsg;
	}

	void addAttachment(Attachment attachment)
	{
		attachments.add(attachment);
	}

	public Attachment[] getAttachments()
	{
		return (Attachment[])attachments.toArray(new Attachment[attachments.size()]);
	}

	public String getCCAddresses()
	{
		return ccAddresses;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("from: "); //$NON-NLS-1$
		sb.append(getFromAddresses());
		sb.append("\n"); //$NON-NLS-1$
		sb.append("to: "); //$NON-NLS-1$
		sb.append(getRecipientAddresses());
		sb.append("\n"); //$NON-NLS-1$
		sb.append("cc: "); //$NON-NLS-1$
		sb.append(getCCAddresses());
		sb.append("\n"); //$NON-NLS-1$
		sb.append("sentdate: "); //$NON-NLS-1$
		sb.append(getSentDate());
		sb.append("\n"); //$NON-NLS-1$
		sb.append("subject: "); //$NON-NLS-1$
		sb.append(getSubject());
		sb.append("plain msg: "); //$NON-NLS-1$
		sb.append(getPlainMsg());
		sb.append("\n"); //$NON-NLS-1$
		sb.append("html msg: "); //$NON-NLS-1$
		sb.append(getHtmlMsg());
		sb.append("\n"); //$NON-NLS-1$
		return sb.toString();
	}

	public String[] getParameterNames(String methodName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getSample(String methodName)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("var msgs = plugins.mail.receiveMail(username, password, true, 0, null, properties);\n");
		sb.append("if (msgs != null)\n");
		sb.append("{\n");
		sb.append("\tfor (var i=0; i < msgs.length; i++)\n");
		sb.append("\t{\n");
		sb.append("\t\tvar msg = msgs[i];\n");
		sb.append("\t\tvar str = '';\n");
		sb.append("\t\tstr += 'From: ' + msg.getFromAddresses() + '\\n';\n");
		sb.append("\t\tstr += 'To: ' + msg.getRecipientAddresses() + '\\n';\n");
		sb.append("\t\tstr += 'CC: ' + msg.getCCAddresses() + '\\n';\n");
		sb.append("\t\tstr += 'Reply to: ' + msg.getReplyAddresses() + '\\n';\n");
		sb.append("\t\tstr += 'Received on: ' + msg.getReceivedDate() + '\\n';\n");
		sb.append("\t\tstr += 'Sent on: ' + msg.getSentDate() + '\\n\\n';\n");
		sb.append("\t\tstr += 'Subject: ' + msg.getSubject() + '\\n\\n';\n");
		sb.append("\t\tstr += 'Plain message: ' + msg.getPlainMsg() + '\\n\\n';\n");
		sb.append("\t\tstr += 'HTML message: ' + msg.getHtmlMsg() + '\\n\\n';\n");
		sb.append("\t\tstr += 'Headers: ' + msg.getHeaders() + '\\n\\n';\n");
		sb.append("\t\tvar attachments = msg.getAttachments();\n");
		sb.append("\t\tif (attachments != null) {\n");
		sb.append("\t\t\tstr += 'Number of attachments: ' + attachments.length + '\\n\\n';\n");
		sb.append("\t\t\tfor (var j=0; j < attachments.length; j++)\n");
		sb.append("\t\t\t{\n");
		sb.append("\t\t\t\tvar attachment = attachments[j];\n");
		sb.append("\t\t\t\tstr += 'Attachment ' + j + '\\n';\n");
		sb.append("\t\t\t\tstr += '\tName: ' + attachment.getName() + '\\n';\n");
		sb.append("\t\t\t\tstr += '\tSize: ' + attachment.getData().length + '\\n\\n';\n");
		sb.append("\t\t\t}\n");
		sb.append("\t\t}\n");
		sb.append("\t\tplugins.file.writeTXTFile('msg' + i + '.txt', str);\n");
		sb.append("\t\tapplication.output('Message ' + i + ' retrieved.');\n");
		sb.append("\t}\n");
		sb.append("}\n");
		sb.append("else\n");
		sb.append("{\n");
		sb.append("\tapplication.output(\"Failed to retrieve messages.\");\n");
		sb.append("}\n");
		return sb.toString();
	}

	public String getToolTip(String methodName)
	{
		if ("getAttachments".equals(methodName)) return "Returns an array of Attachment instances corresponding to the attachments of this message.";
		else if ("getCCAddresses".equals(methodName)) return "Returns a String with all addresses present in the CC field of this message.";
		else if ("getFromAddresses".equals(methodName)) return "Returns a String with all addresses present in the From field of this message.";
		else if ("getHeaders".equals(methodName)) return "Returns a String with all headers of this message.";
		else if ("getHtmlMsg".equals(methodName)) return "Returns a String with the HTML content of this message.";
		else if ("getPlainMsg".equals(methodName)) return "Returns a String with the plain content of this message.";
		else if ("getReceivedDate".equals(methodName)) return "Returns a Date instace corresponding to the moment when the message was received.";
		else if ("getRecipientAddresses".equals(methodName)) return "Returns a String with all addresses in the To field of this message.";
		else if ("getReplyAddresses".equals(methodName)) return "Returns a String with all addresses in the Reply-To field of this message.";
		else if ("getSentDate".equals(methodName)) return "Returns a Date instance corresponding to the moment when this message was sent.";
		else if ("getSubject".equals(methodName)) return "Returns a String with the subject of this message.";
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
