package org.tftp.packets;


import java.nio.ByteBuffer;

/*
Returns byte buffers for the corresponding type of packet that is called
 */
public class PacketFactory {

    //cast a given int's value to a two byte array
    private byte[] getIntBytes(int x){
        return new byte[]{
                (byte) (x >> 8),
                (byte) x
        };
    }


    public ByteBuffer makeAckPacket(int blockNumber){
        byte[] opcodeBytes = getIntBytes(4);
        byte[] blockNumberBytes = getIntBytes(blockNumber);
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(opcodeBytes);
        buffer.put(blockNumberBytes);
        return buffer;
    }

    public ByteBuffer makeDataPacket(byte[] data, int blockNumber){
        byte[] opcodeBytes = getIntBytes(3);
        byte[] blockNumberBytes = getIntBytes(blockNumber);
        ByteBuffer buffer = ByteBuffer.allocate(4+data.length);
        buffer.put(opcodeBytes);
        buffer.put(blockNumberBytes);
        buffer.put(data);
        return buffer;
    }

    public ByteBuffer makeErrorPacket(String errorMessage, int errorCode){
        byte[] opcodeBytes = getIntBytes(5);
        byte[] errorCodeBytes = getIntBytes(errorCode);
        byte[] errorMessageBytes = errorMessage.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(5+errorMessageBytes.length);
        buffer.put(opcodeBytes);
        buffer.put(errorCodeBytes);
        buffer.put(errorMessageBytes);
        buffer.put((byte) 0);
        return buffer;
    }

    public ByteBuffer makeOACKPacket(long encryptionKey){
        byte[] opcodeBytes = getIntBytes(6);
        ByteBuffer buffer = ByteBuffer.allocate(12+"EncryptionKey".getBytes().length);
        buffer.put(opcodeBytes);
        buffer.put((byte) 0);
        buffer.put("EncryptionKey".getBytes());
        buffer.put((byte) 0);
        buffer.putLong(encryptionKey);
        return buffer;
    }


    public ByteBuffer makeRRQPacket(String FileURL, long encryptionKey){
        byte[] opcodeBytes = getIntBytes(6);
        ByteBuffer buffer = ByteBuffer.allocate(13+"EncryptionKey".getBytes().length+FileURL.getBytes().length);
        buffer.put(opcodeBytes);
        buffer.put((byte) 0);
        buffer.put(FileURL.getBytes());
        buffer.put((byte) 0);
        buffer.put("EncryptionKey".getBytes());
        buffer.put((byte) 0);
        buffer.putLong(encryptionKey);
        return buffer;
    }


}
