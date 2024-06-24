

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import Abitur.List;
import Abitur.Queue;
import Database.DataBase;
import Database.Types.*;
import Database.Types.AbilityUsed;
import Network.Connection;
import Network.Packets.Downstream.*;
import Network.Packets.Error;
import Network.Packets.Fields.*;
import Network.Packets.Heartbeat;
import Network.Packets.Packet;

import Network.Packets.Upstream.Login;
import Network.Packets.Upstream.RoundFinished;
import Network.Packets.Upstream.SignUP;
import logging.Logger;

@SuppressWarnings("unused")
public class Server {
    private final Network.ConnectionHost connectionHost;
    private final DataBase db;
    private final Logger logger = new Logger("Server");
    private Thread thread;

    private final Queue<Connection> availablePlayers = new Queue<>();

    public Server(int programPort, String dataBaseHost, int dbPort, String dbUser, String dbPassword, String dbName) throws IOException, SQLException {
        this.connectionHost = new Network.ConnectionHost(programPort);
        this.db = new DataBase(dataBaseHost, dbPort, dbUser, dbPassword, dbName);
    }
    public void start() {
        if (thread != null) {
            this.logger.warn("Already started");
            return;
        }
        this.connectionHost.start();
        this.thread = new Thread(this::thread);
        this.thread.start();
    }
    public void stop() {
        this.thread.interrupt();
        this.connectionHost.stop();
        while (!this.availablePlayers.isEmpty())
            this.availablePlayers.dequeue();
    }

    private void thread() {
        while (this.connectionHost.isAlive() && !this.thread.isInterrupted()) {
            this.connectionHost.updateConnection();
            this.handleIncomingData();
            this.checkWaitingConnections();
            this.checkNewGames();
            this.handleOutgoingData();
            this.incrementRounds();
        }
        logger.info("exiting thread");
    }

    private void handleIncomingData() {
        Queue<Connection> connections = this.connectionHost.getConnectionsWithData();
        while (!connections.isEmpty()) {
            Connection connection = connections.front();
            Queue<Packet> packets = connection.incoming;
            while (!packets.isEmpty()) {
                Packet packet = packets.front();
                this.handlePacket(packet, connection);
                packets.dequeue();
            }
            connections.dequeue();
        }
    }
    private void handlePacket(Packet packet, Connection connection) {
        if (packet instanceof SignUP) {
            this.handleSignUp((SignUP) packet, connection);
        } else if (packet instanceof Login) {
            this.handleLogin((Login) packet, connection);
        } else if (packet instanceof RoundFinished) {
            this.handleRoundFinished(connection);
        } else if (packet instanceof Network.Packets.Upstream.AbilityUsed) {
            this.handleAbilityUsed((Network.Packets.Upstream.AbilityUsed) packet, connection);
        } else if (packet instanceof Heartbeat) {
        } else if (packet instanceof Error) {
            this.handleError((Error) packet, connection);
        } else if (packet == null) {
            logger.error("Got null packet from " + connection);
        } else {
            logger.fdebug("Got unhandled packet: %s from connection: %s", packet.getClass().getName(), connection);
        }
    }

