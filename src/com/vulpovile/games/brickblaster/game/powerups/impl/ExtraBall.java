package com.vulpovile.games.brickblaster.game.powerups.impl;

import java.awt.Color;

import com.vulpovile.games.brickblaster.GamePanel;
import com.vulpovile.games.brickblaster.game.powerups.PowerUp;

public class ExtraBall extends PowerUp{

	
	public ExtraBall() {
		super(Color.GREEN, false);
	}

	@Override
	public void onExpire(GamePanel g) {

	}

	@Override
	public void onObtain(GamePanel g) {
		g.setBallsLeft(g.getBallsLeft()+1);
	}

	@Override
	public void tick(GamePanel g) {

	}

}
