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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.collections.map.LinkedMap;

import com.servoy.j2db.Messages;
import com.servoy.j2db.plugins.IServerAccess;
import com.servoy.j2db.plugins.IServerPlugin;
import com.servoy.j2db.preference.PreferencePanel;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.ITagResolver;
import com.servoy.j2db.util.Text;
import com.servoy.j2db.util.UUID;

/**
 * Server mail component
 * 
 * @author jblok
 */
public class MailServer implements IMailService, IServerPlugin
{
	private Properties settings;
	private IServerAccess application;

	public MailServer()//must have default constructor
	{
	}

	public Properties getProperties()
	{
		Properties props = new Properties();
		props.put(DISPLAY_NAME, Messages.getString("servoy.plugin.mailserver.displayname")); //$NON-NLS-1$
		return props;
	}

	public void load()
	{
	}

	public void initialize(IServerAccess app)
	{
		application = app;
		settings = app.getSettings();

		try
		{
			app.registerRMIService(IMailService.SERVICE_NAME, this);
		}
		catch (Exception e)
		{
			Debug.error(e);
		}

		//copy mail settings to system props, since more libs may need those
		Iterator it = settings.keySet().iterator();
		while (it.hasNext())
		{
			String key = (String)it.next();
			if (key.startsWith("mail."))
			{
				String value = settings.getProperty(key);
				if (value != null && !value.trim().equals("")) //$NON-NLS-1$
				{
					System.setProperty(key, value);
				}
			}
		}
	}

	public void unload()
	{
		settings = null;
	}

	public Map getRequiredPropertyNames()
	{
		LinkedMap req = new LinkedMap();
		req.put("mail.pop3.host", "The name of POP3 server to recieve mails from"); //$NON-NLS-1$ //$NON-NLS-2$
		req.put("mail.pop3.apop.enable", "Whether or not to use APOP for authentication (true/false)"); //$NON-NLS-1$ //$NON-NLS-2$
		req.put("mail.smtp.host", "The name of SMTP server to deliver the mails to"); //$NON-NLS-1$ //$NON-NLS-2$
		req.put("mail.smtp.port", "The port of SMTP server to deliver the mails to"); //$NON-NLS-1$ //$NON-NLS-2$
		req.put("mail.from", "Default 'from' address if none is specified"); //$NON-NLS-1$ //$NON-NLS-2$
		req.put("mail.smtp.auth", "Use authentication (true/false)"); //$NON-NLS-1$ //$NON-NLS-2$
		req.put("mail.smtp.username", "Specify username if using authentication"); //$NON-NLS-1$ //$NON-NLS-2$
		req.put("mail.smtp.password", "Specify password if using authentication"); //$NON-NLS-1$ //$NON-NLS-2$		
		req.put("mail.smtp.connectiontimeout", "Socket connection timeout value in milliseconds. Default is infinite timeout."); //$NON-NLS-1$ //$NON-NLS-2$       
		req.put("mail.smtp.timeout", "Socket I/O timeout value in milliseconds. Default is infinite timeout."); //$NON-NLS-1$ //$NON-NLS-2$
		req.put("mail.smtp.ssl.enable", "Use SSL (true/false)"); //$NON-NLS-1$ 
		req.put(
			"mail.mime.charset",
			"Specify the name of the charset to use for mail encoding (leave emtpy for system default), see http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html forinfo which charset names are usable"); //$NON-NLS-1$ 
		req.put(
			"mail.development.override.address",
			"Specify an email address to which all email will be send instead of the specified To, Cc and Bcc addresses.\nThe specified to, Cc and Bcc addresses will be added to the Subject."); //$NON-NLS-1$ 
		return req;
	}

