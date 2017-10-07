package com.discordeti.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.discordeti.event.CommandNotifier;
import com.discordeti.event.ICommandListener;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

/**
 * Command class
 *
 * @author Ethem Kurt
 */
public class Command extends CommandNotifier
{

	/**
	 * Command
	 */
	private String command;

	/**
	 * Description
	 */
	private final String description;

	/**
	 * Help
	 */
	private String help = "(No help available)";

	/**
	 * Privileges
	 */
	private final Privileges privileges = new Privileges();

	private final HashMap<String, HashSet<String>> servers = new HashMap<>();

	/**
	 * Constructor
	 *
	 * @param command
	 *            Command
	 * @param description
	 *            Description
	 * @param listener
	 *            Listener instance
	 */
	public Command(final String command, final String description, final ICommandListener listener)
	{
		this.command = command;
		this.description = description;
		addListener(listener);
	}

	/**
	 * Get command
	 *
	 * @return Command
	 */
	public String getCommand()
	{
		return command;
	}

	/**
	 * Set command
	 *
	 * @param command
	 *            Command
	 */
	public void setCommand(final String command)
	{
		this.command = command.trim().toLowerCase();
	}

	/**
	 * Get description
	 *
	 * @return Description
	 */
	public String getDescription()
	{
		return description;
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
	 * Get help
	 *
	 * @return Help
	 */
	public String getHelp()
	{
		return help;
	}

	/**
	 * Set help
	 *
	 * @param help
	 *            Help
	 */
	public void setHelp(String help)
	{
		if (help == null)
		{
			help = "";
		}
		this.help = help;
	}

	/**
	 * generate help topic
	 *
	 * @param user
	 *            User instance
	 * @param commands
	 *            Commands instance
	 * @param guild
	 *            Guild instance
	 * @return Help topic
	 */
	public String generateHelp(final User user, final Commands commands, final IGuild guild)
	{
		final StringBuilder sb = new StringBuilder("```");
		sb.append(commands.getDelimiter());
		sb.append(command);
		sb.append("\n\t");
		sb.append(description);
		sb.append("\n\n\t");
		sb.append(help.replaceAll("\\$CMD\\$", command));
		if (guild != null)
		{
			sb.append("\n\n\tRequired roles:");
			final String gid = guild.getID();
			if (servers.containsKey(gid))
			{
				final HashSet<String> roles = servers.get(gid);
				boolean hn = true;
				for (final String rk : roles)
				{
					final IRole role = guild.getRoleByID(rk);
					if (role != null)
					{
						sb.append("\n\t\t");
						sb.append(role.getName());
						sb.append(" : ");
						sb.append(role.getID());
						hn = false;
					}
				}
				if (hn)
				{
					sb.append(" None");
				}
			}
			else
			{
				sb.append(" None");
			}
		}
		sb.append("\n\n\tRequired privileges:");
		if (privileges.getPrivileges().size() > 0)
		{
			for (final Entry<String, Integer> i : privileges.getPrivileges().entrySet())
			{
				sb.append("\n\t\t");
				sb.append(i.getKey());
				sb.append(" : ");
				sb.append(i.getValue());
				sb.append(privileges.checkPrivileges(user) ? " <-- OK" : " <-- Missing");
			}
		}
		else
		{
			sb.append(" None");
		}
		sb.append("```");
		return sb.toString();
	}

	/**
	 * Check roles for user on guild
	 *
	 * @param guild
	 *            Guild
	 * @param user
	 *            User
	 * @return If successful "true", otherwise "false"
	 */
	public boolean checkRoles(final IGuild guild, final IUser user)
	{
		boolean ret = true;
		final String gid = guild.getID();
		if (servers.containsKey(gid))
		{
			for (final String role : servers.get(gid))
			{
				final IRole r = guild.getRoleByID(role);
				if (r != null)
				{
					ret = false;
					for (final IRole ur : user.getRolesForGuild(guild))
					{
						if (ur.getID() == role)
						{
							ret = true;
							break;
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Add role for server command
	 *
	 * @param guild
	 *            Guild
	 * @param role
	 *            Role
	 */
	public void addRoleForServer(final IGuild guild, final IRole role)
	{
		final String gid = guild.getID();
		HashSet<String> roles = null;
		if (servers.containsKey(gid))
		{
			roles = servers.get(gid);
		}
		else
		{
			roles = new HashSet<>();
		}
		roles.add(role.getID());
		servers.put(gid, roles);
	}

	/**
	 * Remove role from server command
	 *
	 * @param guild
	 *            Guild
	 * @param role
	 *            Role
	 */
	public void removeRoleForServer(final IGuild guild, final IRole role)
	{
		final String gid = guild.getID();
		HashSet<String> roles = null;
		if (servers.containsKey(gid))
		{
			roles = servers.get(gid);
		}
		else
		{
			roles = new HashSet<>();
		}
		final String rid = role.getID();
		if (roles.contains(rid))
		{
			roles.remove(rid);
		}
		servers.put(gid, roles);
	}
}
