package org.sakaiproject.mailtool.tool.producers;

import org.sakaiproject.mailtool.tool.params.UserGroupViewParameters;
import org.sakaiproject.mailtool.tool.producers.fragments.UserGroupingProducer;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

public class RecipientSelectorRenderer
{
	private ViewStateHandler viewStateHandler;

	public void makeRcptSelector(UIContainer tofill, String divID)
	{
		UIJointContainer joint = new UIJointContainer(tofill, divID, "mailtool-rcpts:");

		// decorator for the links
		UIFreeAttributeDecorator decorator = new UIFreeAttributeDecorator("onclick",
				"RcptSelect.showResults(this, 'mailtool-roles', event, true); return false;");

		// select by role
		UIBranchContainer cell = UIBranchContainer.make(joint, "mailtool-rcpts-cell:", "1");
		UIMessage msg = UIMessage.make("select_rcpts_by_role");
		UserGroupViewParameters viewParams = new UserGroupViewParameters(
				UserGroupingProducer.VIEW_ID);
		viewParams.type = "role";
		String url = viewStateHandler.getFullURL(viewParams);
		UILink link = UILink.make(cell, "mailtool-rcpt-link", msg, url);
		link.decorate(decorator);

		UIOutput.make(cell, "mailtool-rcpt-separator");

		// select by section
		cell = UIBranchContainer.make(joint, "mailtool-rcpts-cell:", "2");
		msg = UIMessage.make("select_rcpts_by_section");
		viewParams = new UserGroupViewParameters(UserGroupingProducer.VIEW_ID);
		viewParams.type = "section";
		url = viewStateHandler.getFullURL(viewParams);
		link = UILink.make(cell, "mailtool-rcpt-link", msg, url);
		link.decorate(decorator);

		UIOutput.make(cell, "mailtool-rcpt-separator");

		// select by group
		cell = UIBranchContainer.make(joint, "mailtool-rcpts-cell:", "3");
		msg = UIMessage.make("select_rcpts_by_group");
		viewParams = new UserGroupViewParameters(UserGroupingProducer.VIEW_ID);
		viewParams.type = "group";
		url = viewStateHandler.getFullURL(viewParams);
		link = UILink.make(cell, "mailtool-rcpt-link", msg, url);
		link.decorate(decorator);
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
}