	public void sendMail(String to, String from, String subject, String msgText, String cc, String bcc, Attachment[] attachments, String[] overrideProperties)
		throws MessagingException
	{
		ClassLoader saveCl = Thread.currentThread().getContextClassLoader();
		try
		{

			Thread.currentThread().setContextClassLoader(application.getPluginManager().getClassLoader());
			// create a new Session object
			Properties properties = overrideProperties(settings, overrideProperties);
			Session session = Session.getInstance(properties, new SMTPAuthenticator(properties));

			String encoding = properties.getProperty("mail.mime.encoding");
			String charset = properties.getProperty("mail.mime.charset");
			if (charset == null) charset = "UTF-8";

			String plainTextContentType = "text/plain; charset=" + charset;
			String htmlContentType = "text/html; charset=" + charset;

			// create a new MimeMessage object (using the Session created above)
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from == null ? properties.getProperty("mail.from") : from));
			message.setSentDate(new Date());
			String overrideAddress = properties.getProperty("mail.development.override.address");
			String overrideFeedback = null;
			if (overrideAddress != null)
			{
				addRecipients(message, overrideAddress, Message.RecipientType.TO);
				overrideFeedback = " (Override: TO=" + to + ", CC: " + (cc == null ? cc : "") + ", BCC: " + (bcc == null ? bcc : "") + ")";
			}
			else
			{
				addRecipients(message, to, Message.RecipientType.TO);
				addRecipients(message, cc, Message.RecipientType.CC);
				addRecipients(message, bcc, Message.RecipientType.BCC);
			}

			String sub = (subject == null ? Messages.getString("servoy.plugin.mailserver.defaultsubject") : subject);
			if (overrideFeedback != null) sub = sub + overrideFeedback;
			message.setSubject(sub, charset);

			if (msgText == null) msgText = ""; //$NON-NLS-1$
			msgText = msgText.trim();

			int htmlIndex = msgText.toLowerCase().indexOf("<html"); //$NON-NLS-1$
			boolean hasHTML = (htmlIndex != -1);
			boolean hasPlain = !hasHTML || htmlIndex > 0;

			String plain = hasHTML ? msgText.substring(0, htmlIndex) : msgText;
			String html = hasHTML ? msgText.substring(htmlIndex) : null;

			// Make html multipart if it contains embedded images.
			MimeMultipart htmlMultipart = null;
			if (hasHTML && attachments != null && attachments.length > 0)
			{
				// Add embedded attachments.
				EmbeddedImageTagResolver resolver = new EmbeddedImageTagResolver(attachments);
				html = Text.processTags(html, resolver);
				List embeddedImages = resolver.getMimeBodyParts();
				if (embeddedImages.size() > 0)
				{
					htmlMultipart = new MimeMultipart("related"); //$NON-NLS-1$
					MimeBodyPart htmlBodyPart = new MimeBodyPart();

					htmlBodyPart.setContent(html, htmlContentType);
					if (encoding != null)
					{
						htmlBodyPart.setHeader("Content-Transfer-Encoding", encoding);
					}
					htmlMultipart.addBodyPart(htmlBodyPart);

					Iterator iterator = embeddedImages.iterator();
					while (iterator.hasNext())
					{
						htmlMultipart.addBodyPart((MimeBodyPart)iterator.next());
					}
				}
			}

			// Make message multipart if needed (only html with embeded images or combined plain + html messages).
			MimeMultipart messageMultipart = null;
			if (htmlMultipart != null && !hasPlain)
			{
				messageMultipart = htmlMultipart;
			}
			else if (hasPlain && hasHTML)
			{
				messageMultipart = new MimeMultipart("alternative"); //$NON-NLS-1$

				MimeBodyPart textBodyPart = new MimeBodyPart();
				textBodyPart.setContent(plain, plainTextContentType);
				if (encoding != null)
				{
					textBodyPart.setHeader("Content-Transfer-Encoding", encoding);
				}
				messageMultipart.addBodyPart(textBodyPart);

				MimeBodyPart htmlBodyPart = new MimeBodyPart();
				if (htmlMultipart != null)
				{
					htmlBodyPart.setContent(htmlMultipart);
				}
				else
				{
					htmlBodyPart.setContent(html, htmlContentType);
					if (encoding != null)
					{
						htmlBodyPart.setHeader("Content-Transfer-Encoding", encoding);
					}
				}
				messageMultipart.addBodyPart(htmlBodyPart);
			}

			// Add attachments that were not embedded.
			MimeMultipart mixedMultipart = null;

