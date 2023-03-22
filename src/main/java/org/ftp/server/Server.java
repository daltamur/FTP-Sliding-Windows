package org.ftp.server;

import org.ftp.utilities.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Server implements Constants {
    //this will act as the central host that clients will access to make requests
    public static void main(String[] args) throws IOException {
        //arg[0] has the port
        //arg[1] has the IP
        //arg[2] will say 'drop' indicating to mimic 1% packet loss and 'no-drop' otherwise
        int port = Integer.parseInt(args[0]);
        String host = args[1];
        String packetLoss = args[2];
        DatagramChannel serverChannel = DatagramChannel.open().bind(new InetSocketAddress(host, port));
        serverChannel.configureBlocking(true);
        try {
            while (!Thread.interrupted()) {
                ByteBuffer receivedData = ByteBuffer.allocate(maxPacketSize);
                System.out.println("Waiting for user...");
                new Thread(new RequestHandler(serverChannel.receive(receivedData))).start();
                System.out.println("I got it");
            }
        }catch (IOException e){
            e.printStackTrace();
            serverChannel.close();
            System.exit(-1);
        }
    }
}
