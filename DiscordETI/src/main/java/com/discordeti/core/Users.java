package com.discordeti.core;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONObject;

/**
 * Users class
 *
 * @author Ethem Kurt
 */
public class Users implements IConfiguration
{

	/**
	 * Users
	 */
	private final HashMap<Long, User> users = new HashMap<>();

	/**
	 * Constructor
	 */
	public Users()
	{
		load();
	}

	/**
	 * Find user by ID
	 *
	 * @param id
	 *            User ID
	 * @return User instance
	 */
	public User findUser(final long id)
	{
		User ret = null;
		if (users.containsKey(id))
		{
			ret = users.get(id);
		}
		return ret;
	}

	/**
	 * Add user
	 *
	 * @param id
	 *            User ID
	 * @return User instance
	 */
	public User addUser(final long id)
	{
		User ret = null;
		if (users.containsKey(id))
		{
			ret = users.get(id);
		}
		else
		{
			ret = new User(id, new Privileges());
			users.put(id, ret);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.discordeti.core.IConfiguration#save()
	 */
	@Override
	public void save()
	{
		final JSONObject config = new JSONObject();
		for (final Entry<Long, User> i : users.entrySet())
		{
			config.put(i.getKey().toString(), i.getValue().toJSON());
		}
		ConfigIO.save("users.json", config);
	}

	/*
	 * (non-Javadoc)
	 * @see com.discordeti.core.IConfiguration#load()
	 */
	@Override
	public void load()
	{
		final JSONObject config = ConfigIO.load("users.json");
		users.clear();
		for (final Object item : config.keySet())
		{
			try
			{
				final String item_as_string = (String) item;
				final long item_as_long = Long.parseLong(item_as_string);
				users.put(item_as_long, new User(item_as_long, new Privileges(config.optJSONObject(item_as_string))));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
