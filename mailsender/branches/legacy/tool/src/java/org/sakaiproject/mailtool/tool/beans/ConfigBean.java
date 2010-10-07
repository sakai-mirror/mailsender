package org.sakaiproject.mailtool.tool.beans;

import org.sakaiproject.mailtool.logic.ConfigLogic;
import org.sakaiproject.mailtool.model.ConfigEntry;

public class ConfigBean
{
	public static final String CONFIG_SAVED = "configSaved";
	public static final String CONFIG_CANCELLED = "configCancelled";

	private ConfigLogic configLogic;
	private ConfigEntry entry;

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
			configLogic.saveConfig(entry);
			entry = null;
		}
		return CONFIG_SAVED;
	}

	public void setConfigLogic(ConfigLogic cl)
	{
		this.configLogic = cl;
	}
}