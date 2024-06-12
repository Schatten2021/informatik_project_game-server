package Network.Packets.Downstream;

import Network.Packets.Fields.BooleanField;
import Network.Packets.Packet;

import java.io.IOException;
import java.io.InputStream;

public class GameEnd extends Packet {
    public static final byte id = 0x02;
    public final boolean victory;
    public GameEnd(boolean victory) {
        this.victory = victory;
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[2];
        bytes[0] = id;
        bytes[1] = new BooleanField(this.victory).getBytes()[0];
        return bytes;
    }
    public static GameEnd fromStream(InputStream stream) throws IOException {
        return new GameEnd(BooleanField.fromStream(stream).value);
    }
}
