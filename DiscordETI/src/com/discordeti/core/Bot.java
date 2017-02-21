package com.discordeti.core;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.discordeti.event.CommandEventArgs;
import com.discordeti.event.ICommandListener;
import com.jbfvm.core.BrainfuckVM;
import com.plotter.algorithms.JSAlgorithm;
import com.plotter.algorithms.Polynom;
import com.plotter.core.DoubleRange;
import com.plotter.visuals.ImageGraph;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.audio.IAudioProcessor;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IInvite;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;

/**
 * Discord bot class
 * 
 * @author Ethem Kurt
 *
 */
public class Bot {

	/**
	 * Max display help commands
	 */
	public static final int MAX_DISPLAY_HELP_COMMANDS = 40;

	/**
	 * Max display playlists
	 */
	public static final int MAX_DISPLAY_PLAYLISTS = 10;

	/**
	 * Max display tracks
	 */
	public static final int MAX_DISPLAY_TRACKS = 10;

	/**
	 * Commands
	 */
	private Commands commands = new Commands();

	/**
	 * Users
	 */
	private Users users = new Users();

	/**
	 * Servers
	 */
	private Servers servers = new Servers();

	/**
	 * Use text to speech
	 */
	private boolean use_tts = false;

	/**
	 * This object
	 */
	public final Bot dis = this;

	/**
	 * Volume
	 */
	private float volume = 0.1f;

	// private IDiscordClient client;

	private void sendMessage(CommandEventArgs args, String text) {
		try {
			args.getChannel().sendMessage(text, use_tts);
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		} catch (DiscordException e) {
			e.printStackTrace();
		} catch (RateLimitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor
	 * 
	 * @param client
	 *            Client instance
	 */
	public Bot(IDiscordClient client) {
		client.getDispatcher().registerListener(this);

		Command cmd = commands.registerCommand("addcommandrole", "Adds a role to a specififed command",
				new ICommandListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see com.discordeti.event.ICommandListener#onCommand(com.
					 * discordeti.event.CommandEventArgs)
					 */
					@Override
					public void onCommand(CommandEventArgs args) {
						String message = null;
						if (args.getParams().size() == 2) {
							String c = args.getParams().get(0).toLowerCase();
							String rid = args.getParams().get(1);
							if (commands.getCommands().containsKey(c)) {
								Command command = commands.getCommands().get(c);
								IRole role = args.getChannel().getGuild().getRoleByID(rid);
								if (role != null) {
									IGuild guild = args.getChannel().getGuild();
									Object o = servers.getServerAttribute(guild, "commandroles");
									JSONObject commandroles = (o == null) ? new JSONObject() : (JSONObject) o;
									JSONObject re = (commandroles.has(c)) ? commandroles.getJSONObject(c)
											: new JSONObject();
									re.put(rid, true);
									commandroles.put(c, re);
									servers.setServerAttribute(guild, "commandroles", commandroles);
									command.addRoleForServer(guild, role);
									message = "Role \"" + role.getName() + "\" has been added to command "
											+ commands.getDelimiter() + c + ".";
								} else
									message = "Invalid role ID!";
							} else
								message = "Command not defined!";
						} else
							message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
									args.getChannel().getGuild());
						sendMessage(args, message);
					}
				});
		cmd.setHelp("This command adds a role to a specified command.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <command> <role id>");

