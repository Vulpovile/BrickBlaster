package com.vulpovile.games.brickblaster.level;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.vulpovile.games.brickblaster.util.Util;

public class CompilationLevelLoader extends LevelLoader {

	private final File file;
	private int levelCounter = 0;
	private int levelCount = 0;

	public CompilationLevelLoader(File file) {
		this.file = file;
	}

	@Override
	public int loadLevel(byte[] dest) {
		InputStream input = null;
		try
		{
			input = new FileInputStream(file);
			//Skip magic number
			input.skip(4);
			levelCount = input.read() + 1;
			levelCounter = levelCounter % levelCount;
			super.loadStandardLevelFromStream(input, dest, levelCounter);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			Util.cleanClose(input);
		}
		return super.calculateHitables(dest);
	}

	@Override
	public boolean incrementLevelCounter() {
		levelCounter = (levelCounter + 1) % levelCount;
		return levelCounter == 0;
	}

	@Override
	public void reset() {
		levelCounter = 0;
	}

}
