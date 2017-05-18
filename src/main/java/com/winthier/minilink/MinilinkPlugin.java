package com.winthier.minilink;

import com.winthier.minilink.game.GameServer;
import com.winthier.minilink.lobby.LobbyMiniMeCommand;
import com.winthier.minilink.lobby.LobbyServer;
import com.winthier.minilink.message.Message;
import com.winthier.minilink.sql.Database;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MinilinkPlugin extends JavaPlugin {
    private final ConnectionManager connectionManager = new ConnectionManager(this);
    public Database database;
    private Server server;

    @Override
    public void onEnable() {
        database = new Database(this);
        database.init();
        connectionManager.enable();
        // Setup server
        if (isGameServer()) {
            getLogger().info("Game Server");
            server = new GameServer(this);
        } else {
            getLogger().info("Lobby Server");
            server = new LobbyServer(this);
            getCommand("MiniMe").setExecutor(new LobbyMiniMeCommand());
        }
        server.enable();
        getCommand("Minilink").setExecutor(server);
        // Register Bungee plugin channel
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    public boolean isGameServer() {
        return getServer().getPluginManager().getPlugin("Minigames") != null;
    }

    public boolean isLobbyServer() {
        return !isGameServer();
    }

    public GameServer getGameServer() {
        if (server instanceof GameServer) return (GameServer)server;
        return null;
    }

    public LobbyServer getLobbyServer() {
        if (server instanceof LobbyServer) return (LobbyServer)server;
        return null;
    }

    @Override
    public void onDisable() {
        if (server != null) server.disable();
    }

    public String getXserverName() {
        return connectionManager.getServerName();
    }

    public void broadcastMessage(Message message) {
        connectionManager.broadcastMessage(message);
    }

    /**
     * Called by ConnectionManager.onMessage()
     * Hand the incoming message to the server.
     */
    public void receiveMessage(Message message) {
        //System.out.println("Message received: " + message.getType().name() + ": " + message.getOptionsString().replace("\n", "|"));
        server.handleMessage(message);
    }
}
