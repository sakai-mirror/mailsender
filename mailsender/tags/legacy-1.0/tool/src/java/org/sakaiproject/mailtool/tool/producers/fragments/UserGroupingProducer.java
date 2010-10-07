package org.sakaiproject.mailtool.tool.producers.fragments;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailtool.logic.ComposeLogic;
import org.sakaiproject.mailtool.model.EmailRole;
import org.sakaiproject.mailtool.tool.params.UserGroupViewParameters;
import org.sakaiproject.mailtool.tool.params.UsersViewParameters;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
import uk.org.ponder.rsf.content.ContentTypeReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

public class UserGroupingProducer implements ViewComponentProducer, ViewParamsReporter,
		ContentTypeReporter
{
	public static final String VIEW_ID = "userGroup";

	private Log log = LogFactory.getLog(UserGroupingProducer.class);
	private ComposeLogic composeLogic;
	private TargettedMessageList messages;
	private ViewStateHandler viewStateHandler;

	/**
	 * @see uk.org.ponder.rsf.view.ComponentProducer
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		UserGroupViewParameters ugParams = (UserGroupViewParameters) viewparams;
		List<EmailRole> emailRoles = null;
		try
		{
			if ("group".equals(ugParams.type))
			{
				emailRoles = composeLogic.getEmailGroups();
			}
			else if ("section".equals(ugParams.type))
			{
				emailRoles = composeLogic.getEmailSections();
			}
			else if ("role".equals(ugParams.type))
			{
				emailRoles = composeLogic.getEmailRoles();
			}
			else
			{
				messages.addMessage(new TargettedMessage("error.unknown.role.type"));
				return;
			}

			String emailBean = "emailBean.newEmail.";
			if (emailRoles.size() == 0)
			{
				messages.addMessage(new TargettedMessage("no." + ugParams.type + ".found", null,
						TargettedMessage.SEVERITY_INFO));
			}
			else
			{
				for (int i = 0; i < emailRoles.size(); i++)
				{
					// create a branch for looping
					UIBranchContainer roleBranch = UIBranchContainer.make(tofill,
							"mailtool-usersGroupOption:", Integer.toString(i));

					// get the data
					EmailRole role = emailRoles.get(i);
					String[] rolePlural = new String[] { role.getRolePlural() };

					// build the EL binding
					UIBoundBoolean.make(roleBranch, "mailtool-usersGroup", emailBean
							+ ugParams.type + "Ids." + role.getRoleId());

					// add a label to the checkbox
					UIMessage.make(roleBranch, "mailtool-usersGroupLabel",
							"usersbyrole_all_prefix", rolePlural);

					// create the area for user listings
					UIOutput usersArea = UIOutput.make(roleBranch, "mailtool-users");
					usersArea.decorate(new UIIDStrategyDecorator(usersArea.getFullID()));

					//
					// create the 'Select Individuals' and 'Collapse' links
					//
					// create the select link
					UIMessage selectMsg = UIMessage.make("usersbyrole_selectindividuals");

					// create view params for user list links
					UsersViewParameters usersParams = new UsersViewParameters(UsersProducer.VIEW_ID);
					usersParams.type = ugParams.type;
					usersParams.id = role.getRoleId();
					String url = viewStateHandler.getFullURL(usersParams);
					UILink selectLink = UIInternalLink.make(roleBranch,
							"mailtool-usersGroupLink-select", selectMsg, url);

					// create the collapse link
					UIMessage collapseMsg = UIMessage.make("usersbyrole_collapseindividuals");
					UILink collapseLink = UIInternalLink.make(roleBranch,
							"mailtool-usersGroupLink-collapse", collapseMsg, "#");

					// decorators for the select link
					String command = "RcptSelect.showResults(this, '" + usersArea.getFullID() + "', event);";
					command += "MailtoolUtil.hideElement('" + selectLink.getFullID() + "');";
					command += "MailtoolUtil.showElement('" + collapseLink.getFullID() + "', true);";
					command += "return false;";
					UIDecorator onClickDecorator = new UIFreeAttributeDecorator("onclick", command);
					UIDecorator idDecorator = new UIIDStrategyDecorator(selectLink.getFullID());
					selectLink.decorate(onClickDecorator).decorate(idDecorator);

					// decorators for the collapse link
					command = "MailtoolUtil.hideElement(['" + collapseLink.getFullID() + "', '"
							+ usersArea.getFullID() + "']);";
					command += "MailtoolUtil.showElement('" + selectLink.getFullID() + "', true);";
					command += "return false;";
					onClickDecorator = new UIFreeAttributeDecorator("onclick", command);
					idDecorator = new UIIDStrategyDecorator(collapseLink.getFullID());
					collapseLink.decorate(onClickDecorator).decorate(idDecorator);
				}
			}
		}
		catch (GroupNotDefinedException gnde)
		{
			log.error(gnde.getMessage(), gnde);
			messages.addMessage(new TargettedMessage("exception.generic", new String[] { gnde
					.getMessage() }));
		}
		catch (IdUnusedException iue)
		{
			log.error(iue.getMessage(), iue);
			messages.addMessage(new TargettedMessage("exception.generic", new String[] { iue
					.getMessage() }));
		}
	}

	/**
	 * @see uk.org.ponder.rsf.content.ContentTypeReporter
	 */
	public String getContentType()
	{
		// need to define the content type to keep it from being sent as the default
		return ContentTypeInfoRegistry.HTML_FRAGMENT;
	}

	/**
	 * Dependency injection method
	 * 
	 * @param cl
	 */
	public void setComposeLogic(ComposeLogic cl)
	{
		composeLogic = cl;
	}

	/**
	 * Dependency injection method
	 * 
	 * @param messages
	 */
	public void setMessages(TargettedMessageList messages)
	{
		this.messages = messages;
	}

	/**
	 * Dependency injection method
	 * 
	 * @param viewStateHandler
	 */
	public void setViewStateHandler(ViewStateHandler viewStateHandler)
	{
		this.viewStateHandler = viewStateHandler;
	}

	/**
	 * @see uk.org.ponder.rsf.view.ViewComponentProducer
	 */
	public String getViewID()
	{
		return VIEW_ID;
	}

	/**
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter
	 */
	public ViewParameters getViewParameters()
	{
		return new UserGroupViewParameters();
	}
}