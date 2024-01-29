package com.vulpovile.games.brickblaster.game.powerups;

import java.awt.Color;

import com.vulpovile.games.brickblaster.GamePanel;

public abstract class PowerUp {
	public static final short POWERUP_MAGIC = (short) 0xCAFE;
	public final int pupHalfWidth = GamePanel.W >> 5;
	public final int pupHalfHeight = (GamePanel.H >> 5) - (GamePanel.H >> 6);

	public final Color color;
	public final boolean nerf;
	
	public short expiresIn = POWERUP_MAGIC;
	public int x = POWERUP_MAGIC;
	public int y = 0;
	
	public PowerUp(Color color, boolean nerf)
	{
		this.color = color;
		this.nerf = nerf;
	}

	public abstract void onExpire(GamePanel g);

	public abstract void onObtain(GamePanel g);

	public abstract void tick(GamePanel g);
}
