package com.vulpovile.games.brickblaster;

import java.util.concurrent.locks.LockSupport;

public class GameTickThread implements Runnable {
	private final GamePanel panel;
	private boolean running = true;
	private final long sleepNanos;
	
	//private long nextTick = 0;

	GameTickThread(GamePanel panel, long sleepNanos) {
		this.panel = panel;
		this.sleepNanos = sleepNanos;
	}

	public void end() {
		this.running = false;
	}

	public void run() {
		while (running) {
			//Busy wait, not the most excellent idea
			/*while(System.nanoTime() < nextTick)
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			nextTick = System.nanoTime() + sleepNanos;
			panel.tick();*/
			long nanosLost = System.nanoTime();
			panel.tick();
			nanosLost -= System.nanoTime();
			if(sleepNanos+nanosLost > 0)
				LockSupport.parkNanos(sleepNanos+nanosLost);
		}
	}
}