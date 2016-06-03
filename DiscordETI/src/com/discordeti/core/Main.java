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
public class Main {

	/**
	 * Main entry
	 * 
	 * @param args
	 *            Command line arguments
	 */
	public static void main(String[] args) {
		String token = null;
		try (FileReader fr = new FileReader("../../discordeti.key")) {
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
		if (token != null)
			Bot.login(token);
	}
}
