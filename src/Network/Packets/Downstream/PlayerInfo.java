package Network.Packets.Downstream;

import Network.Packets.Fields.AbilityField;
import Network.Packets.Fields.ArrayField;
import Network.Packets.Packet;

import java.io.IOException;
import java.io.InputStream;

public class PlayerInfo extends Packet {
    public static final byte id = 0x04;
    public final ArrayField<AbilityField> abilities;
    public PlayerInfo(ArrayField<AbilityField> abilities) {
        this.abilities = abilities;
    }

    @Override
    public byte[] toBytes() {
        byte[] abilitiesBytes = this.abilities.getBytes();
        byte[] bytes = new byte[abilitiesBytes.length + 1];
        bytes[0] = id;
        System.arraycopy(abilitiesBytes, 0, bytes, 1, abilitiesBytes.length);
        return bytes;
    }

    public static PlayerInfo fromStream(InputStream stream) throws IOException {
        return new PlayerInfo(ArrayField.fromStream(stream));
    }
}
