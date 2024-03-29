package com.vulpovile.games.brickblaster.game.powerups;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.vulpovile.games.brickblaster.game.powerups.impl.ExtraBall;
import com.vulpovile.games.brickblaster.game.powerups.impl.InvertScreen;
import com.vulpovile.games.brickblaster.game.powerups.impl.MultiBall;
import com.vulpovile.games.brickblaster.game.powerups.impl.PaddleExtend;
import com.vulpovile.games.brickblaster.game.powerups.impl.PaddleShrink;
import com.vulpovile.games.brickblaster.game.powerups.impl.SuperBall;

public class PowerUpProvider {
	private static final Random random = new Random();
	private static int totalProb = 0;

	private static final Map<Byte, Integer> pupTable = new HashMap<Byte, Integer>();

	private static void addProbability(int itemID, int probability) {
		pupTable.put((byte) itemID, probability);
		totalProb += probability;
	}

	static
	{
		addProbability(0, 60);
		addProbability(1, 100);
		addProbability(2, 100);
		addProbability(3, 110);
		addProbability(4, 90);
		addProbability(5, 70);
	}

	private static PowerUp fromID(byte itemID) {
		switch (itemID) {
			case 0:
				return new ExtraBall();
			case 1:
				return new InvertScreen();
			case 2:
				return new MultiBall();
			case 3:
				return new PaddleExtend();
			case 4:
				return new PaddleShrink();
			case 5:
				return new SuperBall();
		}
		return null;
	}
	
	public static PowerUp providePowerUp(int x, int y, byte tile) {
		if (random.nextInt(5) == 0)
		{
			int prob = random.nextInt(totalProb);
			int offset = 0;
			for (Map.Entry<Byte, Integer> item : pupTable.entrySet())
			{
				if (prob < offset + item.getValue())
				{
					PowerUp pup = fromID(item.getKey());
					if (pup != null)
					{
						pup.x = x;
						pup.y = y;
					}
					return pup;
				}
				offset += item.getValue();
			}
		}
		return null;
	}
}
