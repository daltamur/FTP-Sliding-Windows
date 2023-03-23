package org.tftp.client;

import org.tftp.packets.ErrorPacket;
import org.tftp.packets.OACKPacket;
import org.tftp.packets.PacketFactory;
import org.tftp.utilities.Constants;
import org.tftp.utilities.ImageViewer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Client implements Constants {

    //Block Number -> data byte array
    static ConcurrentHashMap<Integer, byte[]> DataMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        //arg[0] has ip of server
        //arg[1] has port of server
        //arg[2] has image URL

        //client initiates connection with server
        DatagramChannel client = DatagramChannel.open().bind(null);
        InetSocketAddress serverAddr = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        //we'll add encryption last...
        ByteBuffer buffer = new PacketFactory().makeRRQPacket(args[2], ThreadLocalRandom.current().nextLong());
        client.send(buffer, serverAddr);
        System.out.println("RRQ Sent!");
        buffer = ByteBuffer.allocate(1024);
        //client will ack back, connect to the socket address of the ack, as this is going to be the thread the client communicates with
        long encryptionKey;
        for(;;) {
            client.connect(client.receive(buffer));
            System.out.println("Now connected to server instance!");
            if(PacketFactory.bytesToInt(new byte[]{buffer.get(1), buffer.get(0)}) == 6){
                //we'll get our encryption key here.
                encryptionKey = new OACKPacket(buffer).getEncryptionKey();
                System.out.println(encryptionKey);
                break;
            } else if (PacketFactory.bytesToInt(new byte[]{buffer.get(1), buffer.get(0)}) == 5) {
                //there was an error, likely that the URL gave no image data
                //handle error and close client connection
                ErrorPacket packet = new ErrorPacket(buffer);
                System.out.println("ERROR CODE " + packet.getErrorCode() + ": " + packet.getErrorMessage());
            }else{
                System.out.println("ERROR: Unknown Packet Opcode of " + PacketFactory.bytesToInt(new byte[]{buffer.get(1), buffer.get(0)}));
            }
        }
        //initiate the sliding window protocol, start listening for server packets
        //ack back each packet
        //continue to do this until we get the last packet
        //all data will get stored in a concurrent hashmap such that block number -> packet data
        //close connection to the server, we got all our data

        //gather all the packets from the concurrent hashmap, piece them back together into one large byte array that we'll turn into
        //a file that will be our image

        //display the image
        //new ImageViewer(image).showImage();

    }
}

class SlidingWindowReceiver implements Runnable{
    ByteBuffer receivedData;


    public SlidingWindowReceiver(ByteBuffer receivedData){
        this.receivedData = receivedData;
    }

    @Override
    public void run() {

    }
}
