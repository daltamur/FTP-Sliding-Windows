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
        int length = buffer.limit();
        //get file url
        int curPos = 3;
        Integer zero = 0;
        int encryptionKeyLength = "EncryptionKey".getBytes().length;
        while(buffer.get(curPos) != zero.byteValue()){
            curPos++;
        }
        buffer.position(3);
        byte[] fileBytes = new byte[curPos - 3];
        buffer.get(fileBytes, 0, fileBytes.length);
        fileURL = new String(fileBytes);
        buffer.position(buffer.position()+encryptionKeyLength+2);
        //get encryption key
        byte[] keyBytes = new byte[length - fileBytes.length - encryptionKeyLength - 5];
        buffer.get(keyBytes, 0, keyBytes.length);
        encryptionKeyVal = ByteBuffer.wrap(keyBytes).getLong();
    }

    @Override
    public byte[] getByteArray() {
        return new byte[0];
    }

    public String getFileURL() {
        return fileURL;
    }

    public long getEncryptionKeyVal() {
        return encryptionKeyVal;
    }

    @Override
    public int getOpcode() {
        return 1;
    }
}
