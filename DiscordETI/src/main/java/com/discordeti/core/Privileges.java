package com.discordeti.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

/**
 * Privileges class
 * 
 * @author Ethem Kurt
 *
 */
public class Privileges {

	/**
	 * Privileges
	 */
	private HashMap<String, Integer> privileges = new HashMap<>();

	/**
	 * Default constructor
	 */
	public Privileges() {
		//
	}

	/**
	 * Constructor
	 * 
	 * @param jo
	 *            Configuration as a JSON object
	 */
	public Privileges(JSONObject jo) {
		Integer integer;
		if (jo != null) {
			for (String i : jo.keySet()) {
				integer = jo.optInt(i);
				if (integer != null)
					privileges.put(i, integer);
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
	public void setPrivilege(String privilege, int value) {
		privilege = privilege.trim().toLowerCase();
		if (privileges.containsKey(privilege))
			privileges.remove(privilege);
		privileges.put(privilege, value);
	}

	/**
	 * Revoke privilege
	 * 
	 * @param privilege
	 *            Privilege
	 */
	public void revokePrivilege(String privilege) {
		privilege = privilege.trim().toLowerCase();
		if (privileges.containsKey(privilege))
			privileges.remove(privilege);
	}

	/**
	 * Check privileges
	 * 
	 * @param user
	 *            User
	 * @return If success "true", otherwise "false"
	 */
	public boolean checkPrivileges(User user) {
		boolean ret = false;
		if (user == null)
			ret = (privileges.size() <= 0);
		else {
			ret = true;
			for (Entry<String, Integer> i : privileges.entrySet()) {
				ret = false;
				if (user.getPrivileges().privileges.containsKey(i.getKey()))
					ret = (user.getPrivileges().privileges.get(i.getKey()).intValue() >= i.getValue().intValue());
			}
		}
		return ret;
	}

	/**
	 * Get privileges
	 * 
	 * @return Privileges
	 */
	public Map<String, Integer> getPrivileges() {
		return privileges;
	}

	/**
	 * Serialize to JSON object
	 * 
	 * @return JSON object
	 */
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		for (Entry<String, Integer> i : privileges.entrySet())
			ret.put(i.getKey(), i.getValue());
		return ret;
	}
}
