import org.junit.Test;
import org.tftp.packets.DataPacket;
import org.tftp.server.Image;
import org.tftp.server.ImageGrabber;
import org.tftp.server.RequestHandler;
import org.tftp.utilities.ImageViewer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class FrameTest {

    @Test
    public void FrameBreakupTest() throws IOException {
        ArrayList<ByteBuffer> frames = RequestHandler.getDataFrames(ByteBuffer.wrap(ImageGrabber.getImage("https://i.ytimg.com/vi/9sfYpolGCu8/maxresdefault.jpg").getImageData()));
        //reassemble and see if it gives back the image we're expecting
        int totalSize = 512*(frames.size()-1);
        totalSize+= frames.get(frames.size()-1).limit()-4;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for(ByteBuffer buffer: frames){
            bytes.write(new DataPacket(buffer).getData());
        }

        //this is kinda hard to test with junits but it does work
        byte[] reformedByteArray = bytes.toByteArray();
        InputStream is = new ByteArrayInputStream(reformedByteArray);
        BufferedImage image = ImageIO.read(is);
        new ImageViewer(image).showImage();

    }
}
