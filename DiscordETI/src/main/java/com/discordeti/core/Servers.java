package com.discordeti.core;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONObject;

import sx.blah.discord.handle.obj.IGuild;

/**
 * Servers class
 *
 * @author Ethem Kurt
 */
public class Servers implements IConfiguration
{

	/**
	 * Server entries
	 */
	private final HashMap<String, JSONObject> servers = new HashMap<>();

	/**
	 * Default constructor
	 */
	public Servers()
	{
		load();
	}

	/**
	 * Get server entries
	 *
	 * @return Servers
	 */
	public HashMap<String, JSONObject> getServers()
	{
		return servers;
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
	public Object getServerAttribute(final IGuild guild, final String key)
	{
		Object ret = null;
		final String id = guild.getID();
		if (servers.containsKey(id))
		{
			final JSONObject s = servers.get(id);
			if (s.has(key))
			{
				ret = s.get(key);
			}
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
	public void setServerAttribute(final IGuild guild, final String key, final Object attribute)
	{
		final String id = guild.getID();
		final JSONObject s = servers.containsKey(id) ? servers.get(id) : new JSONObject();
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
	public void removeServerAttribute(final IGuild guild, final String key)
	{
		final String id = guild.getID();
		if (servers.containsKey(id))
		{
			final JSONObject s = servers.get(id);
			if (s.has(key))
			{
				s.remove(key);
				save();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.discordeti.core.IConfiguration#save()
	 */
	@Override
	public void save()
	{
		final JSONObject config = new JSONObject();
		for (final Entry<String, JSONObject> i : servers.entrySet())
		{
			// config.put(i.getKey(), i.getValue().toJSON());
			config.put(i.getKey(), i.getValue());
		}
		ConfigIO.save("servers.json", config);
	}

	/*
	 * (non-Javadoc)
	 * @see com.discordeti.core.IConfiguration#load()
	 */
	@Override
	public void load()
	{
		final JSONObject config = ConfigIO.load("servers.json");
		for (final String key : config.keySet())
		{
			servers.put(key, config.getJSONObject(key));
		}
	}
}
