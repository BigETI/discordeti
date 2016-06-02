package com.discordeti.core;

import java.util.Map.Entry;

import com.discordeti.event.CommandNotifier;
import com.discordeti.event.ICommandListener;

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
	 * @return Help topic
	 */
	public String generateHelp(User user, Commands commands) {
		StringBuilder sb = new StringBuilder("```");
		sb.append(commands.getExecutor());
		sb.append(command);
		sb.append("\n\t");
		sb.append(description);
		sb.append("\n\n\t");
		sb.append(help);
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
}
