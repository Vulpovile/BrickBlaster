package com.vulpovile.games.brickblaster;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class AboutDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 */
	public AboutDialog(Component parent) {
		setTitle("About");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(450, 459);
		setLocationRelativeTo(parent);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		StringBuilder builder = new StringBuilder();
		builder.append(GameBase.PRODUCT_NAME);
		builder.append(" ");
		builder.append(GameBase.ERA);
		builder.append(GameBase.GENERATION);
		builder.append(".");
		builder.append(GameBase.MAJOR_VERSION);
		builder.append(".");
		builder.append(GameBase.MINOR_VERSION);
		if(GameBase.PATCH_VERSION != 0)
		{
			builder.append("_");
			//oh god
			builder.append(String.format("%02d", GameBase.PATCH_VERSION));
			if(GameBase.PATCH_VERSION < 0)
				builder.append("\n(You have a fancy development version!)");
		}
		builder.append("\n\n");
		builder.append("BrickBlaster is based on a classic 70's TV brick destruction game. This game is only single player.\n\n");
		builder.append("Controls:\n");
		builder.append("Mouse - Paddle\n");
		builder.append("Wheel - Aim (on first shoot)\n");
		builder.append("R - Reset\n");
		builder.append("C - Continue\n");
		

		builder.append("\nPower-Ups (Left-To-Right Shine):\n");
		builder.append("Green - Extra Ball\n");
		builder.append("Magenta - Super-Ball\n");
		builder.append("Blue - Multi-Ball\n");
		builder.append("Yellow - Paddle Grow\n");
		
		builder.append("\nPower-Downs (Right-To-Left Shine):\n");
		builder.append("Yellow - Paddle Shrink\n");
		builder.append("Red - Invert screen\n\n");
		
		builder.append("Some power-ups and power-downs are toggles!\n\n");
		
		builder.append("\nCredits:\n");
		

		contentPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		contentPanel.add(scrollPane);

		JTextArea textArea = new JTextArea(builder.toString());
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setBackground(UIManager.getColor("control"));
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});

	}
}
