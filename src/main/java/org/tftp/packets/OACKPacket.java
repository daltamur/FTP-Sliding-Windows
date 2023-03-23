package org.tftp.packets;

import java.nio.ByteBuffer;

/*
//server responds with a different long, we take the floor of the mean of the two longs the
//client and server generate
//note that a long is 8 bytes
6|0|<EncryptionKey>|0|<Some Random Long>
 */
public class OACKPacket extends Packet{
    private long encryptionKey;


    public OACKPacket(ByteBuffer buffer){
        int length = buffer.position();
        int encryptionKeyValLength = new String("EncryptionKey").getBytes().length;
        byte[] longBytes = new byte[8];
        buffer.position(encryptionKeyValLength+4);
        buffer.get(longBytes, 0, 8);
        encryptionKey = ByteBuffer.wrap(longBytes).getLong();
    }

    @Override
    public int getOpcode(){
        return 6;
    }

    public long getEncryptionKey() {
        return encryptionKey;
    }

    @Override
    public byte[] getByteArray() {
        return new byte[0];
    }
}
