package org.sakaiproject.mailsender.tool.producers;

import java.util.ArrayList;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;

/**
 * Renders messages based on severity. Most of this code was stolen straight from the RSF source.
 * 
 * @author Carl Hall
 */
public class SeverityMessageRenderer extends uk.org.ponder.rsf.renderer.MessageRenderer
{
	private MessageLocator messageLocator;

	public void setMessageLocator(MessageLocator messageLocator)
	{
		this.messageLocator = messageLocator;
		super.setMessageLocator(messageLocator);
	}

	@Override
	public UIBranchContainer renderMessageList(TargettedMessageList messageList)
	{
		UIBranchContainer togo = new UIBranchContainer();
		renderSeverityMessages(togo, messageList, TargettedMessage.SEVERITY_ERROR);
		renderSeverityMessages(togo, messageList, TargettedMessage.SEVERITY_INFO);
		return togo;
	}

	/**
	 * Renders messages based on severity
	 * 
	 * @param tofill
	 * @param messageList
	 * @param severity
	 */
	private void renderSeverityMessages(UIContainer tofill, TargettedMessageList messageList,
			int severity)
	{
		// set the branch and message item names
		String branchName = null;
		String messageName = null;
		switch (severity)
		{
			case TargettedMessage.SEVERITY_ERROR:
				branchName = "error-messages:";
				messageName = "error-message:";
				break;
			case TargettedMessage.SEVERITY_INFO:
				branchName = "info-messages:";
				messageName = "info-message:";
				break;
		}

		// collect messages of the requested severity
		// this needs to happen so the appropriate area isn't rendered unnecessarily.
		ArrayList<TargettedMessage> sevMsgs = new ArrayList<TargettedMessage>();
		for (int i = 0; i < messageList.size(); i++)
		{
			TargettedMessage msg = messageList.messageAt(i);
			if (msg.severity == severity)
			{
				sevMsgs.add(msg);
			}
		}

		// render the found messages
		if (sevMsgs.size() > 0)
		{
			UIOutput.make(tofill, branchName);
			for (TargettedMessage msg : sevMsgs)
			{
				String render = messageLocator.getMessage(msg.messagecodes, msg.args);
				UIOutput.make(tofill, messageName, render);
			}
		}
	}
}