package Network.Packets.Fields;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StringField implements Field {
    public final String value;
    public StringField(String value){
        this.value = value;
    }

    @Override
    public byte[] getBytes() {
        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);
        IntegerField size = new IntegerField(stringBytes.length);
        byte[] arr = new byte[stringBytes.length + 4];
        System.arraycopy(size.getBytes(), 0, arr, 0, 4);
        System.arraycopy(stringBytes, 0, arr, 4, stringBytes.length);
        return arr;
    }
    public static StringField fromStream(InputStream stream) throws IOException {
        IntegerField size = IntegerField.fromStream(stream);
        int pos = 0;
        int available = stream.available();
        byte[] bytes = new byte[size.value];
        while (available > 0 && pos < bytes.length) {
            int readCount = Math.min(available, bytes.length - pos);
            System.arraycopy(stream.readNBytes(readCount), 0, bytes, pos, readCount);
            available = stream.available();
            pos += readCount;
        }
        if (pos < bytes.length)
            throw new IOException("Stream did not read enough bytes");
        return new StringField(new String(bytes, StandardCharsets.UTF_8));
    }
}
