package org.tftp.utilities;

import java.util.concurrent.ThreadLocalRandom;

public class XOREncryption {
    public static long generateKey(){
        return ThreadLocalRandom.current().nextLong();
    }

    //another method for encrypting with a key


}
