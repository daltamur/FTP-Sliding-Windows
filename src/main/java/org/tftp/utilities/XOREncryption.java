package org.tftp.utilities;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class XOREncryption {


    //we'll make the key equal to the floor modulus of x and y
    public static long generateKey(long x, long y){
        return Math.floorMod(x, y);
    }

    //the same function is used for encryption and decryption
   public static void XORBuffer(ByteBuffer buffer, long key) {}


}
