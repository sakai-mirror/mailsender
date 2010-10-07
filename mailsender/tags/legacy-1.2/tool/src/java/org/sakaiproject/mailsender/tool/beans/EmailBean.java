package org.sakaiproject.mailsender.tool.beans;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
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
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.api.MailArchiveMessageEdit;
import org.sakaiproject.mailarchive.api.MailArchiveMessageHeaderEdit;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.mailsender.logic.ComposeLogic;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.EmailEntry;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.StringUtil;
import org.springframework.web.multipart.MultipartFile;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class EmailBean
{
	public static final String EMAIL_SENT = "emailSent";
	public static final String EMAIL_FAILED = "emailFailed";
	public static final String EMAIL_CANCELLED = "emailCancelled";

	private boolean debug;
	private Map<String, MultipartFile> multipartMap;
	private Log log = LogFactory.getLog(EmailBean.class);
	private ServerConfigurationService serverConfigurationService;
	private ComposeLogic composeLogic;
	private ConfigLogic configLogic;
	private ExternalLogic externalLogic;
	private EmailEntry emailEntry;
	private MailArchiveService mailArchiveService;
	private TargettedMessageList messages;
	private MessageLocator messageLocator;

	public String cancelEmail()
	{
		return EMAIL_CANCELLED;
	}

	public EmailEntry getNewEmail()
	{
		if (emailEntry == null)
		{
			emailEntry = new EmailEntry(configLogic.getConfig());
		}
		return emailEntry;
	}

	public String sendEmail()
	{
		// make sure we have a minimum of data required
		if (emailEntry == null || emailEntry.getConfig() == null)
		{
			messages.addMessage(new TargettedMessage("error.nothing.send"));
			return EMAIL_FAILED;
		}

		ConfigEntry config = emailEntry.getConfig();
		User curUser = externalLogic.getCurrentUser();

		String fromEmail = "";
		String fromDisplay = "";
		if (curUser != null)
		{
			fromEmail = curUser.getEmail();
			fromDisplay = curUser.getDisplayName();
		}
		String fromString = fromDisplay + " <" + fromEmail + ">";

		InternetAddress from = null;
		try
		{
			from = new InternetAddress(fromEmail, true);
			from.setPersonal(fromDisplay);
		}
		catch (AddressException ae)
		{
			String[] msg = { ae.getMessage() + ": " + fromEmail };
			messages.addMessage(new TargettedMessage("exception.generic", msg));
			log.error(messageLocator.getMessage("exception.generic", msg), ae);
			return EMAIL_FAILED;
		}
		catch (UnsupportedEncodingException uee)
		{
			String[] msg = { uee.getMessage() + ": " + fromDisplay };
			messages.addMessage(new TargettedMessage("exception.generic", msg));
			log.error(messageLocator.getMessage("exception.generic", msg), uee);
			return EMAIL_FAILED;
		}

		String content = emailEntry.getContent();

		HashSet<InternetAddress> emailusers = new HashSet<InternetAddress>();

		// compile the list of emails to send to
		HashSet<String> badEmailAddresses = compileEmailList(fromEmail, emailusers);

		// handle the other recipients
		List<String> emailOthers = emailEntry.getOtherRecipients();
		for (String email : emailOthers)
		{
			try
			{
				InternetAddress address = new InternetAddress(email, true);
				emailusers.add(address);
			}
			catch (AddressException ae)
			{
				badEmailAddresses.add(email);
			}
		}

		if (emailusers.size() == 0)
		{
			if (badEmailAddresses.size() != 0)
				messages.addMessage(new TargettedMessage("error.no.valid.recipients"));
			else
				messages.addMessage(new TargettedMessage("error.no.recipients"));
			return EMAIL_FAILED;
		}

		String subjectPrefix = StringUtil.trimToNull(config.getSubjectPrefix());

		String subject = (subjectPrefix != null ? subjectPrefix + ": " : "")
				+ emailEntry.getSubject();

		// append to the email archive
		String siteId = externalLogic.getSiteID();
		addToArchive(fromString, subject, siteId);

		String smtp_server = serverConfigurationService
				.getString("smtp@org.sakaiproject.email.api.EmailService");
		// String smtp_port = serverConfigurationService.getString("smtp.port");
		try
		{
			Properties props = new Properties();
			props.put("mail.smtp.host", smtp_server);
			// props.put("mail.smtp.port", smtp_port);
			Session s = Session.getInstance(props, null);

			MimeMessage emailMessage = new MimeMessage(s);
			emailMessage.setSentDate(new Date()); // fix of SAK-11181
			if (ConfigEntry.ReplyTo.no_reply_to.name().equals(config.getReplyTo()))
			{
				String replyToName = externalLogic.getCurrentSiteTitle();
				String replyToEmail = "";
				try
				{
					InternetAddress noreplyemail = new InternetAddress(replyToEmail, replyToName);
					emailMessage.setFrom(noreplyemail);
				}
				catch (AddressException ae)
				{
					String[] msg = { ae.getMessage() + ": " + replyToName + " " + replyToEmail};
					messages.addMessage(new TargettedMessage("error.replyto", msg));
					log.error(messageLocator.getMessage("error.replyto", msg), ae);
					return EMAIL_FAILED;
				}
			}
			else // ConfigEntry.ReplyTo.sender
			{
				emailMessage.setFrom(from);
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
				messagetype = "text/html; charset=UTF-8";

			messageBodyPart.setContent(content, messagetype);
			messageBodyPart.addHeader("Content-Transfer-Encoding", "quoted-printable");
			messageBodyPart.addHeader("Content-Type", messagetype);

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// Part two is attachment
			for (MultipartFile file : multipartMap.values())
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
			InternetAddress[] emailAddresses = emailusers.toArray(new InternetAddress[emailusers
					.size()]);
			emailMessage.addRecipients(Message.RecipientType.BCC, emailAddresses);

			// log and send message
			String addresses = InternetAddress.toString(emailAddresses);
			String logMsg = "EmailBean.sendEmail(): [SITE: " + siteId + "], [From: "
					+ externalLogic.getCurrentUserId() + "-" + fromEmail + "], [To: " + addresses
					+ "], [Subject: " + subject + "]";

			// build output message for results screen
			for (InternetAddress addr : emailAddresses)
			{
				String addrStr = null;
				if (addr.getPersonal() != null)
					addrStr = addr.getPersonal();
				else
					addrStr = addr.getAddress();
				messages.addMessage(new TargettedMessage("verbatim",
						new String[] { addrStr }, TargettedMessage.SEVERITY_INFO));
			}

			if (!debug)
			{
				try
				{
					// add an identifier to the message for debugging later.
					// this helps differentiate messages sent from mail sender.
					emailMessage.addHeader("X-Sakai-Client-Id", "mailsender");
					Transport.send(emailMessage);
					log.info(logMsg);
				}
				catch (MessagingException e)
				{
					String[] msg = { e.getMessage() };
					messages.addMessage(new TargettedMessage("exception.generic", msg));
					log.error(messageLocator.getMessage("exception.generic", msg), e);
				}
			}
			else
			{
				log.debug(logMsg);
			}

			// Display Users with Bad Emails if the option is turned on.
			boolean showBadEmails = config.isDisplayInvalidEmails();
			if (showBadEmails && badEmailAddresses.size() > 0)
			{
				// add the message for the result screen
				String names = badEmailAddresses.toString();
				messages.addMessage(new TargettedMessage("invalid.email.addresses",
						new String[] { names.substring(1, names.length() - 1) },
						TargettedMessage.SEVERITY_INFO));
			}
		}
		catch (AddressException ae)
		{
			String[] msg = { ae.getMessage() };
			messages.addMessage(new TargettedMessage("exception.generic", msg));
			log.error(messageLocator.getMessage("exception.generic", ae.getMessage()), ae);
		}
		catch (MessagingException me)
		{
			String[] msg = { me.getMessage() };
			messages.addMessage(new TargettedMessage("exception.generic", msg));
			log.error(messageLocator.getMessage("exception.generic", msg), me);
		}
		catch (UnsupportedEncodingException uee)
		{
			String[] msg = { uee.getMessage() };
			messages.addMessage(new TargettedMessage("exception.generic", msg));
			log.error(messageLocator.getMessage("exception.generic", msg), uee);
		}
		catch (IOException ie)
		{
			String[] msg = { ie.getMessage() };
			messages.addMessage(new TargettedMessage("exception.generic", msg));
			log.error(messageLocator.getMessage("exception.generic", msg), ie);
		}

		return EMAIL_SENT;
	}

	private void addToArchive(String fromString, String subject, String siteId)
	{
		if (emailEntry.getConfig().isAddToArchive())
		{
			StringBuilder attachment_info = new StringBuilder("<br/>");
			int i = 1;
			for (MultipartFile file : multipartMap.values())
			{
				if (file.getSize() > 0)
				{
					attachment_info.append("<br/>");
					attachment_info.append("Attachment #").append(i).append(": ").append(
							file.getName()).append("(").append(file.getSize()).append(" Bytes)");
					i++;
				}
			}
			String emailarchive = "/mailarchive/channel/" + siteId + "/main";
			String content = emailEntry.getContent() + attachment_info.toString();
			appendToArchive(emailarchive, fromString, subject, content);
		}
	}

	/**
	 * Compiles a list of email recipients from role, group and section selections.
	 * 
	 * @param fromEmail
	 * @param emailusers
	 * @return Non-null <code>List</code> of users that have bad email addresses.
	 */
	private HashSet<String> compileEmailList(String fromEmail, HashSet<InternetAddress> emailusers)
	{
		HashSet<String> badEmails = new HashSet<String>();
		// check for roles and add users
		for (String roleId : emailEntry.getRoleIds().keySet())
		{
			try
			{
				List<User> users = composeLogic.getUsersByRole(roleId);
				badEmails.addAll(addEmailUsers(fromEmail, emailusers, users));
			}
			catch (IdUnusedException e)
			{
				log.warn(e.getMessage(), e);
			}
		}

		// check for sections and add users
		for (String sectionId : emailEntry.getSectionIds().keySet())
		{
			try
			{
				List<User> users = composeLogic.getUsersByGroup(sectionId);
				badEmails.addAll(addEmailUsers(fromEmail, emailusers, users));
			}
			catch (IdUnusedException e)
			{
				log.warn(e.getMessage(), e);
			}
		}

		// check for groups and add users
		for (String groupId : emailEntry.getGroupIds().keySet())
		{
			try
			{
				List<User> users = composeLogic.getUsersByGroup(groupId);
				badEmails.addAll(addEmailUsers(fromEmail, emailusers, users));
			}
			catch (IdUnusedException e)
			{
				log.warn(e.getMessage(), e);
			}
		}

		for (String userId : emailEntry.getUserIds().keySet())
		{
			User user = externalLogic.getUser(userId);
			badEmails.addAll(addEmailUser(fromEmail, emailusers, user));
		}
		return badEmails;
	}

	/**
	 * Add users to the email list and perform validation. Will not add "fromEmail" to the list.
	 * 
	 * @param fromEmail
	 * @param emailusers
	 * @param users
	 * @return Non-null <code>List</code> of users that didn't pass validation
	 */
	private HashSet<String> addEmailUsers(String fromEmail, HashSet<InternetAddress> emailusers,
			List<User> users)
	{
		HashSet<String> badUsers = new HashSet<String>();
		for (User user : users)
		{
			badUsers.addAll(addEmailUser(fromEmail, emailusers, user));
		}
		return badUsers;
	}

	/**
	 * Add user to the email list and perform validation. Will not add "fromEmail" or duplicates to
	 * the list.
	 * 
	 * @param fromEmail
	 * @param emailusers
	 * @param users
	 * @return Non-null <code>List</code> of users that didn't pass validation
	 */
	private HashSet<String> addEmailUser(String fromEmail, HashSet<InternetAddress> emailusers,
			User user)
	{
		HashSet<String> badUsers = new HashSet<String>();
		if (!fromEmail.equals(user.getEmail()))
		{
			try
			{
				InternetAddress address = new InternetAddress(user.getEmail(), true);
				address.setPersonal(user.getDisplayName());
				emailusers.add(address);
			}
			catch (AddressException ae)
			{
				badUsers.add(user.getEmail());
			}
			catch (UnsupportedEncodingException ue)
			{
				badUsers.add(user.getEmail());
			}
		}
		return badUsers;
	}

	/**
	 * Append email to Email Archive
	 * 
	 * @param channelRef
	 * @param sender
	 * @param subject
	 * @param body
	 * @return true if success
	 */
	protected boolean appendToArchive(String channelRef, String sender, String subject, String body)
	{
		boolean retval = true;
		MailArchiveChannel channel = null;
		try
		{
			channel = mailArchiveService.getMailArchiveChannel(channelRef);
		}
		catch (Exception e)
		{
			log.debug("Exception: Mailsender.appendToArchive() #1, " + e.getMessage());
			return false;
		}
		if (channel == null)
		{
			log.debug("Mailsender: The channel: " + channelRef + " is null.");

			return false;
		}
		List<String> mailHeaders = new ArrayList<String>();
		if (emailEntry.getConfig().useRichTextEditor())
		{
			mailHeaders.add(MailArchiveService.HEADER_OUTER_CONTENT_TYPE
					+ ": text/html; charset=ISO-8859-1");
			mailHeaders.add(MailArchiveService.HEADER_INNER_CONTENT_TYPE
					+ ": text/html; charset=ISO-8859-1");
		}
		else
		{
			mailHeaders.add(MailArchiveService.HEADER_OUTER_CONTENT_TYPE
					+ ": text/plain; charset=ISO-8859-1");
			mailHeaders.add(MailArchiveService.HEADER_INNER_CONTENT_TYPE
					+ ": text/plain; charset=ISO-8859-1");
		}
		mailHeaders.add("Mime-Version: 1.0");
		mailHeaders.add("From: " + sender);
		mailHeaders.add("Reply-To: " + sender);
		try
		{
			// This way actually sends the email too
			// channel.addMailArchiveMessage(subject, sender,
			// TimeService.newTime(), mailHeaders, null, body);
			MailArchiveMessageEdit edit = (MailArchiveMessageEdit) channel.addMessage();
			MailArchiveMessageHeaderEdit header = edit.getMailArchiveHeaderEdit();
			edit.setBody(body);
			header.replaceAttachments(null);
			header.setSubject(subject);
			header.setFromAddress(sender);
			header.setDateSent(TimeService.newTime());
			header.setMailHeaders(mailHeaders);
			channel.commitMessage(edit, NotificationService.NOTI_NONE);
		}
		catch (Exception e)
		{
			log.debug("Exception: Mailsender.appendToArchive() #2, " + e.getMessage());

			retval = false;
		}
		return retval;
	}

	public void setComposeLogic(ComposeLogic composeLogic)
	{
		this.composeLogic = composeLogic;
	}

	public void setConfigLogic(ConfigLogic configLogic)
	{
		this.configLogic = configLogic;
	}

	public void setExternalLogic(ExternalLogic externalLogic)
	{
		this.externalLogic = externalLogic;
	}

	public void setMultipartMap(Map<String, MultipartFile> multipartMap)
	{
		this.multipartMap = multipartMap;
	}

	public void setMailArchiveService(MailArchiveService mailArchiveService)
	{
		this.mailArchiveService = mailArchiveService;
	}

	public void setServerConfigurationService(ServerConfigurationService scs)
	{
		this.serverConfigurationService = scs;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public void setMessages(TargettedMessageList messages)
	{
		this.messages = messages;
	}

	public void setMessageLocator(MessageLocator messageLocator)
	{
		this.messageLocator = messageLocator;
	}
}