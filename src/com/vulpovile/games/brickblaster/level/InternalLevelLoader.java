package com.vulpovile.games.brickblaster.level;

import java.io.IOException;
import java.io.InputStream;

import com.vulpovile.games.brickblaster.util.Util;

public class InternalLevelLoader extends LevelLoader {

	private static String[] BUILTIN_LEVELS = new String[] { "intro.bbl", "sex", "stagger.bbl", "half.bbl", "challenger.bbl", "heart.bbl", "full.bbl", "hole.bbl", "trickshot.bbl", "smiley.bbl", "chekkit.bbl" };
	private int currLevel = 0;

	public int loadLevel(byte[] dest) {
		String levelResource = "/builtin_levels/" + BUILTIN_LEVELS[currLevel];
		InputStream stream = this.getClass().getResourceAsStream(levelResource);
		if (stream != null)
		{
			try
			{
				super.loadStandardLevelFromStream(stream, dest);
				return super.calculateHitables(dest);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				Util.cleanClose(stream);
			}
		}
		return -1;
	}

	public boolean incrementLevelCounter() {
		this.currLevel = (currLevel + 1) % BUILTIN_LEVELS.length;
		return currLevel == 0;
	}

	public void reset() {
		this.currLevel = 0;
	}

}
