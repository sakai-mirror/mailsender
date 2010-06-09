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
package org.sakaiproject.mailsender.model;

/**
 * EmailRole (can be a role, a section, a group)
 *
 * @author kimsooil
 * @author Carl Hall <carl.hall@et.gatech.edu>
 */
public class EmailRole
{
	private String realmId = "";
	private String roleId = "";
	private String roleSingular = "";
	private String rolePlural = "";
	private String roleType = "";

	public EmailRole(String realmid, String roleid, String rolesingular, String roleplural)
	{
		realmId = realmid;
		roleId = roleid;
		roleSingular = rolesingular;
		rolePlural = roleplural;
	}

	public EmailRole(String realmid, String roleid, String rolesingular, String roleplural,
			String rtype)
	{
		realmId = realmid;
		roleId = roleid;
		roleSingular = rolesingular;
		rolePlural = roleplural;
		roleType = rtype;
	}

	public String getRealmId()
	{
		return realmId;
	}

	public void setRealmId(String realmId)
	{
		this.realmId = realmId;
	}

	public String getRoleId()
	{
		return roleId;
	}

	public void setRoleId(String roleId)
	{
		this.roleId = roleId;
	}

	public String getRoleSingular()
	{
		return roleSingular;
	}

	public void setRoleSingular(String roleSingular)
	{
		this.roleSingular = roleSingular;
	}

	public String getRolePlural()
	{
		return rolePlural;
	}

	public void setRolePlural(String rolePlural)
	{
		this.rolePlural = rolePlural;
	}

	public String getRoleType()
	{
		return roleType;
	}

	public void setRoleType(String roleType)
	{
		this.roleType = roleType;
	}
}