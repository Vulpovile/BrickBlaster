package com.vulpovile.games.brickblaster.game;

import com.vulpovile.games.brickblaster.GamePanel;

public class Ball {
	public static final short BALL_MAGIC = (short)0xBA11;
	
	public boolean superBall = true;
	
	public int ballX = 0;
	public int ballY = BALL_MAGIC;

	public int ballXVelocity = 0;
	public int ballYVelocity = 4;
	
	public int ballWidthHalf = (GamePanel.W >> 6) - (GamePanel.W >> 7);
	public int ballHeightHalf = GamePanel.H >> 6;
	
}
