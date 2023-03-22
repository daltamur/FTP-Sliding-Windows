package org.tftp.packets;
/*
//server responds with a different long, we take the floor of the mean of the two longs the
//client and server generate
6|0|<EncryptionKey>|0|<Some Random Long>
 */
public class OACKPacket extends Packet{
    public long encryptionKey;


    public OACKPacket(){

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
