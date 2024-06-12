package Network.Packets.Fields;

import Abitur.Queue;

import java.io.IOException;
import java.io.InputStream;

public class ArrayField <T extends Field> implements Field {
    private final T[] fields;
    public ArrayField(T[] fields) {
        this.fields = fields;
    }

    public T[] getValue() {
        return this.fields;
    }

    @Override
    public byte[] getBytes() {
        Queue<byte[]> queue = new Queue<>();
        int length = 4;
        for (T field : this.fields) {
            byte[] fieldBytes = field.getBytes();
            queue.enqueue(fieldBytes);
            length += fieldBytes.length;
        }
        byte[] bytes = new byte[length + 4];
        System.arraycopy(bytes, 0, new IntegerField(length).getBytes(), 0, 4);
        int pos = 4;
        while (!queue.isEmpty()) {
            byte[] elementBytes = queue.front();
            System.arraycopy(elementBytes, 0, bytes, pos, elementBytes.length);
            pos += elementBytes.length;
        }
        return bytes;
    }
    public static <T extends Field> ArrayField<T> fromStream(InputStream stream) throws IOException {
        IntegerField length = IntegerField.fromStream(stream);
        //noinspection unchecked
        T[] data = (T[]) new Field[length.value];
        for (int i = 0; i < length.value; i++) {
            //noinspection unchecked
            data[i] = (T) T.fromStream(stream);
        }
        return new ArrayField<>(data);
    }
}
