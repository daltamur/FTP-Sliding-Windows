package org.tftp.server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageGrabber {

    //pull an image from
    public static Image getImage(String fileURL) throws IOException {
        URL fileUrl = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) fileUrl.openConnection();
        String fileName = "";
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK){
            if(httpConn.getHeaderField("Content-Disposition") == null){
                fileName = fileURL.replace('/', '.');
            }
        }
        httpConn.disconnect();
        return new Image(httpConn.getResponseCode(), httpConn.getInputStream().readAllBytes(), fileName);
    }
}
