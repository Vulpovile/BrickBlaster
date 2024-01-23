package com.vulpovile.games.brickblaster;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.JPanel;

import com.vulpovile.games.brickblaster.beep.BeepSoundSystem;
import com.vulpovile.games.brickblaster.game.Ball;

/**
 * Yes I am aware the code for this is horrific
 * I made it in half an hour because I wondered how easy it would be to make
 * I did not care about any conventions, clean code, or anything
 * 
 * Good Luck
 * 
 * @author Vulpovile
 *
 */

public class GamePanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final short W = 320, H = 240;

	private VolatileImage frameBuffer = null;

	public static long ticks = 0;

	private short resetTicks = -1;

	private final BeepSoundSystem sound;

	private short paddle = 0;
	private short paddleHalfWidth = W >> 4;

	private byte[] field = new byte[128];

	private Color[] colors = new Color[] { Color.BLUE, Color.GREEN, Color.CYAN, Color.RED, Color.MAGENTA, Color.YELLOW, Color.DARK_GRAY };

	private ArrayList<Ball> balls = new ArrayList<Ball>(8);

	private String[] levels = new String[] {"intro.bbl", "stagger.bbl", "smiley.bbl", "half.bbl", "challenger.bbl", "heart.bbl", "full.bbl", "hole.bbl", "trickshot.bbl", "chekkit.bbl" };
	private int currLevel = 0;

	private int toHit = -1;

	private int ballsLeft = 5;

	private boolean hookMouse = false;

	Robot robot = null;

	private Rectangle repaintRegion = null;

	//Functions

	public GamePanel(BeepSoundSystem sound) {
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		addMouseListener(this);
		this.sound = sound;
		this.setFocusable(true);
		this.requestFocus();
		this.requestFocusInWindow();
		// Transparent 16 x 16 pixel cursor image.
		//BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
		//Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		//this.setCursor(blankCursor);
		this.setBackground(Color.BLACK);

		try
		{
			robot = new Robot();
		}
		catch (AWTException e)
		{
			e.printStackTrace();
		}

	}

	public Graphics2D revalidateFramebuffer() {
		GraphicsConfiguration gc = getGraphicsConfiguration();
		if (frameBuffer == null || frameBuffer.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE || frameBuffer.getWidth() != W || frameBuffer.getHeight() != H)
		{
			frameBuffer = gc.createCompatibleVolatileImage(W, H);
		}
		return (Graphics2D) frameBuffer.getGraphics();
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2d = revalidateFramebuffer();
		super.paintComponent(g);
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, W, H);

		int w = W / 8;
		int h = (H - H / 4) / 16;
		int gapw = W / 128;
		int gaph = (H - H / 4) / 64;

		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 16; j++)
			{
				byte block = field[(j * 8) + i];
				if (block > 0)
				{
					g2d.setColor(colors[block - 1]);
					g2d.fillRect(i * w + gapw, j * h + gaph, w - gapw, h - gaph);
					g2d.setColor(colors[block - 1].darker());
					g2d.fillRect(i * w + gapw + gapw, j * h + gaph + gaph, w - gapw * 2, h - gaph * 2);
				}
			}
		}
		g2d.setColor(Color.BLACK);
		g2d.fillRect(W - (W >> 7), 0, (W >> 7), H);

		g2d.setColor(Color.GRAY);
		g2d.fillRect(paddle - paddleHalfWidth, H - (H / 16), paddleHalfWidth << 1, (H - H / 2) / 16);
		g2d.setColor(Color.WHITE.darker());
		g2d.fillRect(paddle - paddleHalfWidth, H - (H / 16), paddleHalfWidth << 1, (H - H / 2) / 32);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(paddle - paddleHalfWidth, H - (H / 16), paddleHalfWidth << 1, 1);

		for (int i = 0; i < balls.size(); i++)
		{
			Ball b = balls.get(i);
			if (b.ballY == Ball.BALL_MAGIC)
			{
				g2d.fillRect(paddle - b.ballWidthHalf + b.ballX, H - (H / 16) - (b.ballHeightHalf << 1) - (H / 64), b.ballWidthHalf << 1, b.ballHeightHalf << 1);
			}
			else
			{
				g2d.fillRect(b.ballX - b.ballWidthHalf, b.ballY - b.ballHeightHalf, b.ballWidthHalf << 1, b.ballHeightHalf << 1);
			}
		}

		for (int i = 0; i < ballsLeft - 1; i++)
		{

			g2d.fillRect(i * ((GamePanel.W >> 5)), H - (H >> 6), (GamePanel.W >> 5) - (GamePanel.W >> 6), H >> 5);
		}

		g2d.dispose();
		g.drawImage(frameBuffer, 0, 0, getWidth(), getHeight(), this);
	}

	public void addRepaintRegion(Rectangle rect) {
		if (repaintRegion == null)
			repaintRegion = rect;
		else repaintRegion.add(rect);
	}

	public void tick() {
		//ticks++;

		if (toHit > 0 && resetTicks <= -1 && ballsLeft > 0)
		{
			nextBall: for (int i = 0; i < balls.size(); i++)
			{
				Ball ball = balls.get(i);
				if (ball.ballY == Ball.BALL_MAGIC || ball.ballX == Ball.BALL_MAGIC)
					continue;
				addRepaintRegion(new Rectangle(ball.ballX - ball.ballWidthHalf, ball.ballY - ball.ballHeightHalf, ball.ballWidthHalf << 1, ball.ballHeightHalf << 1));
				for (int y = 0; y < Math.abs(ball.ballYVelocity); y++)
				{
					ball.ballY += Math.signum(ball.ballYVelocity);
					if (ball.ballYVelocity > 0 &&
					/*Check height*/
					ball.ballY + ball.ballHeightHalf > H - (H / 16) && ball.ballY - ball.ballHeightHalf < (H - (H / 16)) + ((H - H / 2) / 16)
					/*Check Paddle Alignment*/
					&& ball.ballX > paddle - paddleHalfWidth - ball.ballWidthHalf && ball.ballX < paddle + paddleHalfWidth + ball.ballWidthHalf)
					{
						ball.ballYVelocity = -6;
						ball.ballXVelocity = ((ball.ballX - paddle) * 6) / (paddleHalfWidth);
						sound.note(1000, 50, 0.5, 0);
						break nextBall;
					}
					else if (ball.ballY - ball.ballHeightHalf > H)
					{
						ball.ballX = Ball.BALL_MAGIC;
						break nextBall;
					}
					else if (ball.ballY < ball.ballHeightHalf && ball.ballYVelocity < 0)
					{
						ball.ballYVelocity = -ball.ballYVelocity;
						sound.note(200, 25, 0.5, 0);
						sound.note(100, 25, 0.5, 0);
						break nextBall;
					}
					else if (ball.ballY < (H - H / 4))
					{
						int w = W >> 3;
						int h = (H - H / 4) >> 4;

						int xIdx1 = (ball.ballX + ball.ballWidthHalf) / w;
						int xIdx2 = (ball.ballX - ball.ballWidthHalf) / w;
						int yIdx = ball.ballYVelocity > 0 ? (ball.ballY + ball.ballHeightHalf) / h : (ball.ballY - ball.ballHeightHalf) / h;

						int index1 = (yIdx * 8) + xIdx1;
						int index2 = (yIdx * 8) + xIdx2;
						//BALL HIT!!! (y)
						if (xIdx1 < 8 && yIdx > 0 && yIdx < 16 && field[index1] > 0)
						{
							ball.ballY -= Math.signum(ball.ballYVelocity);
							int val = field[index1];
							if (val != 7)
							{
								toHit--;
								field[index1] = 0;
								addRepaintRegion(new Rectangle(xIdx1 * w, yIdx * h, w, h));
							}
							ball.ballYVelocity = -ball.ballYVelocity;

							sound.note(100 * val, 25, 0.25, 0);
							sound.note(100 * (val + 2), 25, 0.25, 1);
							break nextBall;
						}
						if (xIdx2 > 0 && yIdx > 0 && yIdx < 16 && field[index2] > 0)
						{
							ball.ballY -= Math.signum(ball.ballYVelocity);
							int val = field[index2];
							if (val != 7)
							{
								toHit--;
								field[index2] = 0;
								addRepaintRegion(new Rectangle(xIdx2 * w, yIdx * h, w, h));
							}
							ball.ballYVelocity = -ball.ballYVelocity;

							sound.note(100 * val, 25, 0.25, 0);
							sound.note(100 * (val + 2), 25, 0.25, 1);
							break nextBall;
						}
						//System.out.println(field[(yIdx * 8) + xIdx]);
					}
				}

				for (int x = 0; x < Math.abs(ball.ballXVelocity); x++)
				{
					if (ball.ballX == Ball.BALL_MAGIC)
						break nextBall;
					ball.ballX += Math.signum(ball.ballXVelocity);
					if (ball.ballX < ball.ballWidthHalf && ball.ballXVelocity < 0 || ball.ballX > W - ball.ballWidthHalf && ball.ballXVelocity > 0)
					{
						ball.ballXVelocity = -ball.ballXVelocity;
						sound.note(200, 25, 0.25, 0);
						sound.note(100, 25, 0.25, 0);
						break nextBall;
					}
					else if (ball.ballY < (H - H / 4))
					{
						int w = W / 8;
						int h = (H - H / 4) / 16;
						//int xIdx = ball.ballX / w;
						//int yIdx = ball.ballY / h;

						int yIdx1 = (ball.ballY + ball.ballHeightHalf) / h;
						int yIdx2 = (ball.ballY - ball.ballHeightHalf) / h;
						int xIdx = ball.ballXVelocity > 0 ? (ball.ballX + ball.ballWidthHalf) / w : (ball.ballX - ball.ballWidthHalf) / w;

						int index1 = (yIdx1 * 8) + xIdx;
						int index2 = (yIdx2 * 8) + xIdx;

						//BaLL hIt!1! (x)
						if (yIdx1 < 16 && xIdx > 0 && xIdx < 8 && field[index1] > 0)
						{
							ball.ballX -= Math.signum(ball.ballXVelocity);
							int val = field[index1];
							if (val != 7)
							{
								toHit--;
								field[index1] = 0;
								addRepaintRegion(new Rectangle(xIdx * w, yIdx1 * h, w, h));
							}
							ball.ballXVelocity = -ball.ballXVelocity;
							sound.note(100 * val, 25, 0.25, 0);
							sound.note(100 * (val + 2), 25, 0.25, 1);
							break nextBall;
						}
						else if (yIdx2 < 16 && xIdx > 0 && xIdx < 8 && field[index2] > 0)
						{
							ball.ballX -= Math.signum(ball.ballXVelocity);
							int val = field[index2];
							if (val != 7)
							{
								toHit--;
								field[index2] = 0;
								addRepaintRegion(new Rectangle(xIdx * w, yIdx2 * h, w, h));
							}
							ball.ballXVelocity = -ball.ballXVelocity;
							sound.note(100 * val, 25, 0.25, 0);
							sound.note(100 * (val + 2), 25, 0.25, 1);
							break nextBall;
						}
						//System.out.println(field[(yIdx * 8) + xIdx]);
					}
				}
				addRepaintRegion(new Rectangle(ball.ballX - ball.ballWidthHalf, ball.ballY - ball.ballHeightHalf, ball.ballWidthHalf << 1, ball.ballHeightHalf << 1));
			}
			for (int i = balls.size() - 1; i >= 0; i--)
			{
				if (balls.get(i).ballX == Ball.BALL_MAGIC)
					balls.remove(i);
			}
			if (balls.size() <= 0)
			{
				resetTicks = 120;
				//sound.note(316, 200, 0.125, 0);
				sound.note(251, 150, 0.25, 0);
				//sound.note(612, 200, 0.125, 2);
				sound.note(458, 150, 0.25, 1);

				sound.note(251, 300, 0.25, 0);
				sound.note(120, 300, 0.25, 1);

				ballsLeft--;
				addRepaintRegion(new Rectangle(0, 0, W, H));
			}
		}
		else if (resetTicks == 0 && toHit == -1)
		{
			this.currLevel = (currLevel + 1) % this.levels.length;
			this.loadLevel();
			addRepaintRegion(new Rectangle(0, 0, W, H));
		}
		else if (resetTicks == 0)
		{
			if (ballsLeft == 0)
			{
				ballsLeft = 5;
				loadLevel();
			}
			balls.clear();
			balls.add(new Ball());
			addRepaintRegion(new Rectangle(0, 0, W, H));
			resetTicks--;
		}
		else if (resetTicks > 0)
		{
			resetTicks--;
		}
		if (toHit == 0)
		{
			resetTicks = 120;
			toHit--;
			/*sound.note(300, 300, 0.2, 0);
			sound.note(400, 300, 0.2, 2);
			

			sound.note(500, 300, 0.2, 0);
			sound.note(600, 300, 0.2, 2);
			

			sound.note(600, 300, 0.2, 0);
			sound.note(800, 300, 0.2, 2);*/
			sound.note(400, 100, 0.2, 0);
			sound.note(500, 100, 0.2, 1);

			sound.note(0, 20, 0.2, 0);
			sound.note(0, 20, 0.2, 1);

			sound.note(400, 50, 0.2, 0);
			sound.note(500, 50, 0.2, 1);

			sound.note(500, 300, 0.2, 0);
			sound.note(600, 300, 0.2, 1);

			sound.note(600, 300, 0.2, 0);
			sound.note(800, 300, 0.2, 1);

			sound.note(1000, 300, 0.2, 0);
			sound.note(800, 300, 0.2, 1);

			sound.note(1200, 300, 0.2, 0);
			sound.note(1000, 300, 0.2, 1);
		}

		if (repaintRegion != null)
		{
			repaintRegion.x = repaintRegion.x * getWidth() / W;
			repaintRegion.y = repaintRegion.y * getHeight() / H;
			repaintRegion.width = (repaintRegion.width + 1) * getWidth() / W;
			repaintRegion.height = (repaintRegion.height + 1) * getHeight() / H;
			repaint(repaintRegion);
			repaintRegion = null;
		}
	}

	public void doContinue() {
		balls.clear();
		balls.add(new Ball());
		toHit = -1;
		this.ballsLeft = 5;
		this.resetTicks = -1;
		loadLevel();
		addRepaintRegion(new Rectangle(0, 0, W, H));
	}

	public void reset() {
		sound.note(200, 100, 0.3, 1);
		sound.note(300, 100, 0.2, 0);
		sound.note(400, 100, 0.2, 2);

		sound.note(600, 100, 0.3, 1);
		sound.note(800, 100, 0.2, 0);
		sound.note(0, 100, 0.2, 2);

		balls.clear();
		balls.add(new Ball());
		toHit = -1;
		this.ballsLeft = 5;
		this.currLevel = 0;
		this.resetTicks = -1;
		loadLevel();
		addRepaintRegion(new Rectangle(0, 0, W, H));
	}

	public void mouseMoved(MouseEvent e) {

		addRepaintRegion(new Rectangle(paddle - paddleHalfWidth, H - (H / 16), paddleHalfWidth << 1, (H - H / 2) / 16));
		for (int i = 0; i < this.balls.size(); i++)
		{
			Ball b = balls.get(i);
			if (b.ballY == Ball.BALL_MAGIC)
				addRepaintRegion(new Rectangle(paddle - b.ballWidthHalf + b.ballX, H - (H / 16) - (b.ballHeightHalf << 1) - (H / 64), b.ballWidthHalf << 1, b.ballHeightHalf << 1));
		}
		paddle = (short) ((e.getX() * (1 + (paddleHalfWidth << 1) / (float) W) * (W / (float) getWidth())) - paddleHalfWidth);
		addRepaintRegion(new Rectangle(paddle - paddleHalfWidth, H - (H / 16), paddleHalfWidth << 1, (H - H / 2) / 16));
		for (int i = 0; i < this.balls.size(); i++)
		{
			Ball b = balls.get(i);
			if (b.ballY == Ball.BALL_MAGIC)
				addRepaintRegion(new Rectangle(paddle - b.ballWidthHalf + b.ballX, H - (H / 16) - (b.ballHeightHalf << 1) - (H / 64), b.ballWidthHalf << 1, b.ballHeightHalf << 1));
		}
		if (hookMouse && robot != null)
			robot.mouseMove(MouseInfo.getPointerInfo().getLocation().x, this.getLocationOnScreen().y + (getHeight() >> 1));
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		for (int i = 0; i < balls.size(); i++)
		{
			if (balls.get(i).ballY == Ball.BALL_MAGIC)
			{
				Ball b = balls.get(i);
				addRepaintRegion(new Rectangle(paddle - b.ballWidthHalf + b.ballX, H - (H / 16) - (b.ballHeightHalf << 1) - (H / 64), b.ballWidthHalf << 1, b.ballHeightHalf << 1));
				b.ballX = Math.min(Math.max(b.ballX + e.getUnitsToScroll(), -this.paddleHalfWidth), this.paddleHalfWidth);
				addRepaintRegion(new Rectangle(paddle - b.ballWidthHalf + b.ballX, H - (H / 16) - (b.ballHeightHalf << 1) - (H / 64), b.ballWidthHalf << 1, b.ballHeightHalf << 1));
			}
		}
	}

	public void keyTyped(KeyEvent e) {

	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_R:
				reset();
				break;
			case KeyEvent.VK_C:
				doContinue();
				break;
			case KeyEvent.VK_H:
				this.hookMouse = !this.hookMouse;
				break;
		}
	}

	public void keyReleased(KeyEvent e) {

	}

	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void loadLevel() {
		String levelResource = "/builtin_levels/" + levels[currLevel];
		InputStream stream = this.getClass().getResourceAsStream(levelResource);
		if (stream != null)
		{
			try
			{
				for (int i = 0; i < field.length; i += 2)
				{
					int read = stream.read();
					if (read != -1)
					{
						field[i] = (byte) (read & 0x0F);
						field[i + 1] = (byte) ((read & 0xF0) >> 4);
					}
				}
				//stream.read(field, 0, field.length);
				calculateHitables();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private void calculateHitables() {
		this.toHit = 0;
		for (int i = 0; i < field.length; i++)
		{
			if (field[i] != 0 && field[i] != 7)
			{
				toHit++;
			}
		}
	}

	public void mouseReleased(MouseEvent arg0) {
		for (int i = 0; i < balls.size(); i++)
		{
			if (balls.get(i).ballY == Ball.BALL_MAGIC)
			{
				Ball ball = balls.get(i);
				ball.ballX = paddle + ball.ballX;
				ball.ballY = H - (H / 16) - (ball.ballHeightHalf << 1) - (H / 64);
				ball.ballXVelocity = 0;
				ball.ballYVelocity = 4;
				break;
			}
		}
	}
}
