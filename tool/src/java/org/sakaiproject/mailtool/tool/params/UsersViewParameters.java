package org.sakaiproject.mailtool.tool.params;

/**
 * This is a view parameters class which defines the variables that are passed from one page to
 * another
 * 
 * @author Carl Hall
 */
public class UsersViewParameters extends UserGroupViewParameters
{
	public String id; // the type of group to produce

	/**
	 * Basic empty constructor
	 */
	public UsersViewParameters()
	{
	}

	/**
	 * Minimal constructor
	 * 
	 * @param viewID
	 *            the target view for these parameters
	 */
	public UsersViewParameters(String viewID)
	{
		this.viewID = viewID;
	}

	/**
	 * Full power constructor
	 * 
	 * @param viewID
	 * @param type
	 */
	public UsersViewParameters(String viewID, String type, String id)
	{
		this.viewID = viewID;
		this.type = type;
		this.id = id;
	}
}