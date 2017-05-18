package com.winthier.minilink.sql;

import com.winthier.minigames.game.Game;
import com.winthier.minilink.MinilinkPlugin;
import com.winthier.minilink.util.PlayerInfo;
import com.winthier.sql.SQLDatabase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Database {
    @Getter private static Database instance;
    public final MinilinkPlugin plugin;
    private final String ACTIVE_SERVER = "Active";
    @Getter private SQLDatabase db;

    public Database(MinilinkPlugin plugin) {
        instance = this;
        this.plugin = plugin;
    }

    public void init() {
        db = new SQLDatabase(plugin);
        db.registerTables(PlayerTable.class,
                          GameTable.class,
                          ServerTable.class,
                          GameConfigTable.class,
                          ServerSettingsTable.class,
                          QueueTable.class);
        db.createAllTables();
    }

    public static List<Class<?>> getClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        return list;
    }

    public PlayerTable getPlayer(UUID uuid, String name) {
        PlayerTable result = db.find(PlayerTable.class).where().eq("uuid", uuid).findUnique();
        if (result == null) {
            result = new PlayerTable(uuid, name);
            db.save(result);
        } else if (name != null && !result.getName().equals(name)) {
            result.setName(name);
            db.save(result);
        }
        return result;
    }

    public PlayerTable getPlayer(OfflinePlayer player) {
        return getPlayer(player.getUniqueId(), player.getName());
    }

    public PlayerTable getPlayer(PlayerInfo player) {
        return getPlayer(player.uuid, player.name);
    }

    public List<PlayerTable> getPlayers(List<PlayerInfo> infos) {
        List<PlayerTable> result = new ArrayList<>(infos.size());
        for (PlayerInfo info : infos) result.add(getPlayer(info));
        return result;
    }

    public ServerTable getServerByXserverName(String xserverName) {
        ServerTable result = db.find(ServerTable.class).where().eq("xserver_name", xserverName).findUnique();
        if (result == null) {
            plugin.getLogger().warning("Server " + xserverName + " was not entered into the servers table. The plugin may not work as expected until this is fixed.");
            result = new ServerTable();
            result.setXserverName(xserverName);
            // Assume that the bungee name equals the xserver
            // name.
            result.setBungeeName(xserverName);
            if (xserverName == plugin.getXserverName()) {
                if (plugin.isGameServer()) {
                    result.setType(ServerType.GAME);
                } else {
                    result.setType(ServerType.LOBBY);
                }
            } else {
                // Educated guess: If an unknown server is
                // requested, it is usually a lobby server.
                result.setType(ServerType.LOBBY);
            }
            db.save(result);
        }
        return result;
    }

    // public ServerTable getDefaultServer() {
    //     ServerTable result = db.find(ServerTable.class).where().eq("type", ServerType.DEFAULT).setMaxRows(1).findUnique();
    //     return result;
    // }

    public List<String> getReturnServers(OfflinePlayer player) {
        List<String> result = new ArrayList<>();
        ServerTable homeServer = getPlayer(player).getHomeServer();
        if (homeServer != null && homeServer.getBungeeName() != null) result.add(homeServer.getBungeeName());
        List<ServerTable> list =  db.find(ServerTable.class).where().isNotNull("bungee_name").findList();
        //for (ServerTable serverTable : list) if (serverTable.getType().isDefaultLobbyServer()) result.add(serverTable.getBungeeName());
        for (ServerTable serverTable : list) if (serverTable.getType().isLobbyServer()) result.add(serverTable.getBungeeName());
        return result;
    }

    public ServerTable getThisServer() {
        return getServerByXserverName(plugin.getXserverName());
    }

    public GameTable getGame(Game game) {
        GameTable result;
        result = db.find(GameTable.class)
            .where().eq("uuid", game.getUuid()).findUnique();
        return result;
    }

    /**
     * Create a GameTable with all necessary fields filled.
     */
    public GameTable createGameTable(Game game, String gameKey) {
        GameTable result;
        result = new GameTable();
        result.setUuid(game.getUuid());
        result.setServer(getServerByXserverName(plugin.getXserverName()));
        result.setName(game.getName());
        result.setGameKey(gameKey);
        result.setState(game.getState().toString());
        result.setPlayerCount(game.getPlayerCount());
        result.setMaxPlayers(game.getMaxPlayers());
        result.setCreationTime(new Timestamp(System.currentTimeMillis()));
        db.save(result);
        return result;
    }

    public GameTable getGame(UUID uuid) {
        GameTable result = db.find(GameTable.class).where().eq("uuid", uuid).findUnique();
        return result;
    }

    public List<GameTable> getMyGames() {
        List<GameTable> result;
        result = db.find(GameTable.class)
            .where().eq("server", getThisServer()).findList();
        return result;
    }

    public List<GameTable> getOpenGamesByKey(String... gameKeys) {
        List<GameTable> result = db.find(GameTable.class).where().in("game_key", Arrays.asList(gameKeys)).neq("state", "OVER").findList();
        return result;
    }

    public void save(Object bean) {
        db.save(bean);
    }

    public void save(List<?> beans) {
        db.save(beans);
    }

    public void delete(List<?> beans) {
        db.delete(beans);
    }

    public ConfigurationSection getGameConfig(String name) {
        List<GameConfigTable> tables = db.find(GameConfigTable.class)
            .where().eq("game_key", name).orderByAscending("page_number").findList();
        if (tables.isEmpty()) return null;
        YamlConfiguration result = new YamlConfiguration();
        StringBuilder sb = new StringBuilder();
        for (GameConfigTable table : tables) sb.append(table.getConfig());
        try {
            result.loadFromString(sb.toString());
        } catch (InvalidConfigurationException ice) {
            ice.printStackTrace();
        }
        return result;
    }

    public void setServerSettings(String title, ServerTable server) {
        ServerSettingsTable settings;
        settings = db
            .find(ServerSettingsTable.class)
            .where().eq("title", title).findUnique();
        if (settings == null) {
            settings = new ServerSettingsTable();
            settings.setTitle(title);
        }
        settings.setServer(server);
        db.save(settings);
    }

    public ServerTable getServerSettings(String title) {
        ServerSettingsTable settings;
        settings = db
            .find(ServerSettingsTable.class)
            .where().eq("title", title).findUnique();
        if (settings == null) return null;
        return settings.getServer();
    }

    public void setActiveServer() {
        setServerSettings(ACTIVE_SERVER, getThisServer());
    }

    public ServerTable getActiveServer() {
        return getServerSettings(ACTIVE_SERVER);
    }

    public int getActiveServerIter() {
        //System.out.println("GET ITER"); // DEBUG
        ServerTable activeServer = getActiveServer();
        //System.out.println("Active Server: " + (activeServer == null ? "null" : activeServer.getXserverName())); // DEBUG
        List<ServerTable> servers;
        servers = db.find(ServerTable.class)
            .where().eq("type", ServerType.GAME)
            .orderByAscending("id").findList();
        //for (ServerTable serverTable : servers) System.out.println("- " + serverTable.getXserverName()); // DEBUG
        if (servers.isEmpty()) return -1; // pathological case; should never happen!
        ServerTable thisServer = getThisServer();
        if (activeServer == null) return -1; // pathological case; should never happen!
        int firstIter = 0;
        // find firstIter
        for (int i = 0; i < servers.size(); ++i) {
            if (activeServer.getId().equals(servers.get(i).getId())) {
                firstIter = i;
                break;
            }
        }
        //System.out.println("First Iter: " + firstIter);
        // check behind iter
        for (int i = firstIter + 1; i < servers.size(); ++i) {
            if (thisServer.getId().equals(servers.get(i).getId())) return i - firstIter;
        }
        // check before iter
        for (int i = 0; i < firstIter; ++i) {
            if (thisServer.getId().equals(servers.get(i).getId())) return servers.size() + i - firstIter;
        }
        return -1; // pathological case; should never happen!
    }

    public void dumpGameConfigs() {
        File dir = new File(plugin.getDataFolder(), "GameConfigs");
        dir.mkdirs();
        for (GameConfigTable table : db.find(GameConfigTable.class).findList()) {
            File file = new File(dir, table.getGameKey() + ".yml");
            try {
                new FileOutputStream(file).getChannel().write(ByteBuffer.wrap(table.getConfig().getBytes()));
            } catch (IOException ioe) {
                System.err.println("Error writing " + file.getPath());
                ioe.printStackTrace();
            }
        }
    }

    public void loadGameConfigs() {
        File dir = new File(plugin.getDataFolder(), "GameConfigs");
        dir.mkdirs();
        for (File file : dir.listFiles()) {
            String gameKey = file.getName();
            if (!gameKey.endsWith(".yml")) continue;
            System.out.println("Loading " + gameKey);
            gameKey = gameKey.substring(0, gameKey.length() - 4);
            String config = null;
            try {
                config = new String(Files.readAllBytes(Paths.get(file.getPath())));
            } catch (IOException ioe) {
                System.err.println("Error reading " + file.getPath());
                ioe.printStackTrace();
                continue;
            }
            if (config == null) return;
            int len = (config.length() - 1) / 255 + 1;
            List<GameConfigTable> tables = new ArrayList<>(len);
            for (int i = 0; i < len; i += 1) {
                String line = config.substring(i * 255, Math.min(i * 255 + 255, config.length()));
                tables.add(new GameConfigTable(gameKey, i + 1, line));
            }
            // Delete old, if any
            db.delete(db.find(GameConfigTable.class)
                                 .where().eq("game_key", gameKey).findList());
            // Save new
            db.save(tables);
        }
    }
}
