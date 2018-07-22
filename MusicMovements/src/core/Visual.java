package core;

/**
 * The visual interface used by visual plugins to register and implement visualizations.
 * Opens a new window and displays the visualization.
 */

public interface Visual {

    /**
     * Loads the song
     * @param song the MusicData object for the source song
     */
    void load(MusicData song);

    /**
     * Begins the visualization by initializing the new window and drawing any backgrounds.
     * Should call song.getFirstFrequency(BucketCount.XXXXX) or song.getFirstAmplitude()
     */
    void start();

    /**
     * Updates the visualization by drawing the next frame.
     * Use song.getNextFrequency() or song.getNextAmplitude() to get updated Frequencies or Amplitude
     */
    void drawNextFrame();

    /**
     * Closes the currently running visualization and window.
     * Called by Framework, not visualizer
     */
    void close();

    /**
     * Tells whether the visualization is stopped.
     * A visualization is stopped if its window has been closed or all frames have been drawn (the song has finished).
     * @return whether this visualization is stopped.
     */
    boolean isStopped();

    /**
     * A String representation of the plugin's name
     * @return the plugin's name
     */
    @Override
    String toString();


}
