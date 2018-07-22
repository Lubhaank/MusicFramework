package gui;


import core.DataLoader;
import core.Framework;
import core.Visual;
import core.DataLoader;
import javax.swing.*;
import java.util.Iterator;
import java.util.ServiceLoader;


public class Main {

    public static void main(String[] args){

        Framework framework = new Framework();

        //Load Data Plugins Using Service Loader


        Iterator<core.DataLoader> dataPlugins = ServiceLoader.load(core.DataLoader.class).iterator();

        while (dataPlugins.hasNext()) framework.addDataPlugin(dataPlugins.next());
        //Load Visualizer Plugins Using Service Loader
        Iterator<Visual> displayPlugins = ServiceLoader.load(Visual.class).iterator();
        while (displayPlugins.hasNext()) framework.addVisualizer(displayPlugins.next());
        SwingUtilities.invokeLater(() -> {
            new GUI(framework);
        });
    }
}
