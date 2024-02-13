package engine.music;

import engine.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicManager {
    private static MusicPlayer musicPlayer;
    private static List<File> musicFilePaths;

    public static void register() {
        File soundsDir = new File(Constants.soundsDir);
        File[] soundFiles = soundsDir.listFiles();

        if (soundFiles != null) {
            musicFilePaths = new ArrayList<>();
            for (File soundFile : soundFiles) {
                if (soundFile.isFile()) {
                    musicFilePaths.add(soundFile);
                }
            }
        }

        // Initialize the MusicPlayer with the first soundtrack, if available
        if (!musicFilePaths.isEmpty()) {
            musicPlayer = new MusicPlayer(musicFilePaths.get(0));
        }
    }

    public static List<File> getSoundtracks(){
        return musicFilePaths;
    }

    public static MusicPlayer getMusicPlayer() {
        return musicPlayer;
    }
}
