package org.sakaiproject.mailtool.logic;

import org.sakaiproject.mailtool.model.ConfigEntry;

public interface ConfigLogic
{
	String CONFIG_SAVED = "configSaved";

	/**
	 * Get the current tool placement config
	 * 
	 * @return
	 */
	ConfigEntry getConfig();

	/**
	 * Get the user view selector type from the tool placement config
	 * 
	 * @return The view selector type found in the tool placement config
	 */
	String getViewSelectType();

	/**
	 * Determine if a rich text editor should be used by checking the settings in the tool placement
	 * config
	 * 
	 * @return true if value is not "htmlarea", false otherwise
	 */
	boolean useRichTextEditor();

	/**
	 * Save the configuration to the tool placement config space
	 * 
	 * @param ce
	 * @return "saveConfig" is returned upon completion
	 */
	String saveConfig(ConfigEntry ce);

	/**
	 * Get the directory where uploads should be stored
	 * 
	 * @return
	 */
	String getUploadDirectory();
}