    private void handleSignUp(SignUP packet, Connection connection) {
        try {
            Player player = this.db.signUp(packet.username.value, packet.password.value);
            connection.player = player;
            this.availablePlayers.enqueue(connection); // player not already in a game
            this.logger.info("Registered player " + player.name);
            this.logger.info("Player + \"" + player.name + "\" now waiting for a game.");
            postLogin(connection);
        } catch (SQLException e) {
            String msg = String.format("Registration failed on connection %s with username \"%s\" because of \"%s\"", connection, packet.username.value, e);
            this.logger.error(msg);
            connection.markForDeletion();
        }
    }
    private void handleLogin(Login packet, Connection connection) {
        try {
            Player player = this.db.login(packet.username, packet.password);
            if (player == null) {
                String msg = String.format("User \"%s\" couldn't login with hash: \"%s\"", packet.username, packet.password);
                this.logger.error(msg);
                connection.markForDeletion();
                return;
            }
            connection.player = player;
            this.logger.debug("Logged in player \"" + player.name + "\" in game: " + player.inGame);
            postLogin(connection);
            if (!player.inGame) {
                this.availablePlayers.enqueue(connection);
                this.logger.info("Player \"" + player.name + "\" now waiting for a game.");
            } else {
                this.playerReenterGame(connection);
            }
        } catch (SQLException | IOException e) {
            connection.markForDeletion();
            String msg = String.format("Login failed on connection %s with username \"%s\" because of \"%s\"", connection, packet.username, e);
            logger.error(msg);
        }
    }
    private void postLogin(Connection connection) {
        try {
            Effect[] allEffects = this.db.allEffects();
            EffectField[] fields = new EffectField[allEffects.length];
            for (int i = 0; i < fields.length; i++) {
                Effect e = allEffects[i];
                fields[i] = new EffectField(e.id, e.name, e.valueEffected, e.time, e.min, e.max, e.relative, e.hitSelf);
            }
            connection.send(new Effects(new ArrayField<>(fields)));
        } catch (SQLException | IOException e) {
            this.logger.ferror("Couldn't send all effects to %s because of \"%s\"", connection, e);
            connection.markForDeletion();
            return;
        }
        try {
            Ability[] allAbilities = this.db.allAbilities();
            AbilityField[] fields = new AbilityField[allAbilities.length];
            for (int i = 0; i < fields.length; i++) {
                Ability a = allAbilities[i];
                IntegerField[] effectIds = new IntegerField[a.effects.length];
                for (int j = 0; j < effectIds.length; j++) {
                    effectIds[j] = new IntegerField(a.effects[j].id);
                }
                fields[i] = new AbilityField(new IntegerField(a.id), new StringField(a.name), new FloatField(a.cost), new ArrayField<>(effectIds));
            }
            connection.send(new Abilities(new ArrayField<>(fields)));
        } catch (SQLException | IOException e) {
            this.logger.ferror("Couldn't send all abilities to %s because of \"%s\"", connection, e);
            connection.markForDeletion();
        }

    }
    private void playerReenterGame(Connection connection) throws SQLException, IOException {
        Player player = connection.player;
        if (player.currentGame == null) {
            this.logger.fatal("player in game but no running games found!");
            this.db.playerNotInGame(player);
            this.availablePlayers.enqueue(connection);
            return;
        }
        this.sendGame(connection, player.currentGame);
        String msg = String.format("Player \"%s\" has continued playing", player.name);
        this.logger.info(msg);
    }

    private void handleAbilityUsed(Network.Packets.Upstream.AbilityUsed abilityUsed, Connection connection) {
        try {
            Ability usedAbility = Ability.load(abilityUsed.abilityId.value, this.db);
            Player player = connection.player;
            if (player == null) {
                this.logger.error("Player trying to use ability without being logged in");
                connection.markForDeletion();
                return;
            }
            Game game = connection.player.currentGame;
            if (game == null) {
                this.logger.error("Player trying to use ability without being in game");
                connection.markForDeletion();
                return;
            }

            if (usedAbility.cost > ((game.player1.id == player.id) ? game.player1MP : game.player2MP)) {
                this.logger.error("Player tried using an ability that is too expensive");
                return;
            }
            this.db.abilityUsed(usedAbility, player, game);
            this.logger.info("Player used ability " + usedAbility.name + " successfully.");
        } catch (SQLException e) {
            this.logger.ferror("couldn't handle Ability used due to \"%s\"", e);
        }
    }
    private void handleRoundFinished(Connection connection) {
        Player player = connection.player;
        if (player == null) {
            this.logger.error("Player trying to finish round while not being logged in");
            connection.markForDeletion();
            return;
        }
        Game game = player.currentGame;
        if (game == null) {
            this.logger.error("Player trying to finish round while not being in a game");
            connection.markForDeletion();
            return;
        }
        try {
            this.db.playerFinishedRound(player);
        } catch (SQLException e) {
            String msg = String.format("Couldn't finish round for player \"%s\", because of \"%s\"", player.name, e);
            this.logger.error(msg);
            connection.markForDeletion();
        }
        game = player.currentGame;
        this.logger.fdebug("Player 1 finished %d turns and Player 2 finished %d", game.player1FinishedTurns, game.player2FinishedTurns);
        if (game.roundFinished())
            this.finishRound(game);

    }
    private void finishRound(Game game) {
        try {
            AbilityUsed[] activeAbilities = this.db.getAllAbilitiesUsed(game);
            this.logger.fdebug("activeAbilities: %s", Arrays.toString(activeAbilities));
            for (AbilityUsed ability: activeAbilities) {
                Queue<Effect> activeEffects = ability.activeEffects(game);
                while (!activeEffects.isEmpty()) {
                    Effect effect = activeEffects.front();
                    float actualValue = ability.value * (effect.max - effect.min) + effect.min;
                    this.applyEffect(game, effect, ability.player1, actualValue);
                    activeEffects.dequeue();
                }
            }
            this.db.updateDB(game);
        } catch (SQLException e) {
            this.logger.ferror("Couldn't finish round due to \"%s\"", e);
        }
    }
    private void applyEffect(Game game, Effect effect, boolean player1, float value) {
        // if player one used the ability that created the effect, and it hit himself, or
        // it was player 2 who used the ability, and it isn't an effect that hit the user,
        // it needs to be applied to player 1.
        if (player1 == effect.hitSelf) {
            if (effect.relative) {
                switch (effect.valueEffected) {
                    case 0: // HP
                        game.player1HP -= game.player1HP * value;
                        break;
                    case 1: // HP-regen
                        game.player1HPRegen -= game.player1HPRegen * value;
                        break;
                    case 2: // MP
                        game.player1MP -= game.player1MP * value;
                        break;
                    case 3: // MP-regen
                        game.player1MPRegen -= game.player1MPRegen * value;
                        break;
                    default:
                        logger.ferror("Unknown affected value %d", effect.valueEffected);
                        break;
                }
            } else {
                switch (effect.valueEffected) {
                    case 0: // HP
                        game.player1HP -= value;
                        break;
                    case 1: // HP-regen
                        game.player1HPRegen -= value;
                        break;
                    case 2: // MP
                        game.player1MP -= value;
                        break;
                    case 3: // MP-regen
                        game.player1MPRegen -= value;
                        break;
                    default:
                        logger.ferror("Unknown affected value %d", effect.valueEffected);
                        break;
                }
            }
        } else {
            if (effect.relative) {
                switch (effect.valueEffected) {
                    case 0: // HP
                        game.player2HP -= game.player2HP * value;
                        break;
                    case 1: // HP-regen
                        game.player2HPRegen -= game.player2HPRegen * value;
                        break;
                    case 2: // MP
                        game.player2MP -= game.player2MP * value;
                        break;
                    case 3: // MP-regen
                        game.player2MPRegen -= game.player2MPRegen * value;
                        break;
                    default:
                        logger.ferror("Unknown affected value %d", effect.valueEffected);
                        break;
                }
            } else {
                switch (effect.valueEffected) {
                    case 0: // HP
                        game.player2HP -= value;
                        break;
                    case 1: // HP-regen
                        game.player2HPRegen -= value;
                        break;
                    case 2: // MP
                        game.player2MP -= value;
                        break;
                    case 3: // MP-regen
                        game.player2MPRegen -= value;
                        break;
                    default:
                        logger.ferror("Unknown affected value %d", effect.valueEffected);
                        break;
                }
            }
        }
    }
    private void handleError(Error packet, Connection connection) {
        connection.markForDeletion();
        logger.error(connection + " had an error: " + packet.code);
    }

