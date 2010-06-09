/**********************************************************************************
 * Copyright 2008, 2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.logic.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.springframework.web.multipart.MultipartFile;

public class ExternalLogicBuiltinImpl extends ExternalLogicImpl
{
	private final Log log = LogFactory.getLog(ExternalLogicBuiltinImpl.class);
	private ServerConfigurationService serverConfigurationService;
	private ConfigLogic configLogic;

	public List<String> sendEmail(ConfigEntry config, String fromEmail, String fromName,
			Map<String, String> to, String subject, String content,
			Map<String, MultipartFile> attachments) throws MailsenderException
	{
		String smtp_server = serverConfigurationService.getString(
				"smtp@org.sakaiproject.email.api.EmailService", "localhost");
		int smtp_port = serverConfigurationService.getInt(
				"smtpPort@org.sakaiproject.email.api.EmailService", 25);

		ArrayList<String> invalids = new ArrayList<String>();
		// to be thrown later if messages are accumulated
		MailsenderException me = new MailsenderException();
		try
		{
			Properties props = new Properties();
			props.put("mail.smtp.host", smtp_server);
			props.put("mail.smtp.port", smtp_port);
			Session s = Session.getInstance(props, null);

			MimeMessage emailMessage = new MimeMessage(s);
			emailMessage.setSentDate(new Date()); // fix of SAK-11181
			String replyToName = null;
			String replyToEmail = null;
			try
			{
				if (ConfigEntry.ReplyTo.no_reply_to.name().equals(config.getReplyTo()))
				{
					replyToName = getCurrentSiteTitle();
					replyToEmail = "";
				}
				else
				// ConfigEntry.ReplyTo.sender
				{
					replyToName = fromName;
					replyToEmail = fromEmail;
				}
				InternetAddress from = new InternetAddress(replyToEmail, replyToName);
				emailMessage.setFrom(from);
			}
			catch (AddressException ae)
			{
				String msg = ae.getMessage() + ": " + replyToName + " " + replyToEmail;
				me.addMessage("invalid.from_replyto.address", msg);
				throw me;
			}
			catch (UnsupportedEncodingException msge)
			{
				String msg = msge.getMessage() + ": " + replyToName + " " + replyToEmail;
				me.addMessage("invalid.from_replyto.address", msg);
				throw me;
			}

			// TODO implement handling of "other_email" to config entry
			// else if (ConfigEntry.ReplyTo.other_email.equals(reply)
			// && getReplyToOtherEmail().equals("") != true)
			// {
			// // need input(email) validation
			// InternetAddress replytoList[] = { new
			// InternetAddress(getConfigParam("replyto")
			// .trim()) };
			// message.setReplyTo(replytoList);
			// }
			emailMessage.setSubject(MimeUtility.encodeText(subject, "UTF-8", "Q"));
			String attachmentDirectory = configLogic.getUploadDirectory();

			// Create the message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();

			// Fill the message
			String messagetype = "text/plain; charset=UTF-8";
			if (config.useRichTextEditor())
			{
				messagetype = "text/html; charset=UTF-8";
			}

			messageBodyPart.setContent(content, messagetype);
			messageBodyPart.addHeader("Content-Transfer-Encoding", "quoted-printable");
			messageBodyPart.addHeader("Content-Type", messagetype);

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// Part two is attachment
			for (MultipartFile file : attachments.values())
			{
				// store the file for permanence
				File f = new File(attachmentDirectory + file.getOriginalFilename());
				file.transferTo(f);
				// attach the saved file to the email
				FileDataSource fds = new FileDataSource(f);
				MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.setDataHandler(new DataHandler(fds));
				attachPart.setFileName(f.getName());
				multipart.addBodyPart(attachPart);
			}
			emailMessage.setContent(multipart);

			if (config.isSendMeACopy())
			{
				emailMessage.addRecipients(Message.RecipientType.CC, fromEmail);
			}

			LinkedList<InternetAddress> emailAddresses = new LinkedList<InternetAddress>();
			for (Entry<String, String> entry : to.entrySet())
			{
				try
				{
					if (entry.getKey() != null)
					{
						InternetAddress ia = new InternetAddress(entry.getKey());
						if (entry.getValue() != null)
						{
							ia.setPersonal(entry.getValue());
						}
						emailAddresses.add(ia);
					}
					else
					{
						collectInvalids(invalids, entry);
					}
				}
				catch (AddressException e)
				{
					collectInvalids(invalids, entry);
				}
				catch (UnsupportedEncodingException ie)
				{
					collectInvalids(invalids, entry);
				}
			}
			InternetAddress[] internetAddresses = emailAddresses
					.toArray(new InternetAddress[emailAddresses.size()]);
			emailMessage.addRecipients(Message.RecipientType.BCC, internetAddresses);

			// add an identifier to the message for debugging later.
			// this helps differentiate messages sent from mail sender.
			emailMessage.addHeader("X-Mailer", "sakai-mailsender");

			// log message for debugging purposes
			if (log.isDebugEnabled())
			{
				String addresses = InternetAddress.toString(internetAddresses);
				String logMsg = "EmailBean.sendEmail(): [SITE: " + getSiteID() + "], [From: "
						+ getCurrentUserId() + "-" + fromEmail + "], [To: " + addresses
						+ "], [Subject: " + subject + "]";
				log.debug(logMsg);
			}

			if (!configLogic.isEmailTestMode())
			{
				try
				{
					Transport.send(emailMessage);
				}
				catch (MessagingException e)
				{
					String msg = e.getMessage();
					me.addMessage("exception.generic", msg);
					log.error(e.getMessage(), e);
				}
			}
			else
			{
				StringBuilder msg = new StringBuilder(
						"sendMail: from: " + arrayToStr(emailMessage.getFrom()) +
						" to: " + arrayToStr(emailMessage.getAllRecipients()) +
						" subject: " + emailMessage.getSubject() +
						" replyTo: " + arrayToStr(emailMessage.getReplyTo()));

					msg.append(
						"\n----[ HEADERS ]--------------------------------------\n" +
						headersToStr(
						emailMessage.getAllHeaders()) +
						"\n----------------------------------------------------\n");

					msg.append("----[ CONTENT ]--------------------------------------\n");

					Object messageContent = emailMessage.getContent();

					if (messageContent instanceof MimeMultipart) {
						MimeMultipart mp = (MimeMultipart) messageContent;

						msg.append(multipartToStr(mp));
					} else {
						msg.append(messageContent);
					}

					msg.append("\n----------------------------------------------------\n");

					log.info(msg.toString());
			}
		}
		catch (MessagingException msge)
		{
			String msg = msge.getMessage();
			me.addMessage("exception.generic", msg);
			log.error(msge.getMessage(), msge);
		}
		catch (IOException ie)
		{
			String msg = ie.getMessage();
			me.addMessage("exception.generic", msg);
			log.error(ie.getMessage(), ie);
		}

		if (me.hasMessages())
		{
			throw me;
		}
		else
		{
			// on success, return the invalid addresses.
			return invalids;
		}
	}

	private void collectInvalids(ArrayList<String> invalids, Entry<String, String> entry)
	{
		invalids.add("\"" + entry.getValue() + "\" <" + entry.getKey() + ">");
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setConfigLogic(ConfigLogic configLogic)
	{
		this.configLogic = configLogic;
	}

	private String multipartToStr(MimeMultipart msg) {
		StringBuilder rv = new StringBuilder();

		if (msg != null) {
			try {
				int parts = msg.getCount();

				for (int i = 0; i < parts; i++) {
					BodyPart part = msg.getBodyPart(i);

					if (part != null) {
						rv.append(part.getContent());
					}
				}
			} catch (MessagingException e) {
				log.warn("Exception aggregating email content", e);
			} catch (IOException e) {
				log.warn("Exception aggregating email content", e);
			}
		}
		
		return rv.toString();
	}

	private String headersToStr(Enumeration<Header> headers) {
		StringBuilder rv = new StringBuilder("[\n");

		if (headers != null) {
			while (headers.hasMoreElements()) {
				Header h = headers.nextElement();

				if (h != null) {
					rv.append("[ " + h.getName() + ": " + h.getValue() + " ]\n");
				}
			}
		}

		rv.append("]");

		return rv.toString();
	}

	private String arrayToStr(Object[] array)
	{
		StringBuilder buf = new StringBuilder();
		if (array != null)
		{
			buf.append("[");
			for (int i = 0; i < array.length; i++)
			{
				if (i != 0) buf.append(", ");
				buf.append(array[i].toString());
			}
			buf.append("]");
		}
		else
		{
			buf.append("");
		}

		return buf.toString();
	}
}
