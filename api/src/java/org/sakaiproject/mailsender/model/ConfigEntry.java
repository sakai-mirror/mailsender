package org.sakaiproject.mailsender.model;

/**
 * Programmatic view of the "Options" page in Mail Sender
 */
public class ConfigEntry
{
	public enum ReplyTo
	{
		sender, no_reply_to
	}

	public enum SubjectPrefixType
	{
		system, custom
	}

	public enum EditorType
	{
		fckeditor, htmlarea
	}

	public enum ConfigParams
	{
		replyto, sendmecopy, emailarchive, subjectprefix, displayinvalidemailaddrs
	}

	private String replyTo;
	private boolean displayInvalidEmails;
	private String editorType;
	private boolean sendMeACopy;
	private boolean addToArchive;
	private String subjectPrefixType;
	private String subjectPrefix;

	public ConfigEntry(String subjectPrefixType, boolean sendMeACopy,
			boolean addToArchive, String replyTo, boolean displayInvalidEmails, String editorType,
			String subjectPrefix)
	{
		setSubjectPrefixType(subjectPrefixType);
		setSubjectPrefix(subjectPrefix);
		setSendMeACopy(sendMeACopy);
		setAddToArchive(addToArchive);
		setReplyTo(replyTo);
		setDisplayInvalidEmails(displayInvalidEmails);
		setEditorType(editorType);
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
		else
			this.replyTo = ReplyTo.valueOf(replyTo).name();
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
		this.editorType = EditorType.valueOf(editorType).name();
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

	public String getSubjectPrefixType()
	{
		return subjectPrefixType;
	}

	public void setSubjectPrefixType(String subjectPrefixType)
	{
		this.subjectPrefixType = SubjectPrefixType.valueOf(subjectPrefixType).name();
	}

	public boolean useRichTextEditor()
	{
		return EditorType.fckeditor.name().equalsIgnoreCase(editorType);
	}
}