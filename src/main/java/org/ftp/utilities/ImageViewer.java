package org.ftp.utilities;

import org.ftp.server.Image;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageViewer {
    BufferedImage image;

    public ImageViewer(String imagePath) throws IOException {
        this.image = ImageIO.read(new File(imagePath));
    }

    public void showImage(){
        ImageIcon icon = new ImageIcon(image);
        JLabel label = new JLabel(icon);
        JOptionPane.showMessageDialog(null, label);
        System.out.println("Showed the image");
    }

    public static void main(String[] args) throws IOException {
        new ImageViewer("/home/dominic/IdeaProjects/FTP-Sliding-Windows/savedFiles/https:..thefader-res.cloudinary.com.private_images.w_640,c_limit,f_auto,q_auto:eco.18960007Final_xagiy6.phoebe-bridgers-cover-story-interview.jpg").showImage();
    }


}
