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

@Entity @Getter @Setter @Table(name = "servers", uniqueConstraints = {@UniqueConstraint(columnNames = {"xserver_name"})})
public class ServerTable {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String xserverName;

    @Column(nullable = false)
    private String bungeeName;

    @Column(nullable = false, length = 5, name = "type")
    private String typeName;

    @Version
    private Integer version;

    void setType(ServerType type) {
        this.typeName = type.key;
    }

    ServerType getType() {
        return ServerType.valueOf(typeName.toUpperCase());
    }
}
