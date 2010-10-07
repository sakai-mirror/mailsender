package org.sakaiproject.mailsender.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.tool.beans.EmailBean;
import org.sakaiproject.user.api.User;

import uk.ac.cam.caret.sakai.rsf.tmp.evolverimpl.SakaiFCKTextEvolver;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ComposeProducer implements ViewComponentProducer, NavigationCaseReporter,
		DefaultView
{
	public static final String VIEW_ID = "compose";

	public String getViewID()
	{
		return VIEW_ID;
	}

	// Spring injected beans
	private SakaiFCKTextEvolver richTextEvolver;

	private ExternalLogic externalLogic;

	private ConfigLogic configLogic;

	private NavBarRenderer navBarRenderer;

	private RecipientSelectorRenderer rcptSelectorRenderer;

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		String emailBean = "emailBean.newEmail";
		// make the navigation bar
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);

		// build out the form elements and labels
		UIForm mainForm = UIForm.make(tofill, "mainForm");

		// get the user then name & email
		User curUser = externalLogic.getCurrentUser();

		String fromEmail = "";
		String fromDisplay = "";
		if (curUser != null)
		{
			fromEmail = curUser.getEmail();
			fromDisplay = curUser.getDisplayName();
		}
		String from = fromDisplay + " <" + fromEmail + ">";
		UIOutput.make(tofill, "from", from);

		// make the recipient selector
		rcptSelectorRenderer.makeRcptSelector(tofill, "select_rcpts:");

		// create the 'other recipients' field
		UIInput.make(mainForm, "otherRecipients", emailBean + ".otherRecipients");

		// create the subject field
		UIInput.make(mainForm, "subject", emailBean + ".subject");

		// create the content editor
		if (configLogic.useRichTextEditor())
		{
			UIInput content = UIInput.make(mainForm, "content-div:", emailBean
					+ ".content");
			richTextEvolver.evolveTextInput(content);
		}
		else
		{
			UIInput.make(mainForm, "content", emailBean + ".content");
		}

		// create 'send me a copy' checkbox
		UIBoundBoolean.make(mainForm, "sendMeCopy", emailBean + ".config.sendMeACopy");

		if (externalLogic.isEmailArchiveAddedToSite())
		{
			UIOutput.make(mainForm, "addToArchiveDiv");
			UIBoundBoolean.make(mainForm, "addToArchive", emailBean
					+ ".config.addToArchive");
		}

		// create buttons for form
		UICommand.make(mainForm, "send-button", UIMessage.make("send_mail_button"),
				"emailBean.sendEmail");
		UICommand.make(mainForm, "cancel-button", UIMessage.make("cancel_mail_button"));
	}

	/**
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List<NavigationCase> reportNavigationCases()
	{
		List<NavigationCase> cases = new ArrayList<NavigationCase>();
		cases.add(new NavigationCase(new SimpleViewParameters(ComposeProducer.VIEW_ID)));
		cases.add(new NavigationCase(EmailBean.EMAIL_SENT, new SimpleViewParameters(
				ResultsProducer.VIEW_ID)));
		return cases;
	}

	/**
	 * Dependency injection method
	 * 
	 * @param richTextEvolver
	 */
	public void setRichTextEvolver(SakaiFCKTextEvolver richTextEvolver)
	{
		this.richTextEvolver = richTextEvolver;
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
	 * @param cl
	 */
	public void setConfigLogic(ConfigLogic cl)
	{
		configLogic = cl;
	}

	/**
	 * Dependency injection method
	 * 
	 * @param rsr
	 */
	public void setRcptSelectorRenderer(RecipientSelectorRenderer rsr)
	{
		rcptSelectorRenderer = rsr;
	}
}