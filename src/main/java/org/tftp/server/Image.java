package org.tftp.server;

public class Image {
    private final int httpCode;
    private final byte[] imageData;
    private final String imageName;

    public Image(int httpCode, byte[] imageData, String imageName){
        this.httpCode = httpCode;
        this.imageData = imageData;
        this.imageName = imageName;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getImageName() {
        return imageName;
    }
}
