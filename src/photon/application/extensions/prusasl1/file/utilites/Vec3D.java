package photon.application.extensions.prusasl1.file.utilites;

public class Vec3D {
    private double x;
    private double y;
    private double z;

    public Vec3D() {
    }

    public Vec3D(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getLength() {
        return Math.sqrt( Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2) );
    }

    public void normalize() {
        final double length = getLength();
        x = x / length;
        y = y / length;
        z = z / length;
    }

    public Vec3D crossProduct(Vec3D v2) {
        return new Vec3D(y * v2.z - z * v2.y, z * v2.x - x * v2.z, x * v2.y - y * v2.x);
    }

    public double dotProduct(Vec3D v2) {
        return x * v2.x + y * v2.y + z * v2.z;
    }
}
