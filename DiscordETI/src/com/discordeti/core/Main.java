package com.discordeti.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Main entry class
 * 
 * @author Ethem Kurt
 *
 */
public class Main implements Runnable {

	/**
	 * Client token
	 */
	private String token;

	/**
	 * Constructor
	 * 
	 * @param token
	 *            Client token
	 */
	public Main(String token) {
		this.token = token;
	}

	/**
	 * Main entry
	 * 
	 * @param args
	 *            Command line arguments
	 */
	public static void main(String[] args) {
		String token = null;
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				System.out.println("Reading file \"" + args[i] + "\"...");
				try (FileReader fr = new FileReader(args[i])) {
					try (BufferedReader br = new BufferedReader(fr)) {
						token = br.readLine().trim();
					} finally {
						//
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					//
				}
				if (token != null) {
					Main bt = new Main(token);
					Thread t = new Thread(bt);
					t.start();
				}
			}
		} else
			System.err.println("No key file specified.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Bot.login(token);
	}
}
