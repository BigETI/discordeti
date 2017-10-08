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
	private final HashMap<Long, JSONObject> servers = new HashMap<>();

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
	public HashMap<Long, JSONObject> getServers()
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
		final Long id = guild.getLongID();
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
		final long id = guild.getLongID();
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
		final long id = guild.getLongID();
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
		for (final Entry<Long, JSONObject> i : servers.entrySet())
		{
			config.put(i.getKey().toString(), i.getValue());
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
		for (final Object key : config.keySet())
		{
			final String key_as_string = (String) key;
			final long key_as_long = Long.parseLong(key_as_string);
			servers.put(key_as_long, config.getJSONObject(key_as_string));
		}
	}
}
