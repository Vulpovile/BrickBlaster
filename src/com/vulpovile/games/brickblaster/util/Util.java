package com.vulpovile.games.brickblaster.util;

import java.io.Closeable;
import java.io.IOException;

public class Util {
	public static void cleanClose(Closeable closeable) {
		if (closeable != null)
		{
			try
			{
				closeable.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
