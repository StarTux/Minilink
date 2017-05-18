package com.winthier.minilink.sql;

import java.sql.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

@Entity @Getter @Setter @Table(name = "players", uniqueConstraints = {@UniqueConstraint(columnNames = {"uuid"})})
public class PlayerTable {
    @Id
    private Integer id;

    @Version
    private Integer version;

    @Column(nullable = false)
    private UUID uuid;

    @Column(length = 16)
    private String name;

    @ManyToOne
    ServerTable homeServer;

    @ManyToOne
    GameTable currentGame;

    @ManyToOne
    QueueTable currentQueue;

    public PlayerTable() {}

    public PlayerTable(UUID uuid) {
        setUuid(uuid);
    }

    public PlayerTable(UUID uuid, String name) {
        this(uuid);
        setName(name);
    }

    public PlayerTable(OfflinePlayer player) {
        this(player.getUniqueId(), player.getName());
    }

    public boolean isSignedUp() {
        if (currentGame != null && !currentGame.getState().equals("OVER")) return true;
        if (currentQueue != null) return true;
        return false;
    }

    public void signOff() {
        setCurrentGame(null);
        setCurrentQueue(null);
    }
}