			for (int i = 0; attachments != null && i < attachments.length; i++)
			{
				Attachment attachment = attachments[i];
				if (attachment == null || attachment.isEmbedded()) continue;

				if (mixedMultipart == null)
				{
					mixedMultipart = new MimeMultipart("mixed"); //$NON-NLS-1$
					MimeBodyPart messageBodyPart = new MimeBodyPart();
					if (messageMultipart != null)
					{
						messageBodyPart.setContent(messageMultipart);
					}
					else
					{
						messageBodyPart.setContent(msgText, hasHTML ? htmlContentType : plainTextContentType);
						if (encoding != null)
						{
							messageBodyPart.setHeader("Content-Transfer-Encoding", encoding);
						}
					}
					mixedMultipart.addBodyPart(messageBodyPart);
				}

				MimeBodyPart attachmentBodyPart = new MimeBodyPart();
				attachmentBodyPart.setDisposition(Part.ATTACHMENT);
				attachmentBodyPart.setDataHandler(new DataHandler(new AttachmentDataSource(attachment)));
				String fileName = attachment.getName();
				fileName = MimeUtility.encodeText(fileName, charset, null);
				attachmentBodyPart.setFileName(fileName);
				mixedMultipart.addBodyPart(attachmentBodyPart);
			}


			if (mixedMultipart != null)
			{
				message.setContent(mixedMultipart);
			}
			else if (messageMultipart != null)
			{
				message.setContent(messageMultipart);
			}
			else
			{
				message.setContent(msgText, hasHTML ? htmlContentType : plainTextContentType);
				if (encoding != null)
				{
					message.setHeader("Content-Transfer-Encoding", encoding);
				}
			}

