package com.winthier.minilink.sql;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter @Setter @Table(name = "games", uniqueConstraints = {@UniqueConstraint(columnNames = {"uuid"})})
public class GameTable {
    @Id
    private Integer id;

    @Version
    private Integer version;

    @Column(nullable = false)
    private UUID uuid;

    @Column(nullable = false)
    @ManyToOne
    private ServerTable server;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String gameKey;

    @Column(nullable = false, length = 4)
    private String state;

    @Column(nullable = false)
    private Integer playerCount;

    @Column(nullable = false)
    private Integer maxPlayers;

    @Column(nullable = false)
    private Timestamp creationTime;

    public List<PlayerTable> getPlayers() {
        return Database.getInstance().getDb().find(PlayerTable.class).where().eq("currentGame", this).findList();
    }

    public boolean isOver() {
        return getState().equals("OVER");
    }

    public void setOver() {
        state = "OVER";
    }
}
