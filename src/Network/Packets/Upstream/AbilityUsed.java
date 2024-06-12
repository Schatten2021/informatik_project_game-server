package Network.Packets.Upstream;

import Network.Packets.Fields.IntegerField;
import Network.Packets.Packet;

import java.io.IOException;
import java.io.InputStream;

public class AbilityUsed extends Packet {
    public static final byte id = 0x02;
    public final IntegerField abilityId;

    public AbilityUsed(IntegerField abilityId) {
        this.abilityId = abilityId;
    }

    @Override
    public byte[] toBytes() {
        byte[] bytes = new byte[5];
        bytes[0] = id;
        System.arraycopy(abilityId.getBytes(), 0, bytes, 1, 4);
        return bytes;
    }
    public static AbilityUsed fromStream(InputStream stream) throws IOException {
        return new AbilityUsed(IntegerField.fromStream(stream));
    }
}
