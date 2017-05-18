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

/**
 * Remember special server properties.
 * - "Default": The default lobby server.
 * - "Active": The currently active game server.
 */
@Entity @Getter @Setter @Table(name = "server_settings", uniqueConstraints = {@UniqueConstraint(columnNames = {"title"})})
public class ServerSettingsTable {
    @Id
    private Integer id;

    @Version
    private Integer version;

    @Column(nullable = false)
    private String title;

    @ManyToOne @Column(nullable = false)
    ServerTable server;
}
