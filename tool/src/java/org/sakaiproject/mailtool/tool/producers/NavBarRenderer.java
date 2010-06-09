package org.sakaiproject.mailtool.tool.producers;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class NavBarRenderer
{
	private MessageLocator messageLocator;

	public void makeNavBar(UIContainer tofill, String divID, String currentViewID)
	{
		UIJointContainer joint = new UIJointContainer(tofill, divID, "navigation:");

		// if on the compose page, make the compose text static and the options
		// text a link
		UIBranchContainer cell = UIBranchContainer.make(joint, "navigation-cell:", "1");
		UIComponent comp = null;
		if (currentViewID.equals(ComposeProducer.VIEW_ID))
			comp = UIMessage.make(cell, "item-text", "compose_toolbar");
		else
			comp = UIInternalLink.make(cell, "item-link", UIMessage
					.make("compose_toolbar"), new SimpleViewParameters(
					ComposeProducer.VIEW_ID));
		comp.decorate(new UIIDStrategyDecorator("navCompose"));

		UIOutput.make(cell, "item-separator");

		// options link
		cell = UIBranchContainer.make(joint, "navigation-cell:", "2");
		if (currentViewID.equals(OptionsProducer.VIEW_ID))
			comp = UIMessage.make(cell, "item-text", "options_toolbar");
		else
		{
			comp = UIInternalLink.make(cell, "item-link", UIMessage
					.make("options_toolbar"), new SimpleViewParameters(
					OptionsProducer.VIEW_ID));
			String msg = messageLocator.getMessage("navigate.lose.data");
			UIFreeAttributeDecorator decorator = new UIFreeAttributeDecorator("onclick",
					"var retval=true;if(Dirty.isDirty()){retval=confirm('" + msg
							+ "');}return retval;");
			comp.decorate(decorator);
		}
		comp.decorate(new UIIDStrategyDecorator("navConfig"));
	}
}
