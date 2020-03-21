/*
 * MIT License
 *
 * Copyright (c) 2018 Bonosoft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package photon.file.parts;

import photon.application.extensions.prusasl1.file.utilites.RenderedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * by bn on 01/07/2018.
 */
public class PhotonFilePreview {
    private int resolutionX;
    private int resolutionY;
    private int imageAddress;
    private int dataSize;

    private byte[] rawImageData;

    private int[] imageData;

    private int p1;
    private int p2;
    private int p3;
    private int p4;

    public PhotonFilePreview(int previewAddress, byte[] file) throws Exception {
        byte[] data = Arrays.copyOfRange(file, previewAddress, previewAddress + 32);
        PhotonInputStream ds = new PhotonInputStream(new ByteArrayInputStream(data));

        resolutionX = ds.readInt();
        resolutionY = ds.readInt();
        imageAddress = ds.readInt();
        dataSize = ds.readInt();
        p1 = ds.readInt();
        p2 = ds.readInt();
        p3 = ds.readInt();
        p4 = ds.readInt();

        rawImageData = Arrays.copyOfRange(file, imageAddress, imageAddress + dataSize);

        decodeImageData();
    }

    public PhotonFilePreview(int resolutionX, int resolutionY) throws Exception {
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
        imageAddress = 0;
        dataSize = resolutionX * resolutionY * 2;
        p1 = 0;
        p2 = 0;
        p3 = 0;
        p4 = 0;

        rawImageData = new byte[dataSize];

        decodeImageData();
    }

    public PhotonFilePreview(final RenderedImage image) throws Exception {
        this.resolutionX = image.getWidth();
        this.resolutionY = image.getHeight();
        dataSize = 0;
        p1 = 0;
        p2 = 0;
        p3 = 0;
        p4 = 0;

        // encode image data
        imageData = image.getImageData();
        encodeImageData();

        // re-decode image. verification only. not realy needed.
        imageData = null;
        decodeImageData();
    }

    public void save(PhotonOutputStream os, int startAddress) throws Exception {
        os.writeInt(resolutionX);
        os.writeInt(resolutionY);
        os.writeInt(startAddress + 4+4+4+4 + 4+4+4+4);
        os.writeInt(dataSize);
        os.writeInt(p1);
        os.writeInt(p2);
        os.writeInt(p3);
        os.writeInt(p4);
        os.write(rawImageData, 0, dataSize);
    }

    public int getByteSize() {
        return 4+4+4+4 + 4+4+4+4 + dataSize;
    }

    private void decodeImageData() {
        imageData = new int[resolutionX * resolutionY];
        int d = 0;
        for (int i = 0; i < dataSize; i++) {
            int dot = rawImageData[i] & 0xFF | ((rawImageData[++i] & 0xFF) << 8);

            int color =   ((dot & 0xF800) << 8) | ((dot & 0x07C0) << 5) | ((dot & 0x001F) << 3);

//            int red = ((dot >> 11) & 0x1F) << 3;
//            int green = ((dot >> 6) & 0x1F) << 3;
//            int blue = (dot & 0x1F) << 3;
//            color = red<<16 | green<<8 | blue;

            int repeat = 1;
            if ((dot & 0x0020) == 0x0020) {
                repeat += rawImageData[++i] & 0xFF | ((rawImageData[++i] & 0x0F) << 8);
            }

            while (repeat > 0) {
                imageData[d++] = color;
                repeat--;
            }
        }
    }

    public int getResolutionX() {
        return resolutionX;
    }

    public int getResolutionY() {
        return resolutionY;
    }

    public int[] getImageData() {
        return imageData;
    }

    public void unLink() {
        rawImageData = null;
        imageData = null;
    }

    private void encodeImageData() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        for(int i = 0; i < imageData.length; i++) {
            final int pixel = imageData[i];

            // read components
            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;

            // reduce to 5 bit color
            red =  map(red, 0, 0xFF, 0, 0x1F) & 0x1F;
            green = map(green, 0, 0xFF, 0, 0x1F) & 0x1F;
            blue = map(blue, 0, 0xFF, 0, 0x1F) & 0x1F;

            // compose dot
            int dot = ((red << 11) | (green << 6) | blue) & 0xFFDF;

            write(out, dot );
        }

        rawImageData = out.toByteArray();
        dataSize = rawImageData.length;
    }

    private int map(int x, int in_min, int in_max, int out_min, int out_max)  {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private void write(final ByteArrayOutputStream out, final int bytes) {
        // write low byte
        out.write( (byte) bytes );

        // write high byte
        out.write( (byte) (bytes >> 8)  );
    }
}
