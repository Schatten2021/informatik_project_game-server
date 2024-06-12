package Network.Packets.Fields;

import java.io.IOException;
import java.io.InputStream;

public class BooleanField implements Field {
    public final boolean value;
    public BooleanField(boolean value) {
        this.value = value;
    }
    public BooleanField(byte value) {
        this.value = (value > 0);
    }

    public byte[] getBytes() {
        return new byte[] {
                (byte) (this.value ? 0x01 : 0x00)
        };
    }
    public static BooleanField fromStream(InputStream stream) throws IOException {
        if (stream.available() <= 0)
            throw new IOException("Stream is empty");
        return new BooleanField(stream.read() > 0);
    }
}
