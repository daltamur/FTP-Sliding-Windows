import org.junit.Assert;
import org.junit.Test;
import org.tftp.packets.*;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class PacketTests {
    @Test
    public void ACKPacketTest(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] intData = {
                (byte) (4 >> 8),
                (byte) 4
        };
        buffer.put(intData);
        buffer.position(2);
        byte[] blockData = {
                (byte) (10 >> 8),
                (byte) 10
        };
        buffer.put(blockData);
        buffer.position(0);
        ACKPacket packet = new ACKPacket(buffer);
        byte[] intDataCheck = {
                0x00,
                0x00,
                intData[0],
                intData[1]
        };
        Integer x = 0;
        Assert.assertEquals(ByteBuffer.wrap(intDataCheck).getInt(), 4);
        Assert.assertEquals(packet.getBlockNumber(), 10);
    }

    @Test
    public void DataPacketTest(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] intData = {
                (byte) (3 >> 8),
                (byte) 3
        };
        buffer.put(intData);
        buffer.position(2);
        byte[] blockData = {
                (byte) (10 >> 8),
                (byte) 10
        };
        buffer.put(blockData);
        byte[] data = "I'm a single cell on a serpent's tongue".getBytes();
        buffer.put(data);
        DataPacket packet = new DataPacket(buffer);
        Assert.assertEquals(packet.getBlockNumber(), 10);
        Assert.assertEquals(new String(packet.getData()), "I'm a single cell on a serpent's tongue");
        Assert.assertTrue(packet.isLastPacket());
        data = new byte[512];
        buffer = ByteBuffer.allocate(1024);
        intData = new byte[]{
                (byte) (3 >> 8),
                (byte) 3
        };
        buffer.put(intData);
        buffer.position(2);
        blockData = new byte[]{
                (byte) (10 >> 8),
                (byte) 10
        };
        buffer.put(blockData);
        buffer.put(data);
        packet = new DataPacket(buffer);
        Assert.assertFalse(packet.isLastPacket());
    }

    @Test
    public void ErrorPacketTest(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] intData = {
                (byte) (3 >> 8),
                (byte) 3
        };
        buffer.put(intData);
        byte[] errCodeData = {
                (byte) (1 >> 8),
                (byte) 1
        };
        buffer.put(errCodeData);
        byte[] errorData = "File Not Found.".getBytes();
        buffer.put(errorData);
        buffer.put((byte) 0);
        ErrorPacket packet = new ErrorPacket(buffer);
        Assert.assertEquals("File Not Found.", packet.getErrorMessage());
        Assert.assertEquals(1, packet.getErrorCode());
    }

    @Test
    public void OACKPacketTest(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] intData = {
                (byte) (6 >> 8),
                (byte) 6
        };
        buffer.put(intData);
        buffer.put((byte) 0);
        buffer.put("EncryptionKey".getBytes());
        buffer.put((byte) 0);
        long testLong = ThreadLocalRandom.current().nextLong();
        buffer.putLong(testLong);
        buffer.put(intData);
        OACKPacket packet = new OACKPacket(buffer);
        Assert.assertEquals(testLong, packet.encryptionKey);
    }


    @Test
    public void RRQPacketTest(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        byte[] intData = {
                (byte) (1 >> 8),
                (byte) 1
        };
        buffer.put(intData);
        buffer.put((byte) 0);
        buffer.put("https://i.ytimg.com/vi/9sfYpolGCu8/maxresdefault.jpg".getBytes());
        buffer.put((byte) 0);
        buffer.put("EncryptionKey".getBytes());
        buffer.put((byte) 0);
        long testLong = ThreadLocalRandom.current().nextLong();
        buffer.putLong(testLong);
        RRQPacket packet = new RRQPacket(buffer);
        Assert.assertEquals("https://i.ytimg.com/vi/9sfYpolGCu8/maxresdefault.jpg", packet.getFileURL());
        Assert.assertEquals(testLong, packet.getEncryptionKeyVal());
    }



}
