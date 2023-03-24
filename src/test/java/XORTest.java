import org.junit.Assert;
import org.junit.Test;
import org.tftp.packets.DataPacket;
import org.tftp.packets.PacketFactory;
import org.tftp.utilities.XOREncryption;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class XORTest {
    @Test
    public void TestXOR(){
        long key = ThreadLocalRandom.current().nextLong();
        ByteBuffer dataPacket = new PacketFactory().makeDataPacket("I'm a can on the string, you're on the end".getBytes(), 15);
        XOREncryption.XORBuffer(dataPacket, key);
        DataPacket packet = new DataPacket(dataPacket);
        Assert.assertNotEquals("I'm a can on the string, you're on the end", new String(packet.getData()));
        Assert.assertNotEquals(15, packet.getBlockNumber());
        //reset buffer position back to zero
        dataPacket.position(0);
        //immediately decrypt after encrypting, let's see if it worked
        XOREncryption.XORBuffer(dataPacket, key);
        packet = new DataPacket(dataPacket);
        Assert.assertEquals("I'm a can on the string, you're on the end", new String(packet.getData()));
        Assert.assertEquals(15, packet.getBlockNumber());
    }
}
