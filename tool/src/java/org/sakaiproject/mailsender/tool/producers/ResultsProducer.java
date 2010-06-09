package org.sakaiproject.mailsender.tool.producers;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ResultsProducer implements ViewComponentProducer
{
	public static final String VIEW_ID = "results";
	private NavBarRenderer navBarRenderer;

	public String getViewID()
	{
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker)
	{
		// make the navigation bar
		navBarRenderer.makeNavBar(tofill, "navIntraTool:", VIEW_ID);
	}

	public void setNavBarRenderer(NavBarRenderer nbr)
	{
		navBarRenderer = nbr;
	}
}