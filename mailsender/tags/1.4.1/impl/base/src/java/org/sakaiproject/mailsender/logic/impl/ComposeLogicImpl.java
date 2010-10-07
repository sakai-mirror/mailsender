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
package org.sakaiproject.mailsender.logic.impl;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.logic.ComposeLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.EmailRole;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.StringUtil;

public class ComposeLogicImpl implements ComposeLogic
{
	private static Log log = LogFactory.getLog(ComposeLogicImpl.class);

	private SiteService siteService;
	private AuthzGroupService authzGroupService;
	private UserDirectoryService userDirectoryService;
	private ExternalLogic externalLogic;
	private ToolManager toolManager;

	private static final int NUMBER_ROLES = 15;

	public void init()
	{
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.impl.ComposeLogic#getEmailRoles()
	 */
	public List<EmailRole> getEmailRoles() throws GroupNotDefinedException
	{
		List<EmailRole> theRoles = new ArrayList<EmailRole>();
		List<EmailRole> configRoles = getConfigRoles();

		String realmId = externalLogic.getSiteRealmID();
		AuthzGroup arole = authzGroupService.getAuthzGroup(realmId);

		for (Iterator i = arole.getRoles().iterator(); i.hasNext();)
		{
			Role r = (Role) i.next();
			String rolename = r.getId();
			String singular = null;
			String plural = null;

			EmailRole configRole = findConfigRole(realmId, rolename, configRoles);
			// check first for an override from config
			if (configRole != null)
			{
				singular = configRole.getRoleSingular();
				plural = configRole.getRolePlural();
			}
			// default case
			else
			{
				singular = rolename;
				plural = rolename;
			}
			// create email role and add to list
			EmailRole emailrole = null;
			if (getGroupAwareRole().equals(rolename))
			{
				emailrole = new EmailRole(realmId, rolename, singular, plural, "role_groupaware");
			}
			else
			{
				emailrole = new EmailRole(realmId, rolename, singular, plural, "role");
			}
			theRoles.add(emailrole);
		}
		Collections.sort(theRoles, new EmailRoleComparator(EmailRoleComparator.SORT_BY.PLURAL));
		return theRoles;
	}

	/**
	 * Get the config roles defined in the tool configuration
	 * 
	 * @param props Properties representing the tool configuration
	 * @return
	 */
	private List<EmailRole> getConfigRoles()
	{
		Properties props = toolManager.getCurrentPlacement().getPlacementConfig();
		ArrayList<EmailRole> configRoles = new ArrayList<EmailRole>();

		// check for and add in the manual roles from the config file
		for (int i = 1; i < (NUMBER_ROLES + 1); i++)
		{
			String rolerealm = StringUtil.trimToNull(props.getProperty("role" + i + "realmid"));
			String rolename = StringUtil.trimToNull(props.getProperty("role" + i + "id"));
			String rolesingular = StringUtil.trimToNull(props.getProperty("role" + i + "singular"));
			String roleplural = StringUtil.trimToNull(props.getProperty("role" + i + "plural"));
			// only add role if all data is present
			if (rolerealm != null && rolename != null && rolesingular != null && roleplural != null)
			{
				EmailRole emailrole = null;
				if (getGroupAwareRole().equals(rolename))
				{
					emailrole = new EmailRole(rolerealm, rolename, rolesingular, roleplural,
							"role_groupaware");
				}
				else
				{
					emailrole = new EmailRole(rolerealm, rolename, rolesingular, roleplural, "role");
				}
				configRoles.add(emailrole);
			}
		}
		return configRoles;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.impl.ComposeLogic#getEmailGroups()
	 */
	public List<EmailRole> getEmailGroups() throws IdUnusedException
	{
		ArrayList<EmailRole> roles = new ArrayList<EmailRole>();
		String siteId = externalLogic.getSiteID();
		Site currentSite = siteService.getSite(siteId);

		Collection<Group> groups = currentSite.getGroups();
		for (Group group : groups)
		{
			if (group.getProperties().getProperty("sections_category") == null)
			{
				String groupName = group.getTitle();
				String groupId = group.getId();
				roles.add(new EmailRole(groupId, groupId, groupName, groupName, "group"));
			}
		}
		Collections.sort(roles, new EmailRoleComparator(EmailRoleComparator.SORT_BY.PLURAL));
		return roles;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.impl.ComposeLogic#getEmailSections()
	 */
	public List<EmailRole> getEmailSections() throws IdUnusedException
	{
		ArrayList<EmailRole> roles = new ArrayList<EmailRole>();
		// adding groups as roles
		String siteId = externalLogic.getSiteID();
		Site currentSite = siteService.getSite(siteId);

		Collection<Group> groups = currentSite.getGroups();
		for (Group group : groups)
		{
			if (group.getProperties().getProperty("sections_category") != null)
			{
				String groupName = group.getTitle();
				String groupId = group.getId();
				roles.add(new EmailRole(groupId, groupId, groupName, groupName, "section"));
			}
		}
		Collections.sort(roles, new EmailRoleComparator(EmailRoleComparator.SORT_BY.PLURAL));
		return roles;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.impl.ComposeLogic#getGroupAwareRole()
	 */
	public String getGroupAwareRole()
	{
		String retval = null;
		String gar = ServerConfigurationService.getString("mailsender.group.aware.role");
		String[] gartokens = gar.split(",");
		try
		{
			String realmId = externalLogic.getSiteRealmID();
			AuthzGroup arole = authzGroupService.getAuthzGroup(realmId);

			for (Iterator i = arole.getRoles().iterator(); i.hasNext();)
			{
				Role r = (Role) i.next();
				String rolename = r.getId();
				for (int t = 0; t < gartokens.length; t++)
				{
					if (gartokens[t].trim().equals(rolename.trim()))
					{
						retval = rolename;
						break;
					}
				}
				if (retval != null)
					break;
			}
		}
		catch (GroupNotDefinedException e)
		{
			log.error(e.getMessage(), e);
		}
		if (retval == null)
		{
			retval = getGroupAwareRoleDefault();
		}
		return retval;
	}

	/**
	 * @see org.sakaiproject.mailsender.logic.impl.ComposeLogic#getGroupAwareRoleDefault()
	 */
	public String getGroupAwareRoleDefault()
	{
		String siteType = externalLogic.getSiteType();
		String defaultRole = "";

		if ("course".equals(siteType))
			defaultRole = "Student";

		if ("project".equals(siteType))
			defaultRole = "access";

		return defaultRole;
	}

	/**
	 * Get users by role
	 * 
	 * @param role
	 * @return set of <code>Member</code>s
	 */
	public List<User> getUsersByRole(String role) throws IdUnusedException
	{
		ArrayList<User> users = new ArrayList<User>();
		String siteId = externalLogic.getSiteID();
		Site currentSite = siteService.getSite(siteId);
		Set<String> userIds = currentSite.getUsersHasRole(role);
		for (String userId : userIds)
		{
			try
			{
				users.add(userDirectoryService.getUser(userId));
			}
			catch (UserNotDefinedException e)
			{
				log.warn("Unable to retrieve user: " + userId);
			}
		}
		Collections.sort(users, new UserComparator());
		return users;
	}

	/**
	 * Get the members associated to a gruop
	 * 
	 * @param groupId
	 * @return
	 * @throws IdUnusedException
	 */
	public List<User> getUsersByGroup(String groupId) throws IdUnusedException
	{
		ArrayList<User> users = new ArrayList<User>();
		String siteId = externalLogic.getSiteID();
		Site currentSite = siteService.getSite(siteId);

		Group group = currentSite.getGroup(groupId);
		Set<String> userIds = group.getUsers();
		for (String userId : userIds)
		{
			try
			{
				users.add(userDirectoryService.getUser(userId));
			}
			catch (UserNotDefinedException e)
			{
				log.warn("Unable to retrieve user: " + userId);
			}
		}
		Collections.sort(users, new UserComparator());
		return users;
	}

	/**
	 * Dependency injection method
	 * 
	 * @param ss
	 */
	public void setSiteService(SiteService ss)
	{
		siteService = ss;
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
	 * @param tl
	 */
	public void setToolManager(ToolManager tl)
	{
		toolManager = tl;
	}

	/**
	 * Dependency injection method
	 * 
	 * @param ags
	 */
	public void setAuthzGroupService(AuthzGroupService ags)
	{
		authzGroupService = ags;
	}

	/**
	 * Dependency injection method
	 * 
	 * @param uds
	 */
	public void setUserDirectoryService(UserDirectoryService uds)
	{
		userDirectoryService = uds;
	}

	/**
	 * Look through configRoles to find a role that matches realmId, roleId
	 * 
	 * @param realmId
	 * @param roleId
	 * @param configRoles
	 * @return A role that matches realmId, roleId. <code>null</code> if not found.
	 */
	private EmailRole findConfigRole(String realmId, String roleId, List<EmailRole> configRoles)
	{
		EmailRole retRole = null;
		for (EmailRole role : configRoles)
		{
			if (role.getRealmId().equals(realmId) && role.getRoleId().equals(roleId))
			{
				retRole = role;
				break;
			}
		}
		return retRole;
	}

	/**
	 * Sorts EmailRoles by role name depending on how it is constructed.
	 * 
	 * @author <a href="mailto:carl.hall@et.gatech.edu">Carl Hall</a>
	 */
	private static class EmailRoleComparator implements Comparator<EmailRole>
	{
		public enum SORT_BY
		{
			SINGULAR, PLURAL;
		}

		Collator collator = Collator.getInstance();
		SORT_BY sortBy;

		public EmailRoleComparator(SORT_BY sortBy)
		{
			this.sortBy = sortBy;
		}

		public int compare(EmailRole o1, EmailRole o2)
		{
			int retval = 0;
			if (sortBy == SORT_BY.SINGULAR)
				retval = collator.compare(o1.getRoleSingular(), o2.getRoleSingular());
			else if (sortBy == SORT_BY.PLURAL)
				retval = collator.compare(o1.getRolePlural(), o2.getRolePlural());
			return retval;
		}
	}

	/**
	 * Sorts users 
	 * 
	 * @author <a href="mailto:carl.hall@et.gatech.edu">Carl Hall</a>
	 */
	private static class UserComparator implements Comparator<User>
	{
		Collator collator = Collator.getInstance();
		public int compare(User user1, User user2)
		{
			String displayName1 = user1.getLastName() + ", " + user1.getFirstName() + " (" + user1.getDisplayId() + ")";
			String displayName2 = user2.getLastName() + ", " + user2.getFirstName() + " (" + user2.getDisplayId() + ")";
			return collator.compare(displayName1, displayName2);
		}
	}
}