		cmd = commands.registerCommand("addfileplaylist", "Adds an audio file to a playlist", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.discordeti.event.ICommandListener#onCommand(com.
			 * discordeti. event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				if (args.getParams().size() > 1) {
					Object o = (JSONObject) servers.getServerAttribute(args.getChannel().getGuild(), "playlists");
					if (o == null) {
						o = new JSONObject();
						servers.setServerAttribute(args.getChannel().getGuild(), "playlists", o);
					}
					JSONObject pls = (JSONObject) o;
					String playlist = args.getParams().get(0).toLowerCase();
					String track = args.getRawParams().substring(playlist.length()).trim();
					JSONArray pl = (pls.has(playlist)) ? pls.getJSONArray(playlist) : new JSONArray();
					JSONObject t = new JSONObject();
					t.put("track", track);
					t.put("is_stream", false);
					pl.put(t);
					pls.put(playlist, pl);
					servers.save();
					message = "Added audio file \"" + track + "\" to playlist \"" + playlist + "\".";
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("Adds an audio stream to a playlist.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <playlist> <audio file>");
		cmd.getPrivileges().setPrivilege("bot_master", 1);

		cmd = commands.registerCommand("addqueue", "Add audio stream to queue", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				if (args.getParams().size() > 0) {
					String track = args.getRawParams();
					try {
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).queue(new URL(track));
						message = "Added \"" + track + "\" to queue.\n\tWARNING: Web radio streams breaks the system!";
					} catch (IOException | UnsupportedAudioFileException e) {
						e.printStackTrace();
						message = "Can't add audio stream.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("Adds an audio stream to the queue.\n\tUsage: " + commands.getDelimiter() + "$CMD$ <url>");

		cmd = commands.registerCommand("addfilequeue", "Adds an audio file to queue", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				if (args.getParams().size() > 0) {
					String track = args.getRawParams();
					try {
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).queue(new File(track));
						message = "Added file \"" + track + "\" to queue.";
					} catch (IOException | UnsupportedAudioFileException e) {
						e.printStackTrace();
						message = "Can't add audio stream.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("Adds an audio file to the queue.\n\tUsage: " + commands.getDelimiter() + "$CMD$ <file name>");
		cmd.getPrivileges().setPrivilege("bot_master", 1);

		cmd = commands.registerCommand("addplaylist", "Adds an audio stream to the queue", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				if (args.getParams().size() > 1) {
					Object o = (JSONObject) servers.getServerAttribute(args.getChannel().getGuild(), "playlists");
					if (o == null) {
						o = new JSONObject();
						servers.setServerAttribute(args.getChannel().getGuild(), "playlists", o);
					}
					JSONObject pls = (JSONObject) o;
					String playlist = args.getParams().get(0).toLowerCase();
					String track = args.getRawParams().substring(playlist.length()).trim();
					JSONArray pl = (pls.has(playlist)) ? pls.getJSONArray(playlist) : new JSONArray();
					JSONObject t = new JSONObject();
					t.put("track", track);
					t.put("is_stream", true);
					pl.put(t);
					pls.put(playlist, pl);
					servers.save();
					message = "Added audio stream \"" + track + "\" to playlist \"" + playlist
							+ "\".\n\tWARNING: Web radio streams breaks the system!";
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("Adds an audio stream to a playlist.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <playlist> <audio stream>");

		cmd = commands.registerCommand("allowapp", "Returns a link to allow this application for your guild.",
				new ICommandListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see com.discordeti.event.ICommandListener#onCommand(com.
					 * discordeti. event.CommandEventArgs)
					 */
					@Override
					public void onCommand(CommandEventArgs args) {
						try {
							sendMessage(args,
									"Allow this application now: https://discordapp.com/oauth2/authorize?client_id="
											+ client.getApplicationClientID() + "&scope=bot&permissions=0");
						} catch (DiscordException e) {
							e.printStackTrace();
						}
					}
				});
		cmd.setHelp("This command returns a link to add this bot to your server.");

		cmd = commands.registerCommand("autojoin", "Auto joins a specified voice channel", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.discordeti.event.ICommandListener#onCommand(com.
			 * discordeti.event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				if (args.getParams().size() == 1) {
					String id = args.getParams().get(0);
					IVoiceChannel voice_channel = args.getChannel().getGuild().getVoiceChannelByID(id);
					if (voice_channel != null) {
						try {
							voice_channel.join();
							servers.setServerAttribute(args.getChannel().getGuild(), "auto_join_voice_channel", id);
							message = "I'll continue joining the voice channel  \"" + voice_channel.getName() + "\".";
						} catch (MissingPermissionsException e) {
							e.printStackTrace();
							message = "Can't join voice channel \"" + voice_channel.getName() + "\".";
						}
					} else
						message = "Invalid voice channel ID.";
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command lets the bot automaticly join a specified voice channel.\n\tUsage: "
				+ commands.getDelimiter() + "$CMD$ <voice channel id>");

		cmd = commands.registerCommand("ban", "Bans a user.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() == 1) {
					IUser target = args.getChannel().getGuild().getUserByID(args.getParams().get(0));
					if (target == null)
						message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
					else
						try {
							args.getChannel().getGuild().banUser(target);
						} catch (MissingPermissionsException | DiscordException | RateLimitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp(
				"This command bans a specified user by ID.\n\tUsage: " + commands.getDelimiter() + "$CMD$ <user id>");

		cmd = commands.registerCommand("brainfuck", "Executes Brainfuck from input", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder sb = new StringBuilder();
				if (args.getParams().size() > 0) {
					String input = args.getRawParams();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					for (char i : input.toCharArray())
						baos.write((byte) i);
					try (ByteArrayOutputStream baos2 = new ByteArrayOutputStream()) {
						PrintStream ps = new PrintStream(baos2);
						BrainfuckVM bfvm = new BrainfuckVM(baos.toByteArray(), ps, null, null);
						bfvm.run();
						sb.append("```Brainfuck output```\n\n```\n");
						sb.append(baos2.toString());
						sb.append("\n```");
					} catch (IOException e) {
						e.printStackTrace();
						sb = new StringBuilder("```Brainfuck error```\n\n```\n");
						sb.append(e.getMessage());
						sb.append("\n```");
					} finally {
						//
					}
				} else
					sb.append(args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild()));
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("This command executes Brainfuck from input.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <Brainfuck>");

		cmd = commands.registerCommand("brainfuckfile", "Executes brainfuck from file", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder sb = new StringBuilder();
				if (args.getParams().size() > 0) {
					String file_name = args.getRawParams();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int n;
					byte[] data = new byte[1024];
					try (FileInputStream fis = new FileInputStream(file_name)) {
						while ((n = fis.read(data)) != -1)
							baos.write(data, 0, n);
						try (ByteArrayOutputStream baos2 = new ByteArrayOutputStream()) {
							PrintStream ps = new PrintStream(baos2);
							BrainfuckVM bfvm = new BrainfuckVM(baos.toByteArray(), ps, null, null);
							bfvm.run();
							sb.append("```Brainfuck output```\n\n```");
							sb.append(baos2.toString());
							sb.append("\n```");
						} catch (IOException e) {
							e.printStackTrace();
							sb = new StringBuilder("```Brainfuck error```\n\n```");
							sb.append(e.getMessage());
							sb.append("```");
						} finally {
							//
						}
					} catch (IOException e) {
						e.printStackTrace();
						sb = new StringBuilder("```Brainfuck error```\n\n```");
						sb.append(e.getMessage());
						sb.append("```");
					} finally {
						//
					}
				} else
					sb.append(args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild()));
				sendMessage(args, sb.toString());
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.setHelp("This command executes Brainfuck from a file.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <file name>");

		cmd = commands.registerCommand("channelid", "Returns the channel ID", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				sendMessage(args, "Channel ID: " + args.getChannel().getID());
			}
		});
		cmd.setHelp("This command shows the current channel ID and is useful for administration purposes.");

		cmd = commands.registerCommand("channels", "Lists all available channels", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder sb = new StringBuilder();
				if (args.getParams().size() == 1) {
					String id = args.getParams().get(0);
					IChannel channel = args.getChannel().getGuild().getChannelByID(id);
					if (channel == null)
						sb.append("Invalid channel ID!");
					else {
						sb.append("Channel \"");
						sb.append(channel.getName());
						sb.append("\" : ");
						sb.append(channel.getID());
						sb.append("\n```\nTopic: ");
						sb.append(channel.getTopic());
						sb.append("\nCreation date: ");
						sb.append(channel.getCreationDate());
						sb.append("\nInvites:");
						try {
							for (IInvite invite : channel.getInvites()) {
								sb.append("\n\thttps://discord.gg/");
								sb.append(invite.getInviteCode());
							}
						} catch (DiscordException | RateLimitException | MissingPermissionsException e) {
							e.printStackTrace();
						}
						sb.append("\n```");
					}
				} else {
					sb.append("Channels:\n```");
					for (IChannel channel : args.getChannel().getGuild().getChannels()) {
						sb.append("\n");
						sb.append(channel.getName());
						sb.append(" : ");
						sb.append(channel.getID());
					}
					sb.append("\n```");
				}
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("This command lists all available channels\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <channel id (optional)>");

		cmd = commands.registerCommand("clear", "Clears the chat", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				try {
					while (args.getChannel().getMessages().size() > 0) {
						if (args.getChannel().getMessages().size() > 100)
							args.getChannel().getMessages().deleteFromRange(0, 99);
						else
							args.getChannel().getMessages().deleteFromRange(0, args.getChannel().getMessages().size());
						Thread.sleep(1000);
					}
					args.getMessage().delete();
					sendMessage(args, "<@" + args.getIssuer().getID() + "> cleared the chat.");
				} catch (DiscordException | MissingPermissionsException | RateLimitException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		cmd.setHelp("This command clears the chat.");

		cmd = commands.registerCommand("disablensfw", "Disables NSFW on a specififed channel", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				IChannel channel = null;
				if (args.getParams().size() == 1) {
					channel = args.getChannel().getGuild().getChannelByID(args.getParams().get(0));
				} else
					channel = args.getChannel();
				if (channel == null)
					message = "Invalid channel ID!";
				else {
					IGuild guild = args.getChannel().getGuild();
					Object o = servers.getServerAttribute(guild, "nsfw");
					JSONObject nsfw = (o == null) ? new JSONObject() : (JSONObject) o;
					if (nsfw.has(channel.getID()))
						nsfw.remove(channel.getID());
					servers.setServerAttribute(guild, "nsfw", nsfw);
					message = "NSFW has been disabled on the channel.";
				}
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command disables NSFW on a specified channel.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <channel id (optional)>");

		cmd = commands.registerCommand("enablensfw", "Enables NSFW on a specififed channel", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				IChannel channel = null;
				if (args.getParams().size() == 1) {
					channel = args.getChannel().getGuild().getChannelByID(args.getParams().get(0));
				} else
					channel = args.getChannel();
				if (channel == null)
					message = "Invalid channel ID!";
				else {
					IGuild guild = args.getChannel().getGuild();
					Object o = servers.getServerAttribute(guild, "nsfw");
					JSONObject nsfw = (o == null) ? new JSONObject() : (JSONObject) o;
					nsfw.put(channel.getID(), true);
					servers.setServerAttribute(guild, "nsfw", nsfw);
					message = "NSFW has been enabled on the channel.";
				}
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command enables NSFW on a specified channel.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <channel id (optional)>");

		cmd = commands.registerCommand("exit", "Disconnects the bot.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				try {
					client.logout();
					System.exit(0);
				} catch (DiscordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.setHelp("This command tells the bot to disconnect from Discord and shut down.");

		commands.registerCommand("hello", "Just a test command", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				sendMessage(args, "Hi <@" + args.getIssuer().getID() + ">!");
			}
		});

		cmd = commands.registerCommand("help", "Show help topics", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				User user = users.findUser(args.getIssuer().getID());
				StringBuilder sb = new StringBuilder("\n**=== __Start of help topic__ ===**\n\n");
				if (args.getParams().size() > 0) {
					String key = args.getParams().get(0);
					if (commands.getCommands().containsKey(key))
						sb.append(commands.getCommands().get(key).generateHelp(user, commands,
								args.getChannel().getGuild()));
					else {
						sb.append("Command \"");
						sb.append(commands.getDelimiter());
						sb.append(key);
						sb.append("\" was not found.\n\n");
					}
				} else {
					int c = 0;
					for (String i : commands.getCommands().keySet()) {
						sb.append("```");
						sb.append(commands.getDelimiter());
						sb.append(i);
						sb.append("\n\t");
						sb.append(commands.getCommands().get(i).getDescription());
						sb.append("```");
						++c;
						if ((c % MAX_DISPLAY_HELP_COMMANDS) == 0) {
							c = 0;
							sendMessage(args, sb.toString());
							sb = new StringBuilder();
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
				sb.append("\n**=== __End of help topic__ ===**");
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("This command is used to show help topics about other available commands.\n\tUse "
				+ commands.getDelimiter() + "$CMD$ <command> to show help topics.");

		cmd = commands.registerCommand("javascript", "Executes JavaScript from input", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder message = new StringBuilder();
				if (args.getParams().size() > 0) {
					ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
					try {
						StringWriter sw = new StringWriter();
						engine.getContext().setWriter(sw);
						engine.eval(args.getRawParams());
						message.append("```JavaScript output```\n\n```\n");
						message.append(sw.toString());
						message.append("\n```");
					} catch (ScriptException e) {
						message = new StringBuilder("```JavaScript error```\n\n```\n");
						message.append(e.getMessage());
						message.append("\n```");
					}
				} else
					message.append(args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild()));
				sendMessage(args, message.toString());
			}
		});
		cmd.setHelp("This command executes JavaScript from input.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <JavaScript>");

		cmd = commands.registerCommand("javascriptfile", "Executes JavaScript from input", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder message = new StringBuilder();
				StringBuilder r = new StringBuilder();
				String line;
				if (args.getParams().size() > 0) {
					String file_name = args.getRawParams();
					try (FileReader fr = new FileReader(file_name)) {
						try (BufferedReader br = new BufferedReader(fr)) {
							ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
							try {
								while ((line = br.readLine()) != null) {
									r.append(line);
									r.append('\n');
								}
								StringWriter sw = new StringWriter();
								engine.getContext().setWriter(sw);
								engine.eval(r.toString());
								message.append("```JavaScript output```\n\n```\n");
								message.append(sw.toString());
								message.append("\n```");
							} catch (ScriptException e) {
								message = new StringBuilder("```JavaScript error```\n\n```\n");
								message.append(e.getMessage());
								message.append("```");
							}
						} finally {
							//
						}
					} catch (IOException e1) {
						message = new StringBuilder("```JavaScript error```\n\n```");
						message.append(e1.getMessage());
						message.append("```");
					} finally {
						//
					}

				} else
					message.append(args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild()));
				sendMessage(args, message.toString());
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.setHelp("This command executes JavaScript from a file.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <file name>");

		cmd = commands.registerCommand("join", "Joins a voice channel.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {

				IVoiceChannel voice_channel = null;
				if (args.getParams().size() == 1) {
					String id = args.getParams().get(0);
					voice_channel = args.getChannel().getGuild().getVoiceChannelByID(id);
				} else {
					String id = args.getChannel().getGuild().getID();
					for (IVoiceChannel vc : args.getIssuer().getConnectedVoiceChannels()) {
						if (vc.getGuild().getID() == id) {
							voice_channel = vc;
							break;
						}
					}
				}
				if (voice_channel != null) {
					try {
						voice_channel.join();
					} catch (MissingPermissionsException e) {
						e.printStackTrace();
					}
					try {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						try {
							AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild())
									.queue(new File("hello.mp3"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							voice_channel.sendMessage("Hello!");
						} catch (MissingPermissionsException | RateLimitException e) {
							e.printStackTrace();
						}
					} catch (DiscordException e) {
						e.printStackTrace();
					}
				} else
					sendMessage(args, "Invalid voice channel ID.");
			}
		});
		cmd.setHelp("This command lets the bot join a voice channel by its id.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <voice channel id (optional)>");

		cmd = commands.registerCommand("kick", "Kicks a user.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() == 1) {
					IUser target = args.getChannel().getGuild().getUserByID(args.getParams().get(0));
					if (target == null)
						message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
					else
						try {
							args.getChannel().getGuild().kickUser(target);
						} catch (MissingPermissionsException | DiscordException | RateLimitException e) {
							e.printStackTrace();
						}
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp(
				"This command kicks a specified user by ID.\n\tUsage: " + commands.getDelimiter() + "$CMD$ <user id>");

		cmd = commands.registerCommand("leave", "Leaves voice channels.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				for (IVoiceChannel i : args.getIssuer().getConnectedVoiceChannels()) {
					if (i.getGuild().getID() == args.getChannel().getGuild().getID()) {
						i.leave();
						break;
					}
				}
			}
		});
		cmd.setHelp("This command lets the bot leave the currently connected voice channel.");

		cmd = commands.registerCommand("leaveall", "Leaves all voice channels.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				for (IVoiceChannel i : args.getIssuer().getConnectedVoiceChannels()) {
					i.leave();
				}
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.setHelp("This command lets the bot leave all voice channels on Discord.");

		cmd = commands.registerCommand("loop", "Loops the audio queue", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				boolean l = !AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).isLooping();
				AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setLoop(l);
				sendMessage(args, "Looping queue has been " + (l ? "enabled." : "disabled."));
			}
		});
		cmd.setHelp("Loops the audio queue.");

		cmd = commands.registerCommand("myid", "Returns your client ID", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				sendMessage(args, "Your ID: " + args.getIssuer().getID());
			}
		});
		cmd.setHelp("This command shows your client ID and is useful for administration purposes.");

		cmd = commands.registerCommand("myprivileges", "Shows your privileges", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				User user = users.findUser(args.getIssuer().getID());
				StringBuilder sb = new StringBuilder("Your privileges:");
				if (user == null)
					sb.append(" None");
				else {
					if (user.getPrivileges().getPrivileges().size() > 0) {
						for (Entry<String, Integer> i : user.getPrivileges().getPrivileges().entrySet()) {
							sb.append("\n\t");
							sb.append(i.getKey());
							sb.append(" : ");
							sb.append(i.getValue());
						}
					} else
						sb.append(" None");
				}
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("This command lists all of your privileges.");

		cmd = commands.registerCommand("nsfws", "Lists all NSFW channels", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder sb = new StringBuilder();
				Object o = servers.getServerAttribute(args.getChannel().getGuild(), "nsfw");
				if (o != null) {
					JSONObject nsfw = (JSONObject) o;
					boolean hn = true;
					for (String k : nsfw.keySet()) {
						IChannel channel = args.getChannel().getGuild().getChannelByID(k);
						if (channel != null) {
							if (hn) {
								sb.append("NSFW channels:\n```");
								hn = false;
							}
							sb.append("\n");
							sb.append(channel.getName());
							sb.append(" : ");
							sb.append(channel.getID());
						}
					}
					if (hn)
						sb.append("There are no NSFW channels.");
					else
						sb.append("\n```");
				} else
					sb.append("There are no NSFW channels.");
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("This command lists all NSFW channels.");

		cmd = commands.registerCommand("pause", "Pauses the audio.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setPaused(true);
			}
		});
		cmd.setHelp("This command pauses the audio stream.");

		cmd = commands.registerCommand("play", "Plays an audio stream.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				if (args.getParams().size() == 1) {
					try {
						URL url = new URL(args.getRawParams());
						// Altfunktion (sollte verworfen werden)
						// args.getChannel().getGuild().getAudioChannel().queueUrl(url);

						// ...
						// AudioPlayer ap =
						// AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild());
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).clean();
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setVolume(volume);
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).queue(url);

						// metaDataQueue.add(new AudioMetaData(null, url,
						// AudioSystem.getAudioFileFormat(url),
						// stream.getFormat().getChannels()));
						message = "Playing: " + args.getRawParams()
								+ "\n\tWARNING: Web radio streams breaks the system!";
					} catch (IOException e) {
						message = "Can't play audio stream.";
					} catch (UnsupportedAudioFileException e) {
						message = "Can't play audio stream.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command lets the bot play an audio stream.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <audio stream url>");

		cmd = commands.registerCommand("playfile", "Plays an audio file.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				if (args.getParams().size() > 0) {
					File file = new File(args.getRawParams());
					try {
						// AudioPlayer ap =
						// AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild());
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).clean();
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setVolume(volume);
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).queue(file);
						message = "Playing: " + file;
					} catch (IOException | UnsupportedAudioFileException e) {
						message = "Can't play audio.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.setHelp("This command lets the bot play an audio file.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <audio file>");

		cmd = commands.registerCommand("playlistorder", "Changes the order of tracks within a playlist",
				new ICommandListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see com.discordeti.event.ICommandListener#onCommand(com.
					 * discordeti.event.CommandEventArgs)
					 */
					@Override
					public void onCommand(CommandEventArgs args) {
						String message = null;
						if (args.getParams().size() == 3) {
							Object o = servers.getServerAttribute(args.getChannel().getGuild(), "playlists");
							String playlist = args.getParams().get(0).toLowerCase();
							try {
								int from_index = Integer.parseInt(args.getParams().get(1));
								int to_index = Integer.parseInt(args.getParams().get(2));
								if (o != null) {
									JSONObject pls = (JSONObject) o;
									if (pls.has(playlist)) {
										JSONArray tracks = pls.getJSONArray(playlist);
										if ((from_index >= 0) && (from_index < tracks.length()) && (to_index >= 0)
												&& (to_index < tracks.length())) {
											if (from_index != to_index) {
												JSONObject track = tracks.getJSONObject(to_index);
												tracks.put(to_index, tracks.getJSONObject(from_index));
												tracks.put(from_index, track);
												message = "Order from tracks \""
														+ tracks.getJSONObject(to_index).getString("track") + "\" and \""
														+ track.getString("track") + "\" from playlist \"" + playlist
														+ "\" has been changed.";
											} else
												message = "That didn't change anything at all.";
										} else
											message = "Invalid index!";
									} else
										message = "Playlist \"" + playlist + "\" not found.";
								} else
									message = "Playlist \"" + playlist + "\" not found.";
							} catch (NumberFormatException e) {
								e.printStackTrace();
								message = "Invalid index!";
							}
						} else
							message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
									args.getChannel().getGuild());
						sendMessage(args, message);
					}
				});
		cmd.setHelp("This command changes the order of tracks within a playlist.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <playlist> <from index> <to index>");

