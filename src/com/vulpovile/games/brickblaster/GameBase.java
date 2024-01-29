package com.vulpovile.games.brickblaster;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JPanel;

import com.vulpovile.games.brickblaster.beep.BeepSoundSystem;
import com.vulpovile.games.brickblaster.level.CompilationLevelLoader;
import com.vulpovile.games.brickblaster.level.SingleLevelLoader;
import com.vulpovile.games.brickblaster.util.FileExtentionFilter;

import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class GameBase extends JPanel implements ActionListener {

	public static final char ERA = 'a';
	public static final byte GENERATION = 0;
	public static final byte MAJOR_VERSION = 0;
	public static final byte MINOR_VERSION = 4;
	public static final byte PATCH_VERSION = 0;
	public static final String PRODUCT_NAME = "BrickBlaster";

	public GameTickThread gameTickThread;

	private final BeepSoundSystem sound = new BeepSoundSystem(3);
	private final GamePanel paddlePanel = new GamePanel(sound);
	private final JMenuItem mntmAbout = new JMenuItem("About");
	private final JMenuItem mntmOpen = new JMenuItem("Open");
	
	private File lastFile = null;

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

		JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);

		JMenuBar menuBar = new JMenuBar();
		toolBar.add(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		mnFile.add(mntmOpen);
		mntmOpen.addActionListener(this);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		mntmAbout.addActionListener(this);
		mnHelp.add(mntmAbout);


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

	public void begin() {
		//16666666L
		gameTickThread = new GameTickThread(paddlePanel, 33333333L);
		new Thread(gameTickThread).start();
	}

	public void destroy() {
		sound.destroy();
		gameTickThread.end();
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == mntmAbout)
		{
			new AboutDialog(GameBase.this).setVisible(true);
		}
		
		else if(arg0.getSource() == mntmOpen)
		{
			JFileChooser jFileChooser = new JFileChooser(lastFile);
			jFileChooser.setFileFilter(new FileExtentionFilter("All Accepted Filetypes", "bbl", "bcl"));
			jFileChooser.addChoosableFileFilter(new FileExtentionFilter("BrickBlaster Levels", "bbl"));
			jFileChooser.addChoosableFileFilter(new FileExtentionFilter("BrickBlaster Compilations", "bcl"));
			
			if(jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION && jFileChooser.getSelectedFile() != null)
			{
				File file = jFileChooser.getSelectedFile();
				if(file.getName().endsWith("bbl"))
				{
					SingleLevelLoader sll = new SingleLevelLoader(file);
					paddlePanel.setLevelLoader(sll);
				}
				else if(file.getName().endsWith("bcl"))
				{
					CompilationLevelLoader cll = new CompilationLevelLoader(file);
					paddlePanel.setLevelLoader(cll);
				}
			}
		}
	}

}
