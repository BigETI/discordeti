package com.discordeti.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Notifier class
 * 
 * @author Ethem Kurt
 *
 * @param <T>
 *            Listener type
 */
public abstract class Notifier<T extends IListener> {

	/**
	 * Listeners
	 */
	private ArrayList<T> listeners = new ArrayList<T>();

	/**
	 * Add listener
	 * 
	 * @param listener
	 *            Listener
	 */
	public void addListener(T listener) {
		listeners.add(listener);
	}

	/**
	 * Remove all listeners
	 */
	public void removeAllListeners() {
		listeners.clear();
	}

	/**
	 * Get all listeners
	 * 
	 * @return All listeners
	 */
	protected List<T> getListeners() {
		return listeners;
	}
}