    private void handleOutgoingData() {
        Queue<Connection> connections = this.connectionHost.getConnections();
        while (!connections.isEmpty()) {
            Connection connection = connections.front();
            connections.dequeue();
            if (connection.player == null) {
                continue;
            }
            try {
                connection.player = connection.player.getUpdated(this.db);
            } catch (SQLException e) {
                this.logger.ferror("Couldn't update player on connection \"%s\" because of \"%s\"", connection, e);
            }
            Player player = connection.player;
            if (player.currentGame == null) {
                continue;
            }

            try {
                player.currentGame = player.currentGame.getUpdated(this.db);
            } catch (SQLException e) {
                this.logger.ferror("Couldn't update player's current game because of \"%s\"", e);
            }
            Game game = player.currentGame;
            if (game.roundFinished()) {
                this.sendRoundFinished(connection, game, player);
            }
            if (game.finished) {
                try {
                    connection.send(new GameEnd((game.player1.id == player.id) ? (game.result > 0) : (game.result < 0)));
                    player.currentGame = null;
                    this.availablePlayers.enqueue(connection);
                } catch (IOException e) {
                    this.logger.ferror("Unable to send GameEnd to %s due to \"%s\"", connection, e);
                }
            }
        }
    }
    private void sendRoundFinished(Connection connection, Game game, Player player) {
        boolean isPlayer1 = game.player1.id == player.id;

        game.abilitiesUsed.toFirst();
        List<AbilityUsed> otherUsedAbilities = new List<>();
        int count = 0;
        while (game.abilitiesUsed.hasAccess()) {
            Database.Types.AbilityUsed abilityUsed = game.abilitiesUsed.getContent();
            if (abilityUsed.round == game.round) {
                otherUsedAbilities.append(abilityUsed);
                count++;
            }
            game.abilitiesUsed.next();
        }

        Network.Packets.Fields.AbilityUsed[] usedAbilities = new Network.Packets.Fields.AbilityUsed[count];
        otherUsedAbilities.toFirst();
        for (int i = 0; i < usedAbilities.length; i++) {
            AbilityUsed abilityUsed = otherUsedAbilities.getContent();
            usedAbilities[i] = new Network.Packets.Fields.AbilityUsed(abilityUsed.ability.id, abilityUsed.value, abilityUsed.round, abilityUsed.player1 != isPlayer1);
            otherUsedAbilities.next();
        }
        try {
            connection.send(new RoundEnd(new ArrayField<>(usedAbilities)));
        } catch (IOException e) {
            this.logger.ferror("Couldn't send RoundEnd to %s because of \"%s\"", connection, e);
            connection.markForDeletion();
        }
    }

