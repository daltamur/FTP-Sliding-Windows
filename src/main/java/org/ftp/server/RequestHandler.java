package org.ftp.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class RequestHandler implements Runnable{
    private final SocketAddress clientAddress;
    private final DatagramChannel channel;
    private ByteBuffer buffer;



    public RequestHandler(SocketAddress clientAddress) throws IOException {
        this.clientAddress = clientAddress;
        this.buffer = ByteBuffer.allocate(1024);
        this.channel = DatagramChannel.open().bind(null);
        this.channel.connect(this.clientAddress);
    }
    @Override
    public void run() {
        buffer.put(new String("ready").getBytes());
        try {
            channel.write(buffer);
            buffer.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for(;;){
            try {
                channel.receive(buffer);
                buffer.flip();
                System.out.println(buffer.position());
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
