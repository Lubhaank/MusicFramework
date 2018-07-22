package core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class FrequencyVisualizer extends JFrame implements Visual {
    private MusicData song;
    private static final BucketData BUCKET_COUNT = BucketData.ONE_HUNDRED;
    private final int numBuckets = BUCKET_COUNT.getValue();
    private double[] frequencies;
    private Panel panel;
    private boolean stopped;

    @Override
    public void load(MusicData source) {
        song = source;
    }

    @Override
    public void start() {
        stopped = false;
        frequencies = song.getFirstFrequency(BUCKET_COUNT);
        panel = new Panel();
        add(panel);
        // Set windowListener so isStopped is true when the window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stopped = true;
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
        pack();
    }

    @Override
    public void drawNextFrame() {
        int panelWidth = panel.getWidth();
        int panelHeight = panel.getHeight();
        // Repaint changing part of the canvas
        panel.repaint(panelWidth / 2 - (panelWidth * 2 / 5), 0,
                panelWidth * 4 / 5, panelHeight * 7 / 8);
        if (!song.frequenciesDone()) frequencies = song.getNextFrequency();
        else stopped = true;
    }

    @Override
    public void close() { dispose(); }

    @Override
    public boolean isStopped() { return stopped; }

    @Override
    public String toString() { return "Bar Frequency Visualizer"; }

    /**
     * A class for the main panel containing the visualization
     */
    private class Panel extends JPanel {
        static final int INITIAL_WIDTH = 500;
        static final int INITIAL_HEIGHT = 400;
        private List<FreqBar> bars;
        Panel() {
            super();
            setPreferredSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));
            setOpaque(true);
            setBackground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 50));
            bars = new ArrayList<>();
            // Adds bars for each frequency bucket
            for (int i = 0; i < numBuckets; i++) {
                // Generate color gradient from Red (RGB 0xff0000) to Green (RGB 0x00ff00)
                int red = 255;
                int green = 0;
                int blue = 0;
                if (i > numBuckets / 2) red -= 255 * (i - numBuckets) / (numBuckets / 2);
                if (i < numBuckets / 2) green += 255 * i / (numBuckets / 2);
                else green = 255;

                bars.add(new FreqBar(Color.decode(String.format("#%02x%02x%02x", red, green, blue)), i));
            }
        }

        // This method must be overridden to draw custom shapes on the panel
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (FreqBar b: bars) b.paintFreqBar(g);
        }
    }

    /**
     * A class for the bars in the visualization
     */
    private class FreqBar {
        private int i;
        private Color color;

        FreqBar (Color c, int i) {
            color = c;
            this.i = i;
        }

        void paintFreqBar(Graphics g) {
            // make width, x, height, etc. relative to window size
            int maxHeight = panel.getHeight() * 7 / 8;
            int width = (int) ((double)(panel.getWidth() * 4 / 5) / numBuckets);
            int x = panel.getWidth() / 2 - ((numBuckets / 2 - i) * width);
            int height;
            final int y = panel.getHeight() * 7 / 8;
            // make height is a function of frequency
            double intensity = frequencies[i] * 5;
            height = (int) (intensity * maxHeight);
            // Graphics g draws shapes
            g.setColor(color);
            g.fillRect(x, y - height, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(x, y - height, width, height);
        }
    }
}
