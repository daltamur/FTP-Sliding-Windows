package org.tftp.packets;

/*
5|<Error Code>|<Error Message>|0
 */
public class ErrorPacket extends Packet{
    private int errorCode;
    private String errorMessage;

    public ErrorPacket(){

    }

    @Override
    public int getOpcode(){
        return 5;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public byte[] getByteArray() {
        return new byte[0];
    }
}
