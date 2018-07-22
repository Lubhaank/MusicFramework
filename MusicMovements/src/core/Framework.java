package core;

import javazoom.jl.converter.Converter;

import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Framework {
    public static final AudioFormat WAV;
    public static final int FPS = 50;
    private static final int ONE_SECOND = 1000;
    private final List<MusicData> library;
    private Clip currentClip;
    private Visual currentVisualizer;
    private List<DataLoader> dataPlugins;
    private List<Visual> visualizers;
    private MusicData selectedSong;
    private Timer visTimer;

    static {
        WAV = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float)44100,
                16, 1, 2, (float)44100, false);
    }
    /**
     * Instantiates a new Framework
     */
    public Framework() {
        library = new ArrayList<>();
        dataPlugins = new ArrayList<>();
        visualizers = new ArrayList<>();
        File wavs = new File("files/");
        File[] oldFiles = wavs.listFiles();
        if (oldFiles == null || oldFiles.length <= 0) return;
        for (File file: oldFiles) {
            String name = file.getName();
            if (file.isDirectory() || !name.contains(".wav")) continue;
            try {
                library.add(new MusicData(file, file.getName().substring(0, file.getName().length() - 4)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addDataPlugin(DataLoader dp) { dataPlugins.add(dp); }

    public List<DataLoader> getDataPlugins() { return dataPlugins; }

    public void addVisualizer(Visual v) { visualizers.add(v); }

    public List<Visual> getVisualizers() { return visualizers; }

    public Visual getCurrentVisualizer() { return currentVisualizer; }

    public MusicData getSelectedSong() { return selectedSong; }

    /**
     * Uses the selected core.DataLoader to add a song to the library
     * @param index the index of the selected core.DataLoader
     * @return      whether the core.DataLoader successfully added a song to the library
     */
    public boolean addSong(int index) {
        DataLoader loader = dataPlugins.get(index);
        File file = loader.onSelect();
        if (file == null) return false;
        String filename = file.getName();
        String name = filename.substring(0, filename.length() - 4);
        try {
            AudioInputStream sourceStream;
            AudioInputStream stream;
            String wavFilename = name + ".wav";
            File wavFile = new File("../../files/" + wavFilename);
            if (wavFile.exists()) return true;
            if (filename.contains("mp3")) { // If it's an mp3 file
                new Converter().convert(file.getAbsolutePath(), wavFile.getAbsolutePath());
                sourceStream = AudioSystem.getAudioInputStream(wavFile);
                if (!AudioSystem.isConversionSupported(sourceStream.getFormat(), WAV))
                    return false;
                stream = AudioSystem.getAudioInputStream(WAV, sourceStream);
            } else if (filename.contains("wav")) { // If it's a wav file
                sourceStream = AudioSystem.getAudioInputStream(file);
                if (!AudioSystem.isConversionSupported(sourceStream.getFormat(), WAV))
                    return false;
                stream = AudioSystem.getAudioInputStream(WAV, sourceStream);
            } else return false; // Not a supported format
            if (!wavFile.exists()) AudioSystem.write(stream, AudioFileFormat.Type.WAVE, wavFile);
            library.add(new MusicData(wavFile, name));
            sourceStream.close();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Sets the currently selected song
     * @param index the index of the selection
     */
    public void loadSong(int index) { selectedSong = library.get(index); }

    /**
     * Whether a song has been loaded or not
     * @return whether a song is loaded
     */
    public boolean songLoaded() { return selectedSong != null; }

    /**
     * Plays the specified song with the specified visualizer
     * @param index the int index of the visualizer
     * @return      whether the song was successfully played
     */
    public boolean play(int index) {
        if (currentClip != null || currentVisualizer != null || selectedSong == null) return false;
        currentVisualizer = visualizers.get(index);
        try {
            currentClip = AudioSystem.getClip();
            currentClip.open(selectedSong.getStream());
            currentVisualizer.load(selectedSong);
            currentVisualizer.start();
            currentClip.start();
            currentVisualizer.drawNextFrame();
            startVisTimer();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void startVisTimer() {
        visTimer = new Timer();
        visTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!currentClip.isActive() || currentVisualizer.isStopped()) stop();
                else currentVisualizer.drawNextFrame();
            }
        }, (long) 100, (long) ONE_SECOND / FPS);
    }

    /**
     * Stops the current visualizer, if there is one
     * @return whether the visualization was successfully stopped
     */
    public boolean stop() {
        if (currentClip == null || currentVisualizer == null) return false;
        visTimer.cancel();
        currentClip.stop();
        currentClip = null;
        currentVisualizer.close();
        currentVisualizer = null;
        return true;
    }

    public List<MusicData> getLibrary() { return library; }



}
