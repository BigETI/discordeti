package com.discordeti.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Main entry class
 *
 * @author Ethem Kurt
 */
public class Main implements Runnable
{

	/**
	 * Client token
	 */
	private final String token;

	/**
	 * Constructor
	 *
	 * @param token
	 *            Client token
	 */
	public Main(final String token)
	{
		this.token = token;
	}

	/**
	 * Main entry
	 *
	 * @param args
	 *            Command line arguments
	 */
	public static void main(final String[] args)
	{
		String token = null;
		if (args.length > 0)
		{
			for (final String arg : args)
			{
				System.out.println("Reading file \"" + arg + "\"...");
				try (FileReader fr = new FileReader(arg))
				{
					try (BufferedReader br = new BufferedReader(fr))
					{
						token = br.readLine().trim();
					}
					finally
					{
						//
					}
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
				finally
				{
					//
				}
				if (token != null)
				{
					final Main bt = new Main(token);
					final Thread t = new Thread(bt);
					t.start();
				}
			}
		}
		else
		{
			System.err.println("No key file specified.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		Bot.login(token);
	}
}
