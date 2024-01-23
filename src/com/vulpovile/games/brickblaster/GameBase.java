package com.vulpovile.games.brickblaster;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.vulpovile.games.brickblaster.beep.BeepSoundSystem;

public class GameBase extends JPanel {


	public static final char ERA = 'a';
	public static final byte GENERATION = 0;
	public static final byte MAJOR_VERSION = 0;
	public static final byte MINOR_VERSION = 3;
	public static final byte PATCH_VERSION = 0;
	public static final String PRODUCT_NAME = "BrickBlaster";


	public GameTickThread gameTickThread;

	private final BeepSoundSystem sound = new BeepSoundSystem(3);
	private final GamePanel paddlePanel = new GamePanel(sound);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public GameBase() {
		this.setLayout(new BorderLayout());
		add(paddlePanel, BorderLayout.CENTER);

		sound.note(200, 150, 0.2, 0);
		sound.note(300, 150, 0.2, 0);
		sound.note(400, 150, 0.2, 0);
		sound.note(500, 150, 0.2, 0);
		sound.note(500, 150, 0.2, 1);
		sound.note(600, 150, 0.2, 1);
		sound.note(700, 150, 0.2, 1);
		sound.note(800, 150, 0.2, 1);

		//sound.note(800, 300, 0.1, 3);
		//sound.note(400, 300, 0.1, 2);
		

		//sound.note(200, 300, 0.1, 3);
		//sound.note(400, 300, 0.1, 2);
		
		//sound.note(100, 150, 0.1, 3);
		//sound.note(300, 150, 0.1, 2);
	}
	
	public void begin(){
		//16666666L
		gameTickThread = new GameTickThread(paddlePanel, 33333333L);
		new Thread(gameTickThread).start();
	}
	
	public void destroy()
	{
		sound.destroy();
		gameTickThread.end();
		/*try
		{
			//Wait for all threads to (hopefully) exit gracefully
			Thread.sleep(1000L);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		//Ensure exit if that does not happen
		System.exit(0);*/
	}

}
