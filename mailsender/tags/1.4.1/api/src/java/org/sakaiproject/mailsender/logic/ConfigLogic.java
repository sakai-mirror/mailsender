/**********************************************************************************
 * Copyright 2008 Sakai Foundation
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
package org.sakaiproject.mailsender.logic;

import org.sakaiproject.mailsender.model.ConfigEntry;

public interface ConfigLogic
{
	String DEFAULT_SUBJECT_PREFIX = "%site_title%:";

	String ALLOW_PREFIX_CHANGE_PROP = "mailsender.allowSubjectPrefixChange";
	String DEFAULT_SUBJECT_PREFIX_PROP = "mailsender.subjectprefix";
	String UPLOAD_DIRECTORY_PROP = "mailsender.upload.directory";
	String WSYIWYG_EDITOR_PROP = "wysiwyg.editor";

	String CONFIG_SAVED = "configSaved";

	/**
	 * Get the current tool placement config
	 *
	 * @return
	 */
	ConfigEntry getConfig();

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

	/**
	 * Allow the user to change the prefix that is prepended to the subject of emails. Set through
	 * system property "mailsender.allowSubjectPrefixChange".
	 *
	 * @return true if allowed, false otherwise [default: false]
	 */
	boolean allowSubjectPrefixChange();

	/**
	 * Returns the default subject prefix. This is normally {@link DEFAULT_SUBJECT_PREFIX} but can
	 * be set by the system property "mailsender.subjectprefix".
	 *
	 * @return
	 */
	String getDefaultSubjectPrefix();
}