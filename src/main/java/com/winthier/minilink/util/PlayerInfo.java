package com.winthier.minilink.util;

import java.util.UUID;
import org.bukkit.entity.Player;

public class PlayerInfo {
    public final UUID uuid;
    public final String name;

    private PlayerInfo(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlayerInfo)) return false;
        final PlayerInfo other = (PlayerInfo)obj;
        // Ignore the name
        if (!uuid.equals(other.uuid)) return false;
        return true;
    }

    public static PlayerInfo fromPlayer(Player player) {
        return new PlayerInfo(player.getUniqueId(), player.getName());
    }

    public static PlayerInfo fromInfo(UUID uuid, String name) {
        return new PlayerInfo(uuid, name);
    }
}
