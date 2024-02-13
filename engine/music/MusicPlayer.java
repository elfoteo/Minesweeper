package engine.music;

import javax.sound.sampled.*;
import java.io.*;

public class MusicPlayer {
    private Clip clip;
    private FloatControl volumeControl;
    private boolean playing = false;

    public MusicPlayer(File soundtrackFile) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundtrackFile);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            playing = true;
        }
    }

    public void stop() {
        if (clip != null && playing) {
            clip.stop();
            playing = false;
        }
    }

    public void setVolumeToPercentage(float percentage) {
        if (volumeControl != null) {
            float maxVolume = volumeControl.getMaximum();
            float minVolume = volumeControl.getMinimum();

            // Calculate the volume value based on the percentage
            float targetVolume = minVolume + (maxVolume - minVolume) * percentage;

            // Ensure the target volume is within the valid range
            if (targetVolume < minVolume) {
                targetVolume = minVolume;
            } else if (targetVolume > maxVolume) {
                targetVolume = maxVolume;
            }

            // Set the volume to the calculated value
            volumeControl.setValue(targetVolume);
        }
    }

    public void changeSoundtrack(File newSoundtrackFile) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        stop();
        if (clip != null) {
            clip.close();
        }
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(newSoundtrackFile);
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);

        volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    }
}
