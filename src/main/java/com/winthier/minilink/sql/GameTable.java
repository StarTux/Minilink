package com.winthier.minilink.sql;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Entity
@Table(name = "games", uniqueConstraints = {@UniqueConstraint(columnNames = {"uuid"})})
public class GameTable {
    @Id
    private Integer id;

    @Version
    private Integer version;

    @NotNull
    private UUID uuid;

    @NotNull
    @ManyToOne
    private ServerTable server;

    @NotEmpty
    private String name;

    @NotEmpty
    private String gameKey;

    @NotEmpty
    @Length(max = 4)
    private String state;

    @NotNull
    private Integer playerCount;

    @NotNull
    private Integer maxPlayers;

    @NotNull
    private Timestamp creationTime;

    @OneToMany(mappedBy = "currentGame")
    List<PlayerTable> players;
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public ServerTable getServer() { return server; }
    public void setServer(ServerTable server) { this.server = server; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGameKey() { return gameKey; }
    public void setGameKey(String gameKey) { this.gameKey = gameKey; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public void setState(Enum<?> state) { this.state = state.name(); }

    public Integer getPlayerCount() { return playerCount; }
    public void setPlayerCount(Integer playerCount) { this.playerCount = playerCount; }

    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }

    public Timestamp getCreationTime() { return creationTime; }
    public void setCreationTime(Timestamp creationTime) { this.creationTime = creationTime; }

    public List<PlayerTable> getPlayers() { return players; }
    public void setPlayers(List<PlayerTable> players) { this.players = players; }

    public boolean isOver() {
        return getState().equals("OVER");
    }
    public void setOver() {
        setState("OVER");
    }
}
