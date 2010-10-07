package org.sakaiproject.mailsender.logic;

import java.util.List;

import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.model.EmailRole;
import org.sakaiproject.user.api.User;

public interface ComposeLogic
{

	/**
	 * Read the tool config and build the email roles that are specified
	 * 
	 * @return return EmailRoles (called from getEmailGroups())
	 */
	List<EmailRole> getEmailRoles() throws GroupNotDefinedException;

	/**
	 * Get a list of groups available for this tool
	 * 
	 * @return
	 */
	List<EmailRole> getEmailGroups() throws IdUnusedException;

	/**
	 * Get the sections as by the section info tool
	 * 
	 * @return
	 */
	List<EmailRole> getEmailSections() throws IdUnusedException;

	/**
	 * Get group-aware role which is set in sakai.properties e.g.
	 * "mailsender.group.aware.role=Student,access"
	 * 
	 * @return return the String of group-aware role name
	 */
	String getGroupAwareRole();

	/**
	 * // OOTB(Out of the box) Sakai defaults
	 * 
	 * @return return default group-aware role by type if type=course, return Student. if
	 *         type=project, return access.
	 */
	String getGroupAwareRoleDefault();

	/**
	 * Retrieve members for the current site that are of a certain role
	 * 
	 * @param role
	 * @return
	 * @throws IdUnusedException
	 */
	List<User> getUsersByRole(String role) throws IdUnusedException;

	/**
	 * Retrieve members for the current site that are of a certain group/section
	 *  
	 * @param groupId
	 * @return
	 * @throws IdUnusedException
	 */
	List<User> getUsersByGroup(String groupId) throws IdUnusedException;
}