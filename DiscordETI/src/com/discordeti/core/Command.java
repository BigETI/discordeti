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
 *
 */
public class Command extends CommandNotifier {

	/**
	 * Command
	 */
	private String command;

	/**
	 * Description
	 */
	private String description;

	/**
	 * Help
	 */
	private String help = "(No help available)";

	/**
	 * Privileges
	 */
	private Privileges privileges = new Privileges();

	private HashMap<String, HashSet<String>> servers = new HashMap<>();

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
	public Command(String command, String description, ICommandListener listener) {
		this.command = command;
		this.description = description;
		addListener(listener);
	}

	/**
	 * Get command
	 * 
	 * @return Command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Set command
	 * 
	 * @param command
	 *            Command
	 */
	public void setCommand(String command) {
		this.command = command.trim().toLowerCase();
	}

	/**
	 * Get description
	 * 
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get privileges
	 * 
	 * @return Privileges
	 */
	public Privileges getPrivileges() {
		return privileges;
	}

	/**
	 * Get help
	 * 
	 * @return Help
	 */
	public String getHelp() {
		return help;
	}

	/**
	 * Set help
	 * 
	 * @param help
	 *            Help
	 */
	public void setHelp(String help) {
		if (help == null)
			help = "";
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
	public String generateHelp(User user, Commands commands, IGuild guild) {
		StringBuilder sb = new StringBuilder("```");
		sb.append(commands.getDelimiter());
		sb.append(command);
		sb.append("\n\t");
		sb.append(description);
		sb.append("\n\n\t");
		sb.append(help.replaceAll("\\$CMD\\$", command));
		if (guild != null) {
			sb.append("\n\n\tRequired roles:");
			String gid = guild.getID();
			if (servers.containsKey(gid)) {
				HashSet<String> roles = servers.get(gid);
				boolean hn = true;
				for (String rk : roles) {
					IRole role = guild.getRoleByID(rk);
					if (role != null) {
						sb.append("\n\t\t");
						sb.append(role.getName());
						sb.append(" : ");
						sb.append(role.getID());
						hn = false;
					}
				}
				if (hn)
					sb.append(" None");
			} else
				sb.append(" None");
		}
		sb.append("\n\n\tRequired privileges:");
		if (privileges.getPrivileges().size() > 0) {
			for (Entry<String, Integer> i : privileges.getPrivileges().entrySet()) {
				sb.append("\n\t\t");
				sb.append(i.getKey());
				sb.append(" : ");
				sb.append(i.getValue());
				sb.append(privileges.checkPrivileges(user) ? " <-- OK" : " <-- Missing");
			}
		} else
			sb.append(" None");
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
	public boolean checkRoles(IGuild guild, IUser user) {
		boolean ret = true;
		String gid = guild.getID();
		if (servers.containsKey(gid)) {
			for (String role : servers.get(gid)) {
				IRole r = guild.getRoleByID(role);
				if (r != null) {
					ret = false;
					for (IRole ur : user.getRolesForGuild(guild)) {
						if (ur.getID() == role) {
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
	public void addRoleForServer(IGuild guild, IRole role) {
		String gid = guild.getID();
		HashSet<String> roles = null;
		if (servers.containsKey(gid))
			roles = servers.get(gid);
		else
			roles = new HashSet<>();
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
	public void removeRoleForServer(IGuild guild, IRole role) {
		String gid = guild.getID();
		HashSet<String> roles = null;
		if (servers.containsKey(gid))
			roles = servers.get(gid);
		else
			roles = new HashSet<>();
		String rid = role.getID();
		if (roles.contains(rid))
			roles.remove(rid);
		servers.put(gid, roles);
	}
}
