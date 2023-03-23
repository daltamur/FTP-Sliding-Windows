package org.tftp.server;

import org.tftp.packets.PacketFactory;
import org.tftp.packets.RRQPacket;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketAddress;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ThreadLocalRandom;

public class RequestHandler implements Runnable{
    private final SocketAddress clientAddress;
    private final DatagramChannel channel;

    private final ByteBuffer initiallyReceivedBuffer;
    private ByteBuffer buffer;



    public RequestHandler(SocketAddress clientAddress, ByteBuffer receivedData) throws IOException {
        receivedData.flip();
        this.clientAddress = clientAddress;
        this.buffer = ByteBuffer.allocate(1024);
        this.channel = DatagramChannel.open().bind(null);
        this.channel.connect(this.clientAddress);
        this.initiallyReceivedBuffer = receivedData;
    }
    @Override
    public void run() {
        //check if the image exists, if it doesn't send an error packet
        //if it does, OACK back with your encryption key

        //only initiate things if an RRQ packet is sent
        if(PacketFactory.bytesToInt(new byte[]{initiallyReceivedBuffer.get(1), initiallyReceivedBuffer.get(0)}) != 1){
            try {
                channel.write(new PacketFactory().makeErrorPacket("Client must first send an RRQ packet.", 0));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        RRQPacket receivedPacket = new RRQPacket(initiallyReceivedBuffer);

        //get the image the user specified
        Image foundImage;
        try {
            foundImage = ImageGrabber.getImage(receivedPacket.getFileURL());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //if image is not found throw an error
        if(foundImage.getHttpCode() != HttpURLConnection.HTTP_OK){
            try {
                channel.write(new PacketFactory().makeErrorPacket("File not found.", 1));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        //tell user we found the image and can initiate the transfer
        try {
            channel.write(new PacketFactory().makeOACKPacket(ThreadLocalRandom.current().nextLong()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //break the image data up into frames


        //send the frames



        //close the connection

    }
}



//This class will be used to send data to the client
class SlidingWindowSender implements Runnable {

    @Override
    public void run() {

    }
}
