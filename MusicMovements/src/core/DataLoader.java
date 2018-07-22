package core;

import java.io.File;

/**
 * The data loader interface used by data plugins to register and implement data loading.
 */

public interface DataLoader {


    /**
     * Returns a File object that is a WAV or MP3 File.
     *
     * For example, this method could open a Swing JOptionPane that allows the
     * user to enter a URL, and the loader downloads the file from the URL, and returns the File.
     *
     * @return a File object for an mp3 or wav file, or null if an exception occurs or there is some other
     *         error while opening the file
     */
    File onSelect();

    /**
     * A String representation of the plugin's name
     *
     * @return the plugin's name
     */
    @Override
    String toString();
}
