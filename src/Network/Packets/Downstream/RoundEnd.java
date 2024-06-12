package Network.Packets.Downstream;

import Network.Packets.Packet;

import java.io.IOException;
import java.io.InputStream;

public class RoundEnd extends Packet {
    public static final byte id = 0x03;
    public RoundEnd() {}

    @Override
    public byte[] toBytes() {
        return new byte[]{id};
    }

    public static RoundEnd fromStream(InputStream stream) throws IOException {
        return new RoundEnd();
    }
}
