package org.tftp.packets;

public abstract class Packet {
    public abstract byte[] getByteArray();
    public abstract int getOpcode();

}
