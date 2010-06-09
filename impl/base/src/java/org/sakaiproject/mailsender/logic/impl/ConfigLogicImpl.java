package org.sakaiproject.mailsender.logic.impl;

import java.io.File;
import java.util.Properties;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.ConfigEntry.ConfigParams;
import org.sakaiproject.mailsender.model.ConfigEntry.EditorType;
import org.sakaiproject.mailsender.model.ConfigEntry.ReplyTo;
import org.sakaiproject.mailsender.model.ConfigEntry.SubjectPrefixType;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;

public class ConfigLogicImpl implements ConfigLogic
{
	private ExternalLogic externalLogic;
	private ToolManager toolManager;
	private ServerConfigurationService serverConfigurationService;

	public ConfigLogicImpl()
	{
	}

	public void setExternalLogic(ExternalLogic externalLogic)
	{
		this.externalLogic = externalLogic;
	}

	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	public ConfigEntry getConfig()
	{
		Properties props = getToolProps();

		// subject prefix type
		String prefixType = getSubjectPrefixType(props);

		// subject prefix
		String subjectPrefix = getSubjectPrefix(props);

		// send me a copy
		boolean sendMeACopy = isSendMeACopy(props);

		// add to email archive
		boolean addToArchive = isAddToArchive(props);

		// reply to
		String replyTo = getReplyTo(props);

		// editor type
		String editorType = getEditorType();

		// display invalid email addresses
		boolean displayInvalidEmails = isDisplayInvalidEmailAddrs(props);

		ConfigEntry config = new ConfigEntry(prefixType, sendMeACopy, addToArchive, replyTo,
				displayInvalidEmails, editorType, subjectPrefix);
		return config;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ConfigLogic#useRichTextEditor
	 */
	public boolean useRichTextEditor()
	{
		String editor = getEditorType();
		return EditorType.fckeditor.name().equalsIgnoreCase(editor);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.mailsender.logic.ConfigLogic#allowSubjectPrefixChange()
	 */
	public boolean allowSubjectPrefixChange()
	{
		boolean allow = serverConfigurationService.getBoolean(ALLOW_PREFIX_CHANGE_PROP, false);
		return allow;
	}

	public String saveConfig(ConfigEntry ce)
	{
		Properties props = getToolProps();

		String displayInvalidEmails = Boolean.toString(ce.isDisplayInvalidEmails());
		props.setProperty(ConfigParams.displayinvalidemailaddrs.name(), displayInvalidEmails);

		String addToArchive = Boolean.toString(ce.isAddToArchive());
		props.setProperty(ConfigParams.emailarchive.name(), addToArchive);

		String replyTo = StringUtil.trimToZero(ce.getReplyTo());
		props.setProperty(ConfigParams.replyto.name(), replyTo);

		String sendMeACopy = Boolean.toString(ce.isSendMeACopy());
		props.setProperty(ConfigParams.sendmecopy.name(), sendMeACopy);

		if (allowSubjectPrefixChange())
		{
			String prefix = "";
			if (SubjectPrefixType.custom.name().equals(ce.getSubjectPrefixType()))
			{
				prefix = StringUtil.trimToZero(ce.getSubjectPrefix());
			}
			props.setProperty(ConfigParams.subjectprefix.name(), prefix);
		}

		toolManager.getCurrentPlacement().save();

		return CONFIG_SAVED;
	}

	public String getUploadDirectory()
	{
		String uploadDir = "/tmp/";
		String ud = serverConfigurationService.getString(UPLOAD_DIRECTORY_PROP);
		ud = StringUtil.trimToNull(ud);
		if (ud != null)
		{
			File dir = new File(ud);
			if (dir.isDirectory())
				uploadDir = ud;
		}
		return uploadDir;
	}

	public String getDefaultSubjectPrefix()
	{
		String defaultPrefix = serverConfigurationService.getString(DEFAULT_SUBJECT_PREFIX_PROP,
				DEFAULT_SUBJECT_PREFIX);
		return defaultPrefix;
	}

	/**
	 * Retrieve the display invalid email addresses option from the tool config
	 * 
	 * @param props
	 *            The tool config properties
	 * @return
	 */
	private boolean isDisplayInvalidEmailAddrs(Properties props)
	{
		String displayinvalidemailaddrs = props.getProperty(ConfigParams.displayinvalidemailaddrs
				.name());
		boolean displayInvalidEmails = parseConfigBoolean(displayinvalidemailaddrs);
		return displayInvalidEmails;
	}

	/**
	 * Retrieve the editor type from the tool configuration, lastly checking the system config
	 * 
	 * @see org.sakaiproject.mailsender.model.ConfigEntry.EditorType
	 * @return
	 */
	private String getEditorType()
	{
		// check the tool config
		String editorType = serverConfigurationService.getString(WSYIWYG_EDITOR_PROP);
		if (StringUtil.trimToNull(editorType) != null)
			editorType = editorType.trim().toLowerCase();
		else
			editorType = EditorType.fckeditor.name();
		return editorType;
	}

	/**
	 * Retrieves the reply to option from the tool configuration<br/>
	 * <br/>
	 * The original implementation of mailsender used yes/no instead of sender/no_reply_to. Rather
	 * than run a db conversion to the new values, the below checks are made to slowly migrate the
	 * data
	 * 
	 * @param props
	 * @see org.sakaiproject.mailsender.model.ConfigEntry.ReplyTo
	 * @return
	 */
	private String getReplyTo(Properties props)
	{
		String replyTo = (String) props.getProperty(ConfigParams.replyto.name());
		if ("no".equalsIgnoreCase(replyTo) || ReplyTo.no_reply_to.name().equalsIgnoreCase(replyTo))
			replyTo = ReplyTo.no_reply_to.name();
		else
			replyTo = ReplyTo.sender.name();
		return replyTo;
	}

	/**
	 * Determines the type of subject prefix by comparing the prefix to null and to the default,
	 * system wide prefix.
	 * 
	 * @param props
	 * @return {@link SubjectPrefixType.system} if null or matches default,
	 *         {@link SubjectPrefixType.custom} otherwise
	 */
	private String getSubjectPrefixType(Properties props)
	{
		String prefixType = SubjectPrefixType.system.name();

		// look up the locally set prefix
		String subjectPrefix = props.getProperty(ConfigParams.subjectprefix.name());

		// if no local prefix, use the system default prefix
		if (StringUtil.trimToNull(subjectPrefix) != null
				&& !getDefaultSubjectPrefix().equals(subjectPrefix))
		{
			prefixType = SubjectPrefixType.custom.name();
		}
		return prefixType;
	}

	/**
	 * <p>
	 * Retrieves the subject prefix from the tool configuration. If no locally set prefix, the
	 * system default prefix is uesd (property: mailsender.subjectprefix). If no system default
	 * prefix found, "%site_tite%: " is used.
	 * </p>
	 * <p>
	 * %site_title% is a special term that is replaced with the current site title.
	 * </p>
	 * 
	 * @param props
	 *            The tool config properties
	 * @return
	 */
	private String getSubjectPrefix(Properties props)
	{
		// use locally set prefix
		String subjectPrefix = props.getProperty(ConfigParams.subjectprefix.name());

		// if no local prefix, use the system default prefix
		if (StringUtil.trimToNull(subjectPrefix) == null)
		{
			subjectPrefix = getDefaultSubjectPrefix();
		}
		subjectPrefix = StringUtil.trimToNull(subjectPrefix);
		if (subjectPrefix != null)
		{
			// add a space to the end of the prefix to separate it from the actual subject
			// this is added here because properties drop trailing spaces
			subjectPrefix = subjectPrefix.replaceAll("%site_title%", externalLogic
					.getCurrentSiteTitle()) + " ";
		}
		return subjectPrefix;
	}

	/**
	 * Retrieve the send me a copy option from the tool configuration
	 * 
	 * @param props
	 *            The tool config properties
	 * @return
	 */
	private boolean isSendMeACopy(Properties props)
	{
		String sendmecopy = (String) props.getProperty(ConfigParams.sendmecopy.name());
		boolean sendMeACopy = parseConfigBoolean(sendmecopy);
		return sendMeACopy;
	}

	/**
	 * Retrieve the add to email archive option from the tool configuration
	 * 
	 * @param props
	 *            The tool config properties
	 * @return
	 */
	private boolean isAddToArchive(Properties props)
	{
		String emailarchive = (String) props.getProperty(ConfigParams.emailarchive.name());
		boolean addToArchive = parseConfigBoolean(emailarchive);
		return addToArchive;
	}

	/**
	 * The original implementation of mailsender used yes/no instead of true/false, so rather than
	 * run a db conversion to the new values, the conversion is made as the data is accessed
	 * 
	 * @param val
	 * @return
	 */
	private boolean parseConfigBoolean(String val)
	{
		boolean retval = false;
		if ("yes".equalsIgnoreCase(val))
		{
			retval = true;
		}
		else
		{
			retval = Boolean.parseBoolean(val);
		}
		return retval;
	}

	/**
	 * Get properties associated to the current place of this tool
	 * 
	 * @return
	 */
	private Properties getToolProps()
	{
		Properties toolProps = toolManager.getCurrentPlacement().getPlacementConfig();
		return toolProps;
	}
}