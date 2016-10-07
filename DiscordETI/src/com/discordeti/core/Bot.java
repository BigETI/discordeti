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
import java.util.Map.Entry;
import java.util.Random;

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
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
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
	 * Commands
	 */
	private Commands commands = new Commands();

	/**
	 * Users
	 */
	private Users users = new Users();

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
		Command cmd = commands.registerCommand("help", "Show help topics", new ICommandListener() {

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
						sb.append(commands.getCommands().get(key).generateHelp(user, commands));
					else {
						sb.append("Command \"");
						sb.append(commands.getExecutor());
						sb.append(key);
						sb.append("\" was not found.\n\n");
					}
				} else {
					for (String i : commands.getCommands().keySet()) {
						sb.append("```");
						sb.append(commands.getExecutor());
						sb.append(i);
						sb.append("\n\t");
						sb.append(commands.getCommands().get(i).getDescription());
						sb.append("```");
					}
				}
				sb.append("\n**=== __End of help topic__ ===**");
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("This command is used to show help topics about other available commands.\n\tUse "
				+ commands.getExecutor() + "$CMD$ <command> to show help topics.");
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
					message = args.getCommand().generateHelp(user, commands);
				sendMessage(args, message);
				users.save();
			}
		});
		cmd.setHelp("This command sets the privileges of a user,\n\tand saves for the next session.\n\tUse "
				+ commands.getExecutor() + "$CMD$ <user id> <privilege> <value> to set a privilege.");
		cmd.getPrivileges().setPrivilege("grant_user", 1);

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
					message = args.getCommand().generateHelp(user, commands);
				sendMessage(args, message);
			}

		});
		cmd.setHelp("This command revokes the privileges of a user,\n\tand saves for the next session.\n\tUse "
				+ commands.getExecutor() + "$CMD$ <user id> <privilege> to revoke a privilege.");
		cmd.getPrivileges().setPrivilege("grant_user", 1);

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
		cmd.getPrivileges().setPrivilege("moderate_channels", 1);
		
		cmd = commands.registerCommand("setexecutor", "Sets the executor", new ICommandListener() {

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
					commands.setExecutor(executor);
					message = "Executor is now set to \"" + commands.getExecutor() + "\"\nExample: "
							+ commands.getExecutor() + "help";
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("moderate_channels", 2);
		cmd.setHelp(
				"An executor is a notation for the bot to parse a message beginning with an executor as a command.\n\tUsage : "
						+ commands.getExecutor() + "$CMD$ <executor>");

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
						message = args.getCommand().generateHelp(user, commands);
					}
				} else
					message = args.getCommand().generateHelp(user, commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("modify_bot", 1);
		cmd.setHelp("This command changes the avatar of the bot.\n\tUsage: " + commands.getExecutor() + "$CMD$ <url> <(optional) image type>\n\n\tImage types are for example \"png\", \"jpeg\", \"gif\" and etc.");

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
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("moderate_channels", 1);
		cmd.setHelp("This command changes the topic of this channel.\n\tUsage: " + commands.getExecutor()
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
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("modify_bot", 1);
		cmd.setHelp("This command sets the username of this bot.\n\tUsage: " + commands.getExecutor() + "$CMD$ <username>");

		cmd = commands.registerCommand("listvoicechannels", "Lists all connected voice channels.",
				new ICommandListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see com.discordeti.event.ICommandListener#onCommand(com.
					 * discordeti.event.CommandEventArgs)
					 */
					@Override
					public void onCommand(CommandEventArgs args) {
						StringBuilder sb = new StringBuilder();
						for (IVoiceChannel i : args.getChannel().getGuild().getVoiceChannels()) {
							sb.append("\n");
							sb.append(i.getName());
							sb.append(" : ");
							sb.append(i.getID());
						}
						sendMessage(args, sb.toString());
					}
				});
		cmd.setHelp("This command lists all available voice channels.");

		cmd = commands.registerCommand("joinvoicechannel", "Joins a voice channel.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				if (args.getParams().size() == 1) {
					String id = args.getParams().get(0);
					IVoiceChannel voice_channel = args.getChannel().getGuild().getVoiceChannelByID(id);
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
				} else
					sendMessage(args,
							args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands));
			}
		});
		cmd.getPrivileges().setPrivilege("moderate_channels", 1);
		cmd.setHelp("This command lets the bot join a voice channel by its id.\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <voice channel id>");

		cmd = commands.registerCommand("leavevoicechannel", "Leaves a voice channel.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				if (args.getParams().size() == 1) {
					String id = args.getParams().get(0);
					IVoiceChannel voice_channel = args.getChannel().getGuild().getVoiceChannelByID(id);
					if (voice_channel != null) {
						voice_channel.leave();
					} else
						sendMessage(args, "Invalid voice channel ID.");
				} else
					sendMessage(args,
							args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands));
			}
		});
		cmd.getPrivileges().setPrivilege("moderate_channels", 1);
		cmd.setHelp("This command lets the bot leave a voice channel by its id.\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <voice channel id>");

		cmd = commands.registerCommand("leaveallvoicechannels", "Leaves all voice channels.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@Override
			public void onCommand(CommandEventArgs args) {
				for (IVoiceChannel i : args.getChannel().getGuild().getVoiceChannels())
					i.leave();
				sendMessage(args, "Left all voice channels.");
			}
		});
		cmd.getPrivileges().setPrivilege("moderate_channels", 2);
		cmd.setHelp("This command lets the bot leave a voice channel by its id.\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <voice channel id>");

		cmd = commands.registerCommand("playaudiostream", "Plays an audio stream.", new ICommandListener() {

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
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).clean();
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setVolume(volume);
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).queue(url);

						// metaDataQueue.add(new AudioMetaData(null, url,
						// AudioSystem.getAudioFileFormat(url),
						// stream.getFormat().getChannels()));
						message = "Playing: " + args.getRawParams();
					} catch (IOException e) {
						message = "Can't play audio stream.";
					} catch (UnsupportedAudioFileException e) {
						message = "Can't play audio stream.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command lets the bot play an audio stream.\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <audio stream url>");

		cmd = commands.registerCommand("playaudio", "Plays an audio stream.", new ICommandListener() {

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
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).clean();
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).setVolume(volume);
						AudioPlayer.getAudioPlayerForGuild(args.getChannel().getGuild()).queue(file);
						message = "Playing: " + file;
					} catch (IOException | UnsupportedAudioFileException e) {
						message = "Can't play audio.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.setHelp("This command lets the bot play a local audio file.\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <audio file>");

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
							message = args.getCommand().generateHelp(user, commands);
					} catch (NumberFormatException e) {
						message = args.getCommand().generateHelp(user, commands);
					}
				} else
					message = args.getCommand().generateHelp(user, commands);
				if (message != null)
					sendMessage(args, message);
			}
		});
		cmd.setHelp("This command changes the volume of the bot (from 0 to 100).\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <volume>");

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
				} catch (DiscordException | RateLimitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.setHelp("This command tells the bot to disconnect from Discord and shut down.");

		cmd = commands.registerCommand("listusers", "Lists all users.", new ICommandListener() {

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
		cmd.getPrivileges().setPrivilege("moderate_channels", 1);
		cmd.setHelp("This command lists all available users.");

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
						message = args.getCommand().generateHelp(user, commands);
					else
						try {
							args.getChannel().getGuild().kickUser(target);
						} catch (MissingPermissionsException | DiscordException | RateLimitException e) {
							e.printStackTrace();
						}
				} else
					message = args.getCommand().generateHelp(user, commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("moderate_channels", 1);
		cmd.setHelp("This command kicks a specified user by ID.\n\tUsage: " + commands.getExecutor() + "$CMD$ <user id>");

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
						message = args.getCommand().generateHelp(user, commands);
					else
						try {
							args.getChannel().getGuild().banUser(target);
						} catch (MissingPermissionsException | DiscordException | RateLimitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				} else
					message = args.getCommand().generateHelp(user, commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("moderate_channels", 2);
		cmd.setHelp("This command bans a specified user by ID.\n\tUsage: " + commands.getExecutor() + "$CMD$ <user id>");

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
					message = args.getCommand().generateHelp(user, commands);
				if (message != null)
					sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.setHelp("This command allows to upload a local file to this channel.\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <file name>");

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
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command repeats the words of the command issuer.\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <message>");

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
					message = args.getCommand().generateHelp(user, commands);
				if (message != null)
					sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("modify_bot", 1);
		cmd.setHelp("This command renames a command.\n\tUsage: " + commands.getExecutor() + "$CMD$ <old command> <new command>");

		cmd = commands.registerCommand("brainfuck", "Executes brainfuck from file", new ICommandListener() {

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
					sb.append(args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands));
				sendMessage(args, sb.toString());
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.getPrivileges().setPrivilege("execute_code", 1);
		cmd.setHelp("This command executes Brainfuck from a file.\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <file name>");

		cmd = commands.registerCommand("rawbrainfuck", "Executes Brainfuck from input", new ICommandListener() {

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
					sb.append(args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands));
				sendMessage(args, sb.toString());
			}
		});
		cmd.getPrivileges().setPrivilege("execute_code", 1);
		cmd.setHelp("This command executes Brainfuck from input.\n\tUsage: " + commands.getExecutor() + "$CMD$ <Brainfuck>");

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
					message.append(args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands));
				sendMessage(args, message.toString());
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.getPrivileges().setPrivilege("execute_code", 1);
		cmd.setHelp("This command executes JavaScript from a file.\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <file name>");

		cmd = commands.registerCommand("rawjavascript", "Executes JavaScript from input", new ICommandListener() {

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
					message.append(args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands));
				sendMessage(args, message.toString());
			}
		});
		cmd.getPrivileges().setPrivilege("execute_code", 1);
		cmd.setHelp("This command executes JavaScript from input.\n\tUsage: " + commands.getExecutor()
				+ "$CMD$ <JavaScript>");

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
						message = args.getCommand().generateHelp(user, commands);
					}
				} else
					message = args.getCommand().generateHelp(user, commands);
				if (message != null)
					sendMessage(args, message);
			}
		});
		cmd.setHelp("This command plots a polynom.\n\tUsage: " + commands.getExecutor() + "$CMD$ <x0> <Optional x1> <Optional x2>...");

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
						ImageGraph<Double, Double> graph = new ImageGraph<Double, Double>(1920, 1080, 100.0, 100.0, 0.0, 0.0);
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
					message = args.getCommand().generateHelp(user, commands);
				if (message != null)
					sendMessage(args, message.toString());
			}
		});
		cmd.setHelp("This command allows you to plot a 2D graph with \"x\" as variable. Usage: " + commands.getExecutor() + " $CMD$ <JavaScript expression>");

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
						message = args.getCommand().generateHelp(user, commands);
					} catch (RateLimitException | MissingPermissionsException | DiscordException e) {
						e.printStackTrace();
					}
				} else
					message = args.getCommand().generateHelp(user, commands);
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command displays a RFC.\n\tUsage: " + commands.getExecutor() + "$CMD$ <RFC>");

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
						sendMessage(args, files.get(id).getName() + " from " + parts[parts.length - 2]);
					try {
						args.getChannel().sendFile(files.get(id));
					} catch (RateLimitException | IOException | MissingPermissionsException | DiscordException e) {
						e.printStackTrace();
					}
				} else
					sendMessage(args, "No files found by search criteria.");
			}
		});
		cmd.getPrivileges().setPrivilege("nude_pictures", 1);
		cmd.setHelp("This command shows a random SuicideGirl picture by search criteria.\n\tUsage: "
				+ commands.getExecutor() + "$CMD$ <Search criteria (optional)>");

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
