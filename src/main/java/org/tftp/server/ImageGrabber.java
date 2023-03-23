package org.tftp.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageGrabber {

    //pull an image from
    public static Image getImage(String fileURL) throws IOException {
        //if the image is cached use that one instead of getting it from http
        Image cachedImage = checkIfCached(fileURL);
        if(cachedImage != null) return cachedImage;

        URL fileUrl = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) fileUrl.openConnection();
        try {
            String fileName = "";
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                if (httpConn.getHeaderField("Content-Disposition") == null) {
                    fileName = fileURL.replace('/', '.');
                }
                byte[] imageBytes = httpConn.getInputStream().readAllBytes();
                //save the file to the savedFiles folder
                FileOutputStream outputStream = new FileOutputStream("savedFiles/"+fileName);
                outputStream.write(imageBytes);

                return new Image(httpConn.getResponseCode(), imageBytes, fileName);
            } else {
                return new Image(httpConn.getResponseCode(), null, null);
            }
        }finally {
            httpConn.disconnect();
        }
    }


    private static Image checkIfCached(String fileURL) throws IOException {
        String fileName = fileURL.replace('/', '.');
        if(new File("savedFiles/"+fileName).exists()) return new Image(HttpURLConnection.HTTP_OK, Files.readAllBytes(Path.of("savedFiles/" + fileName)), fileName);
        return null;
    }
}
