package Network;

import Abitur.Queue;
import Database.Types.Player;
import Network.Packets.Downstream.RoundEnd;
import Network.Packets.Heartbeat;
import Network.Packets.Packet;
import Network.Packets.Error;
import Network.Packets.Upstream.*;
import logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class Connection {
    private static final long heartbeatFrequency = 1000;

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
        this.logger = new Logger("Network.Connection." + socket.getRemoteSocketAddress().toString());
    }
    public void send(Packet packet) throws IOException {
        this.logger.debug("Sending packet " + packet + " to " + this);
        this.out.write(packet.toBytes());
    }
    public void update() throws IOException {
        if (this.delete) {
            throw new IOException("Connection marked for deletion!");
        }
        while (this.in.available() > 0) {
            byte id = this.in.readNBytes(1)[0];
            //TODO: fix
            Packet result;
            switch (id) {
                case Heartbeat.id:
                    result = Heartbeat.fromStream(this.in);
                    break;
                case Login.id:
                    result = Login.fromStream(this.in);
                    break;
                case AbilityUsed.id:
                    result = AbilityUsed.fromStream(this.in);
                    break;
                case RoundFinished.id:
                    result = RoundFinished.fromStream(this.in);
                    break;
                case SignUP.id:
                    result = SignUP.fromStream(this.in);
                    break;
                case Error.id:
                    result = Error.fromStream(this.in);
                    break;
                default:
                    throw new IOException("Invalid packet id " + id);
            }
            if (result instanceof Heartbeat) {
                continue;
            }
            this.incoming.enqueue(result);
            this.logger.debug("received packet " + result.getClass().getName());
        }
        long now = System.currentTimeMillis();
        if ((now - lastHeartbeat) > heartbeatFrequency) {
//            this.logger.fdebug("between now (%d) and lastHeartbeat (%d) were %d milliseconds.", now, lastHeartbeat, now-lastHeartbeat);
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
