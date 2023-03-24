package org.tftp.packets;


import java.nio.ByteBuffer;

/*
04|<BlockNumber>
 */
public class ACKPacket extends Packet{
    private final Integer blockNumber;

    public ACKPacket(ByteBuffer buffer){
        //fill up block number
        byte[] blockBytes = new byte[4];
        buffer.position(2);
        buffer.get(blockBytes, 2, 2);
        blockNumber = ByteBuffer.wrap(blockBytes).getInt();
    }

    @Override
    public int getOpcode(){
        return 4;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

}
