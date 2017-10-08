package com.discordeti.core;

import org.json.JSONObject;

/**
 * User class
 *
 * @author Ethem Kurt
 */
public class User
{

	/**
	 * ID
	 */
	private final long id;

	/**
	 * Privileges
	 */
	private final Privileges privileges;

	/**
	 * Constructor
	 *
	 * @param id
	 *            ID
	 * @param privileges
	 *            Privileges
	 */
	public User(final long id, final Privileges privileges)
	{
		this.id = id;
		this.privileges = privileges;
	}

	/**
	 * Get ID
	 *
	 * @return ID
	 */
	public long getID()
	{
		return id;
	}

	/**
	 * Get privileges
	 *
	 * @return Privileges
	 */
	public Privileges getPrivileges()
	{
		return privileges;
	}

	/**
	 * Serialize to JSON object
	 *
	 * @return JSON object
	 */
	public JSONObject toJSON()
	{
		return privileges.toJSON();
	}
}
