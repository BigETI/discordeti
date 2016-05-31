package com.discordeti.core;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import com.discordeti.event.CommandEventArgs;
import com.discordeti.event.ICommandListener;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.EventSubscriber;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MissingPermissionsException;

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

	// private IDiscordClient client;

	private void sendMessage(CommandEventArgs args, String text) {
		try {
			args.getChannel().sendMessage(text, use_tts);
		} catch (MissingPermissionsException | HTTP429Exception | DiscordException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Constructor
	 * 
	 * @param client
	 *            Client instance
	 */
	public Bot(IDiscordClient client) {
		// this.client = client;
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
				StringBuilder sb = new StringBuilder("\n=== Start of help topic ===\n\n");
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
						sb.append(commands.getExecutor());
						sb.append(i);
						sb.append("\n\t");
						sb.append(commands.getCommands().get(i).getDescription());
						sb.append("\n\n");
					}
				}
				sb.append("=== End of help topic ===");
				sendMessage(args, sb.toString());
			}
		});
		cmd.setHelp("This command is used to show help topics about other available commands.\n\tUse "
				+ commands.getExecutor() + cmd.getCommand() + " <command> to show help topics.");
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
				sendMessage(args, "Hi " + args.getIssuer().getName() + "!");
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
				+ commands.getExecutor() + cmd.getCommand() + " <user id> <privilege> <value> to set a privilege.");
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
				+ commands.getExecutor() + cmd.getCommand() + " <user id> <privilege> to revoke a privilege.");
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
					if (args.getChannel().getMessages().size() > 100)
						args.getChannel().getMessages().deleteFromRange(0, 99);
					else
						args.getChannel().getMessages().bulkDelete(args.getChannel().getMessages());
				} catch (HTTP429Exception | DiscordException | MissingPermissionsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
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
						+ commands.getExecutor() + cmd.getCommand() + " <executor>");

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
		cmd = commands.registerCommand("setavatar", "Changes the avatar of the bot.", new ICommandListener() {

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
					} catch (DiscordException | HTTP429Exception e) {
						message = args.getCommand().generateHelp(user, commands);
					}
				} else
					message = args.getCommand().generateHelp(user, commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("modify_bot", 1);
		cmd.setHelp("This command changes the avatar of the bot.\n\tUsage: " + commands.getExecutor() + cmd.getCommand()
				+ " <url> <(optional) image type>\n\n\tImage types arefor example \"png\", \"jpeg\", \"gif\" and etc.");

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
					} catch (HTTP429Exception | DiscordException | MissingPermissionsException e) {
						message = "Topic can't be changed.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("moderate_channels", 1);
		cmd.setHelp("This command changes the topic of this channel.\n\tUsage: " + commands.getExecutor()
				+ cmd.getCommand() + " <topic>");

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
					} catch (DiscordException | HTTP429Exception e) {
						message = "Username couldn't be changed.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("modify_bot", 1);

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

		cmd = commands.registerCommand("joinvoicechannel", "Joins a voice channel.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onCommand(CommandEventArgs args) {
				if (args.getParams().size() == 1) {
					String id = args.getParams().get(0);
					IVoiceChannel voice_channel = args.getChannel().getGuild().getVoiceChannelByID(id);
					if (voice_channel != null) {
						voice_channel.join();
						try {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
								args.getChannel().getGuild().getAudioChannel().queueFile("hello.mp3");
							} catch (Exception e) {
								e.printStackTrace();
							}
							try {
								voice_channel.sendMessage("Hello!");
							} catch (MissingPermissionsException | HTTP429Exception e) {
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
				+ cmd.getCommand() + " <voice channel id>");

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
				+ cmd.getCommand() + " <voice channel id>");

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
				+ cmd.getCommand() + " <voice channel id>");
		// https://www.youtubeinmp3.com/fetch/?video=http://www.youtube.com/watch?v=i62Zjga8JOM
		cmd = commands.registerCommand("youtube", "Plays a youtube video. (broken)", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				if (args.getParams().size() == 1) {
					String v = args.getParams().get(0);
					try {
						args.getChannel().getGuild().getAudioChannel().clearQueue();
						args.getChannel().getGuild().getAudioChannel().queueUrl(
								"https://www.youtubeinmp3.com/fetch/?video=http://www.youtube.com/watch?v=" + v);
						message = "Playing: http://www.youtube.com/watch?v=" + v;
					} catch (DiscordException e) {
						message = "Can't play youtube video.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command lets the bot play a youtube video.\n\tUsage: " + commands.getExecutor()
				+ cmd.getCommand() + " <youtube video watch id>\n\tExample: " + commands.getExecutor()
				+ cmd.getCommand() + " miZHa7ZC6Z0");

		cmd = commands.registerCommand("playaudiostream", "Plays an audio stream.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				if (args.getParams().size() == 1) {
					String stream = args.getParams().get(0);
					try {
						args.getChannel().getGuild().getAudioChannel().clearQueue();
						args.getChannel().getGuild().getAudioChannel().queueUrl(stream);
						message = "Playing: " + stream;
					} catch (DiscordException e) {
						message = "Can't play audio stream.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command lets the bot play an audio stream.\n\tUsage: " + commands.getExecutor()
				+ cmd.getCommand() + " <audio stream url>");

		cmd = commands.registerCommand("playaudio", "Plays an audio stream.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				if (args.getParams().size() > 0) {
					String file = args.getRawParams();
					try {
						args.getChannel().getGuild().getAudioChannel().clearQueue();
						args.getChannel().getGuild().getAudioChannel().queueFile(file);
						message = "Playing: " + file;
					} catch (DiscordException e) {
						message = "Can't play audio.";
					}
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);
		cmd.setHelp("This command lets the bot play a local audio file.\n\tUsage: " + commands.getExecutor()
				+ cmd.getCommand() + " <audio file>");

		cmd = commands.registerCommand("volume", "Changes the volume of the bot.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() == 1) {
					try {
						float volume = Float.parseFloat(args.getParams().get(0));
						if ((volume >= 0) && (volume <= 100)) {
							args.getChannel().getGuild().getAudioChannel().setVolume(volume * 0.01f);
						} else
							message = args.getCommand().generateHelp(user, commands);
					} catch (NumberFormatException | DiscordException e) {
						message = args.getCommand().generateHelp(user, commands);
					}
				} else
					message = args.getCommand().generateHelp(user, commands);
				if (message != null)
					sendMessage(args, message);
			}
		});
		cmd.setHelp("This command changes the volume of the bot (from 0 to 100).\n\tUsage: " + commands.getExecutor()
				+ cmd.getCommand() + " <volume>");

		cmd = commands.registerCommand("pause", "Pauses the audio.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onCommand(CommandEventArgs args) {
				try {
					args.getChannel().getGuild().getAudioChannel().pause();
				} catch (DiscordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		cmd = commands.registerCommand("resume", "Resumes the audio.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onCommand(CommandEventArgs args) {
				try {
					args.getChannel().getGuild().getAudioChannel().resume();
				} catch (DiscordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		cmd = commands.registerCommand("skip", "Skips the audio.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onCommand(CommandEventArgs args) {
				try {
					args.getChannel().getGuild().getAudioChannel().skip();
				} catch (DiscordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		cmd = commands.registerCommand("stop", "Stops the audio.", new ICommandListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.discordeti.event.ICommandListener#onCommand(com.discordeti.
			 * event.CommandEventArgs)
			 */
			@SuppressWarnings("deprecation")
			@Override
			public void onCommand(CommandEventArgs args) {
				try {
					args.getChannel().getGuild().getAudioChannel().clearQueue();
				} catch (DiscordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

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
				} catch (HTTP429Exception | DiscordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		cmd.getPrivileges().setPrivilege("bot_master", 1);

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
						} catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {
							e.printStackTrace();
						}
				} else
					message = args.getCommand().generateHelp(user, commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("moderate_channels", 1);
		cmd.setHelp("This command kicks a specified user by ID.\n\tUsage: " + commands.getExecutor() + cmd.getCommand()
				+ " <user id>");

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
						} catch (MissingPermissionsException | HTTP429Exception | DiscordException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				} else
					message = args.getCommand().generateHelp(user, commands);
				sendMessage(args, message);
			}
		});
		cmd.getPrivileges().setPrivilege("moderate_channels", 2);
		cmd.setHelp("This command bans a specified user by ID.\n\tUsage: " + commands.getExecutor() + cmd.getCommand()
				+ " <user id>");

		cmd = commands.registerCommand("upload", "Uploads a file.", new ICommandListener() {

			@Override
			public void onCommand(CommandEventArgs args) {
				String message = null;
				User user = users.findUser(args.getIssuer().getID());
				if (args.getParams().size() > 0) {
					String file_name = args.getRawParams();
					File file = new File(file_name);
					try {
						args.getChannel().sendFile(file);
					} catch (IOException | MissingPermissionsException | HTTP429Exception | DiscordException e) {
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
				+ cmd.getCommand() + " <file name>");

		cmd = commands.registerCommand("say", "Says, what the message says", new ICommandListener() {

			@Override
			public void onCommand(CommandEventArgs args) {
				String message;
				if (args.getParams().size() > 0) {
					message = args.getRawParams();
				} else
					message = args.getCommand().generateHelp(users.findUser(args.getIssuer().getID()), commands);
				sendMessage(args, message);
			}
		});
		cmd.setHelp("This command repeats the words of the command issuer.\n\tUsage: " + commands.getExecutor()
				+ cmd.getCommand() + " <message>");

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
		cmd.setHelp("This command renames a command.\n\tUsage: " + commands.getExecutor() + cmd.getCommand()
				+ " <old command> <new command>");

		commands.sort();
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

	/**
	 * Ready event
	 * 
	 * @param event
	 *            Event
	 */
	@EventSubscriber
	public void onReadyEvent(ReadyEvent event) {
		System.out.println("API is ready!");
		// IDiscordClient client = event.getClient();

	}

	/**
	 * Message received event
	 * 
	 * @param event
	 *            Event
	 */
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		System.out.println(event.getMessage().getCreationDate() + " <" + event.getMessage().getAuthor().getName() + "@"
				+ event.getMessage().getChannel().getName() + "> : " + event.getMessage().getContent());
		if (event.getClient().getOurUser().getID().compareTo(event.getMessage().getAuthor().getID()) != 0) {
			commands.parseMessage(this, event.getMessage());
		}
	}

}
