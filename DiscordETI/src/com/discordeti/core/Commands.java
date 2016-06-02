package com.discordeti.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.discordeti.event.CommandEventArgs;
import com.discordeti.event.ICommandListener;

import sx.blah.discord.handle.obj.IMessage;

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
	private char executor = '%';

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
	 * Get executor
	 * 
	 * @return Executor
	 */
	public char getExecutor() {
		return executor;
	}

	/**
	 * Set executor
	 * 
	 * @param executor
	 *            Executor
	 */
	public void setExecutor(char executor) {
		this.executor = executor;
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
		String msg = message.getContent();
		if (msg != null) {
			if (msg.length() > 1) {
				if (msg.charAt(0) == executor) {
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
								if (cmd.getPrivileges().checkPrivileges(user)) {
									ArrayList<String> params = new ArrayList<>();
									for (int i = 1; i < args.length; i++)
										params.add(args[i]);
									String raw_params = "";
									if (args[0].length() < msg.length())
										raw_params = msg.substring(args[0].length(), msg.length()).trim();
									commands.get(args[0])
											.setOnCommand(new CommandEventArgs(bot, cmd, message, params, raw_params));
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
}
