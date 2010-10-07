package org.sakaiproject.mailsender.logic.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.ContentType;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.springframework.web.multipart.MultipartFile;

public class ExternalLogicEmailServiceImpl extends ExternalLogicImpl
{
	private EmailService emailService;

	public void sendEmail(ConfigEntry config, String fromEmail, String fromName,
			Map<String, String> to, String subject, String content,
			Map<String, MultipartFile> attachments) throws MailsenderException
	{
		ArrayList<EmailAddress> tos = new ArrayList<EmailAddress>();
		for (Entry<String, String> entry : to.entrySet())
		{
			tos.add(new EmailAddress(entry.getKey(), entry.getValue()));
		}

		EmailMessage msg = new EmailMessage();

		// set the "reply to" based on config
		if (ConfigEntry.ReplyTo.no_reply_to.name().equals(config.getReplyTo()))
		{
			String replyToName = getCurrentSiteTitle();
			String replyToEmail = "";
			msg.setFrom(new EmailAddress(replyToEmail, replyToName));
		}
		else // ConfigEntry.ReplyTo.sender
		{
			msg.setFrom(new EmailAddress(fromEmail, fromName));
		}

		msg.setSubject(subject);
		// set content type based on editor used
		if (config.useRichTextEditor())
		{
			msg.setContentType(ContentType.TEXT_HTML);
		}
		else
		{
			msg.setContentType(ContentType.TEXT_PLAIN);
		}
		msg.setBody(content);

		for (Entry<String, MultipartFile> entry : attachments.entrySet())
		{
			MultipartFile mf = entry.getValue();
			File f = new File(mf.getOriginalFilename());
			try
			{
				mf.transferTo(f);
			}
			catch (IOException ioe)
			{
				throw new MailsenderException(ioe.getMessage(), ioe);
			}
			Attachment attachment = new Attachment(f);
			msg.addAttachment(attachment);
		}

		// send a copy
		if (config.isSendMeACopy())
		{
			msg.addRecipient(RecipientType.CC, fromName, fromEmail);
		}

		// add all recipients to the bcc field
		msg.addRecipients(RecipientType.BCC, tos);

		// add a special header for tracking
		msg.addHeader("X-Mailer", "sakai-mailsender");
		msg.addHeader("Content-Transfer-Encoding", "quoted-printable");

		emailService.send(msg);
	}

	public void setEmailService(EmailService emailService)
	{
		this.emailService = emailService;
	}
}
