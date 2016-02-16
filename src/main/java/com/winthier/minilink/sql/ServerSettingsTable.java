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

/**
 * Remember special server properties.
 * - "Default": The default lobby server.
 * - "Active": The currently active game server.
 */
@Entity
@Table(name = "server_settings", uniqueConstraints = {@UniqueConstraint(columnNames = {"title"})})
public class ServerSettingsTable {
    @Id
    private Integer id;

    @Version
    private Integer version;

    @NotEmpty
    private String title;

    @ManyToOne
    @NotNull
    ServerTable server;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ServerTable getServer() { return server; }
    public void setServer(ServerTable server) { this.server = server; }
}
