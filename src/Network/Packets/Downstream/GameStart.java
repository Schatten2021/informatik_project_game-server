package Network.Packets.Downstream;

import Network.Packets.Fields.StringField;
import Network.Packets.Fields.FloatField;
import Network.Packets.Packet;

import java.io.IOException;
import java.io.InputStream;


public class GameStart extends Packet {
    public static final byte id = 0x01;
    public final StringField otherName;
    public final FloatField HP;
    public final FloatField MP;
    public GameStart(StringField otherName, FloatField HP, FloatField MP) {
        this.otherName = otherName;
        this.HP = HP;
        this.MP = MP;
    }
    public GameStart(String otherName, float HP, float MP) {
        this.otherName = new StringField(otherName);
        this.HP = new FloatField(HP);
        this.MP = new FloatField(MP);
    }

    @Override
    public byte[] toBytes() {
        byte[] stringBytes = otherName.getBytes();
        byte[] result = new byte[stringBytes.length + 9];
        result[0] = id;
        System.arraycopy(stringBytes, 0, result, 1, stringBytes.length);
        System.arraycopy(this.HP.getBytes(), 0, result, stringBytes.length + 1, HP.getBytes().length);
        System.arraycopy(this.MP.getBytes(), 0, result, stringBytes.length + 5, MP.getBytes().length);
        return result;
    }

    public static GameStart fromStream(InputStream stream) throws IOException {
        return new GameStart(StringField.fromStream(stream), FloatField.fromStream(stream), FloatField.fromStream(stream));
    }
}
