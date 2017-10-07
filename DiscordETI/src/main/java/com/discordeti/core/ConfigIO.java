package com.discordeti.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;

/**
 * Config input and output class
 *
 * @author Ethem Kurt
 */
public class ConfigIO
{

	/**
	 * Save config
	 *
	 * @param file_name
	 *            File name
	 * @param config
	 *            Config JSON object
	 */
	public static void save(final String file_name, final JSONObject config)
	{
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file_name)))
		{
			bw.write(config.toString(4));
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			//
		}
	}

	/**
	 * Load config
	 *
	 * @param file_name
	 *            File name
	 * @return Config JSON object
	 */
	public static JSONObject load(final String file_name)
	{
		JSONObject ret = null;
		String l;
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(file_name)))
		{
			while ((l = br.readLine()) != null)
			{
				sb.append(l);
				sb.append("\n");
			}
		}
		catch (final IOException e)
		{
			sb = new StringBuilder("{}");
			e.printStackTrace();
		}
		finally
		{
			//
		}
		ret = new JSONObject(sb.toString());
		return ret;
	}
}
