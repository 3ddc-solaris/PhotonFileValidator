package photon.application.extensions.prusasl1.file.utilites;

public class DepthBuffer {
    public static final int EMPTY = -1;

    private final int width;
    private final int numLayers;
    private final int[] buffer;

    private int minX = Integer.MAX_VALUE;
    private int maxX = 0;
    private int minDepth = Integer.MAX_VALUE;
    private int maxDepth = 0;

    public DepthBuffer(final int numLayers, final int width) {
        this.width = width;
        this.numLayers = numLayers;
        this.buffer = new int[width*numLayers];
    }

    public synchronized void setDepth(final int layerNum, final int xCoordinate, final int depthValue) {
        buffer[width*layerNum + xCoordinate] = depthValue;

        if(EMPTY != depthValue) {
            if(xCoordinate < minX) minX = xCoordinate;
            if(xCoordinate > maxX) maxX = xCoordinate;
            if(depthValue < minDepth) minDepth = depthValue;
            if(depthValue > maxDepth) maxDepth = depthValue;
        }
    }

    public int getDepth(final int layerNum, final int xCoordinate) {
        return buffer[width*layerNum + xCoordinate];
    }

    public int getWidth() {
        return width;
    }

    public int getNumLayers() {
        return numLayers;
    }

    public NormalMap getNormalMap() {
        //  check for empty file
        if(minX >= maxX || minDepth >= maxDepth)
            throw new IllegalStateException("Empty Z-Buffer");

        // clamp buffer to object size
        final int normalizedWidth = maxX - minX;
        final int[] normalizedBuf = new int[normalizedWidth*numLayers];

        for(int y = 0; y < numLayers; y++) {
            for(int x = 0; x < normalizedWidth; x++) {
                final int rawDepth = buffer[width*y + minX + x];
                normalizedBuf[normalizedWidth*y + x] = EMPTY == rawDepth ? EMPTY : rawDepth - minDepth;
            }
        }

        return new NormalMap(normalizedWidth, numLayers, normalizedBuf);
    }

}
