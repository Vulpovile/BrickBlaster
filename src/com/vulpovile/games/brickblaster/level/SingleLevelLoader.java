package com.vulpovile.games.brickblaster.level;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.vulpovile.games.brickblaster.util.Util;

public class SingleLevelLoader extends LevelLoader {

	private final File file;

	public SingleLevelLoader(File file) {
		this.file = file;
	}

	@Override
	public int loadLevel(byte[] dest) {
		InputStream input = null;
		try
		{
			input = new FileInputStream(file);
			super.loadStandardLevelFromStream(input, dest);
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
		//Does nothing, single file
		return true;
	}

	@Override
	public void reset() {
		//Does nothing, single file
	}

}
