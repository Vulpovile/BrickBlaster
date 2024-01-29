package com.vulpovile.games.brickblaster;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

public class BrickBlasterApplication extends JFrame implements WindowListener, ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GameBase gameBase = new GameBase();

	public BrickBlasterApplication() {
		super(String.format("%s - %s%d.%d.%d", GameBase.PRODUCT_NAME, GameBase.ERA, GameBase.GENERATION, GameBase.MAJOR_VERSION, GameBase.MINOR_VERSION));
		if(GameBase.PATCH_VERSION != 0)
		{
			this.setTitle(this.getTitle() + String.format("_%02d", GameBase.PATCH_VERSION));
		}
		this.setSize(800 + getInsets().left + getInsets().right, 600 + getInsets().top + getInsets().bottom);
		this.getContentPane().setLayout(new BorderLayout());
		this.add(gameBase, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
	}

	public static void main(String[] args) {
		BrickBlasterApplication cardboardApplication = new BrickBlasterApplication();
		cardboardApplication.setVisible(true);
		cardboardApplication.gameBase.begin();
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowClosing(WindowEvent arg0) {
		dispose();
		gameBase.destroy();
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}

	public void actionPerformed(ActionEvent arg0) {
	}
}
