package org.sakaiproject.mailtool.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Programmatic view the "Options" page in Mailtool
 * 
 * @author Carl Hall <carl.hall@et.gatech.edu>
 */
public class ConfigEntry
{
	private Log log = LogFactory.getLog(ConfigEntry.class);

	public interface ConfigEntryOption
	{
		String name();

		String labelKey();
	}

	public enum UserViewSelectType implements ConfigEntryOption
	{
		user("options_users"), tree("options_tree"), sidebyside("options_sidebyside"), foothill(
				"options_foothill");

		private String labelKey;

		UserViewSelectType(String labelKey)
		{
			this.labelKey = labelKey;
		}

		public String labelKey()
		{
			return labelKey;
		}
	}

	public enum ReplyTo implements ConfigEntryOption
	{
		sender("options_replylabel1"), no_reply_to("options_replylabel2");
//		other_email("options_replylabel3");

		private String labelKey;

		ReplyTo(String labelKey)
		{
			this.labelKey = labelKey;
		}

		public String labelKey()
		{
			return labelKey;
		}
	}

	public enum EditorType
	{
		fckeditor, htmlarea
	}

	public enum ConfigParams
	{
		replyto, sendmecopy, emailarchive, messageformat, subjectprefix, recipview,
		displayinvalidemailaddrs, wysiwygeditor
	}

	private String viewSelectType;
	private String replyTo;
	private boolean displayInvalidEmails;
	private String editorType;
	private boolean sendMeACopy;
	private boolean addToArchive;
	private String subjectPrefix;

	public ConfigEntry(String viewSelectType, boolean sendMeACopy, boolean addToArchive,
			String replyTo, boolean displayInvalidEmails, String editorType, String subjectPrefix)
	{
		this.subjectPrefix = subjectPrefix;
		this.sendMeACopy = sendMeACopy;
		this.addToArchive = addToArchive;
		this.viewSelectType = viewSelectType;
		this.replyTo = replyTo;
		this.displayInvalidEmails = displayInvalidEmails;
		this.editorType = editorType;
	}

	public String getViewSelectType()
	{
		return viewSelectType;
	}

	public void setViewSelectType(String viewSelectType)
	{
		try
		{
			this.viewSelectType = UserViewSelectType.valueOf(viewSelectType).name();
		}
		catch (IllegalArgumentException e)
		{
			log.warn("Unknown 'editor type' option: " + replyTo);
		}
	}

	public String getReplyTo()
	{
		return replyTo;
	}

	public void setReplyTo(String replyTo)
	{
		if ("yes".equals(replyTo))
			this.replyTo = ReplyTo.sender.name();
		else if ("no".equals(replyTo))
			this.replyTo = ReplyTo.no_reply_to.name();
//		else if ("otheremail".equals(replyTo))
//			this.replyTo = ReplyTo.other_email.name();
		else
		{
			try
			{
				this.replyTo = ReplyTo.valueOf(replyTo).name();
			}
			catch (IllegalArgumentException e)
			{
				log.warn("Unknown 'reply to' option: " + replyTo);
			}
		}
	}

	public boolean isDisplayInvalidEmails()
	{
		return displayInvalidEmails;
	}

	public void setDisplayInvalidEmails(boolean displayInvalidEmails)
	{
		this.displayInvalidEmails = displayInvalidEmails;
	}

	public String getEditorType()
	{
		return editorType;
	}

	public void setEditorType(String editorType)
	{
		try
		{
			this.editorType = EditorType.valueOf(editorType).name();
		}
		catch (IllegalArgumentException e)
		{
			log.warn("Unknown 'editor type' option: " + replyTo);
		}
	}

	public boolean isSendMeACopy()
	{
		return sendMeACopy;
	}

	public void setSendMeACopy(boolean sendMeACopy)
	{
		this.sendMeACopy = sendMeACopy;
	}

	public boolean isAddToArchive()
	{
		return addToArchive;
	}

	public void setAddToArchive(boolean addToArchive)
	{
		this.addToArchive = addToArchive;
	}

	public String getSubjectPrefix()
	{
		return subjectPrefix;
	}

	public void setSubjectPrefix(String subjectPrefix)
	{
		this.subjectPrefix = subjectPrefix;
	}

	public boolean useRichTextEditor()
	{
		return EditorType.fckeditor.name().equalsIgnoreCase(editorType);
	}
}