package com.winthier.minilink.sql;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import java.sql.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

/**
 * Queued games retrieve their configuration from the
 * GameConfigTable.
 */
@Entity
@Table(name = "queues")
public class QueueTable {
    @Id
    private Integer id;

    @Version
    private Integer version;

    @NotEmpty
    private String gameKey;
    
    @OneToMany(mappedBy = "currentQueue")
    private List<PlayerTable> players;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getGameKey() { return gameKey; }
    public void setGameKey(String game) { this.gameKey = gameKey; }

    public List<PlayerTable> getPlayers() { return players; }
    public void setPlayers(List<PlayerTable> players) { this.players = players; }
}
