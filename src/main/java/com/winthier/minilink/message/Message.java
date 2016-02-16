package com.winthier.minilink.message;

import java.io.Serializable;
import java.util.UUID;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Message implements Serializable {
    private final UUID uuid;
    private final Type type;
    // Set by ConnectionManager.broadcastMessage(), then derived from options.
    private String optionsString;
    // Derived from optionsString.
    private transient YamlConfiguration options = null;
    // Set by ConnectionManager.onMessage()
    private transient String sourceServer = null;

    public Message(Type type) {
        this.uuid = UUID.randomUUID();
        this.type = type;
    }

    public Message(UUID uuid, Type type) {
        this.uuid = uuid;
        this.type = type;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Type getType() {
        return type;
    }

    public void setSourceServer(String sourceServer) {
        this.sourceServer = sourceServer;
    }

    public String getSourceServer() {
        return sourceServer;
    }

    public void saveOptions() {
        if (options == null) return;
        optionsString = options.saveToString();
    }

    public MemoryConfiguration getOptions() {
        if (options == null) {
            options = new YamlConfiguration();
            if (optionsString != null) {
                try {
                    options.loadFromString(optionsString);
                } catch (InvalidConfigurationException ice) {
                    ice.printStackTrace();
                }
            }
        }
        return options;
    }

    public String getOptionsString() {
        if (optionsString == null) return "";
        return optionsString;
    }

    public enum Type {
        // Defined by the game
        CUSTOM,
        // Ack that you received the last message with the same UUID
        ACK,
        // Active server is alive
        ALIVE,
        // Active server going down
        NOT_ALIVE,
        // Create a game
        CREATE_GAME,
        CREATE_GAME_REPLY,
        // Add players to a game
        JOIN_GAME,
        JOIN_GAME_REPLY,
        // Spectate a game
        SPECTATE_GAME,
        SPECTATE_GAME_REPLY,
        // Send players
        GAME_READY,
        //
        REBOOT,
        ;
        public Message create() {
            return new Message(this);
        }
        public Message create(UUID uuid) {
            return new Message(uuid, this);
        }
    }
}
