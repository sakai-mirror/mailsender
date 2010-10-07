package org.sakaiproject.mailsender.tool.producers.fragments;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.logic.ComposeLogic;
import org.sakaiproject.mailsender.tool.params.UsersViewParameters;
import org.sakaiproject.user.api.User;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class UsersProducer implements ViewComponentProducer, ViewParamsReporter
{
	public static final String VIEW_ID = "users";

	private Log log = LogFactory.getLog(UsersProducer.class);
	private ComposeLogic composeLogic;
	private TargettedMessageList messages;

	public UsersProducer()
	{
	}

	public UsersProducer(ComposeLogic composeLogic, TargettedMessageList messages)
	{
		this.composeLogic = composeLogic;
		this.messages = messages;
	}

	public void setComposeLogic(ComposeLogic composeLogic)
	{
		this.composeLogic = composeLogic;
	}

	public void setMessages(TargettedMessageList messages)
	{
		this.messages = messages;
	}

	/**
	 * @see uk.org.ponder.rsf.view.ViewComponentProducer#getViewID()
	 */
	public String getViewID()
	{
		return VIEW_ID;
	}

	/**
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(UIContainer, ViewParameters,
	 *      ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		try
		{
			// cast the view params
			UsersViewParameters viewParams = (UsersViewParameters) viewparams;

			// get the members based on type and id
			List<User> users = null;
			if (viewParams.id != null && viewParams.id.trim().length() != 0)
			{
				if ("section".equals(viewParams.type) || "group".equals(viewParams.type))
				{
					users = composeLogic.getUsersByGroup(viewParams.id);
				}
				else
				{
					users = composeLogic.getUsersByRole(viewParams.id);
				}
			}

			// get the members that match the requested role
			if (users == null || users.size() == 0)
			{
				messages.addMessage(new TargettedMessage(
						"no." + viewParams.type + ".members.found", null,
						TargettedMessage.SEVERITY_INFO));
			}
			else
			{
				int i = 0;
				for (User user : users)
				{
					// populate the page with the members found
					UIBranchContainer cell = UIBranchContainer.make(tofill, "mailsender-userCol:",
							viewParams.id + "-" + Integer.toString(i));
					UIBoundBoolean.make(cell, "mailsender-user", "emailBean.newEmail.userIds."
							+ user.getId());
					String displayName = user.getLastName() + ", " + user.getFirstName() + " ("
							+ user.getDisplayId() + ")";
					UIVerbatim.make(cell, "mailsender-userLabel", displayName);
					i++;
				}
			}
		}
		catch (IdUnusedException e)
		{
			log.error(e.getMessage(), e);
			messages.addMessage(new TargettedMessage("exception.generic", new String[] { e
					.getMessage() }, TargettedMessage.SEVERITY_ERROR));
		}
	}

	/**
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters()
	{
		return new UsersViewParameters();
	}
}