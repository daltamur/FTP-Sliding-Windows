package org.tftp.packets;


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



}
