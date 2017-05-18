package com.winthier.minilink.sql;

import java.sql.Date;
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

@Entity @Getter @Setter @Table(name = "game_configs", uniqueConstraints = {@UniqueConstraint(columnNames = {"game_key", "page_number"})})
public class GameConfigTable {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String gameKey;

    @Column(nullable = false)
    private Integer pageNumber;

    @Column(nullable = false)
    private String config;

    @Version
    private Integer version;

    public GameConfigTable() {}

    public GameConfigTable(String gameKey, Integer pageNumber, String config) {
        setGameKey(gameKey);
        setPageNumber(pageNumber);
        setConfig(config);
    }
}
