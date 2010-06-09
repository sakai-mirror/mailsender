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
package org.sakaiproject.mailsender.tool.producers.fragments;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.logic.ComposeLogic;
import org.sakaiproject.mailsender.model.EmailRole;
import org.sakaiproject.mailsender.tool.params.UserGroupViewParameters;
import org.sakaiproject.mailsender.tool.params.UsersViewParameters;

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

	private final Log log = LogFactory.getLog(UserGroupingProducer.class);
	private ComposeLogic composeLogic;
	private TargettedMessageList messages;
	private ViewStateHandler viewStateHandler;

	public UserGroupingProducer()
	{
	}

	public UserGroupingProducer(ComposeLogic composeLogic, ViewStateHandler viewStateHandler,
			TargettedMessageList messages)
	{
		this.composeLogic = composeLogic;
		this.viewStateHandler = viewStateHandler;
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

	public void setViewStateHandler(ViewStateHandler viewStateHandler)
	{
		this.viewStateHandler = viewStateHandler;
	}

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
							"mailsender-usersGroupOption:", Integer.toString(i));

					// get the data
					EmailRole role = emailRoles.get(i);
					String[] rolePlural = new String[] { role.getRolePlural() };

					// build the EL binding
					UIBoundBoolean.make(roleBranch, "mailsender-usersGroup", emailBean
							+ ugParams.type + "Ids." + role.getRoleId());

					// add a label to the checkbox
					UIMessage msg = UIMessage.make("usersbyrole_all_prefix", rolePlural);

					// create the area for user listings
					UIOutput usersArea = UIOutput.make(roleBranch, "mailsender-users");
					usersArea.decorate(new UIIDStrategyDecorator(usersArea.getFullID()));

					//
					// create the 'Select Individuals' and 'Collapse' links
					//
					// create view params for user list links
					UsersViewParameters usersParams = new UsersViewParameters(UsersProducer.VIEW_ID);
					usersParams.type = ugParams.type;
					usersParams.id = role.getRoleId();
					String url = viewStateHandler.getFullURL(usersParams);
					UILink selectLink = UIInternalLink.make(roleBranch,
							"mailsender-usersGroupLink-select", msg, url);

					// create the collapse link
					UILink collapseLink = UIInternalLink.make(roleBranch,
							"mailsender-usersGroupLink-collapse", msg, "#");

					// decorators for the select link
					String command = "RcptSelect.showIndividuals(this, '" + usersArea.getFullID()
							+ "', '" + selectLink.getFullID() + "', '" + collapseLink.getFullID()
							+ "');return false";
					UIDecorator onClickDecorator = new UIFreeAttributeDecorator("onclick", command);
					UIDecorator idDecorator = new UIIDStrategyDecorator(selectLink.getFullID());
					selectLink.decorate(onClickDecorator).decorate(idDecorator);

					// decorators for the collapse link
					command = "RcptSelect.hideIndividuals('" + usersArea.getFullID() + "', '"
							+ selectLink.getFullID() + "', '" + collapseLink.getFullID()
							+ "');return false";
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