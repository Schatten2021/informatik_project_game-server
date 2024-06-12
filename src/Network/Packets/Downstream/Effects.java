package Network.Packets.Downstream;

import Network.Packets.Fields.ArrayField;
import Network.Packets.Fields.EffectField;
import Network.Packets.Packet;

import java.io.IOException;
import java.io.InputStream;

public class Effects extends Packet {
    public static final byte id = 0x05;
    public final ArrayField<EffectField> effects;

    public Effects(ArrayField<EffectField> effects) {
        this.effects = effects;
    }

    @Override
    public byte[] toBytes() {
        byte[] effectBytes = this.effects.getBytes();
        byte[] bytes = new byte[effectBytes.length + 1];
        bytes[0] = id;
        System.arraycopy(effectBytes, 0, bytes, 1, effectBytes.length);
        return bytes;
    }

    public static Effects fromStream(InputStream stream) throws IOException {
        return new Effects(ArrayField.fromStream(stream));
    }
}
