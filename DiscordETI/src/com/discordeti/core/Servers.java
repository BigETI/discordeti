package com.discordeti.core;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONObject;

import sx.blah.discord.handle.obj.IGuild;

public class Servers implements IConfiguration {

	private HashMap<String, JSONObject> servers = new HashMap<>();

	/**
	 * Defauolt constructor
	 */
	public Servers() {
		load();
	}

	/**
	 * Gets a server attribute
	 * 
	 * @param guild
	 *            Guild
	 * @param key
	 *            Server attribute key
	 * @return Server attribute
	 */
	public Object getServerAttribute(IGuild guild, String key) {
		Object ret = null;
		String id = guild.getID();
		if (servers.containsKey(id)) {
			JSONObject s = servers.get(id);
			if (s.has(key))
				ret = s.get(key);
		}
		return ret;
	}

	/**
	 * Sets a server attribute
	 * 
	 * @param guild
	 *            Guild
	 * @param key
	 *            Server attribute key
	 * @param attribute
	 *            Server attribute
	 */
	public void setServerAttribute(IGuild guild, String key, Object attribute) {
		String id = guild.getID();
		JSONObject s = (servers.containsKey(id)) ? servers.get(id) : new JSONObject();
		s.put(key, attribute);
		servers.put(id, s);
		save();
	}

	/**
	 * Removes a server attribute
	 * 
	 * @param guild
	 *            Guild
	 * @param key
	 *            Server attribute key
	 */
	public void removeServerAttribute(IGuild guild, String key) {
		String id = guild.getID();
		if (servers.containsKey(id)) {
			JSONObject s = servers.get(id);
			if (s.has(key)) {
				s.remove(key);
				save();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.discordeti.core.IConfiguration#save()
	 */
	@Override
	public void save() {
		JSONObject config = new JSONObject();
		for (Entry<String, JSONObject> i : servers.entrySet()) {
			// config.put(i.getKey(), i.getValue().toJSON());
			config.put(i.getKey(), i.getValue());
		}
		ConfigIO.save("servers.json", config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.discordeti.core.IConfiguration#load()
	 */
	@Override
	public void load() {
		JSONObject config = ConfigIO.load("servers.json");
		for (String key : config.keySet()) {
			servers.put(key, config.getJSONObject(key));
		}
	}
}
