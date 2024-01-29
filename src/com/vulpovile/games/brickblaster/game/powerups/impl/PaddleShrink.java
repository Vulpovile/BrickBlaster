package com.vulpovile.games.brickblaster.game.powerups.impl;

import java.awt.Color;

import com.vulpovile.games.brickblaster.GamePanel;
import com.vulpovile.games.brickblaster.game.powerups.PowerUp;

public class PaddleShrink extends PowerUp{

	private static final short PADDLE_INCREASE_AMOUNT = GamePanel.PADDLE_DEFAULT_HALF_WIDTH >> 2;
	
	public PaddleShrink() {
		super(Color.YELLOW, true);
	}

	@Override
	public void onExpire(GamePanel g) {

	}

	@Override
	public void onObtain(GamePanel g) {
		g.setPaddleHalfWidth((short)Math.max(GamePanel.PADDLE_DEFAULT_HALF_WIDTH >> 1, g.getPaddleHalfWidth() - PADDLE_INCREASE_AMOUNT));
	}

	@Override
	public void tick(GamePanel g) {

	}

}
