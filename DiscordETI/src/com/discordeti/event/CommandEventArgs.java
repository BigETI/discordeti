package com.discordeti.event;

import java.util.List;

import com.discordeti.core.Bot;
import com.discordeti.core.Command;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * Command event arguments class
 * 
 * @author Ethem Kurt
 *
 */
public class CommandEventArgs {

	/**
	 * Bot instance
	 */
	private Bot bot;

	/**
	 * Command
	 */
	private Command command;

	/**
	 * Message
	 */
	private IMessage message;

	/**
	 * Parameters
	 */
	private List<String> params;

	/**
	 * Raw parameters
	 */
	private String raw_params;

	/**
	 * Constructor
	 * 
	 * @param bot
	 *            Bot instance
	 * @param command
	 *            Command
	 * @param channel
	 *            Channel
	 * @param issuer
	 *            Issuer
	 * @param params
	 *            Parameters
	 * @param raw_params
	 *            Raw parameters
	 */
	public CommandEventArgs(Bot bot, Command command, IMessage message, List<String> params, String raw_params) {
		this.bot = bot;
		this.command = command;
		this.message = message;
		this.params = params;
		this.raw_params = raw_params;
	}

	/**
	 * Get bot instance
	 * 
	 * @return Bot instance
	 */
	public Bot getBot() {
		return bot;
	}

	/**
	 * Get command
	 * 
	 * @return Command
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * Get message
	 * 
	 * @return Message
	 */
	public IMessage getMessage() {
		return message;
	}

	/**
	 * Get channel
	 * 
	 * @return Channel
	 */
	public IChannel getChannel() {
		return message.getChannel();
	}

	/**
	 * Get issuer
	 * 
	 * @return Issuer
	 */
	public IUser getIssuer() {
		return message.getAuthor();
	}

	/**
	 * Get parameters
	 * 
	 * @return Parameters
	 */
	public List<String> getParams() {
		return params;
	}

	/**
	 * Get raw parameters
	 * 
	 * @return Raw parameters
	 */
	public String getRawParams() {
		return raw_params;
	}
}
