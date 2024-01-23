package com.vulpovile.games.brickblaster.beep;

import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class BeepSoundSystem {

	public static final float SAMPLE_RATE = 8000f;
	public static final int SAMPLE_COUNT = (int) Math.ceil(SAMPLE_RATE/1000);
	private static BeepSoundSystem instance = null;

	private boolean running = true;

	private final Channel[] channels;

	public BeepSoundSystem(int channels) {
		this.channels = new Channel[channels];
		for (int i = 0; i < channels; i++)
		{
			this.channels[i] = new Channel();
			this.channels[i].init();
		}
		instance = this;
	}

	public static BeepSoundSystem getLastInstance() {
		return instance;
	}

	public void destroy() {
		running = false;
		for (int i = 0; i < channels.length; i++)
		{
			this.channels[i].destroy();
		}
	}

	public void note(int hz, int msecs, double vol, int channel) {
		this.channels[channel].note(hz, msecs, vol);
	}

	private class Channel implements Runnable {
		private BlockingQueue<Note> noteQueue = null;
		private SourceDataLine sdl = null;
		private Thread parentThread;

		public void init() {
			if (sdl == null)
			{
				try
				{
					noteQueue = new LinkedBlockingQueue<Note>();
					AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN));
					sdl = AudioSystem.getSourceDataLine(af);
					sdl.open(af);
					sdl.start();
					parentThread = new Thread(this);
					parentThread.start();
				}
				catch (LineUnavailableException ex)
				{
					noteQueue = null;
					ex.printStackTrace();
				}
			}
		}

		public void destroy() {
			if(parentThread != null)
				parentThread.interrupt();
			noteQueue = null;
			if (sdl != null)
			{
				sdl.drain();
				sdl.stop();
				sdl.close();
			}
		}

		public void note(int hz, int msecs, double vol) {
			if (noteQueue != null)
			{
				noteQueue.add(new Note(hz, msecs, vol));
			}
		}

		private void tone(int hz, int msecs, double vol) throws LineUnavailableException {
			if (sdl != null)
			{
				byte[] buf = new byte[1];
				for (int i = 0; i < msecs * SAMPLE_COUNT; i++)
				{
					double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
					buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
					sdl.write(buf, 0, 1);
				}
				sdl.drain();
			}
		}

		public void run() {
			while (running)
			{
				try
				{
					Note note = noteQueue.take();
					if (note != null)
					{
						try
						{
							tone(note.hz, note.msecs, note.vol);
						}
						catch (LineUnavailableException e)
						{
							e.printStackTrace();
						}
					}
				}
				catch (InterruptedException e1)
				{
				}
			}
		}
	}
}

class Note {
	public final int hz;
	public final int msecs;
	public final double vol;

	public Note(int hz, int msecs, double vol) {
		this.hz = hz;
		this.msecs = msecs;
		this.vol = vol;
	}
}
