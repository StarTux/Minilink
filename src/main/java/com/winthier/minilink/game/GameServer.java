package com.winthier.minilink.game;

import com.winthier.minigames.MinigamesPlugin;
import com.winthier.minigames.event.game.GameChangeEvent;
import com.winthier.minigames.event.game.GameStateEvent;
import com.winthier.minigames.event.player.PlayerLeaveEvent;
import com.winthier.minigames.game.Game;
import com.winthier.minilink.MinilinkPlugin;
import com.winthier.minilink.Server;
import com.winthier.minilink.message.Message;
import com.winthier.minilink.sql.GameTable;
import com.winthier.minilink.sql.PlayerTable;
import com.winthier.minilink.sql.ServerTable;
import com.winthier.minilink.util.Configure;
import com.winthier.minilink.util.PlayerInfo;
import com.winthier.minilink.util.Players;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class GameServer extends Server implements Listener {
    // Const
    private long maxGamesCreated = 5;
    private long shutdownTime = 20L * 60L * 60L * 2; // 120 minutes
    //
    private BukkitRunnable task = null;
    // State
    private State state = State.WAIT;
    private long totalTicks = 0L;
    private long noAliveTicks = 0L;
    private long activeTicks = 0L;
    private long shutdownTicks = 0L;
    private boolean receivedAlive = false;
    private boolean receivedNotAlive = false;
    private boolean shutdownCommand = false;
    private boolean activateCommand = false;
    private int gamesCreated = 0;

    public GameServer(MinilinkPlugin plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        maxGamesCreated = plugin.getConfig().getLong("MaxGamesCreated", maxGamesCreated);
        shutdownTime = plugin.getConfig().getLong("ShutdownTime", 120) * 20L * 60L;
        plugin.getLogger().info("MaxGamesCreated="+maxGamesCreated+" ShutdownTime="+shutdownTime);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        task = new BukkitRunnable() {
            @Override public void run() {
                tick();
            }
        };
        task.runTaskTimer(plugin, 1L, 1L);
        ServerTable serverTable = plugin.database.getActiveServer();
        if (serverTable == null || serverTable.getId().equals(plugin.database.getThisServer().getId())) {
            state = State.ACTIVE;
            plugin.database.setActiveServer();
            plugin.broadcastMessage(Message.Type.ALIVE.create());
        }
        // Delete old game columns
        List<GameTable> myGames = plugin.database.getMyGames();
        for (GameTable gameTable : myGames) {
            for (PlayerTable playerTable : gameTable.getPlayers()) {
                playerTable.signOff();
                plugin.database.save(playerTable);
            }
        }
        plugin.database.delete(myGames);
    }

    @Override
    public void disable() {
        try {
            task.cancel();
        } catch (RuntimeException e) {}
    }

    public void shutdown() {
        shutdownCommand = true;
    }

    private void tick() {
        //if (totalTicks % 20L == 0L) plugin.getLogger().info("tick");
        long totalTicks = this.totalTicks++;
        State nextState = state;
        switch (state) {
        case WAIT: {
            long noAliveTicks = this.noAliveTicks++;
            if (activateCommand) {
                activateCommand = false;
                nextState = State.ACTIVE;
                plugin.database.setActiveServer();
                plugin.broadcastMessage(Message.Type.ALIVE.create());
            } else if (receivedNotAlive) {
                // Received not alive message from the active
                // server. Check if it's our turn now.
                receivedNotAlive = false;
                int iter = plugin.database.getActiveServerIter();
                System.out.println("Received not alive. Iter: " + iter);
                if (iter == 1) { // Should never be 0
                    nextState = State.ACTIVE;
                    plugin.database.setActiveServer();
                    plugin.broadcastMessage(Message.Type.ALIVE.create());
                }
            } else if (totalTicks % 20L == 0L && noAliveTicks > 20L * 10L) {
                // Count how long we have not had an alive tick
                // from the active server.
                // - After 10 seconds: Activate if we are next in
                // line.
                // - After 20 seconds: Acitvate if we are second
                // to next in line.
                // - etc.
                // This is to make sure that servers continue
                // working even if one of them hangs for a while.
                int iter = plugin.database.getActiveServerIter();
                System.out.println("No Alive Ticks. Iter: " + iter);
                if (iter >= 0 && (long)iter * 20L * 10L < noAliveTicks) {
                    nextState = State.ACTIVE;
                    plugin.database.setActiveServer();
                    plugin.broadcastMessage(Message.Type.ALIVE.create());
                }
            }
        } break;
        case ACTIVE: {
            // Send tick message
            if (receivedAlive) {
                // Received an unexpected alive message. Silently
                // go into shutdown mode.
                plugin.getLogger().info("Switching to shutdown due to unexpected alive message");
                nextState = State.SHUTDOWN;
            } else if (shutdownCommand) {
                plugin.getLogger().info("Switching to shutdown due to shutdown command");
                nextState = State.SHUTDOWN;
            } else if (gamesCreated > maxGamesCreated) {
                plugin.getLogger().info("Switching to shutdown due to max games exceeded");
                nextState = State.SHUTDOWN;
            } else if (totalTicks % 20L == 0L) {
                plugin.broadcastMessage(Message.Type.ALIVE.create());
            }
            activeTicks++;
        } break;
        case SHUTDOWN: {
            if (totalTicks % 20L == 0L) {
                if (plugin.getServer().getOnlinePlayers().isEmpty()) {
                    plugin.getServer().shutdown();
                    return;
                }
                int running = 0;
                for (GameTable gameTable : plugin.database.getMyGames()) {
                    if (!gameTable.isOver()) running += 1;
                }
                if (running == 0) {
                    new BukkitRunnable() {
                        @Override public void run() {
                            plugin.getServer().shutdown();
                        }
                    }.runTaskLater(plugin, 20L * 10L);
                    return;
                }
            }
            if (shutdownTicks > shutdownTime) {
                // If in shutdown mode more than 1 hour, force
                // shutdown the server.
                for (Game game : MinigamesPlugin.getGameManager().getGames()) {
                    game.cancel();
                }
            }
            shutdownTicks++;
        } break;
        default: {
            plugin.getLogger().warning("GameServer state not covered by tick(): " + state.name());
        } break;
        }
        if (state != nextState) {
            plugin.getLogger().info("GameServer switching state: " + nextState.name());
            if (nextState == State.SHUTDOWN) {
                plugin.broadcastMessage(Message.Type.NOT_ALIVE.create());
            }
            state = nextState;
        }
        receivedAlive = false;
        receivedNotAlive = false;
        shutdownCommand = false;
        activateCommand = false;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.getType()) {
        case CREATE_GAME:
            handleCreateGame(message);
            break;
        case JOIN_GAME:
            handleJoinGame(message);
            break;
        case SPECTATE_GAME:
            handleSpectateGame(message);
            break;
        case ALIVE:
            handleAlive(message);
            break;
        case NOT_ALIVE:
            handleNotAlive(message);
            break;
        case REBOOT:
            handleReboot(message);
            break;
        }
    }

    private boolean gotOwnAliveMessage = false;
    private void handleAlive(Message message) {
        noAliveTicks = 0L;
        receivedAlive = true;
    }

    private void handleNotAlive(Message message) {
        receivedNotAlive = true;
    }

    void handleReboot(Message message) {
        state = State.SHUTDOWN;
        plugin.broadcastMessage(Message.Type.NOT_ALIVE.create());
    }

    /**
     * When a CREATE_GAME message is received, some checks have to
     * be made whether the players that are to join are not
     * already signed up for other games. Then, the game is
     * created and players added, both in the database and in the
     * Minigames plugin. The CREATE_GAME_REPLY message is then
     * sent to report about the success of the operation.
     */
    private void handleCreateGame(Message message) {
        if (!isActive()) return;
        System.out.println(message.getOptionsString());
        List<PlayerInfo> players = Configure.loadPlayers(message.getOptions());
        // Prepare reply message
        Message reply = new Message(message.getUuid(), Message.Type.CREATE_GAME_REPLY);
        // Check players
        List<PlayerTable> playerTables = new ArrayList<>(players.size());
        for (PlayerInfo info : players) {
            PlayerTable table = plugin.database.getPlayer(info);
            if (table.isSignedUp()) {
                Configure.storeResult(reply.getOptions(), false);
                Configure.storeMessage(reply.getOptions(), "Player already signed up.");
                plugin.broadcastMessage(reply);
                return;
            }
            playerTables.add(table);
        }
        // Create game
        Game game = MinigamesPlugin.createGame(Configure.loadGameName(message.getOptions()));
        if (game == null) {
            Configure.storeResult(reply.getOptions(), false);
            Configure.storeMessage(reply.getOptions(), "Could not setup game.");
            plugin.broadcastMessage(reply);
            return;
        }
        this.gamesCreated += 1;
        game.setMaxPlayers(Configure.loadMaxPlayers(message.getOptions()));
        game.configure(message.getOptions());
        // Add players
        for (PlayerInfo info : players) {
            if (!MinigamesPlugin.getInstance().addPlayer(game, info.uuid)) {
                // ignore?
            }
        }
        // Commit to database
        GameTable gameTable = plugin.database.createGameTable(game, Configure.loadGameKey(message.getOptions()));
        for (PlayerTable playerTable : playerTables) {
            playerTable.setCurrentGame(gameTable);
        }
        plugin.database.save(playerTables);
        // Send reply
        Configure.storeResult(reply.getOptions(), true);
        Configure.storeUuid(reply.getOptions(), game.getUuid());
        plugin.broadcastMessage(reply);
    }

    /**
     * 
     */
    private void handleJoinGame(Message message) {
        String msg = handleJoinGame0(message, true);
        Message reply = Message.Type.JOIN_GAME_REPLY.create(message.getUuid());
        if (msg == null) {
            Configure.storeResult(reply.getOptions(), true);
            Configure.storeMessage(reply.getOptions(), "Ok");
        } else {
            Configure.storeResult(reply.getOptions(), false);
            Configure.storeMessage(reply.getOptions(), msg);
        }
        plugin.broadcastMessage(reply);
    }

    private void handleSpectateGame(Message message) {
        String msg = handleJoinGame0(message, false);
        Message reply = Message.Type.SPECTATE_GAME_REPLY.create(message.getUuid());
        if (msg == null) {
            Configure.storeResult(reply.getOptions(), true);
            Configure.storeMessage(reply.getOptions(), "Ok");
        } else {
            Configure.storeResult(reply.getOptions(), false);
            Configure.storeMessage(reply.getOptions(), msg);
        }
        plugin.broadcastMessage(reply);
    }

    /**
     * Helper function to conveniently return the result of a join
     * attempt so the resulting message reply can be built.
     * @return null on success, an error message if something went
     * wrong.
     */
    private String handleJoinGame0(Message message, boolean play) {
        final UUID uuid = Configure.loadUuid(message.getOptions());
        // Build player list
        List<UUID> uuids = new ArrayList<>();
        List<PlayerInfo> players = new ArrayList<>();
        ConfigurationSection playersSection = message.getOptions().getConfigurationSection("players");
        for (String key : playersSection.getKeys(false)) {
            UUID tmpUuid = UUID.fromString(key);
            uuids.add(tmpUuid);
            players.add(PlayerInfo.fromInfo(tmpUuid, playersSection.getString(key)));
        }
        // Fetch and check tables.
        GameTable gameTable = plugin.database.getGame(uuid);
        if (gameTable == null) return "Game not found";
        if (gameTable.getPlayerCount() + players.size() > gameTable.getMaxPlayers()) return "Game slots are full";
        List<PlayerTable> playerTables = plugin.database.getPlayers(players);
        for (PlayerTable playerTable : playerTables) if (playerTable.isSignedUp()) return "Player already signed up";
        // Find game and try to add players.
        Game game = MinigamesPlugin.getGameManager().getGame(uuid);
        if (game == null) return "Game not found";
        boolean result;
        if (play) {
            result = game.joinPlayers(uuids);
        } else {
            result = game.joinSpectators(uuids);
        }
        if (!result) return "Player slots are full";
        // Update database
        for (PlayerTable playerTable : playerTables) playerTable.setCurrentGame(gameTable);
        plugin.database.save(playerTables);
        // Games in the PLAY state won't cause another GAME_READY
        // message, so send it on the next tick.
        if (game.getState() == Game.State.PLAY) {
            final Message reply = Message.Type.GAME_READY.create();
            reply.getOptions().set("UUID", game.getUuid().toString());
            reply.getOptions().createSection("players", message.getOptions().getConfigurationSection("players").getValues(true));
            new BukkitRunnable() {
                @Override public void run() {
                    plugin.broadcastMessage(reply);
                }
            }.runTask(plugin);
        }
        return null; // success
    }

    /**
     * If a game becomes ready to be played, the signed up players
     * have to be sent to this server.
     */
    @EventHandler
    public void onGameState(GameStateEvent event) {
        final Game game = event.getGame();
        final GameTable gameTable = plugin.database.getGame(game);
        gameTable.setState(event.getState().name());
        plugin.database.save(gameTable);
        // Update game state
        gameTable.setState(event.getState());
        plugin.database.save(gameTable);
        switch (event.getState()) {
        case PLAY: {
            // Create message
            Message message = Message.Type.GAME_READY.create();
            Configure.storeUuid(message.getOptions(), game.getUuid());
            Configure.storePlayers(message.getOptions(), Players.playersFromTable(gameTable));
            Configure.storeGameName(message.getOptions(), event.getGame().getName());
            if (event.getGame().getConfig().getBoolean("ShouldAnnounce", false)) {
                Configure.storeShouldAnnounce(message.getOptions(), true);
            }
            // Send message
            plugin.broadcastMessage(message);
            break;
        }
        case OVER: {
            // Sign off players
            for (PlayerTable playerTable : gameTable.getPlayers()) playerTable.signOff();
            // Players should be sent home by the handler of PlayerLeaveEvent.
            // TODO send message(?)
            break;
        }
        }
    }

    /**
     * When a game changes any of its properties, the game table
     * must be updated.
     * As of now, the impact is limited to the player count.
     */
    @EventHandler
    public void onGameChange(GameChangeEvent event) {
        final Game game = event.getGame();
        final GameTable gameTable = plugin.database.getGame(game);
        // Update game state
        gameTable.setPlayerCount(game.getPlayerCount());
        // Save
        plugin.database.save(gameTable);
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        final Player player = event.getPlayer();
        // Send player home.
        if (player != null) SendHomeTask.sendHome(plugin, event.getPlayer());
        // Remove player from games in the db.
        OfflinePlayer off = player;
        if (off == null) off = event.getOfflinePlayer();
        final PlayerTable playerTable = plugin.database.getPlayer(off);
        playerTable.signOff();
        plugin.database.save(playerTable);
    }

    @Override
    public boolean command(CommandSender sender, Command command, String label, String args[]) {
        if (false) {
        } else if (args.length == 1 && "Shutdown".equalsIgnoreCase(args[0])) {
            shutdownCommand = true;
            sender.sendMessage("Shutting down asap.");
        } else if (args.length == 1 && "Activate".equalsIgnoreCase(args[0])) {
            activateCommand = true;
            sender.sendMessage("Activating asap.");
        } else if (args.length == 1 && "Debug".equalsIgnoreCase(args[0])) {
            sender.sendMessage("State: " + state.name());
            sender.sendMessage("Ticks: " + totalTicks + " (" + (totalTicks/20/60) + " Minutes)");
            sender.sendMessage("ShutdownTicks: " + shutdownTicks + " (" + (shutdownTicks/20/60) + " Minutes)");
            sender.sendMessage("GamesCreated: " + gamesCreated + "/" + maxGamesCreated);
            sender.sendMessage("NoAliveTicks: " + noAliveTicks);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Is this the active game server or are we waiting?
     */
    public boolean isActive() {
        return state == State.ACTIVE;
    }

    public static enum State {
        WAIT,
        ACTIVE,
        SHUTDOWN,
        ;
    }
}
