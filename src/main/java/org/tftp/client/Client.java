package org.tftp.client;

import org.tftp.utilities.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Client implements Constants {
    static ConcurrentHashMap<Integer, byte[]> DataMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        //arg[0] has ip of server
        //arg[1] has port of server
        //art[2] has ip of client
        //arg[3] has port of client
        //arg[4] has image URL

        //client initiates connection with server
        DatagramChannel client = DatagramChannel.open().bind(null);
        InetSocketAddress serverAddr = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        Scanner scanner = new Scanner(System.in);
        String msg = scanner.nextLine();
        ByteBuffer buffer = ByteBuffer.allocate(msg.getBytes().length);
        client.send(buffer, serverAddr);
        System.out.println("Data Sent!");
        buffer.flip();
        //client will ack back, connect to the socket address of the ack, as this is going to be the thread the client communicates with
        client.connect(client.receive(buffer));
        System.out.println("Now connected to server instance!");
        System.out.println(new String(buffer.array()));
        //initiate the sliding window protocol, start listening for server packets
        //ack back each packet
        //continue to do this until we get the last packet
        //all data will get stored in a cuncurrent hackmap such that block number -> packet data
        for(;;){
            msg = scanner.nextLine();
            buffer = ByteBuffer.allocate(msg.getBytes().length);
            buffer.put(msg.getBytes());
            buffer.flip();
            client.write(buffer);
            System.out.println("Data Sent!");
            buffer.flip();
        }
        //close connection to the server, we got all our data

        //gather all the packets from the concurrent hashmap, piece them back together into one large byte array that we'll turn into
        //a file that will be our image

        //display the image

    }


}
