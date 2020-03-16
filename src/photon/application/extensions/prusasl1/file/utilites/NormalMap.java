package photon.application.extensions.prusasl1.file.utilites;

public class NormalMap {
    private final int width;
    private final int height;
    private final Vec3D[] map;

    public NormalMap(final int width, final int height, final int[] zBufferData) {
        this.width = width;
        this.height = height;
        this.map = new Vec3D[width*height];

        // https://stackoverflow.com/questions/34644101/calculate-surface-normals-from-depth-image-using-neighboring-pixels-cross-produc
        /*
        dzdx=(z(x+1,y)-z(x-1,y))/2.0;
        dzdy=(z(x,y+1)-z(x,y-1))/2.0;
        direction=(-dzdx,-dzdy,1.0)
        magnitude=sqrt(direction.x**2 + direction.y**2 + direction.z**2)
        normal=direction/magnitude
         */
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                final int pos = width*y+x;
                if(DepthBuffer.EMPTY == zBufferData[pos]) {
                    map[pos] = null;
                }
                else {
                    final double dzdx=(Z(x+1,y,zBufferData)-Z(x-1,y,zBufferData))/2.0;
                    final double dzdy=(Z(x,y+1,zBufferData)-Z(x,y-1,zBufferData))/2.0;

                    final Vec3D normal = new Vec3D(-dzdx,-dzdy,1.0);
                    normal.normalize();
                    map[pos] = normal;
                }
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Vec3D getNormal(final int x, final int y) {
        return map[width*y+x];
    }

    private int X(final int x) {
        if(x < 0 ) return 0;
        if(x >= width) return width-1;
        return x;
    }

    private int Y(final int y) {
        if(y < 0 ) return 0;
        if(y >= height) return height-1;
        return y;
    }

    private int Z(final int x, final int y, final int[] zBufferData) {
        final int z = zBufferData[width*Y(y)+X(x)];
        return DepthBuffer.EMPTY == z ? Integer.MAX_VALUE : z;
    }
}
