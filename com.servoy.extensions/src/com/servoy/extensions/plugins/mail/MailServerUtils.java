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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * @author Jan Blok
 */
public class MailServerUtils
{
	public static MailMessage createMailMessage(Message m, int recieveMode) throws MessagingException, IOException
	{
		MailMessage mm = new MailMessage();
		if (m != null)
		{
			try
			{
				mm.fromAddresses = createAddressString(m.getFrom());
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
			try
			{
				mm.recipientAddresses = createAddressString(m.getRecipients(Message.RecipientType.TO));
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
			try
			{
				mm.ccAddresses = createAddressString(m.getRecipients(Message.RecipientType.CC));
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
			try
			{
				mm.replyAddresses = createAddressString(m.getReplyTo());
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
			try
			{
				mm.subject = m.getSubject();
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
			try
			{
				mm.receivedDate = m.getReceivedDate();
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
			try
			{
				mm.sentDate = m.getSentDate();
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
			try
			{
				mm.headers = createHeaderString(m.getAllHeaders());
			}
			catch (Exception e)
			{
				Debug.error(e);
			}

			if (recieveMode != IMailService.HEADERS_ONLY)
			{
				try
				{
					handlePart(mm, m, recieveMode);
				}
				catch (Exception e)
				{
					Debug.error(e);
				}
			}
		}
		return mm;
	}

	private static void handleContent(MailMessage mm, Object content, int recieveMode) throws MessagingException, IOException
	{
		if (content instanceof Multipart)
		{
			Multipart multi = (Multipart)content;
			for (int i = 0; i < multi.getCount(); i++)
			{
				handlePart(mm, multi.getBodyPart(i), recieveMode);
			}
		}
		else if (content instanceof BodyPart)
		{
			handlePart(mm, (Part)content, recieveMode);
		}
		else
		{
			Debug.trace("?"); //$NON-NLS-1$
		}
	}

	private static void handlePart(MailMessage mm, Part messagePart, int recieveMode) throws MessagingException, IOException
	{
		// -- Get the content type --
		String contentType = messagePart.getContentType();
		String charset = getCharsetFromContentType(contentType);
		if (contentType.startsWith("text/plain")) //$NON-NLS-1$
		{
			mm.plainMsg = createText(messagePart, charset);
		}
		else if (contentType.startsWith("text/html")) //$NON-NLS-1$
		{
			mm.htmlMsg = createText(messagePart, charset);
		}
		else if (contentType.startsWith("multipart")) //$NON-NLS-1$
		{
			handleContent(mm, messagePart.getContent(), recieveMode);
		}
		else
		{
			if (recieveMode != IMailService.NO_ATTACHMENTS)
			{
				mm.addAttachment(createAttachment(messagePart));
			}
		}
	}

	/**
	 * @param messagePart
	 * @return
	 */
	private static String createText(Part messagePart, String charsetName) throws MessagingException, IOException
	{
		StringBuffer retval = new StringBuffer();
		InputStream is = messagePart.getInputStream();

		Charset charset = null;
		if (charsetName != null)
		{
			try
			{
				charset = Charset.forName(charsetName);
			}
			catch (Exception ex)
			{
				Debug.trace(ex);//notfound of bad name, dono what todo now, try with default decoder...
			}
		}

		BufferedReader reader = null;
		if (charset != null)
		{
			reader = new BufferedReader(new InputStreamReader(is, charset));
		}
		else
		{
			reader = new BufferedReader(new InputStreamReader(is));
		}

		String line = null;
		while ((line = reader.readLine()) != null)
		{
			retval.append(line);
			retval.append('\n');
		}
		reader.close();
		is.close();
		return retval.toString();
	}

	private static Attachment createAttachment(Part messagePart) throws MessagingException, IOException
	{
		InputStream is = messagePart.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Utils.streamCopy(is, baos);
		is.close();
		return new Attachment(messagePart.getFileName(), baos.toByteArray());
	}

	private static String createAddressString(Address[] addresses)
	{
		if (addresses == null) return null;
		StringBuffer retval = new StringBuffer();
		for (int i = 0; i < addresses.length; i++)
		{
			if (addresses[i] != null)
			{
				retval.append(addresses[i].toString());
				if (i < addresses.length - 1)
				{
					retval.append(";"); //$NON-NLS-1$
				}
			}
		}
		return retval.toString();
	}

	private static String createHeaderString(Enumeration headers)
	{
		StringBuffer retval = new StringBuffer();
		while (headers.hasMoreElements())
		{
			Header header = (Header)headers.nextElement();
			retval.append(header.getName());
			retval.append("="); //$NON-NLS-1$
			retval.append(header.getValue());
			if (headers.hasMoreElements())
			{
				retval.append("\n"); //$NON-NLS-1$
			}
		}
		return retval.toString();
	}

	private static String getCharsetFromContentType(String contentType)
	{
		String charset = null;
		if (contentType != null)
		{
			StringTokenizer contentTypeTokens = new StringTokenizer(contentType, ";");
			String ctToken = null;
			while (contentTypeTokens.hasMoreTokens())
			{
				ctToken = contentTypeTokens.nextToken().trim();
				if (ctToken.startsWith("charset"))
				{
					int charsetNameIdx = ctToken.indexOf("=");
					if (charsetNameIdx != -1 && charsetNameIdx < ctToken.length() - 1)
					{
						charset = ctToken.substring(charsetNameIdx + 1).trim();
						// sometimes you find " or ' as well around the charset name
						if (charset.startsWith("\"")) charset = charset.substring(1);
						if (charset.endsWith("\"")) charset = charset.substring(0, charset.length() - 1);
						if (charset.startsWith("\'")) charset = charset.substring(1);
						if (charset.endsWith("\'")) charset = charset.substring(0, charset.length() - 1);
					}
				}
			}
		}
		return charset;
	}
}
