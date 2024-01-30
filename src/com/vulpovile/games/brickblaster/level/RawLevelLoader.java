package com.vulpovile.games.brickblaster.level;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.vulpovile.games.brickblaster.util.Util;

public class RawLevelLoader extends LevelLoader {

	private final File file;
	private int levelCounter = 0;
	private boolean eofReached = false; 

	public RawLevelLoader(File file) {
		this.file = file;
	}

	@Override
	public int loadLevel(byte[] dest) {
		InputStream input = null;
		try
		{
			input = new FileInputStream(file);
			eofReached = super.loadStandardLevelFromStream(input, dest, levelCounter);
		}
		catch (EOFException e)
		{
			eofReached = true;
			e.printStackTrace();
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
		if(eofReached)
		{
			levelCounter = 0;
			eofReached = false;
			return true;
		}
		levelCounter++;
		return false;
	}

	@Override
	public void reset() {
		levelCounter = 0;
	}

}
