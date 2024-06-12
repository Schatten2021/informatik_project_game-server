package Network.Packets;

import java.io.IOException;
import java.io.InputStream;

public abstract class Packet {
    public byte id;
    public abstract byte[] toBytes();
    public static Packet fromStream(InputStream stream) throws IOException {
        throw new RuntimeException("Not implemented yet");
    }
}
