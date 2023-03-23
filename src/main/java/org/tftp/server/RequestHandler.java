package org.tftp.server;

import org.tftp.packets.ACKPacket;
import org.tftp.packets.PacketFactory;
import org.tftp.packets.RRQPacket;
import org.tftp.utilities.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.concurrent.*;

public class RequestHandler implements Runnable {
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

    public static ArrayList<ByteBuffer> getDataFrames(ByteBuffer imageData) {
        imageData.flip();
        imageData.limit(imageData.capacity());
        ArrayList<ByteBuffer> imageFrames = new ArrayList<>();
        int i = 0;
        int frameCount = 0;
        byte[] frameBytes;
        while (i < imageData.limit()) {
            if (imageData.limit() - i >= 512) {
                frameBytes = new byte[512];
                imageData.get(frameBytes, 0, 512);
                imageFrames.add(new PacketFactory().makeDataPacket(frameBytes, frameCount));
                i += 512;
                frameCount++;
            } else {
                frameBytes = new byte[imageData.limit() - i];
                imageData.get(frameBytes, 0, frameBytes.length);
                imageFrames.add(new PacketFactory().makeDataPacket(frameBytes, frameCount));
                i += (imageData.limit() - i);
                frameCount++;
            }
        }
        return imageFrames;
    }

    @Override
    public void run() {
        //check if the image exists, if it doesn't send an error packet
        //if it does, OACK back with your encryption key

        //only initiate things if an RRQ packet is sent
        if (PacketFactory.bytesToInt(new byte[]{initiallyReceivedBuffer.get(1), initiallyReceivedBuffer.get(0)}) != 1) {
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
        if (foundImage.getHttpCode() != HttpURLConnection.HTTP_OK) {
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
        while (windowEndPos <= frames.size() - 1) {
            if (ACKMap.size() < Constants.windowSize && windowEndPos < Constants.windowSize - 1) {
                try {
                    new Thread(new SlidingWindowSender(frames.get(windowEndPos), windowEndPos, clientAddress, ACKMap)).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                windowEndPos++;
            } else if (ACKMap.containsKey(windowStartPos)) {
                //slide the window by 1
                windowStartPos++;
                try {
                    new Thread(new SlidingWindowSender(frames.get(windowEndPos), windowEndPos, clientAddress, ACKMap)).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                windowEndPos++;

            }
        }


        while (ACKMap.size() != frames.size()) {}//just spin until the ack map catches up with the frames
        //disconnect
        System.out.println("Image transferred");
        try {
            channel.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //This class will be used to send data to the client
    class SlidingWindowSender implements Runnable {
        private ByteBuffer dataToSend;
        private int blockNumber;
        private SocketAddress clientAddress;
        private ConcurrentHashMap<Integer, ACKPacket> ACKMap;
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        private ByteBuffer ackBuffer = ByteBuffer.allocate(1024);
        private DatagramChannel connection = DatagramChannel.open().bind(null);


        public SlidingWindowSender(ByteBuffer dataToSend, int blockNumber, SocketAddress clientAddress, ConcurrentHashMap<Integer, ACKPacket> ACKMap) throws IOException {
            this.dataToSend = dataToSend;
            this.blockNumber = blockNumber;
            this.clientAddress = clientAddress;
            this.ACKMap = ACKMap;
        }

        @Override
        public void run() {
            //Send a packet and keep trying to send it until you get an ACK back
            boolean receivedACK = false;
            //our task we will time
            Callable<Void> Callable = () -> {
                ByteBuffer receivedData = ByteBuffer.allocate(1024);
                try {
                    connection.receive(ackBuffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            };
            while (!receivedACK) {
                try {
                    int val = ThreadLocalRandom.current().nextInt(100);
                    if(val != 99) {
                        System.out.println("Sending block "+blockNumber);
                        connection.send(dataToSend, clientAddress);
                    }else{
                        System.out.println("Dropping Packet " + blockNumber);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ACKPacket ackPacket;
                //give the client a half second to receive data
                Future<Void> task = executorService.submit(Callable);
                try {
                    task.get(500, TimeUnit.MILLISECONDS);
                }catch (Exception e){
                    // had a timeout, do nothing
                    System.out.println("No ACK received for "+blockNumber);
                    continue;
                }
                //if caught, put the ACK packet in the hashmap and exit gracefully
                //Make sure it was an ack packet that we caught
                if (PacketFactory.bytesToInt(new byte[]{ackBuffer.get(1), ackBuffer.get(0)}) != 4) continue;
                ackPacket = new ACKPacket(ackBuffer);
                if (ackPacket.getBlockNumber() != blockNumber) {
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
}
