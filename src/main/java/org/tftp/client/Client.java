package org.tftp.client;

import com.oracle.coherence.common.base.Timeout;
import org.tftp.packets.DataPacket;
import org.tftp.packets.ErrorPacket;
import org.tftp.packets.OACKPacket;
import org.tftp.packets.PacketFactory;
import org.tftp.utilities.Constants;
import org.tftp.utilities.ImageViewer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client implements Constants {

    //Block Number -> data byte array
    public static ConcurrentHashMap<Integer, byte[]> DataMap = new ConcurrentHashMap<>();
    public static AtomicBoolean lastPacketSent = new AtomicBoolean(false);

    public static void main(String[] args) throws IOException{
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
            client.receive(buffer);
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
        client.socket().setSoTimeout(1000);
        while(!lastPacketSent.get()){
            //give the client a second to receive data
            try (Timeout t = Timeout.after(1, TimeUnit.SECONDS)) {
                ByteBuffer receivedData = ByteBuffer.allocate(1024);
                try {
                    new Thread(new SlidingWindowReceiver(receivedData, client.receive(receivedData))).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }catch (InterruptedException e) {
                // thread timed out or was otherwise interrupted
                if(lastPacketSent.get()) break;
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
    }
}

class SlidingWindowReceiver implements Runnable{
    ByteBuffer receivedData;
    InetSocketAddress serverConnection;

    DatagramChannel connection = DatagramChannel.open().bind(null);


    public SlidingWindowReceiver(ByteBuffer receivedData, SocketAddress serverConnection) throws IOException {
        this.receivedData = receivedData;
        connection.connect(serverConnection);
    }

    @Override
    public void run() {
        //process the packet, send an ACK back if it is a data packet
        //if it is anything other than an error packet just throw it out
        if(PacketFactory.bytesToInt(new byte[]{receivedData.get(1), receivedData.get(0)}) == 3){
            DataPacket packet = new DataPacket(receivedData);
            Client.DataMap.put(packet.getBlockNumber(), packet.getData());
            //send the ACK
            try {
                connection.write(new PacketFactory().makeAckPacket(packet.getBlockNumber()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //set the atomic boolean to true to indicate to the main client thread that we're done receiving
            if(packet.isLastPacket()) Client.lastPacketSent.set(true);
        }
        try {
            connection.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            connection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
