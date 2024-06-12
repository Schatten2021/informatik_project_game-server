package Network.Packets.Fields;

import java.io.IOException;
import java.io.InputStream;

public class EffectField implements Field {
    private final int id;
    private final String name;
    private final int valueAffected;
    private final int time;
    private final float min;
    private final float max;
    private final boolean isPercent;
    public EffectField(int id, String name, int valueAffected, int time, float min, float max, boolean isPercent) {
        this.id = id;
        this.name = name;
        this.valueAffected = valueAffected;
        this.time = time;
        this.min = min;
        this.max = max;
        this.isPercent = isPercent;
    }

    public byte[] getBytes() {
        byte[] stringBytes = new StringField(this.name).getBytes();
        // ID: 4; Value affected: 4; Time: 4; Min: 4; max: 4; relative: 1; string: ?
        // = 4 * 5 + 1 + ? = 21+
        int size = 21 + stringBytes.length;
        byte[] finalBytes = new byte[size];
        System.arraycopy(new IntegerField(this.id).getBytes(), 0, finalBytes, 0, 4);
        System.arraycopy(stringBytes, 0, finalBytes, 8, stringBytes.length);
        System.arraycopy(new IntegerField(this.valueAffected).getBytes(), 0, finalBytes, 8 + stringBytes.length, 4);
        System.arraycopy(new IntegerField(this.time).getBytes(), 0, finalBytes, 16 + stringBytes.length, 4);
        System.arraycopy(new FloatField(this.min).getBytes(), 0, finalBytes, 24 + stringBytes.length, 4);
        System.arraycopy(new FloatField(this.max).getBytes(), 0, finalBytes, 32 + stringBytes.length, 4);
        System.arraycopy(new BooleanField(this.isPercent).getBytes(), 0, finalBytes, 40 + stringBytes.length, 4);
        return finalBytes;
    }

    public static EffectField fromStream(InputStream stream) throws IOException {
        IntegerField id = IntegerField.fromStream(stream);
        StringField name = StringField.fromStream(stream);
        IntegerField valueAffected = IntegerField.fromStream(stream);
        IntegerField time = IntegerField.fromStream(stream);
        FloatField min = FloatField.fromStream(stream);
        FloatField max = FloatField.fromStream(stream);
        BooleanField isPercent = BooleanField.fromStream(stream);
        return new EffectField(id.value, name.value, valueAffected.value, time.value, min.value, max.value, isPercent.value);
    }
}
