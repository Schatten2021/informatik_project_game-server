package Network.Packets.Upstream;

import Network.Packets.Fields.StringField;
import Network.Packets.Packet;

import java.io.IOException;
import java.io.InputStream;

public class SignUP extends Packet {
    public static final byte id = 0x04;
    public final StringField username;
    public final StringField password;
    public SignUP(String username, String password) {
        this.username = new StringField(username);
        this.password = new StringField(password);
    }
    public SignUP(StringField username, StringField password) {
        this.username = username;
        this.password = password;
    }
    public byte[] toBytes() {
        byte[] usernameBytes = username.getBytes();
        byte[] passwordBytes = password.getBytes();
        byte[] bytes = new byte[usernameBytes.length + passwordBytes.length + 1];
        bytes[0] = id;
        System.arraycopy(usernameBytes, 0, bytes, 1, usernameBytes.length);
        System.arraycopy(passwordBytes, 0, bytes, usernameBytes.length + 1, passwordBytes.length);
        return bytes;
    }
    public static SignUP fromStream(InputStream stream) throws IOException {
        return new SignUP(StringField.fromStream(stream), StringField.fromStream(stream));
    }
}
