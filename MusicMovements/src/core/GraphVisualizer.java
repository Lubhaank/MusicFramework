package core;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.style.XYStyler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GraphVisualizer implements Visual {

    private MusicData song;
    private static final BucketData BUCKET_COUNT = BucketData.ONE_HUNDRED;
    private int numBuckets = BUCKET_COUNT.getValue();
    private double[] yData;
    private double[] xData;
    private double[] negYdata;
    private org.knowm.xchart.XYChart chart;
    private SwingWrapper<XYChart> sw;
    private XYStyler style;
    private JFrame display;
    private boolean stopped;

    @Override
    public void load(MusicData source) {
        song = source; //Save Song
    }

    @Override
    public void start() {
        stopped = false;
        yData = song.getFirstFrequency(BUCKET_COUNT); //get first frequencies
        xData = new double[numBuckets];
        for(int i = 0;i<numBuckets;i++){
            xData[i] = i;
        }
        // Create Chart from XChart
        chart = QuickChart.getChart("Frequency Visualizer", "Frequency Bucket",
                "Amplitude", "freqPos", xData, yData);
        negYdata = negate(yData);
        chart.addSeries("freqNeg",xData,negYdata);
        style = chart.getStyler();//Style Chart
        style.setChartTitleFont(new Font("Lato",Font.PLAIN,36));
        style.setChartFontColor(Color.WHITE);
        style.setChartBackgroundColor(Color.BLACK);
        style.setPlotBorderVisible(false);
        style.setAxisTicksMarksVisible(false);
        style.setAxisTickLabelsColor(Color.BLACK);
        style.setMarkerSize(0);
        style.setPlotGridLinesVisible(false);
        style.setLegendVisible(false);
        style.setPlotBackgroundColor(Color.BLACK);
        style.setAxisTitlePadding(0);
        style.setChartFontColor(Color.BLACK);
        style.setSeriesColors(new Color[] {Color.RED,Color.decode("#A00D00")});
        style.setXAxisTicksVisible(false);
        style.setYAxisTicksVisible(false);

        // Show the chart
        sw = new SwingWrapper<XYChart>(chart);

        Thread t = new Thread(()-> {

            display = sw.displayChart();
            display.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            display.addWindowListener(new WindowAdapter() { //Checking when window closed
                @Override
                public void windowClosed(WindowEvent e) {
                    stopped = true;
                }
            });
        });
        t.start();
    }

    /**
     * Updates the visualization by drawing the next frame.
     */
    @Override
    public void drawNextFrame() {
        yData = song.getNextFrequency(); //Get next double arr of frequencies
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                negYdata = negate(yData); //Negate the data for other side of graph and add series to chart

                chart.updateXYSeries("freqNeg", null, negYdata, null);
                chart.updateXYSeries("freqPos", null, yData, null);
                sw.repaintChart();
            } catch (Exception e) {
            }
        });
    }

    @Override
    public void close() {//Closing of window
        display.dispose();
    }

    private double[] negate(double[] arr) {
        double[] neg = new double[arr.length];
        for(int i = 0;i<arr.length;i++){
            neg[i] = arr[i]*-.7;
        }
        return neg;
    }

    @Override
    public boolean isStopped() {//Check if song is stopped
        return stopped;
    }

    @Override
    public String toString(){
        return "Graph Visualizer";
    }
}
