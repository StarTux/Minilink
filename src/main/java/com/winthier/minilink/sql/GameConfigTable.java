package com.winthier.minilink.sql;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
import java.sql.Date;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import org.bukkit.OfflinePlayer;

@Entity
@Table(name = "game_configs", uniqueConstraints = {@UniqueConstraint(columnNames = {"game_key", "page_number"})})
public class GameConfigTable {
    @Id
    private Integer id;

    @NotEmpty
    private String gameKey;

    @NotNull
    private Integer pageNumber;

    @NotNull
    private String config;

    @Version
    private Integer version;

    public GameConfigTable() {}
    public GameConfigTable(String gameKey, Integer pageNumber, String config) {
        setGameKey(gameKey);
        setPageNumber(pageNumber);
        setConfig(config);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getGameKey() { return gameKey; }
    public void setGameKey(String gameKey) { this.gameKey = gameKey; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
