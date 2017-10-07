package com.discordeti.event;

/**
 * Command listener interface
 * 
 * @author Ethem Kurt
 *
 */
public interface ICommandListener extends IListener {
	public void onCommand(CommandEventArgs args);
}
