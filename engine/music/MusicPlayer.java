package engine.music;

import javax.sound.sampled.*;
import java.io.*;

/**
 * The MusicPlayer class handles the playback of music in the game engine.
 */
public class MusicPlayer {
    private Clip clip; // The audio clip for playback
    private FloatControl volumeControl; // Control for adjusting volume
    private boolean playing = false; // Flag to indicate if music is currently playing

    /**
     * Constructs a new MusicPlayer with the specified soundtrack file.
     * @param soundtrackFile The file containing the soundtrack to be played.
     */
    public MusicPlayer(File soundtrackFile) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundtrackFile);
            clip = AudioSystem.getClip(); // Initialize the audio clip
            clip.open(audioInputStream); // Open the audio stream

            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN); // Get the volume control
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ignore) {
            // Exception handling, ignoring for simplicity
        }
    }

    /**
     * Starts playback of the soundtrack.
     */
    public void play() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the clip continuously
            clip.start(); // Start playback
            playing = true; // Update playing status
        }
    }

    /**
     * Stops playback of the soundtrack.
     */
    public void stop() {
        if (clip != null && playing) {
            clip.stop(); // Stop playback
            playing = false; // Update playing status
        }
    }

    /**
     * Sets the volume of the soundtrack to the specified percentage.
     * @param percentage The percentage of the maximum volume (0.0 to 1.0).
     */
    public void setVolumeToPercentage(float percentage) {
        if (volumeControl != null) {
            float maxVolume = volumeControl.getMaximum();
            float minVolume = volumeControl.getMinimum();

            // Calculate the target volume based on the percentage
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

    /**
     * Changes the current soundtrack to the one specified by the new soundtrack file.
     * @param newSoundtrackFile The file containing the new soundtrack.
     */
    public void changeSoundtrack(File newSoundtrackFile) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        stop(); // Stop playback of the current soundtrack
        if (clip != null) {
            clip.close(); // Close the current clip
        }
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(newSoundtrackFile); // Get the audio input stream for the new soundtrack
        clip = AudioSystem.getClip(); // Initialize a new clip
        clip.open(audioInputStream); // Open the audio stream for the new clip

        volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN); // Get the volume control for the new clip
    }
}
