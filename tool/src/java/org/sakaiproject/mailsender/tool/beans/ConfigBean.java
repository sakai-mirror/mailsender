package org.sakaiproject.mailsender.tool.beans;

import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.ConfigEntry.SubjectPrefixType;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class ConfigBean
{
	public static final String CONFIG_SAVED = "configSaved";
	public static final String CONFIG_SAVE_FAILED = "configSaveFail";
	public static final String CONFIG_CANCELLED = "configCancelled";

	private ConfigEntry entry;
	private ConfigLogic configLogic;
	private TargettedMessageList messages;

	public void setConfigLogic(ConfigLogic configLogic)
	{
		this.configLogic = configLogic;
	}

	public void setMessages(TargettedMessageList messageList)
	{
		this.messages = messageList;
	}

	public ConfigEntry getConfig()
	{
		if (entry == null)
		{
			entry = configLogic.getConfig();
		}
		return entry;
	}

	public String cancelConfig()
	{
		return CONFIG_CANCELLED;
	}

	public String saveConfig()
	{
		if (entry != null)
		{
			if (SubjectPrefixType.custom.name().equals(entry.getSubjectPrefixType())
					&& (entry.getSubjectPrefix() == null || entry.getSubjectPrefix().trim()
							.length() == 0))
			{
				messages.addMessage(new TargettedMessage("custom_prefix_required"));
				return CONFIG_SAVE_FAILED;
			}
			else
			{
				configLogic.saveConfig(entry);
				entry = null;
			}
		}
		return CONFIG_SAVED;
	}
}