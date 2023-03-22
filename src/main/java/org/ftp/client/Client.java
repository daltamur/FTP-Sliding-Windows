package org.ftp.client;

import org.ftp.utilities.Constants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

public class Client implements Constants {
    public static void main(String[] args) throws IOException {
        DatagramChannel client = DatagramChannel.open().bind(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
        InetSocketAddress serverAddr = new InetSocketAddress("localhost", 8000);
        client.connect(serverAddr);
        Scanner scanner = new Scanner(System.in);
        String msg = scanner.nextLine();
        ByteBuffer buffer = ByteBuffer.allocate(msg.getBytes().length);
        client.send(buffer, serverAddr);
        System.out.println("Data Sent!");
        client.disconnect();
        buffer.flip();
        client.connect(client.receive(buffer));
        System.out.println("Now connected to server instance!");
        for(;;){
            msg = scanner.nextLine();
            buffer = ByteBuffer.allocate(msg.getBytes().length);
            buffer.put(msg.getBytes());
            buffer.flip();
            client.write(buffer);
            System.out.println("Data Sent!");
            buffer.flip();
        }

    }


}
