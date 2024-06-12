package Network.Packets;

import Network.Packets.Fields.IntegerField;

import java.io.IOException;
import java.io.InputStream;

public class Error extends Packet {
    public static final byte id = (byte) 0xFF;
    public final int code;
    public Error(int code) {
        this.code = code;
    }

    @Override
    public byte[] toBytes() {
        byte[] result = new byte[5];
        result[0] = id;
        System.arraycopy(new IntegerField(code).getBytes(), 0, result, 1, 4);
        return result;
    }

    public static Error fromStream(InputStream stream) throws IOException {
        return new Error(IntegerField.fromStream(stream).value);
    }
}
