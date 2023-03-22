package org.tftp.packets;


import java.nio.ByteBuffer;

/*
4|<BlockNumber>
 */
public class ACKPacket extends Packet{
    private int blockNumber;

    public ACKPacket(ByteBuffer buffer){
        //call function to fill up blockNumber
    }

    @Override
    public int getOpcode(){
        return 4;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    @Override
    public byte[] getByteArray() {
        return new byte[0];
    }
}
