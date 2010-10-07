package org.sakaiproject.mailsender.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.ConfigEntry.ConfigEntryOption;
import org.sakaiproject.mailsender.model.ConfigEntry.ReplyTo;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class OptionsProducer implements ViewComponentProducer, NavigationCaseReporter
{
	public static final String VIEW_ID = "options";

	private NavBarRenderer navBarRenderer;
	private ExternalLogic externalLogic;

	public String getViewID()
	{
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		String configBean = "configBean";
		String config = configBean + ".config";
		// make the navigation bar
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);

		UIForm optionsForm = UIForm.make(tofill, "optionsForm");

		// Send Me A Copy
		UIBoundBoolean.make(optionsForm, "sendMeACopy", config + ".sendMeACopy");

		// Add to email archive
		if (externalLogic.isEmailArchiveAddedToSite())
		{
			UIOutput.make(optionsForm, "addToArchiveDiv");
			UIBoundBoolean.make(optionsForm, "addToArchive", config + ".addToArchive");
		}

		// Reply-to
		ReplyTo[] replyTos = ReplyTo.values();
		UISelect replyToSelect = makeUISelect(optionsForm, "replyToSelect", replyTos, config + ".replyTo")
				.setMessageKeys();
		String replyToFullId = replyToSelect.getFullID();
		for (int i = 0; i < replyTos.length; i++)
		{
			makeSelectBranch(optionsForm, replyToFullId, "replyToRow:", "replyTo",
					"replyToOptionLabel", i);
		}

		// Display invalid emails
		String[] labels = new String[] { "options_displayinvalidemails_yes",
				"options_displayinvalidemails_no" };
		String[] values = new String[] { Boolean.TRUE.toString(), Boolean.FALSE.toString() };
		UISelect invalidEmailsSelect = UISelect.make(optionsForm, "invalidEmailsSelect", values,
				labels, config + ".displayInvalidEmails").setMessageKeys();
		String invalidEmailsFullId = invalidEmailsSelect.getFullID();
		for (int i = 0; i < values.length; i++)
		{
			makeSelectBranch(optionsForm, invalidEmailsFullId, "invalidEmailsRow:",
					"invalidEmails", "invalidEmailsLabel", i);
		}

		UICommand.make(optionsForm, "update-button", UIMessage.make("options_update_button"),
				configBean + ".saveConfig");
		UICommand.make(optionsForm, "cancel-button", UIMessage.make("options_cancel_button"));
	}

	/**
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List<NavigationCase> reportNavigationCases()
	{
		// All cases [except for errors] return to the compose screen
		List<NavigationCase> cases = new ArrayList<NavigationCase>();
		cases.add(new NavigationCase(new SimpleViewParameters(ComposeProducer.VIEW_ID)));
		return cases;
	}

	/**
	 * Dependency injection method
	 * 
	 * @param nbr
	 */
	public void setNavBarRenderer(NavBarRenderer nbr)
	{
		navBarRenderer = nbr;
	}

	/**
	 * Dependency injection method
	 * 
	 * @param el
	 */
	public void setExternalLogic(ExternalLogic el)
	{
		externalLogic = el;
	}

	/**
	 * Convenience method to build a UISelect object using a ConfigEntryOption enum array.
	 * 
	 * @param tofill
	 * @param ID
	 * @param configOptions
	 * @param valuebinding
	 * @param initvalue
	 * @return
	 */
	private UISelect makeUISelect(UIContainer tofill, String ID, ConfigEntryOption[] configOptions,
			String valuebinding)
	{
		String[] options = new String[configOptions.length];
		String[] labels = new String[configOptions.length];
		for (int i = 0; i < options.length; i++)
		{
			options[i] = configOptions[i].name();
			labels[i] = configOptions[i].labelKey();
		}
		UISelect uiSelect = UISelect.make(tofill, ID, options, labels, valuebinding);
		return uiSelect;
	}

	/**
	 * Factory for building a branch on a select that fills the choice and label then adds a label
	 * decorator
	 * 
	 * @param tofill
	 * @param selectId
	 * @param branchName
	 * @param choiceName
	 * @param labelName
	 * @param i
	 */
	private void makeSelectBranch(UIContainer tofill, String selectId, String branchName,
			String choiceName, String labelName, int i)
	{
		UIBranchContainer branch = UIBranchContainer.make(tofill, branchName, Integer.toString(i));
		UISelectChoice choice = UISelectChoice.make(branch, choiceName, selectId, i);
		UISelectLabel label = UISelectLabel.make(branch, labelName, selectId, i);
		UILabelTargetDecorator labelDec = new UILabelTargetDecorator(choice);
		label.decorate(labelDec);
	}
}