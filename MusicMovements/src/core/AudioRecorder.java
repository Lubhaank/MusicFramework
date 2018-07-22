package core;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * AudioRecorder is a FileLoader plugin for the music visualization framework. It prompts the user to specify
 * the lenghth of the recording they want to make, and then gives it to the framework as 'myRecordingWAV'.
 * This plugin is made in part by a a java recording example found at the following website:
 * http://www.codejava.net/coding/capture-and-record-sound-into-wav-file-with-java-sound-api
 */
public class AudioRecorder implements DataLoader{
    /**
     * A sample program is to demonstrate how to record sound in Java
     * author: www.codejava.net
     */

    // record duration, in milliseconds
    private long recordTime = 10000;

    // path of the wav file
    private File wavFile = new File("../files/myRecordingWAV_2.wav");;

    // format of audio file
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // the line from which audio data is captured
    private TargetDataLine line;

    @Override
    public File onSelect() {
        // Setting up JOptionPane for url and name input
        JTextField timeField = new JTextField();
        JPanel myPanel = new JPanel(new GridLayout(1, 2));
        myPanel.add(new JLabel("Length of recording (in seconds): "));
        myPanel.add(timeField);
        myPanel.setPreferredSize(new Dimension(400, 80));
        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "How long do you want to record? Default is 10 seconds.", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            recordTime = Long.parseLong(timeField.getText())*1000;
        }

        final AudioRecorder recorder = new AudioRecorder();

        // creates a new thread that waits for a specified
        // of time before stopping
        Thread stopper = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(recordTime);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                recorder.finish();
            }
        });

        stopper.start();

        // start recording
        recorder.start();
        return wavFile;
    }

    @Override
    public String toString() {
        return "Audio Recorder";
    }

    /**
     * Defines an audio format
     */
    private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
    }

    /**
     * Captures the sound and record into a WAV file
     */
    private void start() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();   // start capturing

            AudioInputStream ais = new AudioInputStream(line);

            // start recording
            AudioSystem.write(ais, fileType, wavFile);

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Closes the target data line to finish capturing and recording
     */
    private void finish() {
        line.stop();
        line.close();
        System.out.println("Finished");
    }
}
