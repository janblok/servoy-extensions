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
package com.servoy.extensions.plugins.mail.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import com.servoy.extensions.plugins.mail.IMailService;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.Utils;

/**
 * @author jblok
 */
public class MailProvider implements IScriptObject
{
	private final MailPlugin plugin;
	private IMailService mailService = null;
	private String sendMailException;

	MailProvider(MailPlugin plugin)
	{
		this.plugin = plugin;
	}

	@Deprecated
	public MailMessage[] js_recieveMail(Object[] args)
	{
		return js_receiveMail(args);
	}

	/**
	 * Receive mails from pop3 account.
	 *
	 * @sample
	 * var properties = new Array();
	 * properties[0] = 'mail.pop3.port=995';
	 * properties[1] = 'mail.pop3.ssl.enable=true';
	 * properties[2] = 'mail.pop3.host=myserver.com';
	 * properties[3] = 'mail.pop3.user=user@myserver.com';
	 * 
	 * var msgs = plugins.mail.receiveMail('mylogin', 'secretpass',  true,  0,  null, properties);
	 * if (msgs != null) //if is null error occurred!
	 * {
	 * 	for (var i = 0 ; i < msgs.length ; i++)
	 * 	{
	 * 		var msg = msgs[i]
	 * 		application.output(msg.getFromAddresses())
	 * 		application.output(msg.getRecipientAddresses())
	 * 		application.output(msg.getReplyAddresses())
	 * 		application.output(msg.getSentDate())
	 * 		application.output(msg.getHeaders())
	 * 		application.output(msg.getSubject())
	 * 		application.output(msg.getHtmlMsg())
	 * 		application.output(msg.getPlainMsg())
	 * 		var attachments = msg.getAttachments()
	 * 		if (attachments != null) 
	 * 		{
	 * 			for (var j = 0 ; j < attachments.length ; j++)
	 * 			{
	 * 				var attachment = attachments[j]
	 * 				application.output(attachment.getName())
	 * 				var attachmentDataByteArray = attachment.getData()
	 * 				//write attachmentDataByteArray to a file...
	 * 			}
	 * 		}
	 * 	}
	 * }
	 * 
	 * //it is also possible to first receive the headers and later receive a full message with particular 'sentdate'
	 * //var receiveMode = 1;//0=FULL,1=HEADERS_ONLY,2=NO_ATTACHMENTS
	 * //var msgs = plugins.mail.receiveMail('me', 'test', true ,receiveMode);
	 * 
	 * //when first did receive the headers(=all_field+subject,no body and no attachemnt), get a msg with a specific sentdate
	 * //var msgs = plugins.mail.receiveMail('me', 'test', true , 1 , theSentDateObjectFormPreviousHeaderLoading);
	 * 
	 * //it is possbile to set all kind of pop3 properties
	 * //var properties = new Array()
	 * //properties[0] = 'mail.pop3.host=myserver.com'
	 * //properties specification can be found at:http://java.sun.com/products/javamail/javadocs/com/sun/mail/pop3/package-summary.html
	 * //var msgs = plugins.mail.receiveMail('me', 'test', true , 0 , null, properties);
	 *
	 * @param userName 
	 * @param password 
	 * @param leaveMsgsOnServer 
	 * @param receiveMode optional 
	 * @param onlyreceiveMsgWithSentDate optional 
	 * @param overridePreferencePOP3Host/properties_array optional 
	 */
	public MailMessage[] js_receiveMail(Object[] args)
	{
		//3 args are required
		if (args == null || args.length < 3) return null;
		for (int i = 0; i < 3; i++)
		{
			if (args[i] == null) return null;
		}
		String userName = args[0].toString();
		String password = args[1].toString();
		boolean leaveMsgsOnServer = Utils.getAsBoolean(args[2]);

		int receiveMode = 0;//0 is all
		if (args.length >= 4 && args[3] != null) receiveMode = Utils.getAsInteger(args[3]);
		Date onlyreceiveMsgWithSentDate = null;//null is all
		if (args.length >= 5 && args[4] != null && args[4] instanceof Date) onlyreceiveMsgWithSentDate = (Date)args[4];
		String[] overrideProperties = null;
		if (args.length >= 6 && args[5] != null)
		{
			if (args[5] instanceof Object[])
			{
				Object[] array = (Object[])args[5];
				overrideProperties = new String[array.length];
				System.arraycopy(array, 0, overrideProperties, 0, overrideProperties.length);
			}
			else if (args[5] instanceof String)
			{
				overrideProperties = new String[] { "mail.pop3.host=" + args[5] }; //$NON-NLS-1$
			}
		}

		//create if not yet created
		createMailService();

		//incase the server is not started in developer		
		if (mailService != null)
		{
			//receive mail
			try
			{
				return mailService.receiveMail(plugin.getClientPluginAccess().getClientID(), userName, password, leaveMsgsOnServer, receiveMode,
					onlyreceiveMsgWithSentDate, overrideProperties);
			}
			catch (Exception e)
			{
				Debug.error(e);
				return null;
			}
		}
		else
		{
			return null;//Todo throw app execption here? with "mail server not running"?
		}
	}