    private void checkNewGames() {
        while (!this.availablePlayers.isEmpty()) {
            // get first player
            Connection connection1 = this.availablePlayers.front();
            if (connection1.isDeleted()) {
                continue;
            }
            Player player1 = connection1.player;
            this.availablePlayers.dequeue();
            // only one player in queue
            if (this.availablePlayers.isEmpty()) {
                this.availablePlayers.enqueue(connection1);
                break;
            }

            // get second player
            Connection connection2 = this.availablePlayers.front();
            if (connection2.isDeleted()) {
                this.availablePlayers.enqueue(connection1);
                continue;
            }
            Player player2 = connection2.player;
            this.availablePlayers.dequeue();

            // add the game in the database
            Game game;
            try {
                game = this.db.newGame(player1, player2);
                if (game == null)
                    throw new SQLException("Game returned null");
            } catch (SQLException e) {
                String msg = String.format("Couldn't pair player \"%s\" to player \"%s\" because of SQLException \"%s\".", player1.name, player2.name, e);
                this.logger.error(msg);
                connection1.markForDeletion();
                connection2.markForDeletion();
                return;
            }

            // tell the players that they are in a game
            try {
                this.sendGame(connection1, game);
                this.sendGame(connection2, game);
                String msg = String.format("Player \"%s\" now playing against \"%s\".", player1.name, player2.name);
                this.logger.info(msg);
            } catch (IOException | SQLException e) {
                String msg = String.format("Couldn't pair player \"%s\" to player \"%s\" because of \"%s\".", player1.name, player2.name, e);
                this.logger.error(msg);
                connection1.markForDeletion();
                connection2.markForDeletion();
            }
        }
    }
    private void sendGame(Connection connection, Game game) throws IOException, SQLException {
        Player player = connection.player;
        Player other;
        boolean isPlayer1 = player.id == game.player1ID;
        if (isPlayer1) {
            other = game.player2;
        } else {
            other = game.player1;
        }
        AbilityUsed[] usedAbilities = this.db.getAbilitiesUsed(game);
        Network.Packets.Fields.AbilityUsed[] fields = new Network.Packets.Fields.AbilityUsed[usedAbilities.length];
        for (int i = 0; i < usedAbilities.length; i++) {
            AbilityUsed ability = usedAbilities[i];
            fields[i] = new Network.Packets.Fields.AbilityUsed(ability.ability.id, ability.value, ability.round, isPlayer1 != ability.player1);
        }
        connection.send(new GameStart(
                player.defaultHP,
                player.regenHP,
                player.defaultMP,
                player.regenMP,
                other.name,
                other.defaultHP,
                other.regenHP,
                other.defaultMP,
                other.regenMP,
                fields,
                game.round
        ));
        // reset the connection.player configuration so that the "currentGame" variable points to the correct game.
        if (game.player1.id == player.id) {
            connection.player = game.player1;
        } else {
            connection.player = game.player2;
        }
    }
    private void checkWaitingConnections() {
        Queue<Connection> previouslyWaitingConnections = new Queue<>();
        while (!this.availablePlayers.isEmpty()) {
            previouslyWaitingConnections.enqueue(this.availablePlayers.front());
            this.availablePlayers.dequeue();
        }
        while (!previouslyWaitingConnections.isEmpty()) {
            Connection connection = previouslyWaitingConnections.front();
            previouslyWaitingConnections.dequeue();
            if (!connection.isDeleted()) {
                this.availablePlayers.enqueue(connection);
                continue;
            }
            String msg = String.format("Player \"%s\" now stopped waiting for a connection due to a disconnect.", connection.player);
            this.logger.info(msg);
        }
    }
    private void incrementRounds() {
        Game[] runningGames;
        try {
            runningGames = this.db.getRunningGames();
        } catch (SQLException e) {
            this.logger.ferror("Couldn't get running games because of \"%s\"", e);
            return;
        }
        for (Game game : runningGames) {
            if (game.roundFinished()) {
                try {
                    this.db.newRound(game);
                    this.logger.fdebug("new round in game %s", game);
                } catch (SQLException e) {
                    this.logger.ferror("Couldn't update round of game %s", game);
                }
            }
        }
    }
}
