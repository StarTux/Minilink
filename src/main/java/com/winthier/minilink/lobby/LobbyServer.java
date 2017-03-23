package com.winthier.minilink.lobby;

import com.winthier.chat.ChatPlugin;
import com.winthier.chat.channel.Channel;
import com.winthier.minilink.MinilinkPlugin;
import com.winthier.minilink.Server;
import com.winthier.minilink.message.Message;
import com.winthier.minilink.sql.GameTable;
import com.winthier.minilink.sql.PlayerTable;
import com.winthier.minilink.sql.ServerTable;
import com.winthier.minilink.util.Bungee;
import com.winthier.minilink.util.Configure;
import com.winthier.minilink.util.JSON;
import com.winthier.minilink.util.JsonBuilder;
import com.winthier.minilink.util.Msg;
import com.winthier.minilink.util.PlayerInfo;
import com.winthier.minilink.util.Players;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LobbyServer extends Server {
    private BukkitRunnable tickTask = null;
    private Map<UUID, Task> tasks = new HashMap<>();
    private static LobbyServer instance;

    public LobbyServer(MinilinkPlugin plugin) {
        super(plugin);
        instance = this;
    }

    public static LobbyServer getInstance()
    {
        return instance;
    }

    @Override
    public void enable() {
        tickTask = new BukkitRunnable() {
            @Override public void run() {
                tick();
            }
        };
        tickTask.runTaskTimer(plugin, 20L, 20L);
    }

    @Override
    public void disable() {
        try {
            tickTask.cancel();
        } catch (RuntimeException e) {}
    }

    /**
     * Called once a second.
     */
    private void tick() {
        for (Task task : tasks.values()) task.tick();
    }

    public void removeTask(UUID uuid) {
        tasks.remove(uuid);
    }

    @Override
    public void handleMessage(Message message) {
        Task task = tasks.get(message.getUuid());
        if (task != null) {
            task.handleMessage(message);
            return;
        }
        switch (message.getType()) {
        case GAME_READY:
            handleGameReady(message);
            break;
        }
    }

    private void handleGameReady(Message message) {
        UUID uuid = UUID.fromString(message.getOptions().getString("UUID"));
        GameTable gameTable = plugin.database.getGame(uuid);
        if (gameTable == null) {
            plugin.getLogger().warning("Ready game not found: " + uuid);
            return;
        }
        plugin.getLogger().info("Game ready: " + gameTable.getName());
        ServerTable serverTable = gameTable.getServer();
        // Update home server
        List<PlayerTable> playerTables = gameTable.getPlayers();
        for (PlayerTable playerTable : playerTables) {
            if (plugin.getServer().getPlayer(playerTable.getUuid()) != null) {
                playerTable.setHomeServer(plugin.database.getThisServer());
                plugin.database.save(playerTable);
            }
        }
        // Send players to server with a small delay
        int i = 0;
        for (PlayerTable tmp : playerTables) {
            final PlayerTable playerTable = tmp;
            new BukkitRunnable() {
                @Override public void run() {
                    Player player = plugin.getServer().getPlayer(playerTable.getUuid());
                    if (player == null) return;
                    Msg.send(player, "&bYour game is ready...");
                    // Send player to server
                    Bungee.send(plugin, player, serverTable.getBungeeName());
                }
            }.runTaskLater(plugin, i++ * 20);
        }
        if (Configure.loadShouldAnnounce(message.getOptions())) {
            Channel channel = ChatPlugin.getInstance().findChannel("mini");
            if (channel == null) {
                plugin.getLogger().warning("Channel [mini] not found");
            } else {
                List<Object> msg = new ArrayList<>();
                List<PlayerInfo> players = Configure.loadPlayers(message.getOptions());
                final String playerName = players.isEmpty() ? "Server" : players.get(0).getName();
                final String gameName = Configure.loadGameName(message.getOptions());
                final UUID gameUuid = Configure.loadUuid(message.getOptions());
                msg.add(Msg.format("&3&lMini&r %s started a game of &6%s&r: ", playerName, gameName));
                msg.add(JSON.button("&r[&6Join&r]", "/minime join " + gameUuid.toString(), "&a"+gameName+"\n&r&oJoin"));
                msg.add(" ");
                msg.add(JSON.button("&r[&6Spec&r]", "/minime spec " + gameUuid.toString(), "&a"+gameName+"\n&r&oSpectate"));
                for (Player player : channel.getLocalMembers()) {
                    if (player != null) {
                        Msg.sendRaw(player, msg);
                        player.playSound(player.getEyeLocation(), Sound.ENTITY_FIREWORK_TWINKLE, 1.0f, 1.2f);
                    }
                }
            }
        }
    }

    @Override
    public boolean command(CommandSender sender, Command command, String label, String args[]) {
        Player player = sender instanceof Player ? (Player)sender : null;
        if (args.length == 0) {
        } else if ("Create".equalsIgnoreCase(args[0]) && args.length >= 3) {
            final String gameKey = args[1];
            final ConfigurationSection gameConfig = plugin.database.getGameConfig(gameKey);
            if (gameConfig == null) throw new CommandException("Game not found: " + gameKey);
            final List<PlayerInfo> players = Players.parsePlayers(args, 2);
            createGame(gameKey, gameConfig, players);
        } else if ("JoinOrCreate".equalsIgnoreCase(args[0]) && args.length >= 3) {
            final String gameKey = args[1];
            final ConfigurationSection gameConfig = plugin.database.getGameConfig(gameKey);
            if (gameConfig == null) throw new CommandException("Game not found: " + gameKey);
            final List<PlayerInfo> players = Players.parsePlayers(args, 2);
            joinOrCreateGame(gameKey, gameConfig, players);
        } else if ("List".equalsIgnoreCase(args[0]) && args.length == 2) {
            String gameKey = args[1];
            List<GameTable> games = plugin.database.getOpenGamesByKey(gameKey);
            Msg.send(sender, "Games(%d):", games.size());
            for (GameTable game : games) {
                String info = String.format("%s %s %d people, %d/%d players ", game.getName(), game.getGameKey(), game.getPlayers().size(), game.getPlayerCount(), game.getMaxPlayers());
                if (player != null) {
                    JsonBuilder.listBuilder()
                        .addMap().set("color", "yellow").set("text", info).done()
                        .addMap().set("color", "gold").set("text", "[join]").createMap("clickEvent").set("action", "run_command").set("value", "/minilink join " + game.getUuid()).done().done()
                        .addMap().set("color", "gold").set("text", "[spectate]").createMap("clickEvent").set("action", "run_command").set("value", "/minilink spectate " + game.getUuid()).done().done()
                        .send(player);
                } else {
                    sender.sendMessage(info);
                }
            }
        } else if ("Join".equalsIgnoreCase(args[0]) && args.length == 2) {
            if (player == null) throw new CommandException("Player expected");
            UUID gameUuid = UUID.fromString(args[1]);
            List<PlayerInfo> players = Players.getPlayerInfos(player);
            joinGame(gameUuid, players);
        } else if ("Spectate".equalsIgnoreCase(args[0]) && args.length == 2) {
            if (player == null) throw new CommandException("Player expected");
            UUID gameUuid = UUID.fromString(args[1]);
            List<PlayerInfo> players = Players.getPlayerInfos(player);
            spectateGame(gameUuid, players);
        } else {
            return false;
        }
        return true;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Setup tasks for more complicated methods
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    void joinOrCreateGame(String gameKey, ConfigurationSection gameConfig, List<PlayerInfo> players, UUID uuid) {
        JoinOrCreateTask task = new JoinOrCreateTask(this, gameKey, gameConfig, players, true, uuid);
        tasks.put(uuid, task);
        task.start();
    }

    void joinGame(String gameKey, List<PlayerInfo> players, UUID uuid) {
        JoinOrCreateTask task = new JoinOrCreateTask(this, gameKey, null, players, false, uuid);
        tasks.put(uuid, task);
        task.start();
    }
    
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Quick methods to send the appropriate packages.
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public void createGame(String gameKey, ConfigurationSection gameConfig, List<PlayerInfo> players, UUID messageUuid) {
        Message message = Message.Type.CREATE_GAME.create(messageUuid);
        Configure.storeGameConfig(message.getOptions(), gameConfig);
        Configure.storeGameKey(message.getOptions(), gameKey);
        Configure.storePlayers(message.getOptions(), players);
        // Broadcast
        plugin.broadcastMessage(message);
    }

    public void joinGame(UUID gameUuid, List<PlayerInfo> players, UUID messageUuid) {
        Message message = Message.Type.JOIN_GAME.create(messageUuid);
        Configure.storeUuid(message.getOptions(), gameUuid);
        Configure.storePlayers(message.getOptions(), players);
        plugin.broadcastMessage(message);
    }

    public void spectateGame(UUID gameUuid, List<PlayerInfo> players, UUID messageUuid) {
        Message message = Message.Type.SPECTATE_GAME.create(messageUuid);
        Configure.storeUuid(message.getOptions(), gameUuid);
        Configure.storePlayers(message.getOptions(), players);
        // Broadcast
        plugin.broadcastMessage(message);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Overrides for the above, with random messageUuid
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public void joinOrCreateGame(String gameKey, ConfigurationSection gameConfig, List<PlayerInfo> players) {
        joinOrCreateGame(gameKey, gameConfig, players, UUID.randomUUID());
    }

    public void joinGame(String gameKey, List<PlayerInfo> players) {
        joinGame(gameKey, players, UUID.randomUUID());
    }

    public void createGame(String gameKey, ConfigurationSection gameConfig, List<PlayerInfo> players) {
        createGame(gameKey, gameConfig, players, UUID.randomUUID());
    }

    public void joinGame(UUID gameUuid, List<PlayerInfo> players) {
        joinGame(gameUuid, players, UUID.randomUUID());
    }

    public void spectateGame(UUID gameUuid, List<PlayerInfo> players) {
        spectateGame(gameUuid, players, UUID.randomUUID());
    }
}
