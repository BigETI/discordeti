package com.discordeti.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONObject;

import com.discordeti.event.CommandEventArgs;
import com.discordeti.event.ICommandListener;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;

/**
 * Command pool class
 * 
 * @author Ethem Kurt
 *
 */
public class Commands {

	/**
	 * Commands
	 */
	private LinkedHashMap<String, Command> commands = new LinkedHashMap<>();

	/**
	 * Executor
	 */
	private char delimiter = '%';

	/**
	 * Loaded
	 */
	private boolean loaded = false;

	/**
	 * Register command
	 * 
	 * @param command
	 *            Command
	 * @param description
	 *            Description
	 * @param listener
	 *            Listener instance
	 * 
	 * @return Command instance
	 */
	public Command registerCommand(String command, String description, ICommandListener listener) {
		command = command.trim().toLowerCase();
		Command ret = new Command(command, description, listener);
		if (commands.containsKey(command))
			commands.remove(command);
		commands.put(command, ret);
		return ret;
	}

	/**
	 * Get commands
	 * 
	 * @return Commands
	 */
	public Map<String, Command> getCommands() {
		return commands;
	}

	/**
	 * Get delimiter
	 * 
	 * @return Delimiter
	 */
	public char getDelimiter() {
		return delimiter;
	}

	/**
	 * Set executor
	 * 
	 * @param delimiter
	 *            Delimiter
	 */
	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Parse message
	 * 
	 * @param bot
	 *            Bot instance
	 * @param message
	 *            Message
	 */
	public void parseMessage(Bot bot, IMessage message) {
		if (loaded) {
			String msg = message.getContent();
			if (msg != null) {
				if (msg.length() > 1) {
					if (msg.charAt(0) == delimiter) {
						msg = msg.substring(1).trim();
						String[] args = msg.split("\\s+");
						if (args != null) {
							if (args.length > 0) {
								args[0] = args[0].toLowerCase();
								if (commands.containsKey(args[0])) {
									Command cmd = commands.get(args[0]);
									Users users = bot.getUsers();
									String id = message.getAuthor().getID();
									User user = users.findUser(id);
									IGuild guild = message.getGuild();
									if (cmd.checkRoles(guild, message.getAuthor())
											&& cmd.getPrivileges().checkPrivileges(user)) {
										ArrayList<String> params = new ArrayList<>();
										for (int i = 1; i < args.length; i++)
											params.add(args[i]);
										String raw_params = "";
										if (args[0].length() < msg.length())
											raw_params = msg.substring(args[0].length(), msg.length()).trim();
										commands.get(args[0]).setOnCommand(
												new CommandEventArgs(bot, cmd, message, params, raw_params));
									} else
										System.out.println("[" + args[0] + "] Permission denied for "
												+ message.getAuthor().getName() + "@" + message.getChannel().getName());
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Sort commands by name ascending
	 */
	public void sort() {
		TreeMap<String, Command> copy = new TreeMap<>(commands);
		commands.clear();
		commands.putAll(copy);
	}

	/**
	 * Rename command
	 * 
	 * @param old_cmd_name
	 *            Old command
	 * @param new_cmd_name
	 *            New command
	 * @return If successful "true", otherwise "false"
	 */
	public boolean renameCommand(String old_cmd_name, String new_cmd_name) {
		boolean ret = false;
		Command o;
		old_cmd_name = old_cmd_name.trim().toLowerCase();
		new_cmd_name = new_cmd_name.trim().toLowerCase();
		if (commands.containsKey(old_cmd_name)) {
			o = commands.get(old_cmd_name);
			if (!(commands.containsKey(new_cmd_name))) {
				commands.put(new_cmd_name, o);
				commands.remove(old_cmd_name);
				o.setCommand(new_cmd_name);
				sort();
				ret = true;
			}
		}
		return ret;
	}

	public void reloadRoles(Servers servers, IDiscordClient client) {
		for (Entry<String, JSONObject> ss : servers.getServers().entrySet()) {
			IGuild guild = client.getGuildByID(ss.getKey());
			if (guild != null) {
				if (ss.getValue().has("commandroles")) {
					JSONObject crso = ss.getValue().getJSONObject("commandroles");
					for (String rsk : crso.keySet()) {
						if (commands.containsKey(rsk)) {
							Command command = commands.get(rsk);
							JSONObject rso = crso.getJSONObject(rsk);
							for (String rk : rso.keySet()) {
								IRole role = guild.getRoleByID(rk);
								if (role != null)
									command.addRoleForServer(guild, role);
							}
						}
					}
				}
			}
		}
		loaded = true;
	}
}
