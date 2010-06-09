package org.sakaiproject.mailsender.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailEntry
{
	private Map<String, String> roleIds = new HashMap<String, String>();
	private Map<String, String> sectionIds = new HashMap<String, String>();
	private Map<String, String> groupIds = new HashMap<String, String>();
	private Map<String, String> userIds = new HashMap<String, String>();
	private String from;
	private List<String> otherRecipients = new ArrayList<String>();
	private String subject = "";
	private String content = "";
	private List<String> attachments = new ArrayList<String>();
	private ConfigEntry config;

	public EmailEntry(ConfigEntry config)
	{
		this.config = config;
	}

	public String getFrom()
	{
		return from;
	}

	public void setFrom(String from)
	{
		this.from = from;
	}

	public List<String> getOtherRecipients()
	{
		return otherRecipients;
	}

	public void setOtherRecipients(String otherRecipients)
	{
		this.otherRecipients = new ArrayList<String>();
		if (otherRecipients != null && otherRecipients.trim().length() > 0)
		{
			String[] rcpts = otherRecipients.replace(';', ',').split(",");
			for (String rcpt : rcpts)
			{
				this.otherRecipients.add(rcpt.trim());
			}

			if (this.otherRecipients.size() == 0)
				this.otherRecipients.add(otherRecipients.trim());
		}
	}

	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		if (subject == null)
			this.subject = "";
		else
			this.subject = subject;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		if (content == null)
			this.content = "";
		else
			this.content = content;
	}

	public List<String> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(List<String> attachments)
	{
		this.attachments = attachments;
	}

	public ConfigEntry getConfig()
	{
		return config;
	}

	public void setOtherRecipients(List<String> otherRecipients)
	{
		this.otherRecipients = otherRecipients;
	}

	public void setConfig(ConfigEntry config)
	{
		this.config = config;
	}

	public Map<String, String> getRoleIds()
	{
		return roleIds;
	}

	public void setRoleIds(Map<String, String> roleIds)
	{
		this.roleIds = roleIds;
	}

	public Map<String, String> getSectionIds()
	{
		return sectionIds;
	}

	public void setSectionIds(Map<String, String> sectionIds)
	{
		this.sectionIds = sectionIds;
	}

	public Map<String, String> getGroupIds()
	{
		return groupIds;
	}

	public void setGroupIds(Map<String, String> groupIds)
	{
		this.groupIds = groupIds;
	}

	public Map<String, String> getUserIds()
	{
		return userIds;
	}

	public void setUserIds(Map<String, String> userIds)
	{
		this.userIds = userIds;
	}
}