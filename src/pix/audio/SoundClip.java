package pix.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundClip {
	
	private Clip clip;
	private FloatControl gain;
	
	public SoundClip(String file) {
		try {
			InputStream input = SoundClip.class.getResourceAsStream(file);
			InputStream bufferedIn = new BufferedInputStream(input);
			AudioInputStream ais = AudioSystem.getAudioInputStream(bufferedIn);
			clip = AudioSystem.getClip();
			clip.open(ais);
			
			gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
		
	}
	
	public void play() {
		if(clip == null)
			return;
		
		stop();
		clip.setFramePosition(0);
		while(!clip.isRunning()) {
			clip.start();
		}
	}
	
	public void stop() {
		if(clip.isRunning())
			clip.stop();
	}
	
	public void close() {
		stop();
		clip.drain();
		clip.close();
	}
	
	public void loop() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
		play();
	}
	
	public void setGain(float value) {
		gain.setValue(value);
	}
	
	public boolean isRunning() {
		return clip.isRunning();
	}
	
}
