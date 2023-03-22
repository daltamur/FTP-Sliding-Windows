package org.tftp.packets;


import java.nio.ByteBuffer;

/*

3|<Block#>|<Data>
 */
public class DataPacket extends Packet{
    private int blockNumber;
    private byte[] data;
    public DataPacket(ByteBuffer buffer){
        //this will call a function that fills in blockNumber and data

    }

    @Override
    public int getOpcode(){
        return 3;
    }

    public byte[] getData() {
        return data;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    @Override
    public byte[] getByteArray() {
        return new byte[0];
    }
}
