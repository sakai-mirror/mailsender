/******************************************************************************
 * ExternalLogic.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2006 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.mailsender.logic;

import java.util.Map;

import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * This is the interface for logic which is external to our app logic
 */
public interface ExternalLogic
{
	static String NO_LOCATION = "noLocationAvailable";

	// permissions
	String PERM_ADMIN = "mailtool.admin";
	String PERM_SEND = "mailtool.send";

	/**
	 * @return the current sakai user id (not username)
	 */
	String getCurrentUserId();

	/**
	 * Get details for the current user
	 * 
	 * @return
	 */
	User getCurrentUser();

	/**
	 * Get the display name for a user by their unique id
	 * 
	 * @param userId
	 *            the current sakai user id (not username)
	 * @return display name (probably firstname lastname) or "----------" (10 hyphens) if none found
	 */
	String getUserDisplayName(String userId);

	/**
	 * Get details for a user
	 * 
	 * @param userId
	 * @return
	 */
	User getUser(String userId);

	/**
	 * @param locationId
	 *            a unique id which represents the current location of the user (entity reference)
	 * @return the title for the context or "--------" (8 hyphens) if none found
	 */
	String getCurrentSiteTitle();

	/**
	 * Get the current site's details
	 * 
	 * @return
	 */
	Site getCurrentSite();

	/**
	 * Get the site id for the current site
	 * 
	 * @return
	 */
	String getSiteID();

	/**
	 * Get the realm id for the current site
	 * 
	 * @return
	 */
	String getSiteRealmID();

	/**
	 * Get the type of the current site
	 * 
	 * @return
	 */
	String getSiteType();

	/**
     * Check if this user has site update access
     * 
     * @param userId
     *            the internal user id (not username)
     * @param locationId
     *            a unique id which represents the current location of the user (entity reference)
     * @return true if the user has site update access, false otherwise
     */
    boolean isUserSiteAdmin(String userId, String locationId);

	/**
	 * Check if this user has super admin access
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	boolean isUserAdmin(String userId);

	/**
	 * Check if a user has a specified permission within a context, primarily a convenience method
	 * and passthrough
	 * 
	 * @param userId
	 *            the internal user id (not username)
	 * @param permission
	 *            a permission string constant
	 * @param locationId
	 *            a unique id which represents the current location of the user (entity reference)
	 * @return true if allowed, false otherwise
	 */
	boolean isUserAllowedInLocation(String userId, String permission, String locationId);

	/**
	 * Check if the email archive tool is added to the current site
	 * 
	 * @return true if email archive tool exists, false otherwise
	 */
	boolean isEmailArchiveAddedToSite();

	/**
	 * Send email to a list of users.
	 * 
	 * @param config
	 * @param from
	 * @param to
	 *            Map of email address <address, display name>
	 * @param content
	 * @param badEmailAddresses
	 * @param subject
	 */
	void sendEmail(ConfigEntry config, String fromEmail, String fromName, Map<String, String> to,
			String subject, String content, Map<String, MultipartFile> attachments)
			throws MailsenderException;

	/**
	 * Append email to Email Archive
	 * 
	 * @param config
	 * @param channelRef
	 * @param sender
	 * @param subject
	 * @param body
	 * @return true if success
	 */
	boolean addToArchive(ConfigEntry config, String channelRef, String sender, String subject,
			String body);

	/**
     * @return the current location id of the current user
     */
    String getCurrentLocationId();
}
