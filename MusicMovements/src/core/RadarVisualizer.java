package core;

import org.knowm.xchart.RadarChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.RadarStyler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RadarVisualizer implements Visual {
    private MusicData song;
    private static final BucketData BUCKET_COUNT = BucketData.FIFTY;
    private static final int SMOOTHNESS = 5;
    private int numBuckets = BUCKET_COUNT.getValue();
    private org.knowm.xchart.RadarChart chart;
    private SwingWrapper<RadarChart> sw;
    private double[] frequencies;
    private String[] arr;
    private boolean stopped;
    private JFrame display;
    private double offset;
    private RadarStyler style;
    private double[] amps;
    private int ampIndex = 0;

    @Override
    public void load(MusicData song) {
        this.song = song; //Save song passed in as a local variable. Used to visualize
    }

    @Override
    public void start() {
        amps = new double[SMOOTHNESS];
        amps[0] = song.getFirstAmplitude(); //Initialize all amplitudes
        stopped = false;
        frequencies = song.getFirstFrequency(BUCKET_COUNT); //Initialize all frequencies and number of buckets
        //Make chart from XCHART API
        makeChart();
        sw = new SwingWrapper<>(chart);
        Thread t = new Thread(()-> {
            display = sw.displayChart();
            display.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            display.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    stopped = true;
                }
            });
        });
        t.start();
    }

    @Override
    public void drawNextFrame() {
        ampIndex++;
        if(!song.amplitudesDone()){//tries to smooth color transitions
            if(ampIndex>=SMOOTHNESS)ampIndex = 0;
            amps[ampIndex] = song.getNextAmplitude();
        }
        style.setStartAngleInDegrees(style.getStartAngleInDegrees() + .1);
        if (!song.frequenciesDone()) frequencies = song.getNextFrequency();
        else stopped = true;
        //Math to calculate size of chart
        double max = 0;
        for(int i = 0;i<frequencies.length;i++) {
            if (frequencies[i] > max) max = frequencies[i];
        }
        if (max / 2 > offset) offset = max / 2;
        else offset -= .003;
        for(int i = 0;i<frequencies.length;i++){
            frequencies[i] += offset;
            if(frequencies[i]>1)frequencies[i]=1;
        }
        double finalAmp = average(amps);
        SwingUtilities.invokeLater(() -> {
            try {
                RadarStyler s = chart.getStyler();
                Color c = new Color(255, 50, (int)((finalAmp) * 255)); // Color the chart based off amplitude
                s.setSeriesColors(new Color[] {c});
                chart.removeSeries("freqs");
                chart.addSeries("freqs", frequencies, arr);//Redraw graph
                sw.repaintChart();
            } catch (Exception e) {
            }
        });
    }

    private double average(double[] arr){
        double sum = 0;
        double count = 0;
        for (double d:arr) {
            sum+=d;
            if(d!=0)count++;
        }
        return sum * 1.0 /count;
    }

    @Override
    public void close() {
        display.dispose();//Must close window on close
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    private void makeChart(){
        // Create Chart with style
        chart = new org.knowm.xchart.RadarChart(800, 600);
        chart.setTitle(song.toString());
        style = chart.getStyler();
        style.setChartTitleFont(new Font("Lato",Font.PLAIN,36));
        style.setChartFontColor(Color.WHITE);
        style.setChartBackgroundColor(Color.BLACK);
        style.setPlotBorderVisible(false);
        style.setAxisTicksMarksVisible(false);
        style.setAxisTitleVisible(false);
        style.setMarkerSize(0);
        style.setPlotGridLinesVisible(false);
        style.setLegendVisible(false);
        style.setPlotBackgroundColor(Color.BLACK);

        // Series
        arr = new String[numBuckets];
        for(int i = 0;i<numBuckets;i++){
            arr[i] = Integer.toString(i);
        }
        chart.setVariableLabels(arr);
        chart.addSeries("freqs", frequencies, arr);
    }

    @Override
    public String toString() { return "Radar Chart Visualizer"; }
}
