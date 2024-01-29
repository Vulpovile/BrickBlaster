package com.vulpovile.games.brickblaster.game.powerups.impl;

import java.awt.Color;

import com.vulpovile.games.brickblaster.GamePanel;
import com.vulpovile.games.brickblaster.game.powerups.PowerUp;

public class InvertScreen extends PowerUp {

	public InvertScreen() {
		super(Color.RED, true);
		this.expiresIn = 30*8;
	}

	@Override
	public void onExpire(GamePanel g) {
		g.setFlipped(false);
	}

	@Override
	public void onObtain(GamePanel g) {
		PowerUp p = g.getLikePowerup(this);
		if(p != null)
		{
			p.expiresIn = 0;
		}
		else {
			g.obtainPowerUp(this);
			g.setFlipped(true);
		}
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
