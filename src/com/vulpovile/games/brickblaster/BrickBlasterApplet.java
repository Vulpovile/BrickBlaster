package com.vulpovile.games.brickblaster;

import java.awt.BorderLayout;

import javax.swing.JApplet;

public class BrickBlasterApplet extends JApplet{

	/**
	 * 
	 */
	

	private GameBase gameBase = new GameBase();
	
	private static final long serialVersionUID = 1L;

	@Override
	public void init(){
		this.getContentPane().setLayout(new BorderLayout());
		this.add(gameBase, BorderLayout.CENTER);
	}
	
	@Override
	public void start(){
		gameBase.begin();
	}
	
	@Override
	public void stop(){}
	
	@Override
	public void destroy(){
		gameBase.destroy();
	}
	
}
