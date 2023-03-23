package org.tftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    public static void main(String[] args) throws IOException {
        //test http download
        //we will get the file url from the client and make the file's name equal to the url
        Integer x = 5;
        System.out.println(x.byteValue());
        String fileURL = "https://thefader-res.cloudinary.com/private_images/w_640,c_limit,f_auto,q_auto:eco/18960007Final_xagiy6/phoebe-bridgers-cover-story-interview.jpg";
        URL fileUrl = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) fileUrl.openConnection();
        if(httpConn.getResponseCode() == HttpURLConnection.HTTP_OK){
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            System.out.println(contentType);
            if(httpConn.getHeaderField("Content-Disposition") == null){
                fileName = "savedFiles" + File.separator + fileURL.replace('/', '.');
            }
            InputStream inputStream = httpConn.getInputStream();
            String savedFilePath = fileName;
            FileOutputStream outputStream = new FileOutputStream(savedFilePath);
            outputStream.write(inputStream.readAllBytes());
        }
    }
}