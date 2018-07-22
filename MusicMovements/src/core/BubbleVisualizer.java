package core;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

public class BubbleVisualizer implements Visual {
    private MusicData song;
    private boolean stopped;
    private static final BucketData BUCKET_COUNT = BucketData.FIFTY;
    private static final int SMOOTHNESS = 5;
    private int numBuckets = BUCKET_COUNT.getValue();
    private org.knowm.xchart.XYChart chart;
    private SwingWrapper<XYChart> sw;
    private double[] yData;
    private double[] xData;
    private JFrame display;




    @Override
    public void load(MusicData song) {
        this.song = song;


    }

    @Override
    public void start() {
        stopped = false;
        yData = song.getFirstFrequency(BUCKET_COUNT); //get first frequencies
        xData = new double[numBuckets];
        for(int i = 0;i<numBuckets;i++){
            xData[i] = i;
        }
        chart = new XYChartBuilder().width(800).height(600).build();
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        chart.setTitle(song.toString());
        chart.getStyler().setChartTitleVisible(false);
        chart.getStyler().setMarkerSize(16);
        chart.getStyler().setChartBackgroundColor(Color.BLACK);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setPlotGridLinesVisible(false);
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setPlotBackgroundColor(Color.BLACK);
        chart.getStyler().setSeriesColors(new Color[] {Color.RED,Color.decode("#A00D00")});
        chart.getStyler().setXAxisTicksVisible(false);
        chart.getStyler().setYAxisTicksVisible(false);
        chart.addSeries("Gaussian Blob", xData,yData );
        sw = new SwingWrapper<XYChart>(chart);
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
        yData = song.getNextFrequency(); //Get next double arr of frequencies
        Random rand = new Random();
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {

                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();
                Color randomColor = new Color(r, g, b);
                chart.removeSeries("Gaussian Blob");
                chart.addSeries("Gaussian Blob", xData,yData );
                chart.getStyler().setSeriesColors(new Color[] {randomColor});
                sw.repaintChart();
            } catch (Exception e) {
            }
        });

    }


    @Override
    public void close() {
        display.dispose();

    }


    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public String toString(){
        return "Bubble Visualizer";
    }
}
