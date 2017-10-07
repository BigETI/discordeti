package com.discordeti.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.json.JSONObject;

/**
 * Privileges class
 *
 * @author Ethem Kurt
 */
public class Privileges
{

	/**
	 * Privileges
	 */
	private final HashMap<String, Integer> privileges = new HashMap<>();

	/**
	 * Default constructor
	 */
	public Privileges()
	{
		//
	}

	/**
	 * @param jsonObject
	 *            Configuration as a JSON object
	 */
	public Privileges(final JSONObject jsonObject)
	{
		if (Objects.nonNull(jsonObject))
		{
			for (final Object item : jsonObject.keySet())
			{
				if (item instanceof String)
				{
					final String itemAsString = (String) item;
					privileges.put(itemAsString, jsonObject.optInt(itemAsString));
				}
			}
		}
	}

	/**
	 * Set privilege
	 *
	 * @param privilege
	 *            Privilege
	 * @param value
	 *            Value
	 */
	public void setPrivilege(String privilege, final int value)
	{
		privilege = privilege.trim().toLowerCase();
		if (privileges.containsKey(privilege))
		{
			privileges.remove(privilege);
		}
		privileges.put(privilege, value);
	}

	/**
	 * Revoke privilege
	 *
	 * @param privilege
	 *            Privilege
	 */
	public void revokePrivilege(String privilege)
	{
		privilege = privilege.trim().toLowerCase();
		if (privileges.containsKey(privilege))
		{
			privileges.remove(privilege);
		}
	}

	/**
	 * Check privileges
	 *
	 * @param user
	 *            User
	 * @return If success "true", otherwise "false"
	 */
	public boolean checkPrivileges(final User user)
	{
		boolean ret = false;
		if (user == null)
		{
			ret = privileges.size() <= 0;
		}
		else
		{
			ret = true;
			for (final Entry<String, Integer> i : privileges.entrySet())
			{
				ret = false;
				if (user.getPrivileges().privileges.containsKey(i.getKey()))
				{
					ret = user.getPrivileges().privileges.get(i.getKey()).intValue() >= i.getValue().intValue();
				}
			}
		}
		return ret;
	}

	/**
	 * Get privileges
	 *
	 * @return Privileges
	 */
	public Map<String, Integer> getPrivileges()
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
		final JSONObject ret = new JSONObject();
		for (final Entry<String, Integer> i : privileges.entrySet())
		{
			ret.put(i.getKey(), i.getValue());
		}
		return ret;
	}
}
