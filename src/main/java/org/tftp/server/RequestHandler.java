package org.tftp.server;

import com.oracle.coherence.common.base.Timeout;
import org.tftp.packets.*;
import org.tftp.utilities.Constants;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class RequestHandler implements Runnable{
    private final SocketAddress clientAddress;
    private final DatagramChannel channel;

    private final ByteBuffer initiallyReceivedBuffer;
    private ByteBuffer buffer;

    private ConcurrentHashMap<Integer, ACKPacket> ACKMap = new ConcurrentHashMap<>();



    public RequestHandler(SocketAddress clientAddress, ByteBuffer receivedData) throws IOException {
        receivedData.flip();
        this.clientAddress = clientAddress;
        this.buffer = ByteBuffer.allocate(1024);
        this.channel = DatagramChannel.open().bind(null);
        this.channel.connect(this.clientAddress);
        this.initiallyReceivedBuffer = receivedData;
    }

    public static ArrayList<ByteBuffer> getDataFrames(ByteBuffer imageData){
        imageData.flip();
        imageData.limit(imageData.capacity());
        ArrayList<ByteBuffer> imageFrames = new ArrayList<>();
        int i = 0;
        byte[] frameBytes;
        while(i < imageData.limit()){
            if(imageData.limit()-i >= 512){
                frameBytes = new byte[512];
                imageData.get(frameBytes, 0, 512);
                imageFrames.add(new PacketFactory().makeDataPacket(frameBytes, i));
                i+=512;
            }else{
                frameBytes = new byte[imageData.limit()-i];
                imageData.get(frameBytes, 0, frameBytes.length);
                imageFrames.add(new PacketFactory().makeDataPacket(frameBytes, i));
                i+=(imageData.limit() - i);
            }
        }
        return imageFrames;
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
        ArrayList<ByteBuffer> frames = getDataFrames(ByteBuffer.wrap(foundImage.getImageData()));

        //send the frames
        int windowStartPos = 0;
        int windowEndPos = 0;
        while(windowEndPos < frames.size()-1){
            if(ACKMap.size() < Constants.windowSize && windowEndPos <= Constants.windowSize-1){
                try {
                    new Thread(new SlidingWindowSender(frames.get(windowEndPos), windowEndPos, clientAddress, ACKMap)).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                windowEndPos++;
            }else if (ACKMap.containsKey(windowStartPos)){
                //slide the window by 1
                windowStartPos++;
                windowEndPos++;
                try {
                    new Thread(new SlidingWindowSender(frames.get(windowEndPos), windowEndPos, clientAddress, ACKMap)).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        while(ACKMap.size() != frames.size()){//just spin until the ack map catches up with the frames}



        //close the connection

    }
}



//This class will be used to send data to the client
class SlidingWindowSender implements Runnable {
    private ByteBuffer dataToSend;
    private  int blockNumber;
    private SocketAddress clientAddress;
    private ConcurrentHashMap<Integer, ACKPacket> ACKMap;

    private ByteBuffer ackBuffer = ByteBuffer.allocate(1024);
    private DatagramChannel connection = DatagramChannel.open().bind(null);


    public SlidingWindowSender(ByteBuffer dataToSend, int blockNumber, SocketAddress clientAddress, ConcurrentHashMap<Integer, ACKPacket>ACKMap) throws IOException {
        this.dataToSend = dataToSend;
        this.blockNumber = blockNumber;
        this.clientAddress = clientAddress;
        this.ACKMap = ACKMap;
    }
    @Override
    public void run() {
        //Send a packet and keep trying to send it until you get an ACK back
        boolean receivedACK = false;
        while(!receivedACK) {
            try {
                connection.send(dataToSend, clientAddress);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ACKPacket ackPacket;
            try (Timeout t = Timeout.after(1, TimeUnit.SECONDS)) {
                connection.receive(ackBuffer);
            }catch (InterruptedException e){
                continue;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //if caught, put the ACK packet in the hashmap and exit gracefully
            if(PacketFactory.bytesToInt(new byte[]{ackBuffer.get(1), ackBuffer.get(0)}) != 3) continue;
            ackPacket = new ACKPacket(ackBuffer);
            if(ackPacket.getBlockNumber() != blockNumber){
                ACKMap.put(blockNumber, ackPacket);
                continue;
            }
            ACKMap.put(blockNumber, ackPacket);
            receivedACK = true;
        }
        try {
            connection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
