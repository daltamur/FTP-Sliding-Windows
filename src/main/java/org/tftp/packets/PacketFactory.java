package org.tftp.packets;


import java.nio.ByteBuffer;

/*
Returns byte buffers for the corresponding type of packet that is called
all get functions also flip the buffer to get it ready for reading
 */
public class PacketFactory {

    //cast a given int's value to a two byte array
    private byte[] getIntBytes(int x){
        return new byte[]{
                (byte) (x >> 8),
                (byte) x
        };
    }

    public static int bytesToInt(byte[] intBuf){
        return ByteBuffer.wrap(new byte[]{
                0x00,
                0x00,
                intBuf[1],
                intBuf[0]
        }).getInt();
    }


    public ByteBuffer makeAckPacket(int blockNumber){
        byte[] opcodeBytes = getIntBytes(4);
        byte[] blockNumberBytes = getIntBytes(blockNumber);
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put(opcodeBytes);
        buffer.put(blockNumberBytes);
        buffer.flip();
        return buffer;
    }

    public ByteBuffer makeDataPacket(byte[] data, int blockNumber){
        byte[] opcodeBytes = getIntBytes(3);
        byte[] blockNumberBytes = getIntBytes(blockNumber);
        ByteBuffer buffer = ByteBuffer.allocate(4+data.length);
        buffer.put(opcodeBytes);
        buffer.put(blockNumberBytes);
        buffer.put(data);
        buffer.flip();
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
        buffer.flip();
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
        buffer.flip();
        return buffer;
    }


    public ByteBuffer makeRRQPacket(String FileURL, long encryptionKey){
        byte[] opcodeBytes = getIntBytes(1);
        ByteBuffer buffer = ByteBuffer.allocate(13+"EncryptionKey".getBytes().length+FileURL.getBytes().length);
        buffer.put(opcodeBytes);
        buffer.put((byte) 0);
        buffer.put(FileURL.getBytes());
        buffer.put((byte) 0);
        buffer.put("EncryptionKey".getBytes());
        buffer.put((byte) 0);
        buffer.putLong(encryptionKey);
        buffer.flip();
        return buffer;
    }


}
