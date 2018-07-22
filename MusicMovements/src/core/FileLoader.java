package core;

import javax.swing.*;
import java.io.File;

public class FileLoader implements DataLoader {


    @Override
    public File onSelect() {
        JFileChooser chooser = new JFileChooser(); //JAVA Swing file Chooser
        chooser.showOpenDialog(null);
        return chooser.getSelectedFile();
    }

    @Override
    public String toString() {
        return "Local File";
    }
}


