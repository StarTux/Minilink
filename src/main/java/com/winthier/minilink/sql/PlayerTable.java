package com.winthier.minilink.sql;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import java.sql.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import org.bukkit.OfflinePlayer;

@Entity
@Table(name = "players", uniqueConstraints = {@UniqueConstraint(columnNames = {"uuid"})})
public class PlayerTable {
    @Id
    private Integer id;

    @Version
    private Integer version;

    @NotNull
    private UUID uuid;

    @Length(max = 16)
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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public ServerTable getHomeServer() { return homeServer; }
    public void setHomeServer(ServerTable homeServer) { this.homeServer = homeServer; }

    public GameTable getCurrentGame() { return currentGame; }
    public void setCurrentGame(GameTable currentGame) { this.currentGame = currentGame; }

    public QueueTable getCurrentQueue() { return currentQueue; }
    public void setCurrentQueue(QueueTable currentQueue) { this.currentQueue = currentQueue; }

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
