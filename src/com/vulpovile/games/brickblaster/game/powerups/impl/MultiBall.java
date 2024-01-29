package com.vulpovile.games.brickblaster.game.powerups.impl;

import java.awt.Color;
import java.util.ArrayList;

import com.vulpovile.games.brickblaster.GamePanel;
import com.vulpovile.games.brickblaster.game.Ball;
import com.vulpovile.games.brickblaster.game.powerups.PowerUp;

public class MultiBall extends PowerUp{

	public MultiBall() {
		super(Color.BLUE, false);
	}

	@Override
	public void onExpire(GamePanel g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onObtain(GamePanel g) {
		ArrayList<Ball> balls = g.getBalls();
		if(balls.size() > 0)
		{
			Ball b = balls.get(0);
			Ball newBall = new Ball();
			newBall.ballX = b.ballX;
			newBall.ballY = b.ballY;
			newBall.ballXVelocity = b.ballXVelocity - 1;
			newBall.ballYVelocity = b.ballYVelocity;
			balls.add(newBall);
			newBall = new Ball();
			newBall.ballX = b.ballX;
			newBall.ballY = b.ballY;
			newBall.ballXVelocity = b.ballXVelocity + 1;
			newBall.ballYVelocity = b.ballYVelocity;
			balls.add(newBall);
		}
	}

	@Override
	public void tick(GamePanel g) {
		// TODO Auto-generated method stub
		
	}

}
