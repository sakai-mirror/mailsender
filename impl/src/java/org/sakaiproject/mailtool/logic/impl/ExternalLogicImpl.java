/******************************************************************************
 * ExternalLogicImpl.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2006 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.mailtool.logic.impl;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailtool.logic.ExternalLogic;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * This is the implementation for logic which is external to our app logic
 * 
 * @author Sakai App Builder -AZ
 */
public class ExternalLogicImpl implements ExternalLogic
{

	private static Log log = LogFactory.getLog(ExternalLogicImpl.class);

	private FunctionManager functionManager;
	private ToolManager toolManager;
	private SecurityService securityService;
	private SessionManager sessionManager;
	private SiteService siteService;

	private UserDirectoryService userDirectoryService;

	/**
	 * Place any code that should run when this class is initialized by spring here
	 */
	public void init()
	{
		log.debug("init");
		// register Sakai permissions for this tool
		functionManager.registerFunction(ITEM_WRITE_ANY);
		functionManager.registerFunction(ITEM_READ_HIDDEN);
	}

	/**
	 * @see org.sakaiproject.mailtool.logic.ExternalLogic#getCurrentSiteTitle()
	 */
	public String getCurrentSiteTitle()
	{
		Site site = getCurrentSite();
		String title = "----------";
		if (site != null)
		{
			title = site.getTitle();
		}
		return title;
	}

	/**
	 * @see org.sakaiproject.mailtool.logic.ExternalLogic#getCurrentSite()
	 */
	public Site getCurrentSite()
	{
		String locationId = toolManager.getCurrentPlacement().getContext();
		Site site = null;
		try
		{
			site = siteService.getSite(locationId);
		}
		catch (IdUnusedException e)
		{
			log.error("Cannot get the info about locationId: " + locationId);
		}
		return site;
	}

	/**
	 * @see org.sakaiproject.mailtool.logic.ExternalLogic#getCurrentUserId()
	 */
	public String getCurrentUserId()
	{
		return sessionManager.getCurrentSessionUserId();
	}

	/**
	 * @see org.sakaiproject.mailtool.logic.ExternalLogic#getSiteID()
	 */
	public String getSiteID()
	{
		return toolManager.getCurrentPlacement().getContext();
	}

	/**
	 * @see org.sakaiproject.mailtool.logic.ExternalLogic#getSiteRealmID()
	 */
	public String getSiteRealmID()
	{
		return ("/site/" + getSiteID());
	}

	/**
	 * @see org.sakaiproject.mailtool.logic.ExternalLogic#getSiteType()
	 */
	public String getSiteType()
	{
		String type = null;
		try
		{
			type = siteService.getSite(getSiteID()).getType();
		}
		catch (IdUnusedException e)
		{
			log.debug(e.getMessage(), e);
		}
		return type;
	}

	/**
	 * Get the details for the current user
	 * 
	 * @return
	 */
	public User getCurrentUser()
	{
		User user = userDirectoryService.getCurrentUser();
		return user;
	}

	/**
	 * Get the details for a user
	 * 
	 * @param userId
	 * @return
	 * @see org.sakaiproject.mailtool.logic.ExternalLogic#getUser(java.lang.String)
	 */
	public User getUser(String userId)
	{
		User user = null;
		try
		{
			user = userDirectoryService.getUser(userId);
		}
		catch (UserNotDefinedException e)
		{
			log.warn("Cannot get user for id: " + userId);
		}
		return user;
	}

	/**
	 * @see org.sakaiproject.mailtool.logic.ExternalLogic#getUserDisplayName(java.lang.String)
	 */
	public String getUserDisplayName(String userId)
	{
		String name = "--------";
		User user = getUser(userId);
		if (user != null)
		{
			name = user.getDisplayName();
		}
		return name;
	}

	/**
	 * @see org.sakaiproject.mailtool.logic.ExternalLogic#isUserAdmin(java.lang.String)
	 */
	public boolean isUserAdmin(String userId)
	{
		return securityService.isSuperUser(userId);
	}

	/**
	 * @see org.sakaiproject.mailtool.logic.ExternalLogic#isUserAllowedInLocation(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean isUserAllowedInLocation(String userId, String permission, String locationId)
	{
		boolean allowed = false;
		if (securityService.unlock(userId, permission, locationId))
		{
			allowed = true;
		}
		return allowed;
	}

	/**
	 * Check that the email archive has been added to the current site
	 */
	public boolean isEmailArchiveAddedToSite()
	{
		boolean hasEmailArchive = false;
		String toolid = "sakai.mailbox";
		try
		{
			String siteId = toolManager.getCurrentPlacement().getContext();
			Site site = siteService.getSite(siteId);

			Collection toolsInSite = site.getTools(toolid);
			if (!toolsInSite.isEmpty())
			{
				hasEmailArchive = true;
			}
		}
		catch (Exception e)
		{
			log.debug("Exception: OptionsBean.isEmailArchiveAddedToSite(), " + e.getMessage());
		}
		return hasEmailArchive;
	}

	public void setFunctionManager(FunctionManager functionManager)
	{
		this.functionManager = functionManager;
	}

	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	public void setSecurityService(SecurityService securityService)
	{
		this.securityService = securityService;
	}

	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}
}