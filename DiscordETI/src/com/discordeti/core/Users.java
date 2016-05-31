package com.discordeti.core;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONObject;

/**
 * Users class
 * 
 * @author Ethem Kurt
 *
 */
public class Users {

	/**
	 * Users
	 */
	private HashMap<String, User> users = new HashMap<>();

	/**
	 * Constructor
	 */
	public Users() {
		load();
	}

	/**
	 * Find user by ID
	 * 
	 * @param id
	 *            User ID
	 * @return User instance
	 */
	public User findUser(String id) {
		User ret = null;
		if (users.containsKey(id))
			ret = users.get(id);
		return ret;
	}

	/**
	 * Add user
	 * 
	 * @param id
	 *            User ID
	 * @return User instance
	 */
	public User addUser(String id) {
		User ret = null;
		if (users.containsKey(id))
			ret = users.get(id);
		else {
			ret = new User(id, new Privileges());
			users.put(id, ret);
		}
		return ret;
	}

	/**
	 * Save configuration
	 */
	public void save() {
		JSONObject config = new JSONObject();
		for (Entry<String, User> i : users.entrySet()) {
			config.put(i.getKey(), i.getValue().toJSON());
		}
		ConfigIO.save("users.json", config);
	}

	/**
	 * Load configuration
	 */
	public void load() {
		JSONObject config = ConfigIO.load("users.json");
		users.clear();
		for (String i : config.keySet()) {
			users.put(i, new User(i, new Privileges(config.optJSONObject(i))));
		}
	}
}
