package core;

import org.jtransforms.fft.DoubleFFT_1D;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.DoubleStream;

import static org.apache.commons.math3.util.FastMath.sqrt;


public class MusicData {

    private static final int FFT_N = 2048;
    private final File file;
    private AudioFormat format;
    private AudioInputStream stream;
    private String name;
    private double[] samples;
    private int frameLength;

    private int nextAmplitude;
    private double[] amplitudes;
    private int nextFreq;
    private BucketData numBuckets;
    private Map<Integer, double[]> frequencies;

    /**
     * Creates a new MusicData object
     *
     * @param file          the File to the WAV file for this song
     * @param name          the String name of the song
     * @throws IOException  if the stream made from the file is not valid
     *                      or there is some other error in reading the stream
     */
    public MusicData(File file, String name) throws IOException {
        this.file = file;
        this.name = name;
        stream = extractStream();
        this.format = stream.getFormat();
        frameLength = (int) stream.getFrameLength();
        initSamples();
        stream.close();
    }

    private AudioInputStream extractStream() {
        try {
            return AudioSystem.getAudioInputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initSamples() throws IOException {
        int frameSize = format.getFrameSize();
        int size = frameLength * frameSize;
        int channels = format.getChannels();
        byte[] bytes = new byte[size];
        int numRead = stream.read(bytes);
        if (numRead != size || stream.available() > 0) throw new IOException();
        samples = new double[frameLength];
        for (int j = 0; j < frameLength * frameSize; j += frameSize) {
            short value = 0;
            for (int k = 0; k < channels; k++) {
                value += (short) ((bytes[j + 2 * k] & 0xff) + ((bytes[j + 2 * k + 1] & 0xff) << 8));
            }
            samples[j / frameSize] = value;
        }
    }

    /**
     * Calculates the amplitudes of each sample of the song and returns the first amplitude.
     * @return the amplitude of the first sample as a double
     */
    public double getFirstAmplitude() {
        nextAmplitude = 0;
        calcAmplitudes();
        return getNextAmplitude();
    }

    /**
     * Gets the amplitude of the next sample, relative to the max amplitude in the song, scaled out of 1.
     * Must call getFirstAmplitude before this.
     * @return the amplitude as a double
     */
    public double getNextAmplitude() {
        double result = amplitudes[nextAmplitude];
        nextAmplitude++;
        return result;
    }

    /**
     * Returns true if all amplitude values have been accessed
     * @return whether all amplitudes have been seen
     */
    public boolean amplitudesDone() { return nextAmplitude == amplitudes.length; }

    /**
     * Gets all amplitudes for the song at once
     * @return an array of doubles that represent the amplitude at each frame
     */
    public double[] getAllAmplitudes() {
        if (amplitudes == null) getFirstAmplitude();
        return Arrays.copyOf(amplitudes, amplitudes.length);
    }

    private void calcAmplitudes() {
        DoubleStream stream = Arrays.stream(samples);
        amplitudes = stream.map(Math::abs).toArray();
        separateIntoFrames();
        scaleValuesToMax(amplitudes);
    }

    private void separateIntoFrames() {
        int samplesPer = ((int) getSampleRate()) / Framework.FPS;
        int newLength = getFrameLength() / samplesPer;
        int numFrames = amplitudes.length;
        double[] newAmp = new double[newLength];
        for (int i = 0; i < newLength; i ++) {
            int range = samplesPer;
            if (samplesPer * (i + 1) > numFrames) range = numFrames - samplesPer * i;
            double[] section = Arrays.copyOfRange(amplitudes, i * samplesPer, i * samplesPer + range);
            newAmp[i] = max(section);
        }
        amplitudes = newAmp;
    }

    /**
     * Scales all values in the array to a fraction of the max value.
     * @param values the array to operate on
     */
    public static void scaleValuesToMax(double[] values) {
        double max = 0;
        for (double val : values) if (val > max) max = val;
        for (int i = 0; i < values.length; i++) values[i] /= max;
    }

    /**
     * Calculates the frequencies at each frame of the song and returns the first set of frequencies
     * @param buckets the number of frequency "buckets" to separate the signal into.
     *                Buckets are approximately evenly distributed across the musical scale and
     *                between approximately 0Hz and 22kHz, above the upper limit of human hearing.
     * @return        an array representing the frequencies represented in the first frame
     */
    public double[] getFirstFrequency(BucketData buckets) {
        numBuckets = buckets;
        nextFreq = 0;
        calcFrequencies();
        return getNextFrequency();
    }

    /**
     * Gets the frequencies of the next frame, separated into buckets between approximately 0Hz and 22kHz.
     * The 0th bucket is at index 0, the 1st at index 1, and so on.
     * The values in the buckets represent magnitude of the frequency relative to the max in the song, scaled out of 1.
     * Must call getFirstFrequency before this.
     * @return an array representing the frequencies represented in the frame.
     */
    public double[] getNextFrequency() {
        double[] result = frequencies.get(nextFreq);
        nextFreq++;
        return result;
    }

    /**
     * Returns true if all frequency values have been accessed
     * @return whether all frequency values have been seen
     */
    public boolean frequenciesDone() { return nextFreq == frequencies.size(); }

    private void calcFrequencies() {
        float fs = getSampleRate();
        frequencies = new HashMap<>();
        DoubleFFT_1D fft = new DoubleFFT_1D(FFT_N);
        int samplesPerFrame = ((int) fs) / Framework.FPS;
        int numFrames = samples.length;
        for (int i = 0; i < numFrames; i += samplesPerFrame) {
            // windowing
            int windowSize = samplesPerFrame;
            if (i + samplesPerFrame > numFrames) windowSize = numFrames - i;
            double[] windowedSamples = Arrays.copyOfRange(samples, i, i + windowSize);
            double[] periodicSamples = new double[2 * FFT_N];
            // extend to make periodic
            copyToFill(windowedSamples, periodicSamples);
            fft.realForwardFull(periodicSamples);
            double[] freq = sortIntoBuckets(periodicSamples);
            frequencies.put(i / samplesPerFrame, freq);
        }
        scaleFreqToMax();
    }

    private void copyToFill(double[] src, double[] dest) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] = src[i % src.length];
        }
    }

    private double[] sortIntoBuckets(double[] samples) {
        int offset = 1;
        int maxI = FFT_N / 2 + 2 * offset; // 44.1kHz sample rate
        // calculate magnitudes
        for (int i = 0; i < maxI; i += 2) {
            samples[i / 2] = sqrt(samples[i] * samples[i] + samples[i + 1] * samples[i + 1]);
        }
        double[] result = new double[numBuckets.getValue()];
        double multiplier = numBuckets.getMultiplier();
        double realRange = 1;
        int j = offset;
        int range = 1;
        maxI = maxI / 2;
        for (int i = 0; i < numBuckets.getValue(); i++) {
            if (j + range > maxI) range = maxI - j;
            double[] section = Arrays.copyOfRange(samples, j, j + range);
            result[i] = max(section);

            j += range;
            realRange *= multiplier;
            range = (int) realRange;
        }
        return result;
    }

    private static double max(double[] array) {
        double max = 0;
        for (double d: array) if (d > max) max = d;
        return max;
    }

    private void scaleFreqToMax() {
        double max = 0;
        for (Map.Entry<Integer, double[]> entry: frequencies.entrySet()) {
            double[] value = entry.getValue();
            for (double aValue : value) {
                if (aValue > max) max = aValue;
            }
        }
        for (Map.Entry<Integer, double[]> entry: frequencies.entrySet()) {
            double[] value = entry.getValue();
            for (int i = 0; i < value.length; i++) {
                value[i] = (value[i] / max);
            }
        }
    }

    /**
     * Gets the length of the song, in seconds
     * @return the length
     */
    public int getLength() { return (int) (frameLength / format.getFrameRate()); }

    float getSampleRate() {
        return format.getSampleRate();
    }

    int getFrameLength() { return frameLength; }

    AudioInputStream getStream() { return extractStream(); }

    /**
     * Gets the raw audio samples for analysis
     * @return the array of samples as doubles
     */
    public double[] getSamples() { return Arrays.copyOf(samples, samples.length); }

    @Override
    public String toString() { return name; }

    /**
     * Gets the number of frames drawn over the course of the entire song
     * @return the number of frames
     */
    public int getNumberOfFrames() { return (int) (getFrameLength() * Framework.FPS / getSampleRate()); }

    Map<Integer, double[]> getFrequencies() { return frequencies; }


}
