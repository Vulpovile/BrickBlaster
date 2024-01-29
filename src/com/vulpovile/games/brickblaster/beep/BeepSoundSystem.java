package com.vulpovile.games.brickblaster.beep;

import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class BeepSoundSystem {
	public final static byte TYPE_SQUARE = 0;
	public final static byte TYPE_SINE = 1;
	public final static byte TYPE_NOISE = 2;
	public final static byte TYPE_GRATE = 4;

	public static final float SAMPLE_RATE = 14000f;
	public static final int SAMPLE_COUNT = (int) Math.ceil(SAMPLE_RATE / 1000);
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
		this.channels[channel].note(hz, msecs, (float) vol, TYPE_SINE);
	}

	public void note(int hz, int msecs, double vol, int channel, byte type) {
		this.channels[channel].note(hz, msecs, (float) vol, type);
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
			if (parentThread != null)
				parentThread.interrupt();
			noteQueue = null;
			if (sdl != null)
			{
				sdl.drain();
				sdl.stop();
				sdl.close();
			}
		}

		public void note(int hz, int msecs, float vol, byte type) {
			if (noteQueue != null)
			{
				noteQueue.add(new Note(hz, msecs, vol, type));
			}
		}

		private void sine(int hz, int msecs, float vol) throws LineUnavailableException {
			if (sdl != null)
			{
				byte[] buf = new byte[1];
				for (int i = 0; i < msecs * SAMPLE_COUNT; i++)
				{
					float angle = i / (SAMPLE_RATE / hz) * 2.0F * 3.14159F;
					buf[0] = (byte) (Math.sin(angle) * 127.0F * vol);
					sdl.write(buf, 0, 1);
				}
			}
		}

		private void square(int hz, int msecs, float vol) throws LineUnavailableException {
			if (sdl != null)
			{
				byte[] buf = new byte[1];
				for (int i = 0; i < msecs * SAMPLE_COUNT; i++)
				{
					float angle = i / (SAMPLE_RATE / hz) * 2.0F * 3.14159F;
					buf[0] = (byte) (Math.signum(Math.sin(angle)) * 127.0F * vol);
					sdl.write(buf, 0, 1);
				}
			}
		}

		byte reverse(int b) {
			   	b = (b & 0xF0) >> 4 | (b & 0x0F) << 4;
				b = (b & 0xCC) >> 2 | (b & 0x33) << 2;
				b = (b & 0xAA) >> 1 | (b & 0x55) << 1;
				return (byte)b;
		}

		private void grate(int hz, int msecs, float vol) throws LineUnavailableException {
			if (sdl != null)
			{
				byte[] buf = new byte[1];
				for (int i = 0; i < msecs * SAMPLE_COUNT; i++)
				{
					float angle = i / (SAMPLE_RATE / hz) * 2.0F * 3.14159F;
					buf[0] = reverse((byte) (Math.sin(angle) * 127.0F * vol));
					sdl.write(buf, 0, 1);
				}
			}
		}
		
		private void noise(int hz, int msecs, float vol) throws LineUnavailableException {
			//TODO implement
			try
			{
				Thread.sleep(msecs);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
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
							switch (note.type) {
								case TYPE_SQUARE:
									square(note.hz, note.msecs, note.vol);
									break;
								case TYPE_SINE:
									sine(note.hz, note.msecs, note.vol);
									break;
								case TYPE_NOISE:
									noise(note.hz, note.msecs, note.vol);
									break;
								case TYPE_GRATE:
									grate(note.hz, note.msecs, note.vol);
									break;
							}
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

	public class Note {

		public final int hz;
		public final int msecs;
		public final float vol;
		public final byte type;

		public Note(int hz, int msecs, float vol, byte type) {
			this.hz = hz;
			this.msecs = msecs;
			this.vol = vol;
			this.type = type;
		}
	}
}
