package org.tftp.utilities;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class XOREncryption {


    //we'll make the key equal to the floor modulus of x and y
    //client key is x, server key is y
    public static long generateKey(long x, long y){
        return x*y;
    }

    //the same function is used for encryption and decryption
   public static void XORBuffer(ByteBuffer buffer, long key) {
        byte[] longBytes = ByteBuffer.allocate(Long.BYTES).putLong(key).array();
        //make a byte array to hold the byte buffer
       byte[]  bufferBytes = new byte[buffer.limit()];
       buffer.get(bufferBytes, 0, bufferBytes.length);
        int currentBufferPos = 0;
        buffer.flip();
        while(currentBufferPos < buffer.limit()) {
            for (int i = 0; i < 8; i++) {
                if(currentBufferPos+i >= buffer.limit()) break;
                buffer.put((byte) (bufferBytes[currentBufferPos+i] ^ longBytes[i]));
            }
            currentBufferPos+=8;
        }
        //flip to read from it again
       buffer.flip();
   }


}
