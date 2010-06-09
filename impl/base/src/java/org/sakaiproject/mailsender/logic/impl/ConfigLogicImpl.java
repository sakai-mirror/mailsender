package org.sakaiproject.mailsender.logic.impl;

import java.io.File;
import java.util.Properties;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.ConfigEntry.ConfigParams;
import org.sakaiproject.mailsender.model.ConfigEntry.EditorType;
import org.sakaiproject.mailsender.model.ConfigEntry.ReplyTo;
import org.sakaiproject.mailsender.model.ConfigEntry.UserViewSelectType;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;

public class ConfigLogicImpl implements ConfigLogic
{

	private ToolManager toolManager;
	private ServerConfigurationService serverConfigurationService;

	public void init()
	{
	}

	public ConfigEntry getConfig()
	{
		Properties props = getToolProps();

		// subject prefix
		String subjectPrefix = getSubjectPrefix(props);

		// send me a copy
		boolean sendMeACopy = isSendMeACopy(props);

		// add to email archive
		boolean addToArchive = isAddToArchive(props);

		// user view select type
		String viewSelectType = getViewSelectType(props);

		// reply to
		String replyTo = getReplyTo(props);

		// editor type
		String editorType = getEditorType(props);

		// display invalid email addresses
		boolean displayInvalidEmails = isDisplayInvalidEmailAddrs(props);

		ConfigEntry config = new ConfigEntry(viewSelectType, sendMeACopy, addToArchive, replyTo,
				displayInvalidEmails, editorType, subjectPrefix);
		return config;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ConfigLogic#useRichTextEditor
	 */
	public boolean useRichTextEditor()
	{
		Properties props = getToolProps();
		String editor = getEditorType(props);
		return EditorType.fckeditor.name().equalsIgnoreCase(editor);
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.ConfigLogic#getViewSelectType
	 */
	public String getViewSelectType()
	{
		Properties props = getToolProps();
		String viewSelectType = getViewSelectType(props);
		return viewSelectType;
	}

	public String saveConfig(ConfigEntry ce)
	{
		Properties props = getToolProps();

		String displayInvalidEmails = Boolean.toString(ce.isDisplayInvalidEmails());
		props.setProperty(ConfigParams.displayinvalidemailaddrs.name(), displayInvalidEmails);

		String addToArchive = Boolean.toString(ce.isAddToArchive());
		props.setProperty(ConfigParams.emailarchive.name(), addToArchive);

		String viewSelectType = StringUtil.trimToZero(ce.getViewSelectType());
		props.setProperty(ConfigParams.recipview.name(), viewSelectType);

		String replyTo = StringUtil.trimToZero(ce.getReplyTo());
		props.setProperty(ConfigParams.replyto.name(), replyTo);

		String sendMeACopy = Boolean.toString(ce.isSendMeACopy());
		props.setProperty(ConfigParams.sendmecopy.name(), sendMeACopy);

		String subjectPrefix = StringUtil.trimToZero(ce.getSubjectPrefix());
		props.setProperty(ConfigParams.subjectprefix.name(), subjectPrefix);

//		String editorType = StringUtil.trimToZero(ce.getEditorType());
//		props.setProperty(ConfigParams.wysiwygeditor.name(), editorType);
//		// remove the property by old name to convert to enum name
//		props.remove("wysiwyg.editor");

		toolManager.getCurrentPlacement().save();

		return CONFIG_SAVED;
	}

	public String getUploadDirectory()
	{
		String uploadDir = "/tmp/";
		String ud = serverConfigurationService.getString("mailsender.upload.directory");
		ud = StringUtil.trimToNull(ud);
		if (ud != null)
		{
			File dir = new File(ud);
			if (dir.isDirectory())
				uploadDir = ud;
		}
		return uploadDir;
	}

	/**
	 * Spring injection method
	 * 
	 * @param tm
	 */
	public void setToolManager(ToolManager tm)
	{
		toolManager = tm;
	}

	/**
	 * Spring injection method
	 * 
	 * @param scs
	 */
	public void setServerConfigurationService(ServerConfigurationService scs)
	{
		serverConfigurationService = scs;
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
	 * @param props
	 * @see org.sakaiproject.mailsender.model.ConfigEntry.EditorType
	 * @return
	 */
	private String getEditorType(Properties props)
	{
		// check the tool config
		String editorType = props.getProperty(ConfigParams.wysiwygeditor.name());
		//
		// Commenting the check for tool specific property setting since other editors are set by
		// the system property
		//
//		// check the tool config under the old name
//		if (editorType == null)
//		{
//			editorType = props.getProperty("wysiwyg.editor");
//			// check for property overrides
//			if (editorType == null)
//				editorType = serverConfigurationService.getString("wysiwyg.editor");
//		}
		if (editorType != null && editorType.trim().length() > 0)
			editorType = editorType.trim().toLowerCase();
		else
			editorType = EditorType.fckeditor.name();
		return editorType;
	}

	/**
	 * Retrieves the reply to option from the tool configuration<br/> <br/> The original
	 * implementation of mailsender used yes/no instead of sender/no_reply_to.  Rather than run a db
	 * conversion to the new values, the below checks are made to slowly migrate the data
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
	 * Retrieves the subject prefix from the tool configuration
	 * 
	 * @param props
	 *            The tool config properties
	 * @return
	 */
	private String getSubjectPrefix(Properties props)
	{
		String subjectPrefix = props.getProperty(ConfigParams.subjectprefix.name());
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
	 * Retrieve the type of view selector
	 * 
	 * @param props
	 *            The tool config properties
	 * @see org.sakaiproject.mailsender.model.ConfigEntry.UserViewSelectType
	 * @return
	 */
	private String getViewSelectType(Properties props)
	{
		String viewSelectType = (String) props.getProperty(ConfigParams.recipview.name());
		if (viewSelectType != null && viewSelectType.trim().length() > 0)
			viewSelectType = viewSelectType.trim().toLowerCase();
		else
			viewSelectType = UserViewSelectType.tree.name();
		return viewSelectType;
	}

	/**
	 * The original implementation of mailsender used yes/no instead of true/false, so rather than run
	 * a db conversion to the new values, the conversion is made as the data is accessed
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