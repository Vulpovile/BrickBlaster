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
import java.util.ArrayList;

import javax.swing.JPanel;

import com.vulpovile.games.brickblaster.beep.BeepSoundSystem;
import com.vulpovile.games.brickblaster.game.Ball;
import com.vulpovile.games.brickblaster.game.powerups.PowerUp;
import com.vulpovile.games.brickblaster.game.powerups.PowerUpProvider;
import com.vulpovile.games.brickblaster.level.InternalLevelLoader;
import com.vulpovile.games.brickblaster.level.LevelLoader;

/**
 * Yes I am aware the code for this is not excellent
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
	public static final short PADDLE_DEFAULT_HALF_WIDTH = W >> 4;

	private final BeepSoundSystem sound;

	public static int ticks = 0;

	private VolatileImage frameBuffer = null;

	private short resetTicks = -1;

	private short paddle = 0;
	private short paddleHalfWidth = PADDLE_DEFAULT_HALF_WIDTH;

	private byte[] field = new byte[128];

	private Color[] colors = new Color[] { Color.BLUE, Color.GREEN, Color.CYAN, Color.RED, Color.MAGENTA, Color.YELLOW, Color.DARK_GRAY };

	private ArrayList<Ball> balls = new ArrayList<Ball>(8);

	private int toHit = -1;

	private int ballsLeft = 5;

	private boolean hookMouse = false;

	private Robot robot = null;

	private Rectangle repaintRegion = null;

	private ArrayList<PowerUp> fallingPowerUps = new ArrayList<PowerUp>();
	private ArrayList<PowerUp> obtainedPowerUps = new ArrayList<PowerUp>();

	private boolean flip = false;

	private LevelLoader levelLoader = new InternalLevelLoader();

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

		if (flip)
		{
			g2d.scale(1, -1);
			g2d.translate(0, -H);
		}

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

		for (int i = 0; i < fallingPowerUps.size(); i++)
		{
			PowerUp powerUp = fallingPowerUps.get(i);
			gapw = powerUp.pupHalfWidth >> 2;
			gaph = powerUp.pupHalfHeight >> 1;
			g2d.setColor(powerUp.color);
			g2d.fillRect(powerUp.x - powerUp.pupHalfWidth, powerUp.y - powerUp.pupHalfHeight, powerUp.pupHalfWidth << 1, powerUp.pupHalfHeight << 1);
			g2d.setColor(powerUp.color.darker());
			g2d.fillRect(powerUp.x - powerUp.pupHalfWidth + gapw, powerUp.y - powerUp.pupHalfHeight + gaph, (powerUp.pupHalfWidth << 1) - gapw, (powerUp.pupHalfHeight << 1) - gaph);

			int linePos = ticks % (powerUp.pupHalfWidth << 1);
			if (linePos << 1 < (powerUp.pupHalfWidth << 1))
			{
				g2d.setColor(Color.WHITE);
				if (powerUp.nerf)
					g2d.fillRect(powerUp.x + powerUp.pupHalfWidth - (linePos << 1) - gapw, powerUp.y - powerUp.pupHalfHeight, gaph, powerUp.pupHalfHeight << 1);
				else g2d.fillRect(powerUp.x - powerUp.pupHalfWidth + (linePos << 1), powerUp.y - powerUp.pupHalfHeight, gaph, powerUp.pupHalfHeight << 1);
			}
		}

		g2d.setColor(Color.GRAY);
		g2d.fillRect(paddle - paddleHalfWidth, H - (H / 16), paddleHalfWidth << 1, (H - H / 2) / 16);
		g2d.setColor(Color.WHITE.darker());
		g2d.fillRect(paddle - paddleHalfWidth, H - (H / 16), paddleHalfWidth << 1, (H - H / 2) / 32);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(paddle - paddleHalfWidth, H - (H / 16), paddleHalfWidth << 1, 1);

		for (int i = 0; i < balls.size(); i++)
		{
			Ball b = balls.get(i);
			g2d.setColor(b.superBall ? Color.MAGENTA : Color.WHITE);
			if (b.ballY == Ball.BALL_MAGIC)
			{
				g2d.fillRect(paddle - b.ballWidthHalf + b.ballX, H - (H / 16) - (b.ballHeightHalf << 1) - (H / 64), b.ballWidthHalf << 1, b.ballHeightHalf << 1);
			}
			else
			{
				g2d.fillRect(b.ballX - b.ballWidthHalf, b.ballY - b.ballHeightHalf, b.ballWidthHalf << 1, b.ballHeightHalf << 1);
			}
		}

		g2d.setColor(Color.WHITE);
		for (int i = 0; i < ballsLeft - 1; i++)
		{
			g2d.fillRect(i * ((GamePanel.W >> 5)), H - (H >> 6), (GamePanel.W >> 5) - (GamePanel.W >> 6), H >> 5);
		}

		g2d.dispose();
		g.drawImage(frameBuffer, 0, 0, getWidth(), getHeight(), this);
	}

	public void addRepaintRegion(Rectangle rect) {
		if (flip)
		{
			rect.y = H - rect.y - rect.height;
		}
		if (repaintRegion == null)
			repaintRegion = rect;
		else repaintRegion.add(rect);
	}

	public void tick() {
		ticks++;

		if (toHit > 0 && resetTicks <= -1 && ballsLeft > 0)
		{
			for (int i = fallingPowerUps.size() - 1; i >= 0; i--)
			{
				PowerUp powerUp = fallingPowerUps.get(i);
				addRepaintRegion(new Rectangle(powerUp.x - powerUp.pupHalfWidth, powerUp.y - powerUp.pupHalfHeight, powerUp.pupHalfWidth << 1, powerUp.pupHalfHeight << 1));
				powerUp.y += H >> 7;
				if (powerUp.y > H)
				{
					fallingPowerUps.remove(i);
					continue;
				}
				if (/*Check height*/
				powerUp.y + powerUp.pupHalfHeight > H - (H / 16) && powerUp.y - powerUp.pupHalfHeight < (H - (H / 16)) + ((H - H / 2) / 16)
				/*Check Paddle Alignment*/
				&& powerUp.x > paddle - paddleHalfWidth - powerUp.pupHalfWidth && powerUp.x < paddle + paddleHalfWidth + powerUp.pupHalfWidth)
				{
					sound.note(1200, 15, 0.5, 2);
					sound.note(1600, 15, 0.5, 2);

					powerUp.onObtain(this);

					fallingPowerUps.remove(i);
					continue;
				}
				addRepaintRegion(new Rectangle(powerUp.x - powerUp.pupHalfWidth, powerUp.y - powerUp.pupHalfHeight, powerUp.pupHalfWidth << 1, powerUp.pupHalfHeight << 1));
			}

			for (int i = obtainedPowerUps.size() - 1; i >= 0; i--)
			{
				PowerUp powerUp = obtainedPowerUps.get(i);
				if (powerUp.expiresIn != PowerUp.POWERUP_MAGIC)
				{
					powerUp.expiresIn--;
					if (powerUp.expiresIn <= 0)
					{
						obtainedPowerUps.remove(i);
						powerUp.onExpire(this);
					}
				}
				powerUp.tick(this);
			}

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
						continue;
					}
					else if (ball.ballY - ball.ballHeightHalf > H)
					{
						ball.ballX = Ball.BALL_MAGIC;
						continue nextBall;
					}
					else if (ball.ballY < ball.ballHeightHalf && ball.ballYVelocity < 0)
					{
						ball.ballYVelocity = -ball.ballYVelocity;
						sound.note(200, 25, 0.5, 0);
						sound.note(100, 25, 0.5, 0);
						continue;
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
						if (xIdx1 < 8 && yIdx >= 0 && yIdx < 16 && field[index1] > 0)
						{
							ball.ballY -= Math.signum(ball.ballYVelocity);
							int val = field[index1];
							if (val != 7)
							{
								toHit--;
								addRepaintRegion(new Rectangle(xIdx1 * w, yIdx * h, w, h));
								PowerUp powerUp = PowerUpProvider.providePowerUp(ball.ballX, ball.ballY, field[index1]);
								if (powerUp != null)
								{
									addRepaintRegion(new Rectangle(powerUp.x - powerUp.pupHalfWidth, powerUp.y - powerUp.pupHalfHeight, powerUp.pupHalfWidth << 1, powerUp.pupHalfHeight << 1));
									this.fallingPowerUps.add(powerUp);
								}
								field[index1] = 0;
							}
							if (!ball.superBall || val == 7)
								ball.ballYVelocity = -ball.ballYVelocity;

							sound.note(100 * val, 25, 0.25, 0);
							sound.note(100 * (val + 2), 25, 0.25, 1);
							continue;
						}
						if (xIdx2 >= 0 && yIdx >= 0 && yIdx < 16 && field[index2] > 0)
						{
							ball.ballY -= Math.signum(ball.ballYVelocity);
							int val = field[index2];
							if (val != 7)
							{
								toHit--;
								addRepaintRegion(new Rectangle(xIdx2 * w, yIdx * h, w, h));
								PowerUp powerUp = PowerUpProvider.providePowerUp(ball.ballX, ball.ballY, field[index2]);
								if (powerUp != null)
								{
									addRepaintRegion(new Rectangle(powerUp.x - powerUp.pupHalfWidth, powerUp.y - powerUp.pupHalfHeight, powerUp.pupHalfWidth << 1, powerUp.pupHalfHeight << 1));
									this.fallingPowerUps.add(powerUp);
								}
								field[index2] = 0;
							}
							if (!ball.superBall || val == 7)
								ball.ballYVelocity = -ball.ballYVelocity;

							sound.note(100 * val, 25, 0.25, 0);
							sound.note(100 * (val + 2), 25, 0.25, 1);
							continue;
						}
					}
				}

				for (int x = 0; x < Math.abs(ball.ballXVelocity); x++)
				{
					if (ball.ballX == Ball.BALL_MAGIC)
						continue nextBall;
					ball.ballX += Math.signum(ball.ballXVelocity);
					if (ball.ballX < ball.ballWidthHalf && ball.ballXVelocity < 0 || ball.ballX > W - ball.ballWidthHalf && ball.ballXVelocity > 0)
					{
						ball.ballXVelocity = -ball.ballXVelocity;
						sound.note(200, 25, 0.25, 0);
						sound.note(100, 25, 0.25, 0);
						continue;
					}
					else if (ball.ballY < (H - H / 4))
					{
						int w = W / 8;
						int h = (H - H / 4) / 16;

						int yIdx1 = (ball.ballY + ball.ballHeightHalf) / h;
						int yIdx2 = (ball.ballY - ball.ballHeightHalf) / h;
						int xIdx = ball.ballXVelocity > 0 ? (ball.ballX + ball.ballWidthHalf) / w : (ball.ballX - ball.ballWidthHalf) / w;

						int index1 = (yIdx1 * 8) + xIdx;
						int index2 = (yIdx2 * 8) + xIdx;

						//BALL HIT!!! (x)
						if (yIdx1 < 16 && xIdx >= 0 && xIdx < 8 && field[index1] > 0)
						{
							ball.ballX -= Math.signum(ball.ballXVelocity);
							int val = field[index1];
							if (val != 7)
							{
								toHit--;
								addRepaintRegion(new Rectangle(xIdx * w, yIdx1 * h, w, h));
								PowerUp powerUp = PowerUpProvider.providePowerUp(ball.ballX, ball.ballY, field[index1]);
								if (powerUp != null)
								{
									addRepaintRegion(new Rectangle(powerUp.x - powerUp.pupHalfWidth, powerUp.y - powerUp.pupHalfHeight, powerUp.pupHalfWidth << 1, powerUp.pupHalfHeight << 1));
									this.fallingPowerUps.add(powerUp);
								}
								field[index1] = 0;
							}
							if (!ball.superBall || val == 7)
								ball.ballXVelocity = -ball.ballXVelocity;
							sound.note(100 * val, 25, 0.25, 0);
							sound.note(100 * (val + 2), 25, 0.25, 1);
							continue;
						}
						else if (yIdx2 < 16 && xIdx >= 0 && xIdx < 8 && field[index2] > 0)
						{
							ball.ballX -= Math.signum(ball.ballXVelocity);
							int val = field[index2];
							if (val != 7)
							{
								toHit--;
								addRepaintRegion(new Rectangle(xIdx * w, yIdx2 * h, w, h));
								PowerUp powerUp = PowerUpProvider.providePowerUp(ball.ballX, ball.ballY, field[index2]);
								if (powerUp != null)
								{
									addRepaintRegion(new Rectangle(powerUp.x - powerUp.pupHalfWidth, powerUp.y - powerUp.pupHalfHeight, powerUp.pupHalfWidth << 1, powerUp.pupHalfHeight << 1));
									this.fallingPowerUps.add(powerUp);
								}
								field[index2] = 0;
							}
							if (!ball.superBall || val == 7)
								ball.ballXVelocity = -ball.ballXVelocity;
							sound.note(100 * val, 25, 0.25, 0);
							sound.note(100 * (val + 2), 25, 0.25, 1);
							continue;
						}
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
				sound.note(251, 150, 0.25, 0);
				sound.note(458, 150, 0.25, 1);

				sound.note(251, 300, 0.25, 0);
				sound.note(120, 300, 0.25, 1);

				ballsLeft--;
				addRepaintRegion(new Rectangle(0, 0, W, H));
			}
		}
		else if (resetTicks == 0 && toHit == -1)
		{
			revertDefaults();
			levelLoader.incrementLevelCounter();
			this.toHit = levelLoader.loadLevel(field);
			addRepaintRegion(new Rectangle(0, 0, W, H));
		}
		else if (resetTicks == 0)
		{
			if (ballsLeft == 0)
			{
				revertDefaults();
				ballsLeft = 5;
				this.toHit = levelLoader.loadLevel(field);
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

			sound.note(0, 100, 0.2, 0);
			sound.note(0, 100, 0.2, 1);

			sound.note(400, 50, 0.2, 0);
			sound.note(500, 50, 0.2, 1);
			sound.note(0, 20, 0.2, 0);
			sound.note(0, 20, 0.2, 1);

			sound.note(500, 300, 0.2, 0);
			sound.note(600, 300, 0.2, 1);
			sound.note(0, 20, 0.2, 0);
			sound.note(0, 20, 0.2, 1);

			sound.note(600, 300, 0.2, 0);
			sound.note(800, 300, 0.2, 1);
			sound.note(0, 20, 0.2, 0);
			sound.note(0, 20, 0.2, 1);

			sound.note(1000, 300, 0.2, 0);
			sound.note(800, 300, 0.2, 1);
			sound.note(0, 20, 0.2, 0);
			sound.note(0, 20, 0.2, 1);

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

	public void revertDefaults() {
		this.flip = false;
		this.paddleHalfWidth = PADDLE_DEFAULT_HALF_WIDTH;
		this.fallingPowerUps.clear();
		this.obtainedPowerUps.clear();
	}

	public void doContinue() {
		revertDefaults();
		balls.clear();
		balls.add(new Ball());
		toHit = -1;
		this.ballsLeft = 5;
		this.resetTicks = -1;
		this.toHit = levelLoader.loadLevel(field);
		addRepaintRegion(new Rectangle(0, 0, W, H));
	}

	public void reset() {
		revertDefaults();
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
		levelLoader.reset();
		this.resetTicks = -1;
		this.toHit = levelLoader.loadLevel(field);
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
			case KeyEvent.VK_N:
				resetTicks = 0;
				toHit = -1;
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

	public ArrayList<Ball> getBalls() {
		return this.balls;
	}

	public boolean hasPowerUp(PowerUp powerUp) {
		return obtainedPowerUps.contains(powerUp);
	}

	public PowerUp getLikePowerup(PowerUp powerUp) {
		int idx = obtainedPowerUps.indexOf(powerUp);
		if (idx > -1)
		{
			return obtainedPowerUps.get(idx);
		}
		return null;
	}

	public void obtainPowerUp(PowerUp powerUp) {
		this.obtainedPowerUps.add(powerUp);
	}

	public void setFlipped(boolean flipped) {
		this.addRepaintRegion(new Rectangle(0, 0, W, H));
		this.flip = flipped;
	}

	public void setPaddleHalfWidth(short width) {
		addRepaintRegion(new Rectangle(paddle - paddleHalfWidth, H - (H / 16), paddleHalfWidth << 1, (H - H / 2) / 16));
		this.paddleHalfWidth = width;
		addRepaintRegion(new Rectangle(paddle - paddleHalfWidth, H - (H / 16), paddleHalfWidth << 1, (H - H / 2) / 16));
	}

	public short getPaddleHalfWidth() {
		return this.paddleHalfWidth;
	}

	public int getBallsLeft() {
		return this.ballsLeft;
	}

	public void setBallsLeft(int ballsLeft) {
		this.ballsLeft = ballsLeft;
		addRepaintRegion(new Rectangle(0, H - (H >> 6), W, H >> 5));
	}

	public void setLevelLoader(LevelLoader levelLoader) {
		this.levelLoader = levelLoader;
		reset();
	}
}
