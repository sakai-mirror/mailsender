package org.sakaiproject.mailtool.model;

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