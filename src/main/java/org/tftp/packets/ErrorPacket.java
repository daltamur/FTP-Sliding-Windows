package org.tftp.packets;

import java.nio.ByteBuffer;

/*
05|<Error Code>|<Error Message>|0
 */
public class ErrorPacket extends Packet{
    private final int errorCode;
    private final String errorMessage;

    public ErrorPacket(ByteBuffer buffer){
        //omit the zero that gets written at the end
        int totalLength = buffer.limit()-1;
        buffer.position(2);
        //get error code from buffer
        byte[] errCodeBytes = new byte[4];
        buffer.get(errCodeBytes, 2, 2);
        errorCode = ByteBuffer.wrap(errCodeBytes).getInt();
        byte[] errMessageArr = new byte[totalLength-4];
        buffer.get(errMessageArr, 0, errMessageArr.length);
        errorMessage = new String(errMessageArr);
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

}
