package com.discordeti.event;

/**
 * Command notifier class
 * 
 * @author Ethem Kurt
 *
 */
public class CommandNotifier extends Notifier<ICommandListener> {

	/**
	 * Set "onCommand" event
	 * 
	 * @param args
	 *            Arguments
	 */
	public void setOnCommand(CommandEventArgs args) {
		for (ICommandListener i : getListeners()) {
			i.onCommand(args);
		}
	}
}
