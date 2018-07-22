package gui;

import core.DataLoader;
import core.Framework;
import core.MusicData;
import core.Visual;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GUI extends JFrame{

    private static final Color BG_COLOR = Color.decode("#5AB9EA");
    private static final Color BG_COLOR_2 = Color.decode("#FFFFFF");
    private static final Color BUTTON_FG_COLOR = Color.decode("#000000");
    private static final Color BUTTON_BG_COLOR = Color.decode("#8860D0");
    private final Framework framework;
    private List<DataLoader> dataPluginsList;
    private List<MusicData> librarySongsList;
    private List<Visual> visualizerPluginsList;

    private JList dataPluginsJLIST;
    private JList libraryJLIST;
    private JList visualizerJLIST;

    private JPanel dataPlugins;
    private JPanel visualizerPlugins;
    private JPanel libraryPanel;

    private MusicData currentSong;
    private JLabel currentSongLabel;

    private JLabel status;

    private static final Border PANEL_BORDER = BorderFactory.createEmptyBorder(10, 10, 10, 10);

    /**
     * Creates the GUI for the framework
     * @param framework the framework object
     */
    public GUI(Framework framework){
        this.framework = framework;
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1000,800));
        setLayout(new BorderLayout());

        //Header
        JPanel header = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Welcome to the Music Visualizer",SwingConstants.CENTER);
        headerLabel.setFont(new Font("Lato", Font.BOLD, 36));
        header.add(headerLabel,BorderLayout.BEFORE_FIRST_LINE);
        header.setBackground(BG_COLOR);

        status = new JLabel("",SwingConstants.CENTER);
        status.setPreferredSize(new Dimension(300,35));
        status.setFont(new Font("Lato",Font.PLAIN,16));
        header.add(status,BorderLayout.AFTER_LAST_LINE);

        add(header,BorderLayout.BEFORE_FIRST_LINE);


        //GET DATA SOURCES :)
        dataPlugins = getDataPanel();
        add(dataPlugins,BorderLayout.WEST);


        //GET VISUALIZERS :)
        visualizerPlugins = getVisPanel();
        add(visualizerPlugins,BorderLayout.EAST);


        //GET LIBRARY
        libraryPanel = getLibPanel();
        add(libraryPanel,BorderLayout.CENTER);


        //Make current song stuffs
        JPanel currentStuff = new JPanel(new BorderLayout());
        currentStuff.setPreferredSize(new Dimension(900,100));
        currentSongLabel = new JLabel();
        currentSongLabel.setFont(new Font("Lato",Font.BOLD,30));
        currentSongLabel.setHorizontalAlignment(0);
        JLabel sel = new JLabel("Selected Song: ",SwingConstants.CENTER);
        sel.setFont(new Font("Lato",Font.PLAIN,24));
        currentStuff.add(sel,BorderLayout.BEFORE_FIRST_LINE);
        currentStuff.add(currentSongLabel,BorderLayout.CENTER);
        currentStuff.setBackground(BG_COLOR);
        add(currentStuff,BorderLayout.AFTER_LAST_LINE);
        setBackground(BG_COLOR);
        pack();
    }

    private JPanel getLibPanel() {
        librarySongsList = framework.getLibrary();
        List<String> librarySongsStrings = new ArrayList<>();
        for (MusicData song:librarySongsList) {
            librarySongsStrings.add(song.toString());
        }
        JPanel libraryPanel = new JPanel(new BorderLayout());
        libraryPanel.setPreferredSize(new Dimension(300,0));
        libraryJLIST = new JList(librarySongsStrings.toArray()); //Uses string[] to make list
        libraryJLIST.setFont(new Font("Lato",Font.PLAIN,20));
        libraryPanel.setBorder(new EmptyBorder(10,10,10,10));
        libraryJLIST.setBackground(BG_COLOR_2);
        JLabel libHeader = new JLabel("Song Library:",SwingConstants.CENTER);
        libHeader.setFont(new Font("Lato",Font.PLAIN,30));
        libraryPanel.add(libHeader,BorderLayout.BEFORE_FIRST_LINE);
        libraryPanel.add(libraryJLIST,BorderLayout.CENTER);
        JButton loader3 = new JButton("Select Song");
        loader3.setPreferredSize(new Dimension(300,40));
        loader3.setBackground(BUTTON_BG_COLOR);
        loader3.setForeground(BUTTON_FG_COLOR);
        loader3.setFocusPainted(false);
        loader3.setOpaque(true);
        loader3.setBorderPainted(false);
        loader3.addActionListener(e -> {loadLibrarySong(libraryJLIST.getSelectedIndex());});
        libraryPanel.add(loader3,BorderLayout.AFTER_LAST_LINE);
        libraryPanel.setBackground(BG_COLOR);
        libraryJLIST.setBorder(PANEL_BORDER);
        return libraryPanel;
    }

    private JPanel getVisPanel() {
        visualizerPluginsList = framework.getVisualizers();
        List<String> visualizerPluginsString = new ArrayList<>();
        for(Visual vis: visualizerPluginsList){
            visualizerPluginsString.add(vis.toString());
        }

        JPanel visualizerPlugins = new JPanel(new BorderLayout());
        visualizerPlugins.setPreferredSize(new Dimension(300,0));
        visualizerJLIST = new JList(visualizerPluginsString.toArray()); //Uses string[] to make list
        visualizerJLIST.setFont(new Font("Lato",Font.PLAIN,20)); //Font size
        visualizerJLIST.setBackground(BG_COLOR_2);
        visualizerPlugins.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel visHeader = new JLabel("Visualizers:",SwingConstants.CENTER);
        visHeader.setFont(new Font("Lato",Font.PLAIN,30));
        visualizerPlugins.add(visHeader,BorderLayout.BEFORE_FIRST_LINE);
        visualizerPlugins.add(visualizerJLIST,BorderLayout.CENTER);
        visualizerPlugins.setBackground(BG_COLOR);
        JButton play = new JButton("Play");
        play.setBackground(BUTTON_BG_COLOR);
        play.setForeground(BUTTON_FG_COLOR);
        play.setFocusPainted(false);
        play.setOpaque(true);
        play.setBorderPainted(false);
        play.setPreferredSize(new Dimension(300,40));
        play.addActionListener(e -> {loadVisPlugin(visualizerJLIST.getSelectedIndex());});
        visualizerPlugins.add(play,BorderLayout.AFTER_LAST_LINE);
        visualizerJLIST.setBorder(PANEL_BORDER);
        return visualizerPlugins;
    }

    private JPanel getDataPanel() {
        dataPluginsList = framework.getDataPlugins();
        List<String> dataPluginString = new ArrayList<>();
        for(DataLoader data: dataPluginsList){
            dataPluginString.add(data.toString());
        }

        JPanel dataPlugins = new JPanel(new BorderLayout());
        dataPlugins.setPreferredSize(new Dimension(300,0));
        dataPluginsJLIST = new JList(dataPluginString.toArray()); //Uses string[] to make list
        dataPluginsJLIST.setFont(new Font("Lato",Font.PLAIN,20)); //Font size
        dataPluginsJLIST.setBackground(BG_COLOR_2);
        dataPlugins.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel dataHeader = new JLabel("Data Loaders:",SwingConstants.CENTER);
        dataHeader.setFont(new Font("Lato",Font.PLAIN,30));
        dataPlugins.add(dataHeader,BorderLayout.BEFORE_FIRST_LINE);
        dataPlugins.add(dataPluginsJLIST,BorderLayout.CENTER);
        JButton loader = new JButton("Load");
        loader.setPreferredSize(new Dimension(300,40));
        loader.setBackground(BUTTON_BG_COLOR);
        loader.setForeground(BUTTON_FG_COLOR);
        loader.setFocusPainted(false);
        loader.setOpaque(true);
        loader.setBorderPainted(false);
        loader.addActionListener(e -> { loadDataPlugin(dataPluginsJLIST.getSelectedIndex());});
        dataPlugins.add(loader,BorderLayout.AFTER_LAST_LINE);
        dataPlugins.setBackground(BG_COLOR);
        dataPluginsJLIST.setBorder(PANEL_BORDER);
        return dataPlugins;
    }

    /**
     * Applies the chosen data plugin
     * @param s the selected plugin's index
     */
    public void loadDataPlugin(int s){
        if(s == -1){
            JOptionPane.showMessageDialog(null,"Please Select a Data Plugin to load from");
            return;
        }
        status.setText("Status: Loading song... Please Wait");
        if(framework.addSong(s)){
            status.setText("Status: Done Loading song... Running");
            System.out.println("GOOD JOB");
            remove(libraryPanel);
            libraryPanel = getLibPanel();
            add(libraryPanel,BorderLayout.CENTER);
            redraw();
        } else{
            status.setText("");
            JOptionPane.showMessageDialog(null, "Unable to load file. Please try again.");
        }
    }

    /**
     * Applies the chosen display plugin
     * @param s the selected plugin's index
     */
    public void loadVisPlugin(int s){
        if(s == -1){
            JOptionPane.showMessageDialog(null,"Please Select a Visualizer");
            return;
        } else if(!framework.songLoaded()){
            JOptionPane.showMessageDialog(null, "Please Select a song to visualize");
            return;
        }
        if (!framework.play(s)){
            String msg = "An unkown error has occurred. Please try again.";
            if(framework.getCurrentVisualizer() != null)msg = "Visualizer is playing. Please close it before visualizing another song";
            JOptionPane.showMessageDialog(null, msg);
        }

    }

    /**
     * Loads a song to be played
     * @param s the int index of the song
     */
    public void loadLibrarySong(int s){
        if(s == -1){
            JOptionPane.showMessageDialog(null,"Please Select a Song to Visualize");
            return;
        }
        currentSong = librarySongsList.get(s);
        int time = currentSong.getLength();
        int minutes = time / (60);
        int seconds = (time) % 60;
        String timeStr = String.format("%d:%02d", minutes, seconds);
        currentSongLabel.setText(currentSong.toString() + " - " + timeStr);
        framework.loadSong(s);
        redraw();
    }

    private void redraw() {
        repaint();
        pack();
    }
}
