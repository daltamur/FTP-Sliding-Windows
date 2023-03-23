import org.junit.Assert;
import org.junit.Test;
import org.tftp.packets.*;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class PacketTests {
    @Test
    public void ACKPacketTest(){
        ByteBuffer buffer = new PacketFactory().makeAckPacket(10);
        ACKPacket packet = new ACKPacket(buffer);
        Assert.assertEquals(packet.getBlockNumber(), 10);
    }

    @Test
    public void DataPacketTest(){
        ByteBuffer buffer = new PacketFactory().makeDataPacket("I'm a single cell on a serpent's tongue".getBytes(), 10);
        DataPacket packet = new DataPacket(buffer);
        Assert.assertEquals(packet.getBlockNumber(), 10);
        Assert.assertEquals(new String(packet.getData()), "I'm a single cell on a serpent's tongue");
        Assert.assertTrue(packet.isLastPacket());
        buffer = new PacketFactory().makeDataPacket(new byte[512], 10);
        packet = new DataPacket(buffer);
        Assert.assertFalse(packet.isLastPacket());
    }

    @Test
    public void ErrorPacketTest(){
        ByteBuffer buffer = new PacketFactory().makeErrorPacket("File Not Found.", 1);
        ErrorPacket packet = new ErrorPacket(buffer);
        Assert.assertEquals("File Not Found.", packet.getErrorMessage());
        Assert.assertEquals(1, packet.getErrorCode());
    }

    @Test
    public void OACKPacketTest(){
        long testLong = ThreadLocalRandom.current().nextLong();
        ByteBuffer buffer = new PacketFactory().makeOACKPacket(testLong);
        OACKPacket packet = new OACKPacket(buffer);
        Assert.assertEquals(testLong, packet.getEncryptionKey());
    }


    @Test
    public void RRQPacketTest(){
        long testLong = ThreadLocalRandom.current().nextLong();
        ByteBuffer buffer = new PacketFactory().makeRRQPacket("https://i.ytimg.com/vi/9sfYpolGCu8/maxresdefault.jpg", testLong);
        RRQPacket packet = new RRQPacket(buffer);
        Assert.assertEquals("https://i.ytimg.com/vi/9sfYpolGCu8/maxresdefault.jpg", packet.getFileURL());
        Assert.assertEquals(testLong, packet.getEncryptionKeyVal());
    }



}
