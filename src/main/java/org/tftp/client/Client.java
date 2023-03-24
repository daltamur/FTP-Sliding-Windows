package org.tftp.client;

import org.tftp.packets.*;
import org.tftp.utilities.Constants;
import org.tftp.utilities.ImageViewer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Constants {

    //Block Number -> data byte array
    public static ConcurrentHashMap<Integer, byte[]> DataMap = new ConcurrentHashMap<>();
    public static AtomicBoolean lastPacketSent = new AtomicBoolean(false);

    public static AtomicInteger totalPackets = new AtomicInteger();
    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void main(String[] args) throws IOException {
        //arg[0] has ip of server
        //arg[1] has port of server
        //arg[2] has image URL

        //client initiates connection with server
        DatagramChannel client = DatagramChannel.open().bind(null);
        InetSocketAddress serverAddr = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        long clientXORKey = ThreadLocalRandom.current().nextLong();
        ByteBuffer buffer = new PacketFactory().makeRRQPacket(args[2], ThreadLocalRandom.current().nextLong());
        client.send(buffer, serverAddr);
        System.out.println("RRQ Sent!");
        buffer = ByteBuffer.allocate(1024);
        //client will ack back, connect to the socket address of the ack, as this is going to be the thread the client communicates with
        long encryptionKey;
        for(;;) {
            client.receive(buffer);
            System.out.println("Now connected to server instance!");
            if(PacketFactory.bytesToInt(new byte[]{buffer.get(1), buffer.get(0)}) == 6){
                //we'll get our encryption key here.
                buffer.flip();
                encryptionKey = new OACKPacket(buffer).getEncryptionKey();
                System.out.println(encryptionKey);
                break;
            } else if (PacketFactory.bytesToInt(new byte[]{buffer.get(1), buffer.get(0)}) == 5) {
                //there was an error, likely that the URL gave no image data
                //handle error and close client connection
                buffer.flip();
                ErrorPacket packet = new ErrorPacket(buffer);
                client.close();
                System.out.println("ERROR CODE " + packet.getErrorCode() + ": " + packet.getErrorMessage());
                System.exit(-1);
            }else{
                buffer.flip();
                client.close();
                System.out.println("ERROR: Unknown Packet Opcode of " + PacketFactory.bytesToInt(new byte[]{buffer.get(1), buffer.get(0)}));
                System.exit(-1);
            }
        }
        //initiate the sliding window protocol, start listening for server packets
        //ack back each packet
        //continue to do this until we get the last packet
        //all data will get stored in a concurrent hashmap such that block number -> packet data

        //our task we will time
        Callable<Void> Callable = () -> {
            ByteBuffer receivedData = ByteBuffer.allocate(1024);
            try {
                new Thread(new SlidingWindowReceiver(receivedData, client.receive(receivedData))).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        };
        executorService.submit(Callable);
        while(!lastPacketSent.get()){
            //give the client a half second to receive data
            Future<Void> task = executorService.submit(Callable);
            try {
                task.get(500, TimeUnit.MILLISECONDS);
            }catch (Exception e){
                // had a timeout, do nothing
                if(lastPacketSent.get() && DataMap.size() == totalPackets.get()) break;
            }
        }

        //loop again to get the rest of the packets even if the last packet was sent
        int totalPacket = totalPackets.get();
        while(DataMap.size() != totalPacket){
            //give the client a half second to receive data
            Future<Void> task = executorService.submit(Callable);
            try {
                task.get(500, TimeUnit.MILLISECONDS);
            }catch (Exception e){
                // had a timeout, do nothing
                if(DataMap.size() == totalPacket) break;
            }
        }

        //close connection to the server, we got all our data
        client.close();
        client.disconnect();
        //gather all the packets from the concurrent hashmap, piece them back together into one large byte array that we'll turn into
        //a file that will be our image
        System.out.println("Image successfully received. Displaying result...");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        List<Map.Entry<Integer, byte[]>> list = new ArrayList<>(DataMap.entrySet());
        list.sort(Map.Entry.comparingByKey());
        for(Map.Entry<Integer, byte[]> e : list) {
            bytes.write(e.getValue());
        }

        //display the image
        byte[] reformedByteArray = bytes.toByteArray();
        InputStream is = new ByteArrayInputStream(reformedByteArray);
        BufferedImage image = ImageIO.read(is);
        new ImageViewer(image).showImage();
        //Thread terminates after this
        System.exit(0);
    }
}

class SlidingWindowReceiver implements Runnable{
    ByteBuffer receivedData;
    DatagramChannel connection = DatagramChannel.open().bind(null);
    SocketAddress serverConnection;


    public SlidingWindowReceiver(ByteBuffer receivedData, SocketAddress serverConnection) throws IOException {
        //xor decrypt receivedData right here
        this.receivedData = receivedData;
        this.serverConnection = serverConnection;
    }

    @Override
    public void run() {
        //process the packet, send an ACK back if it is a data packet
        //if it is anything other than an error packet just throw it out
        if(PacketFactory.bytesToInt(new byte[]{receivedData.get(1), receivedData.get(0)}) == 3){
            receivedData.flip();
            DataPacket packet = new DataPacket(receivedData);
            //send the ACK
            try {
                ByteBuffer ACKPacket = new PacketFactory().makeAckPacket(packet.getBlockNumber());
                //xor encrypt ack packet right here
                connection.send(ACKPacket, serverConnection);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //just a print statement for myself to monitor the packets that were caught
            //System.out.println(packet.getBlockNumber());

            //set the atomic boolean to true to indicate to the main client thread that we're done receiving
            if(packet.isLastPacket()) {
                Client.lastPacketSent.set(true);
                Client.totalPackets.set(packet.getBlockNumber()+1);
            }
            Client.DataMap.put(packet.getBlockNumber(), packet.getData());
        }

        try {
            connection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
