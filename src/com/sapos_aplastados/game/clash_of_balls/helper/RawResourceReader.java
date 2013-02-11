package com.sapos_aplastados.game.clash_of_balls.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class RawResourceReader
{
	
	public static BufferedReader readFromRawResource(final Context context,
			final int resourceId) {
		final InputStream inputStream = context.getResources().openRawResource(
				resourceId);
		final InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream);
		return new BufferedReader(inputStreamReader);
	}
	
	public static String readTextFileFromRawResource(final Context context,
			final int resourceId)
	{
		final BufferedReader bufferedReader = readFromRawResource(context
				, resourceId);

		String nextLine;
		final StringBuilder body = new StringBuilder();

		try
		{
			while ((nextLine = bufferedReader.readLine()) != null)
			{
				body.append(nextLine);
				body.append('\n');
			}
		}
		catch (IOException e)
		{
			return null;
		}

		return body.toString();
	}
}
