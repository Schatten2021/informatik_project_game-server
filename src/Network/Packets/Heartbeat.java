package Network.Packets;

import java.io.IOException;
import java.io.InputStream;

public class Heartbeat extends Packet {
    public static final byte id = 0x00;
    public Heartbeat() {}

    @Override
    public byte[] toBytes() {
        return new byte[]{0x00};
    }
    public static Heartbeat fromStream(InputStream stream) throws IOException {
        return new Heartbeat();
    }
}
