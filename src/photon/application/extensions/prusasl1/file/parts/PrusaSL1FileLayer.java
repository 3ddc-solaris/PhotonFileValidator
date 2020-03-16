package photon.application.extensions.prusasl1.file.parts;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PrusaSL1FileLayer {
    private final int index;
    private final InputStream dataStream;

    public PrusaSL1FileLayer(final int index, final InputStream dataStream) {
        this.index = index;
        this.dataStream = dataStream;
    }

    public int getIndex() {
        return index;
    }

    public BufferedImage getImage() throws IOException {
        return ImageIO.read(new BufferedInputStream(dataStream));
    }
}
