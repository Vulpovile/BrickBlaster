package com.vulpovile.games.brickblaster.level;

import java.io.IOException;
import java.io.InputStream;

public abstract class LevelLoader {
	public abstract int loadLevel(byte[] dest);

	public abstract boolean incrementLevelCounter();

	public abstract void reset();

	protected final void loadStandardLevelFromStream(InputStream stream, byte[] destination) throws IOException {
		loadStandardLevelFromStream(stream, destination, 0);
	}

	protected final void loadStandardLevelFromStream(InputStream stream, byte[] destination, int offset) throws IOException {
		if (offset > 0)
			stream.skip(offset * (destination.length >> 1));
		for (int i = 0; i < destination.length; i += 2)
		{
			int read = stream.read();
			if (read != -1)
			{
				destination[i] = (byte) (read & 0x0F);
				destination[i + 1] = (byte) ((read & 0xF0) >> 4);
			}
		}
	}

	protected final int calculateHitables(byte[] field) {
		int toHit = 0;
		for (int i = 0; i < field.length; i++)
		{
			if (field[i] != 0 && field[i] != 7)
			{
				toHit++;
			}
		}
		return toHit;
	}
}