		cmd = commands.registerCommand("playlists", "Lists playlists", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder sb = new StringBuilder();
				Object o = (JSONObject) servers.getServerAttribute(args.getChannel().getGuild(), "playlists");
				if (o == null) {
					o = new JSONObject();
					servers.setServerAttribute(args.getChannel().getGuild(), "playlists", o);
				}
				JSONObject pls = (JSONObject) o;
				if (args.getParams().size() == 1) {
					String playlist = args.getParams().get(0).toLowerCase();
					if (pls.has(playlist)) {
						sb.append("Playlist \"");
						sb.append(playlist);
						JSONArray pl = pls.getJSONArray(playlist);
						int i = 0, len = pl.length();
						if (len > 0) {
							sb.append("\":\n```\n");
							for (i = 0; i < len; i++) {
								JSONObject t = pl.getJSONObject(i);
								String track = t.getString("track");
								boolean is_stream = t.getBoolean("is_stream");
								sb.append("\n");
								sb.append(i);
								sb.append(" - ");
								sb.append(is_stream ? "[STREAM] " : "[FILE] ");
								sb.append(track);
								if (((i + 1) % MAX_DISPLAY_TRACKS) == 0) {
									sb.append("\n```");
									sendMessage(args, sb.toString());
									if ((i + 1) < len) {
										sb = new StringBuilder();
										sb.append("```");
									} else
										return;
								}
							}
							if ((len % MAX_DISPLAY_TRACKS) != 0)
								sb.append("\n```");
						} else
							sb.append("\" is empty.");
					} else {
						sb.append("There is no playlist called \"");
						sb.append(playlist);
						sb.append("\".");
					}
				} else {
					sb.append("Available playlists:");
					int c = 0;
					for (String pl : pls.keySet()) {
						if (c == 0)
							sb.append("\n```");
						sb.append("\n");
						sb.append(pl);
						++c;
						if ((c % MAX_DISPLAY_PLAYLISTS) == 0) {
							c = 0;
							sb.append("\n```");
							sendMessage(args, sb.toString());
							sb = new StringBuilder();
						}
					}
					if (c > 0)
						sb.append("\n```");
					else
						return;
				}
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("Lists all playlists.\n\tUsage: " + commands.getDelimiter() + "$CMD$ <playlist (optional)>");

		cmd = commands.registerCommand("playlistsort", "Sorts a playlist", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				if (args.getParams().size() == 2) {
					Object o = servers.getServerAttribute(args.getChannel().getGuild(), "playlists");
					String playlist = args.getParams().get(0).toLowerCase();
					if (o != null) {
						JSONObject pls = (JSONObject) o;
						if (pls.has(playlist)) {
							JSONArray tracks = pls.getJSONArray(playlist);
							TreeMap<String, JSONObject> sorted = new TreeMap<>();
							for (int i = 0, len = tracks.length(); i < len; i++) {
								JSONObject track = tracks.getJSONObject(i);
								sorted.put(track.getString("track"), track);
							}
							JSONArray sorted_tracks;
							switch (args.getParams().get(1).toLowerCase()) {
							case "asc":
								sorted_tracks = new JSONArray();
								for (String key : sorted.keySet())
									sorted_tracks.put(sorted.get(key));
								pls.put(playlist, sorted_tracks);
								message = "Playlist \"" + playlist + "\" has been sorted ascending.";
								break;
							case "desc":
								sorted_tracks = new JSONArray();
								for (String key : sorted.descendingKeySet())
									sorted_tracks.put(sorted.get(key));
								pls.put(playlist, sorted_tracks);
								message = "Playlist \"" + playlist + "\" has been sorted descending.";
								break;
							default:
								message = "Invalid criteria!";
							}
						} else
							message = "Playlist \"" + playlist + "\" not found.";

					} else
						message = "Playlist \"" + playlist + "\" not found.";

				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command sorts a playliost by ciriteria.\n\n\tCriterias:\n\t\tasc\n\t\tdesc\n\n\tUsage: "
				+ commands.getDelimiter() + "$CMD$ <playlist> <criteria>");

		cmd = commands.registerCommand("playplaylist", "Plays a playlist", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				if (args.getParams().size() == 1) {
					Object o = (JSONObject) servers.getServerAttribute(args.getChannel().getGuild(), "playlists");
					if (o == null) {
						o = new JSONObject();
						servers.setServerAttribute(args.getChannel().getGuild(), "playlists", o);
					}
					JSONObject pls = (JSONObject) o;
					String playlist = args.getParams().get(0).toLowerCase();
					if (pls.has(playlist)) {
						JSONArray pl = pls.getJSONArray(playlist);
						// AudioPlayer ap =
						// AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild());
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).clean();
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setVolume(volume);
						for (int i = 0, len = pl.length(); i < len; i++) {
							JSONObject t = pl.getJSONObject(i);
							String track = t.getString("track");
							boolean is_stream = t.getBoolean("is_stream");
							try {
								if (is_stream)
									AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild())
											.queue(new URL(track));
								else
									AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild())
											.queue(new File(track));

							} catch (IOException | UnsupportedAudioFileException e) {
								e.printStackTrace();
							}
						}
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setVolume(volume);
						message = "Playing playlist \"" + playlist + "\"";

					} else
						message = "Playlist  \"" + playlist + "\" does not exist.";
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("Removes an entire playlist.\n\tUsage: " + commands.getDelimiter() + "$CMD$ <playlist>");

		cmd = commands.registerCommand("plot", "Plots a graph using JavaScript", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() > 0) {
					try {
						String script = args.getRawParams();
						JSAlgorithm jsa = new JSAlgorithm("y = " + script + ";");
						ImageGraph<Double, Double> graph = new ImageGraph<Double, Double>(1920, 1080, 100.0, 100.0, 0.0,
								0.0);
						graph.plot(new DoubleRange(-100.0, 100.0, 50000), jsa, Color.WHITE);
						File f = new File("tempplot.png");
						try {
							ImageIO.write(graph, "PNG", f);
							try {
								args.getChannel().sendFile(new File("tempplot.png"));
								message = script;
							} catch (IOException | MissingPermissionsException | DiscordException
									| RateLimitException e) {
								message = "Failed to upload plot.";
							}
						} catch (IOException e) {
							message = "Plot couldn't be saved.";
						}
					} catch (ScriptException e) {
						message = "```JavaScript error```\n\n```\n" + e.getMessage() + "\n```";
					}
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				if (message != null)
					sendMessage(args, message.toString());
			}
		});
		cmd.setHelp("This command allows you to plot a 2D graph with \"x\" as variable. Usage: "
				+ commands.getDelimiter() + " $CMD$ <JavaScript expression>");

		cmd = commands.registerCommand("polynom", "Plots a polynom", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() > 0) {
					try {
						double[] factors = new double[args.getParams().size()];
						for (int i = 0; i < factors.length; i++)
							factors[i] = Double.parseDouble(args.getParams().get(i));
						Polynom poly = new Polynom(factors);
						ImageGraph<Double, Double> graph = new ImageGraph<>(1920, 1080, 100.0, 100.0, 0.0, 0.0);
						graph.plot(new DoubleRange(-100.0, 100.0, 50000), poly, Color.WHITE);
						File f = new File("tempplot.png");
						try {
							ImageIO.write(graph, "PNG", f);
							try {
								args.getChannel().sendFile(new File("tempplot.png"));
								message = poly.toString();
							} catch (IOException | MissingPermissionsException | DiscordException
									| RateLimitException e) {
								message = "Failed to upload plot.";
							}
						} catch (IOException e) {
							message = "Plot couldn't be saved.";
						}

					} catch (NumberFormatException e) {
						message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
					}
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				if (message != null)
					sendMessage(args, message);
			}
		});
		cmd.setHelp("This command plots a polynom.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <x0> <Optional x1> <Optional x2>...");

		cmd = commands.registerCommand("removeautojoin", "Does not auto join a voice channel anymore",
				new ICommandListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see com.discordeti.event.ICommandListener#onCommand(com.
					 * discordeti.event.CommandEventArgs)
					 */
					@Override
					public void onCommand(CommandEventArgs args) {
						servers.removeServerAttribute(args.getChannel().getGuild(), "auto_join_voice_channel");
						sendMessage(args, "I accept your decision, and I'll continue with my life.");
					}
				});
		cmd.setHelp("The bot won't automaticly join the specified voice channel on this server.");

		cmd = commands.registerCommand("removecommandrole", "Removes a role from a specififed command",
				new ICommandListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see com.discordeti.event.ICommandListener#onCommand(com.
					 * discordeti.event.CommandEventArgs)
					 */
					@Override
					public void onCommand(CommandEventArgs args) {
						String message = null;
						if (args.getParams().size() == 2) {
							String c = args.getParams().get(0).toLowerCase();
							String rid = args.getParams().get(1);
							if (commands.getCommands().containsKey(c)) {
								Command command = commands.getCommands().get(c);
								IRole role = args.getChannel().getGuild().getRoleByID(rid);
								if (role != null) {
									IGuild guild = args.getChannel().getGuild();
									Object o = servers.getServerAttribute(guild, "commandroles");
									JSONObject commandroles = (o == null) ? new JSONObject() : (JSONObject) o;
									JSONObject re = (commandroles.has(c)) ? commandroles.getJSONObject(c)
											: new JSONObject();
									if (re.has(rid))
										re.remove(rid);
									commandroles.put(c, re);
									servers.setServerAttribute(guild, "commandroles", commandroles);
									command.removeRoleForServer(guild, role);
									message = "Role \"" + role.getName() + "\" has been removed from command "
											+ commands.getDelimiter() + c + ".";
								} else
									message = "Invalid role ID!";
							} else
								message = "Command not defined!";
						} else
							message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
									args.getChannel().getGuild());
						sendMessage(args, message);
					}
				});
		cmd.setHelp("This command removes a role from a specified command.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <command> <role id>");

		cmd = commands.registerCommand("removeplaylist", "Removes a playlist", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				if ((args.getParams().size() == 1) || (args.getParams().size() == 2)) {
					Object o = (JSONObject) servers.getServerAttribute(args.getChannel().getGuild(), "playlists");
					if (o == null) {
						o = new JSONObject();
						servers.setServerAttribute(args.getChannel().getGuild(), "playlists", o);
					}
					JSONObject pls = (JSONObject) o;
					String playlist = args.getParams().get(0).toLowerCase();
					if (pls.has(playlist)) {
						if (args.getParams().size() == 2) {
							JSONArray tracks = pls.getJSONArray(playlist);
							try {
								int index = Integer.parseInt(args.getParams().get(1));
								if ((index >= 0) && (index < tracks.length())) {
									message = "Removed track \"" + tracks.getJSONObject(index).getString("track")
											+ "\" from playlist \"" + playlist + "\".";
									tracks.remove(index);
								} else
									message = "Invalid index!";
							} catch (NumberFormatException e) {
								e.printStackTrace();
								message = "Invalid index!";
							}
						} else {
							pls.remove(playlist);
							message = "Removed playlist \"" + playlist + "\".";
						}
					}
					servers.save();
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("Removes an entire playlist.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <playlist> <song index (optional)>");

		cmd = commands.registerCommand("renameplaylist", "Renames a playlist", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				if (args.getParams().size() == 2) {
					Object o = (JSONObject) servers.getServerAttribute(args.getChannel().getGuild(), "playlists");
					String from_playlist = args.getParams().get(0).toLowerCase();
					String to_playlist = args.getParams().get(1).toLowerCase();
					if (o != null) {
						JSONObject pls = (JSONObject) o;

						if (pls.has(from_playlist)) {
							if (pls.has(to_playlist))
								message = "Playlist \"" + to_playlist + "\" already exists.";
							else {
								pls.put(to_playlist, pls.getJSONArray(from_playlist));
								pls.remove(from_playlist);
								message = "Playlist \"" + from_playlist + "\" has been renamed to \"" + to_playlist
										+ "\".";
							}
						} else
							message = "Playlist \"" + from_playlist + "\" not found.";
					} else
						message = "Playlist \"" + from_playlist + "\" not found.";
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command renames a specified playlist.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <from playlist> <to playlist>");

		cmd = commands.registerCommand("resume", "Resumes the audio.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setPaused(false);
			}
		});
		cmd.setHelp("This command resumes the audio stream.");

		cmd = commands.registerCommand("revokeprivilege", "Revoke privilege for user", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() == 2) {
					String id = args.getParams().get(0);
					String privilege = args.getParams().get(1);
					user = users.findUser(id);
					if (user != null) {
						user.getPrivileges().revokePrivilege(privilege);
					}
					message = "Privilege \"" + privilege + "\" revoked for user " + id;
					users.save();
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				sendMessage(args, message);
			}

		});
		cmd.setHelp("This command revokes the privileges of a user,\n\tand saves for the next session.\n\tUse "
				+ commands.getDelimiter() + "$CMD$ <user id> <privilege> to revoke a privilege.");
		cmd.getPrivileges().setPrivilege("bot_master", 1);

		// "https://tools.ietf.org/pdf/rfc3514.pdf"
		cmd = commands.registerCommand("rfc", "Shows a link to this RFC", new ICommandListener() {

			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() == 1) {
					try {
						int rfc_num = Integer.parseInt(args.getParams().get(0));
						message = "https://tools.ietf.org/pdf/rfc" + rfc_num + ".pdf";
						args.getMessage().delete();
					} catch (NumberFormatException e) {
						message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
					} catch (RateLimitException | MissingPermissionsException | DiscordException e) {
						e.printStackTrace();
					}
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command displays a RFC.\n\tUsage: " + commands.getDelimiter() + "$CMD$ <RFC>");

		cmd = commands.registerCommand("roles", "Lists all roles", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder sb = new StringBuilder();
				if (args.getParams().size() == 1) {
					IRole role = args.getChannel().getGuild().getRoleByID(args.getParams().get(0));
					if (role != null) {
						Color color = role.getColor();
						sb.append("Role \"");
						sb.append(role.getName());
						sb.append("\" : ");
						sb.append(role.getID());
						sb.append("\n```Creation date: ");
						sb.append(role.getCreationDate());
						sb.append("\nRole position: ");
						sb.append(role.getPosition());
						sb.append("\nColor: ");
						sb.append(Integer.toHexString(color.getRGB()));
						sb.append("\nPermissions:");
						for (Permissions permissions : role.getPermissions()) {
							sb.append("\n\t");
							sb.append(permissions.toString());
						}
						sb.append("\n```");
					} else
						sb.append("Invalid role ID");
				} else {
					sb.append("Roles:\n```");
					for (IRole role : args.getChannel().getGuild().getRoles()) {
						sb.append("\n");
						sb.append(role.getName());
						sb.append(" : ");
						sb.append(role.getID());
					}
					sb.append("\n```");
				}
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("This command lists all available roles.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <role id (optional)>");

		cmd = commands.registerCommand("say", "Says, what the message says", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				if (args.getParams().size() > 0) {
					message = args.getRawParams();
					try {
						args.getMessage().delete();
					} catch (MissingPermissionsException | DiscordException | RateLimitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command repeats the words of the command issuer.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <message>");

		cmd = commands.registerCommand("setavatar", "Changes the avatar of the bot.", new ICommandListener() {

			/**
			 * @param url
			 *            URL
			 * @return Image type
			 */
			private String getImageTypeFromURL(String url) {
				String ret = "";
				String[] results = url.split("\\.+");
				if (results != null) {
					if (results.length > 0)
						ret = results[results.length - 1];
				}
				return ret;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				User user = users.findUser(args.getIssuer().getID());
				boolean success = false;
				String url = null;
				String type = null;
				if (args.getParams().size() == 1) {
					success = true;
					url = args.getParams().get(0);
					type = getImageTypeFromURL(url);
				} else if (args.getParams().size() == 2) {
					success = true;
					url = args.getParams().get(0);
					type = args.getParams().get(1);
				}
				if (success) {
					Image image = Image.forUrl(type, url);

					try {
						client.changeAvatar(image);
						message = "Avatar has been changed.";
					} catch (DiscordException | RateLimitException e) {
						message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
					}
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("modify_bot", 1);
		cmd.setHelp("This command changes the avatar of the bot.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <url> <(optional) image type>\n\n\tImage types are for example \"png\", \"jpeg\", \"gif\" and etc.");

		cmd = commands.registerCommand("setcommand", "Changes the command", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() > 1) {
					String old_cmd_name = args.getParams().get(0);
					String new_cmd_name = args.getParams().get(1);
					if (commands.renameCommand(old_cmd_name, new_cmd_name))
						message = "Command \"" + old_cmd_name + "\" has been set to \"" + new_cmd_name + "\".";
					else
						message = "Command \"" + old_cmd_name + "\" doesn't exist or \"" + new_cmd_name
								+ "\" already exists.";
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				if (message != null)
					sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("modify_bot", 1);
		cmd.setHelp("This command renames a command.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <old command> <new command>");

		cmd = commands.registerCommand("setdelimiter", "Sets the executor", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;

				if (args.getParams().size() == 1) {
					char executor = args.getParams().get(0).charAt(0);
					commands.setDelimiter(executor);
					message = "Executor is now set to \"" + commands.getDelimiter() + "\"\nExample: "
							+ commands.getDelimiter() + "help";
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("modify_bot", 1);
		cmd.setHelp(
				"A delimiter is a notation for the bot to parse a message beginning with a specified character as a command.\n\tUsage : "
						+ commands.getDelimiter() + "$CMD$ <delimiter>");

		cmd = commands.registerCommand("setprivilege", "Set privilege for user", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() == 3) {
					String id = args.getParams().get(0);
					String privilege = args.getParams().get(1);
					try {
						int value = Integer.parseInt(args.getParams().get(2));
						user = users.findUser(id);
						if (user == null) {
							user = users.addUser(id);
						}
						user.getPrivileges().setPrivilege(privilege, new Integer(value));
						message = "Privilege \"" + privilege + " : " + value + "\" set for user " + id;
					} catch (NumberFormatException e) {
						message = "Value is not a number.";
					}
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				sendMessage(args, message);
				users.save();
			}
		});
		cmd.setHelp("This command sets the privileges of a user,\n\tand saves for the next session.\n\tUse "
				+ commands.getDelimiter() + "$CMD$ <user id> <privilege> <value> to set a privilege.");
		cmd.getPrivileges().setPrivilege("bot_master", 1);

		cmd = commands.registerCommand("settopic", "Sets a topic.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				if (args.getParams().size() > 0) {
					String topic = args.getRawParams();
					try {
						args.getChannel().changeTopic(topic);
						message = "Topic has been changed to \"" + topic + "\".";
					} catch (DiscordException | MissingPermissionsException | RateLimitException e) {
						message = "Topic can't be changed.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command changes the topic of this channel.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <topic>");

		cmd = commands.registerCommand("setusername", "Changes the bot's username.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				if (args.getParams().size() == 1) {
					String username = args.getParams().get(0);
					try {
						client.changeUsername(username);
						message = "Username has been changed to \"" + username + "\".";
					} catch (DiscordException | RateLimitException e) {
						message = "Username couldn't be changed.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands,
							args.getChannel().getGuild());
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("modify_bot", 1);
		cmd.setHelp("This command sets the username of this bot.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <username>");

		cmd = commands.registerCommand("shuffle", "Shuffles the audio queue", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).shuffle();
				sendMessage(args, "Everyday I am shuffleing!");
			}
		});
		cmd.setHelp("Shuffles the audio queue.");

		cmd = commands.registerCommand("skip", "Skips the audio.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).skip();
			}
		});
		cmd.setHelp("This command skips the audio stream.");

		cmd = commands.registerCommand("stop", "Stops the audio.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).clean();
				AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setVolume(volume);
			}
		});
		cmd.setHelp("This command stops the audio stream.");

		cmd = commands.registerCommand("suicidegirl", "Show a SuicideGirl picture", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				Object o = servers.getServerAttribute(args.getChannel().getGuild(), "nsfw");
				String cid = args.getChannel().getID();
				if (o == null)
					message = "NSFW is disabled on this channel. Use " + commands.getDelimiter() + "enablensfw or "
							+ commands.getDelimiter() + "enablensfw " + cid + " to enable this feature.";
				else {
					JSONObject nsfw = (JSONObject) o;
					if (nsfw.has(cid)) {
						String filter = "";
						if (args.getParams().size() > 0)
							filter = args.getRawParams().trim();
						ArrayList<File> files = findFilesRecursively(new File("./sg/"),
								new String[] { "jpg", "jpeg", "png", "gif", "tiff", "tga", "svg" }, filter);

						// Store in config (W.I.P.)

						if (files.size() > 0) {
							int id = (new Random()).nextInt(files.size());
							String[] parts = files.get(id).getAbsolutePath().split("[\\/,\\\\]");
							if (parts.length > 1)
								message = files.get(id).getName() + " from " + parts[parts.length - 2];
							try {
								args.getChannel().sendFile(files.get(id));
							} catch (RateLimitException | IOException | MissingPermissionsException
									| DiscordException e) {
								e.printStackTrace();
							}
						} else
							message = "No files found by search criteria.";
					} else
						message = "NSFW is disabled on this channel. Use " + commands.getDelimiter() + "enablensfw or "
								+ commands.getDelimiter() + "enablensfw " + cid + " to enable this feature.";
				}
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command shows a random SuicideGirl picture by search criteria.\n\tUsage: "
				+ commands.getDelimiter() + "$CMD$ <Search criteria (optional)>");

		cmd = commands.registerCommand("toggletts", "Toggle text to speech :D", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				use_tts = !use_tts;
				sendMessage(args, "Text to speech is now " + (use_tts ? "enabled." : "disabled."));
			}
		});
		cmd.setHelp("This command toggles the text to speech feature this bot (ab)uses.");

		cmd = commands.registerCommand("upload", "Uploads a file.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() > 0) {
					String file_name = args.getRawParams();
					File file = new File(file_name);
					try {
						args.getChannel().sendFile(file);
					} catch (IOException | MissingPermissionsException | DiscordException | RateLimitException e) {
						message = "Failed to upload \"" + file_name + "\"";
					}
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				if (message != null)
					sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.setHelp("This command allows to upload a local file to this channel.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <file name>");

		cmd = commands.registerCommand("users", "Lists all users.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				for (IUser i : args.getChannel().getUsersHere()) {
					if (first)
						first = false;
					else
						sb.append("\n");
					sb.append(i.getName());
					sb.append(" : ");
					sb.append(i.getID());
				}
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("This command lists all available users.");

		cmd = commands.registerCommand("validatejson", "Validates JSON", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder params = new StringBuilder();
				for (String i : args.getParams())
					params.append(i);
				try {
					new JSONObject(params.toString());
					sendMessage(args, "This is a valid JSON object.");
				} catch (Exception e) {
					try {
						new JSONArray(params.toString());
						sendMessage(args, "This is a valid JSON array.");
					} catch (Exception _e) {
						sendMessage(args, "This is not valid JSON.");
					}
				}
			}

		});
		cmd.setHelp("This command validates any JSON.\n\tObject or array doesn't matter.");

		cmd = commands.registerCommand("voicechannels", "Lists all available voice channels.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.discordeti.event.ICommandListener#onCommand(com.
			 * discordeti.event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				StringBuilder sb = new StringBuilder();
				if (args.getParams().size() == 1) {
					IVoiceChannel voice_channel = args.getChannel().getGuild()
							.getVoiceChannelByID(args.getParams().get(0));
					if (voice_channel == null)
						sb.append("Invalid voice channel ID!");
					else {
						sb.append("Voice channel \"");
						sb.append(voice_channel.getName());
						sb.append("\" : ");
						sb.append(voice_channel.getID());
						sb.append("\n```\nTopic: ");
						sb.append(voice_channel.getTopic());
						sb.append("\nCreation date: ");
						sb.append(voice_channel.getCreationDate());
						sb.append("\nUser limit: ");
						sb.append(voice_channel.getUserLimit());
						sb.append("\nBitrate: ");
						sb.append(voice_channel.getBitrate());
						sb.append("\n```");
					}
				} else {
					sb.append("Voice channels:\n```");
					for (IVoiceChannel i : args.getChannel().getGuild().getVoiceChannels()) {
						sb.append("\n");
						sb.append(i.getName());
						sb.append(" : ");
						sb.append(i.getID());
					}
					sb.append("\n```");
				}
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("This command lists all available voice channels.\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <voice channel id (optional)>");

		cmd = commands.registerCommand("volume", "Changes the volume of the bot.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() == 1) {
					try {
						float v = Float.parseFloat(args.getParams().get(0));
						if ((v >= 0) && (v <= 100)) {
							volume = v * 0.01f;
							AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setVolume(volume);
						} else
							message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
					} catch (NumberFormatException e) {
						message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
					}
				} else
					message = args.getCommand().generateHelp(user, commands, args.getChannel().getGuild());
				if (message != null)
					sendMessage(args, message);
			}
		});
		cmd.setHelp("This command changes the volume of the bot (from 0 to 100).\n\tUsage: " + commands.getDelimiter()
				+ "$CMD$ <volume>");

		commands.sort();

		client.getDispatcher().registerListener(new IListener<ReadyEvent>() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * sx.blah.discord.api.events.IListener#handle(sx.blah.discord.api.
			 * events.Event)
			 */
			@Override
			public void handle(ReadyEvent event) {
				System.out.println("=== API is ready! ===");

				for (IGuild guild : client.getGuilds()) {
					Object o = servers.getServerAttribute(guild, "auto_join_voice_channel");
					if (o != null) {
						IVoiceChannel voice_channel = guild.getVoiceChannelByID((String) o);
						if (voice_channel != null)
							try {
								voice_channel.join();
							} catch (MissingPermissionsException e) {
								e.printStackTrace();
							}
					}
				}
				commands.reloadRoles(servers, client);
			}
		});
		client.getDispatcher().registerListener(new IListener<MessageReceivedEvent>() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * sx.blah.discord.api.events.IListener#handle(sx.blah.discord.api.
			 * events.Event)
			 */
			@Override
			public void handle(MessageReceivedEvent event) {
				System.out.println(
						event.getMessage().getCreationDate() + " <" + event.getMessage().getAuthor().getName() + "@"
								+ event.getMessage().getChannel().getName() + "> : " + event.getMessage().getContent());
				if (event.getClient().getOurUser().getID().compareTo(event.getMessage().getAuthor().getID()) != 0) {
					commands.parseMessage(dis, event.getMessage());
				}
			}
		});
	}

	private ArrayList<File> findFilesRecursively(File path, String[] file_types, String contains) {
		ArrayList<File> ret = new ArrayList<>();
		contains = contains.toLowerCase();
		if (path.isDirectory()) {
			for (File file : path.listFiles()) {
				if (file.isDirectory())
					ret.addAll(findFilesRecursively(file, file_types, contains));
				else if (file.getPath().toLowerCase().contains(contains)) {
					for (String file_type : file_types) {
						if (file.getName().endsWith("." + file_type)) {
							ret.add(file);
							break;
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Get commands
	 * 
	 * @return Commands
	 */
	public Commands getCommands() {
		return commands;
	}

	/**
	 * Get users
	 * 
	 * @return Users
	 */
	public Users getUsers() {
		return users;
	}

	/**
	 * Get servers
	 * 
	 * @return Servers
	 */
	public Servers getServers() {
		return servers;
	}

	/**
	 * Login bot
	 * 
	 * @param token
	 *            Token
	 * @return Bot instance
	 */
	public static Bot login(String token) {
		Bot bot = null; // Initializing the bot variable

		ClientBuilder builder = new ClientBuilder();
		builder.withToken(token);
		try {
			IDiscordClient client = builder.login();
			bot = new Bot(client);
		} catch (DiscordException e) {
			System.err.println("Error occurred while logging in!");
			e.printStackTrace();
		}

		return bot;
	}

}