			Transport.send(message);
		}
		catch (Exception ex)
		{
			Debug.error("SMTPSend " + ex.getMessage(), ex); //$NON-NLS-1$
			if (ex instanceof MessagingException) throw (MessagingException)ex;
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(saveCl);
		}
	}

	private void addRecipients(Message message, String recp, Message.RecipientType type) throws Exception
	{
		if (recp != null)
		{
			StringTokenizer tk = new StringTokenizer(recp, ";,"); //$NON-NLS-1$
			int nrto = tk.countTokens();
			InternetAddress[] addresses = new InternetAddress[nrto];
			for (int i = 0; i < nrto; i++)
			{
				String rcpt = tk.nextToken();
				addresses[i] = new InternetAddress(rcpt);
			}
			message.setRecipients(type, addresses);
		}
	}

	private Properties overrideProperties(Properties defaults, String[] overrideProperties)
	{
		Properties properties = new Properties(defaults);

		if (overrideProperties == null) return properties;

		for (String property : overrideProperties)
		{
			if (property != null)
			{
				int j = property.indexOf('=');
				if (j > 0)
				{
					String propertyName = property.substring(0, j);
					String propertyValue = property.substring(j + 1);
					properties.put(propertyName, propertyValue);
				}
			}
		}
		return properties;
	}

	public MailMessage[] receiveMail(String userName, String password, boolean leaveMsgsOnServer, int recieveMode, Date onlyRecieveMsgWithSentDate,
		String[] overrideProperties)
	{
		Store store = null;
		Folder folder = null;
		ClassLoader saveCl = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(application.getPluginManager().getClassLoader());
			// Create empty properties
			// -- Get hold of the default session --
			Properties properties = overrideProperties(settings, overrideProperties);
			Session session = Session.getDefaultInstance(properties, null);
			session.setDebug(Boolean.valueOf(settings.getProperty("mail.pop3.debug", "false")).booleanValue()); //$NON-NLS-1$ //$NON-NLS-2$
			// -- Get hold of a POP3 message store, and connect to it --
			store = session.getStore("pop3"); //$NON-NLS-1$
			String host = properties.getProperty("mail.pop3.host"); //$NON-NLS-1$
			Debug.trace("receiveMail: host=" + host + ", user=" + userName); //$NON-NLS-1$ //$NON-NLS-2$
			store.connect(host, userName, password);
			// -- Try to get hold of the default folder --
			folder = store.getDefaultFolder();
			if (folder == null) throw new Exception("No default folder"); //$NON-NLS-1$
			// -- ...and its INBOX --
			folder = folder.getFolder("INBOX"); //$NON-NLS-1$
			if (folder == null) throw new Exception("No POP3 INBOX found"); //$NON-NLS-1$
			// -- Open the folder for read only --
			folder.open(leaveMsgsOnServer ? Folder.READ_ONLY : Folder.READ_WRITE);
			// -- Get the message wrappers and process them --
			Message[] message = folder.getMessages();
			if (recieveMode == IMailService.HEADERS_ONLY || onlyRecieveMsgWithSentDate != null)
			{
				FetchProfile fp = new FetchProfile();
				fp.add(FetchProfile.Item.ENVELOPE);
				folder.fetch(message, fp);
			}
			MailMessage[] retval = new MailMessage[message.length];
			for (int i = 0; i < message.length; i++)
			{
				Message msg = message[i];
				if (onlyRecieveMsgWithSentDate != null)
				{
					if (!onlyRecieveMsgWithSentDate.equals(msg.getSentDate())) continue;
				}
				retval[i] = MailServerUtils.createMailMessage(msg, recieveMode);
				if (!leaveMsgsOnServer) msg.setFlag(Flags.Flag.DELETED, true);
			}
			return retval;
		}
		catch (Exception ex)
		{
			Debug.error(ex);
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(saveCl);

			// -- Close down nicely --
			try
			{
				if (folder != null) folder.close(!leaveMsgsOnServer);
			}
			catch (Exception ex1)
			{
				Debug.error(ex1);
			}
			try
			{
				if (store != null) store.close();
			}
			catch (Exception ex2)
			{
				Debug.error(ex2);
			}
		}
		return null;
	}

	/*
	 * @see IPlugin#getPreferencePanels()
	 */
	public PreferencePanel[] getPreferencePanels()
	{
		return new PreferencePanel[] { new MailPreferencePanel(application) };
	}

	private class SMTPAuthenticator extends javax.mail.Authenticator
	{
		private final Properties properties;

		public SMTPAuthenticator(Properties properties)
		{
			this.properties = properties;
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication()
		{
			String username = properties.getProperty("mail.smtp.username"); //$NON-NLS-1$
			String password = properties.getProperty("mail.smtp.password"); //$NON-NLS-1$
			return new PasswordAuthentication(username, password);
		}
	}

	public MailMessage createMailMessageFromBinary(byte[] data)
	{
		MimeMessage mm = null;
		ClassLoader saveCl = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(application.getPluginManager().getClassLoader());
			try
			{
				ByteArrayInputStream is = new ByteArrayInputStream(data);
				mm = new MimeMessage(null, is);
				is.close();
			}
			catch (Exception e)
			{
				Debug.error(e);
			}

			MailMessage sm = null;
			try
			{
				if (mm != null) sm = MailServerUtils.createMailMessage(mm, IMailService.FULL);
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
			return sm;
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(saveCl);
		}
	}

	private class EmbeddedImageTagResolver implements ITagResolver
	{
		Map attachmentMap;
		List mimeBodyParts;

		public EmbeddedImageTagResolver(Attachment[] attachments)
		{
			mimeBodyParts = new ArrayList();
			attachmentMap = new HashMap();
			for (Attachment element : attachments)
			{
				attachmentMap.put(element.getName(), element);
			}
		}

		public String getStringValue(String name)
		{
			Attachment attachment = (Attachment)attachmentMap.get(name);
			if (attachment != null)
			{
				try
				{
					MimeBodyPart mbp = new MimeBodyPart();
					mbp.setDisposition(Part.ATTACHMENT);
					mbp.setDataHandler(new DataHandler(new AttachmentDataSource(attachment)));
					mimeBodyParts.add(mbp);
					attachment.setEmbedded(true);
					String contentID = "servoy-" + UUID.randomUUID(); //$NON-NLS-1$
					mbp.setContentID("<" + contentID + ">"); //$NON-NLS-1$ //$NON-NLS-2$
					return "cid:" + contentID; //$NON-NLS-1$
				}
				catch (MessagingException e)
				{
					Debug.error(e);
				}
			}
			return "%%" + name + "%%"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		public List getMimeBodyParts()
		{
			return mimeBodyParts;
		}
	}
}
