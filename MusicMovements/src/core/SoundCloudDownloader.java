package core;

import okhttp3.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class SoundCloudDownloader implements DataLoader {


    private static final String SITE = "https://www.klickaud.com/download.php";
    private OkHttpClient client = new OkHttpClient();
    private File savedFile;
    private boolean success;

    @Override
    public File onSelect() {
        String url = "";
        String name = "";
        // Setting up JOptionPane for url and name input
        JTextField urlField = new JTextField();
        JTextField nameField = new JTextField();
        JPanel myPanel = new JPanel(new GridLayout(2, 2));
        myPanel.add(new JLabel("SoundCloud url: "));
        myPanel.add(urlField);
        myPanel.add(Box.createHorizontalStrut(15)); // a spacer
        myPanel.add(new JLabel("Song name: "));
        myPanel.add(nameField);
        myPanel.setPreferredSize(new Dimension(400, 80));
        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "What is the SoundCloud url and name of the song?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            url = urlField.getText();
            name = nameField.getText();
        }
        if (name.equals("") || url.equals("")) return null;
        Request request = new Request.Builder()
                .url(SITE + "?value=" + url)
                .build();
        try {
            savedFile = null;
            success = false;
            // Send POST request to site to get url
            Response response;
            response = client.newCall(request).execute();
            String stringResponse = response.body().string();
            // Parse response for download link
            int i = stringResponse.indexOf("https://cf-media.sndcdn.com/");
            System.out.println(stringResponse);
            System.out.println( i);
            StringBuilder fileUrl = new StringBuilder();
            for (; stringResponse.charAt(i) != '\''; i++) fileUrl.append(stringResponse.charAt(i));
            String downloadUrl = fileUrl.toString();
            // Clear old downloaded files
            File directory = new File("files/SCDL");
            File[] oldFiles = directory.listFiles();
            if (!(oldFiles == null || oldFiles.length <= 0))
                for (File f : oldFiles) f.delete();
            // Download new mp3 file
            savedFile = new File("files/SCDL/" + name + ".mp3");
            success = downloadFile(downloadUrl, savedFile);
            response.close();
            if (!success) return null; // Return null to signify an error
            return savedFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isValidURL(String url){
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return url.contains("soundcloud");
    }

    /**
     * Downloads and saves file from downloadUrl to savedFile
     * @param downloadUrl the url
     * @param savedFile   the File destination
     * @return            whether the file successfully downloaded
     */
    private boolean downloadFile(String downloadUrl, File savedFile) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        Response response;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file: " + response);
            }
            FileOutputStream fos = new FileOutputStream(savedFile.getPath());
            fos.write(response.body().bytes());
            fos.close();
            return true;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return "SoundCloud Song";
    }

}
