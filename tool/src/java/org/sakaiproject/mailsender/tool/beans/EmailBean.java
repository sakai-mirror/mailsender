package org.sakaiproject.mailsender.tool.beans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.logic.ComposeLogic;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.EmailEntry;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.StringUtil;
import org.springframework.web.multipart.MultipartFile;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class EmailBean
{
	public static final String EMAIL_SENT = "emailSent";
	public static final String EMAIL_FAILED = "emailFailed";
	public static final String EMAIL_CANCELLED = "emailCancelled";

	private Map<String, MultipartFile> multipartMap;
	private Log log = LogFactory.getLog(EmailBean.class);
	private ComposeLogic composeLogic;
	private ConfigLogic configLogic;
	private ExternalLogic externalLogic;
	private EmailEntry emailEntry;
	private TargettedMessageList messages;

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

		String content = emailEntry.getContent();

		HashMap<String, String> emailusers = new HashMap<String, String>();

		// compile the list of emails to send to
		HashSet<String> badEmailAddresses = compileEmailList(fromEmail, emailusers);

		// handle the other recipients
		List<String> emailOthers = emailEntry.getOtherRecipients();
		for (String email : emailOthers)
		{
			emailusers.put(email, null);
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

		try
		{
			externalLogic.sendEmail(config, fromEmail, fromDisplay, emailusers, subject, content,
					multipartMap);

			// build output message for results screen
			for (Entry<String, String> entry : emailusers.entrySet())
			{
				String addrStr = null;
				if (entry.getValue() != null)
					addrStr = entry.getValue();
				else
					addrStr = entry.getKey();
				messages.addMessage(new TargettedMessage("verbatim", new String[] { addrStr },
						TargettedMessage.SEVERITY_INFO));
			}

			// append to the email archive
			String siteId = externalLogic.getSiteID();
			String fromString = fromDisplay + " <" + fromEmail + ">";
			addToArchive(config, fromString, subject, siteId);
		}
		catch (MailsenderException me)
		{
			messages.addMessage(new TargettedMessage("verbatim", me.getMessage()));
			return EMAIL_FAILED;
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

		return EMAIL_SENT;
	}

	private void addToArchive(ConfigEntry config, String fromString, String subject, String siteId)
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
			externalLogic.addToArchive(config, emailarchive, fromString, subject, content);
		}
	}

	/**
	 * Compiles a list of email recipients from role, group and section selections.
	 * 
	 * @param fromEmail
	 * @param emailusers
	 * @return Non-null <code>List</code> of users that have bad email addresses.
	 */
	private HashSet<String> compileEmailList(String fromEmail, HashMap<String, String> emailusers) // HashSet<EmailAddress>
																									// emailusers)
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
	private HashSet<String> addEmailUsers(String fromEmail, HashMap<String, String> emailusers,
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
	private HashSet<String> addEmailUser(String fromEmail, HashMap<String, String> emailusers,
			User user)
	{
		HashSet<String> badUsers = new HashSet<String>();
		if (!fromEmail.equals(user.getEmail()))
		{
			emailusers.put(user.getEmail(), user.getDisplayName());
		}
		return badUsers;
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

	public void setMessages(TargettedMessageList messages)
	{
		this.messages = messages;
	}
}