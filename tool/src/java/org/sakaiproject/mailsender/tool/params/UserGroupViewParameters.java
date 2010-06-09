package org.sakaiproject.mailsender.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * This is a view parameters class which defines the variables that are passed from one page to
 * another
 * 
 * @author Carl Hall
 */
public class UserGroupViewParameters extends SimpleViewParameters
{
	public String type; // the type of group to produce

	/**
	 * Basic empty constructor
	 */
	public UserGroupViewParameters()
	{
	}

	/**
	 * Minimal constructor
	 * 
	 * @param viewID
	 *            the target view for these parameters
	 */
	public UserGroupViewParameters(String viewID)
	{
		this.viewID = viewID;
	}

	/**
	 * Full power constructor
	 * 
	 * @param viewID
	 * @param type
	 */
	public UserGroupViewParameters(String viewID, String type)
	{
		this.viewID = viewID;
		this.type = type;
	}
}