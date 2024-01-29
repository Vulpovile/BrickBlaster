package com.vulpovile.games.brickblaster.game.powerups.impl;

import java.awt.Color;
import java.util.Random;

import com.vulpovile.games.brickblaster.GamePanel;
import com.vulpovile.games.brickblaster.game.Ball;
import com.vulpovile.games.brickblaster.game.powerups.PowerUp;

public class SuperBall extends PowerUp{

	private Ball selectedBall;
	
	public SuperBall() {
		super(Color.MAGENTA, false);
		this.expiresIn = 30*15;
	}

	@Override
	public void onExpire(GamePanel g) {
		selectedBall.superBall = false;
	}

	@Override
	public void onObtain(GamePanel g) {
		Random random = new Random();
		selectedBall = g.getBalls().get(random.nextInt(g.getBalls().size()));
		selectedBall.superBall = true;
		g.obtainPowerUp(this);
	}

	@Override
	public void tick(GamePanel g) {
		
	}
	
	@Override
	public boolean equals(Object o)
	{
		return o != null && o instanceof InvertScreen;
	}

}
