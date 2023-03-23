package org.tftp.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class RequestHandler implements Runnable{
    private final SocketAddress clientAddress;
    private final DatagramChannel channel;
    private ByteBuffer buffer;



    public RequestHandler(SocketAddress clientAddress, DatagramChannel serverChannel) throws IOException {
        this.clientAddress = clientAddress;
        this.buffer = ByteBuffer.allocate(1024);
        this.channel = DatagramChannel.open().bind(null);
        this.channel.connect(this.clientAddress);
        //server channel writes to the client with the server's randomized xor key and a string representing the new channel's socket address to send stuff to
    }
    @Override
    public void run() {
        buffer.put(new String("ready").getBytes());
        System.out.println(clientAddress);
        try {
            channel.send(buffer, clientAddress);
            buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Sent ack to client");
        for(;;){
            try {
                channel.receive(buffer);
                buffer.flip();
                byte bytes[] = new byte[buffer.limit()];
                buffer.get(bytes, 0, buffer.limit());
                System.out.println(Thread.currentThread().getId());
                System.out.println(new String(bytes));
                if(new String(bytes).equals("exit")){
                    channel.disconnect();
                    channel.close();
                    break;
                }
                buffer.clear();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
