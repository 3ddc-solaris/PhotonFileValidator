package photon.application.extensions.prusasl1.file.utilites;

import photon.application.extensions.prusasl1.configuration.PrusaSL1Configuration;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class RenderedImage {
    private final BufferedImage image;
    private final int[] imageData;

    public RenderedImage(final BufferedImage source) {
        if(BufferedImage.TYPE_INT_RGB == source.getType()) {
            image = source;
        }
        else {
            image = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
            image.getGraphics().drawImage(source, 0, 0, null);
        }
        imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public RenderedImage(final DepthBuffer depthBuffer) {
        this(depthBuffer.getNormalMap());
    }

    public RenderedImage(final NormalMap normalMap) {
        this( new BufferedImage( normalMap.getWidth(), normalMap.getHeight(), BufferedImage.TYPE_INT_RGB ) );

        final PrusaSL1Configuration configuration = PrusaSL1Configuration.getInstance();
        final int resolutionX = normalMap.getWidth();
        final int resolutionY = normalMap.getHeight();

        // calculate preview image from depth buffer
        final Vec3D view = new Vec3D(0, 0, 1);

        final int backgroundColor = configuration.getBackgroundColorRGB();
        final int ambientColor = configuration.getAmbientColorRGB();
        final int diffuseColor = configuration.getDiffuseColorRGB();

        final double dRed = (diffuseColor >> 16) & 0xFF;
        final double dGreen = (diffuseColor >> 8) & 0xFF;
        final double dBlue = diffuseColor & 0xFF;

        final int aRed = (ambientColor >> 16) & 0xFF;
        final int aGreen = (ambientColor >> 8) & 0xFF;
        final int aBlue = ambientColor & 0xFF;


        for(int layer = 0; layer < resolutionY; layer++) {
            for(int x = 0; x < resolutionX; x++) {
                final Vec3D normal = normalMap.getNormal(x, layer);
                int color;

                if(null == normal) {
                    color = backgroundColor;
                }
                else {
                    // output normal as color
                    /*final int red = (int) (255f * ( normal.getX()));
                    final int green = (int) (255f * ( normal.getY()));
                    final int blue = (int) (255f * ( normal.getZ()));*/

                    final double intensity = Math.max(view.dotProduct(normal), 0);
                    final int red = Math.min(aRed + (int) (dRed * intensity), 0xFF);
                    final int green = Math.min(aGreen + (int) (dGreen * intensity), 0xFF);
                    final int blue = Math.min(aBlue + (int) (dBlue * intensity), 0xFF);

                    color = red << 16 | green << 8 | blue;
                }

                // direct out
                //imageData[resolutionX*layer+x] = color;

                // rotate about 180 degree
                imageData[resolutionX*(resolutionY-(layer+1)) + (resolutionX-(x+1))] = color;
            }
        }
    }

    public RenderedImage getScaledImage(final int maxWidth, final int maxHeight) {
        final double scaleX = (double) maxWidth / (double) getWidth();
        final double scaleY = (double) maxHeight / (double) getHeight();

        final double scale = Math.min(scaleX, scaleY);
        final int width = (int) ((double) getWidth() * scale);
        final int height = (int) ((double) getHeight() * scale);

        final BufferedImage after = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        final AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

        return new RenderedImage(scaleOp.filter(image, after));
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int[] getImageData() {
        return imageData;
    }

    public int getImageDataSize() {
        return imageData.length;
    }
}