	/**
	 * Helper method to only get the plain addresses.
	 *
	 * @sample
	 * var plainArray = plugins.mail.getPlainMailAddresses('John Cobb <from_me@example.com>;Pete Cobb<from_pete@example.com>');
	 * application.output(plainArray[0]) //will return 'from_me@example.com'
	 *
	 * @param addressesString 
	 */
	public String[] js_getPlainMailAddresses(String addresses)
	{
		if (addresses == null) return new String[0];

		List<String> retval = new ArrayList<String>();
		StringTokenizer tk = new StringTokenizer(addresses.toLowerCase(), " ;,<>[]()'\":\n\t\r"); //$NON-NLS-1$
		while (tk.hasMoreTokens())
		{
			String token = tk.nextToken();
			if (token.indexOf('@') != -1) retval.add(token);
		}

		return retval.toArray(new String[retval.size()]);
	}

	/**
	 * Checks whether the given e-mail address is valid or not.
	 *
	 * @sample
	 * plugins.mail.isValidEmailAddress("me@example.com");
	 *
	 * @param email 
	 */
	public boolean js_isValidEmailAddress(String email)
	{
		return Utils.isValidEmailAddress(email);
	}

	private void createMailService()
	{
		if (mailService == null)
		{
			try
			{
				IClientPluginAccess access = plugin.getClientPluginAccess();
				mailService = (IMailService)access.getServerService(IMailService.SERVICE_NAME);
			}
			catch (Exception ex)
			{
				Debug.error(ex);
			}
		}
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	/**
	 * Helper method, returns MailMessage object from binary or 7bits string.
	 *
	 * @sample
	 * var msg = plugins.mail.getMailMessage(myBlob);
	 * if (msg != null) //if is null error occurred!
	 * {
	 * 	application.output(msg.getFromAddresses())
	 * }
	 *
	 * @param binaryblob/string 
	 */
	public MailMessage js_getMailMessage(Object data)
	{
		if (data == null) return null;

		//create if not yet created
		createMailService();

		//incase the server is not started in developer		
		if (mailService != null)
		{
			try
			{
				if (data.getClass().isArray())
				{
					return mailService.createMailMessageFromBinary(plugin.getClientPluginAccess().getClientID(), (byte[])data);
				}
				else if (data instanceof String)
				{
					return mailService.createMailMessageFromBinary(plugin.getClientPluginAccess().getClientID(), ((String)data).getBytes());
				}
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
		}
		else
		{
			return null;//Todo throw app execption here? with "mail server not running"?
		}
		return null;
	}

	/**
	 * Creates a binary attachment object.
	 *
	 * @sample
	 * var attachment1 = plugins.mail.createBinaryAttachment('logo1.gif',plugins.file.readFile('c:/temp/a_logo.gif'));
	 * var attachment2 = plugins.mail.createBinaryAttachment('logo2.gif',plugins.file.readFile('c:/temp/another_logo.gif'));
	 * var success = plugins.mail.sendMail('to_someone@example.com', 'John Cobb <from_me@example.org>', 'subject', 'msgText',null,null,new Array(attachment1,attachment2));
	 * if (!success) 
	 * {
	 * 	plugins.dialogs.showWarningDialog('Alert','Failed to send mail','OK');
	 * }
	 *
	 * @param filename 
	 * @param binarydata 
	 * @param mimeType optional 
	 */
	public Attachment js_createBinaryAttachment(Object[] args)
	{
		if (args == null || args.length < 2) return null;
		if (args[0] == null || args[1] == null) return null;
		String name = args[0].toString();
		if (!(args[1] instanceof byte[])) return null;
		byte[] data = (byte[])args[1];
		String mimeType = null;
		if (args.length > 2) mimeType = args[2].toString();
		return new Attachment(name, data, mimeType);
	}

	/**
	 * Creates a text based attachment object.
	 *
	 * @sample
	 * var attachment = plugins.mail.createTextAttachment('readme.html','<html>bla bla bla', 'text/html');
	 * var success = plugins.mail.sendMail('to_someone@example.com', 'John Cobb <from_me@example.com>', 'subject', 'msgText',null,null,attachment);
	 * if (!success) 
	 * {
	 * 	plugins.dialogs.showWarningDialog('Alert','Failed to send mail','OK');
	 * }
	 *
	 * @param filename 
	 * @param textdata 
	 * @param mimeType optional 
	 */
	public Attachment js_createTextAttachment(Object[] args)
	{
		if (args == null || args.length < 2) return null;
		if (args[0] == null || args[1] == null) return null;
		String name = args[0].toString();
		byte[] data = args[1].toString().getBytes();
		String mimeType = null;
		if (args.length > 2) mimeType = args[2].toString();
		return new Attachment(name, data, mimeType == null ? "text/plain" : mimeType); //$NON-NLS-1$
	}

	/**
	 * Send a mail, if you make the msgText start with <html> the message will be sent in html (and you can use all html formatting).
	 *
	 * @sample
	 * var attachment1 = plugins.mail.createBinaryAttachment('embedded',plugins.file.readFile('c:/temp/a_logo.gif'));
	 * var msgText = 'plain msg<html>styled html msg<img src="%%embedded%%"></html>';
	 * var success = plugins.mail.sendMail('to_someone@example.com,to_someone_else@example.net', 'John Cobb <from_me@example.com>', 'subject', msgText,null,'unnamed@example.com',[attachment1]);
	 * if (!success) 
	 * {
	 * 	plugins.dialogs.showWarningDialog('Alert','Failed to send mail','OK');
	 * }
	 * 
	 * //it is possbile to set all kind of smtp properties
	 * //var properties = new Array()
	 * //properties[0] = 'mail.smtp.host=myserver.com'
	 * //properties specification can be found at:http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
	 * //var msgs = plugins.mail.sendMail('to_someone@example.com,to_someone_else@example.net', 'John Cobb <from_me@example.com>', 'subject', msgText,null,'unnamed@example.com',null, properties);
	 *
	 * @param to[,to2,toN]  
	 * @param from[,reply] 
	 * @param subject 
	 * @param msgText 
	 * @param cc,cc2,ccN optional 
	 * @param bcc,bcc2,bccN optional 
	 * @param attachment/attachments_array optional 
	 * @param overridePreferenceSMTPHost/properties_array optional 
	 */
	public boolean js_sendMail(Object[] args)
	{
		sendMailException = null;

		//4 args are required
		if (args == null || args.length < 4) return false;

		//3 args cannot be null
		if (args[0] == null) return false;
		if (args[2] == null) return false;
		if (args[3] == null) return false;

		String to = args[0].toString();
		String from = (args[1] == null ? null : args[1].toString());
		String subject = args[2].toString();
		String msgText = args[3].toString();
		String cc = null;
		if (args.length >= 5 && args[4] != null) cc = args[4].toString();
		String bcc = null;
		if (args.length >= 6 && args[5] != null) bcc = args[5].toString();

		//create if not yet created
		createMailService();

		Attachment[] attachments = null;
		if (args.length >= 7 && args[6] != null)
		{
			if (args[6] instanceof Object[])
			{
				Object[] array = (Object[])args[6];
				attachments = new Attachment[array.length];
				System.arraycopy(array, 0, attachments, 0, attachments.length);
			}
			else if (args[6] instanceof Attachment)
			{
				attachments = new Attachment[1];
				attachments[0] = (Attachment)args[6];
			}
		}

		String[] overrideProperties = null;
		if (args.length >= 8 && args[7] != null)
		{
			if (args[7] instanceof Object[])
			{
				Object[] array = (Object[])args[7];
				overrideProperties = new String[array.length];
				System.arraycopy(array, 0, overrideProperties, 0, overrideProperties.length);
			}
			else if (args[7] instanceof String)
			{
				overrideProperties = new String[] { "mail.smtp.host=" + args[7] }; //$NON-NLS-1$
			}
		}

		//incase the server is not started in developer		
		if (mailService != null)
		{
			//send mail
			try
			{
				mailService.sendMail(plugin.getClientPluginAccess().getClientID(), to, from, subject, msgText, cc, bcc, attachments, overrideProperties);
				return true;
			}
			catch (Exception mex)
			{
				Debug.error(mex);
				sendMailException = mex.getMessage();
				if (mex.getCause() != null)
				{
					String nested = mex.getCause().getMessage();
					if (!("".equals(nested))) sendMailException = sendMailException + "; " + nested; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return false;
			}
		}
		else
		{
			return false;//Todo throw app execption here? with "mail server not running"?
		}
	}

	@Deprecated
	public String js_getLastSendMailException()
	{
		return js_getLastSendMailExceptionMsg();
	}

	/**
	 * Get the exception that occurred in the last sendMail attempt (null if no exception occurred).
	 *
	 * @sample
	 * var success = plugins.mail.sendMail('to_someone@example.com,to_someone_else@example.org', 'John Cobb <from_me@example.com>', 'subject', 'my message',null,'unnamed@example.com');
	 * if (!success) 
	 * {
	 * 	plugins.dialogs.showWarningDialog('Alert',plugins.mail.getLastSendMailExceptionMsg(),'OK');
	 * }
	 */
	public String js_getLastSendMailExceptionMsg()
	{
		return sendMailException;
	}

	public String[] getParameterNames(String methodName)
	{
		return null;
	}

	public String getSample(String methodName)
	{
		return null;
	}

	public String getToolTip(String methodName)
	{
		return null;
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { MailMessage.class, Attachment.class };
	}
}
