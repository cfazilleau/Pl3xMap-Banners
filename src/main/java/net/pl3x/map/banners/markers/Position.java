package net.pl3x.map.banners.markers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.pl3x.map.core.markers.Point;

public record Position(int x, int y, int z) {
    public Point toPoint() {
        return Point.of(x(), z());
    }

    public static Position load(DataInputStream in) throws IOException {
        return new Position(in.readInt(), in.readInt(), in.readInt());
    }

    public void save(DataOutputStream out) throws IOException {
        out.writeInt(x());
        out.writeInt(y());
        out.writeInt(z());
    }
}
