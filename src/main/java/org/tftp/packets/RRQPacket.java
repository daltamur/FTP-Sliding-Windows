package org.tftp.packets;


import java.nio.ByteBuffer;

/*
No need to add the octet transmission mode, as that's the only one we're supporting
___________________________________________________________________________________
1|0|FileURL|0|EncryptionKey|0|<Some Random Long>
 */
public class RRQPacket extends Packet{
    private String fileURL;
    private long encryptionKeyVal;

    public RRQPacket(ByteBuffer buffer){

    }

    @Override
    public byte[] getByteArray() {
        return new byte[0];
    }

    @Override
    public int getOpcode() {
        return 1;
    }
}
