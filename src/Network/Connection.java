package Network;

import Abitur.Queue;
import Database.Types.Player;
import Network.Packets.Heartbeat;
import Network.Packets.Packet;
import Network.Packets.Error;
import Network.Packets.Upstream.*;
import logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection {
    private static final long heartbeatFrequency = 10;

    private final Socket socket;
    public final Queue<Packet> incoming = new Queue<>();
    private final InputStream in;
    private final OutputStream out;
    private final Logger logger;
    private long lastHeartbeat = 0;
    private boolean delete = false;
    public Player player;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = this.socket.getInputStream();
        this.out = this.socket.getOutputStream();
        this.logger = new Logger("Network.ConnectionHost.Connection" + socket.getRemoteSocketAddress().toString());
    }
    public void send(Packet packet) throws IOException {
        this.out.write(packet.toBytes());
    }
    public void update() throws IOException {
        if (this.delete) {
            throw new IOException("Connection marked for deletion!");
        }
        while (this.in.available() > 0) {
            byte id = this.in.readNBytes(1)[0];
            Packet result = switch (id) {
                case 0x00 -> Heartbeat.fromStream(this.in);
                case 0x01 -> Login.fromStream(this.in);
                case 0x02 -> AbilityUsed.fromStream(this.in);
                case 0x03 -> RoundFinished.fromStream(this.in);
                case 0x04 -> SignUP.fromStream(this.in);
                case (byte) 0xFF -> Error.fromStream(this.in);
                default -> throw new IOException("Invalid packet id " + id);
            };
            if (result instanceof Heartbeat) {
                continue;
            }
            this.incoming.enqueue(result);
            this.logger.debug("received packet " + result.getClass().getName());
        }
        long now = System.currentTimeMillis();
        if (now - lastHeartbeat > heartbeatFrequency) {
            this.lastHeartbeat = now;
            this.send(new Heartbeat());
        }
    }

    public String toString() {
        return String.format("<Socket to %s >", this.socket.getRemoteSocketAddress());
    }

    public void stop() {
        try {
            this.socket.close();
        } catch (IOException e) {
            this.logger.error(e.getMessage());
        }
    }
    public boolean isDeleted() {
        return this.delete || this.socket.isClosed();
    }
    public void markForDeletion() {
        this.delete = true;
        this.stop();
    }
}
