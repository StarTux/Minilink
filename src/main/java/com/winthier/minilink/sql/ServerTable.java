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
@Table(name = "servers", uniqueConstraints = {@UniqueConstraint(columnNames = {"xserver_name"})})
public class ServerTable {
    @Id
    private Integer id;

    @NotEmpty
    private String xserverName;

    @NotEmpty
    private String bungeeName;

    @NotNull
    private ServerType type;

    @Version
    private Integer version;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getXserverName() { return xserverName; }
    public void setXserverName(String xserverName) { this.xserverName = xserverName; }

    public String getBungeeName() { return bungeeName; }
    public void setBungeeName(String bungeeName) { this.bungeeName = bungeeName; }

    public ServerType getType() { return type; }
    public void setType(ServerType type) { this.type = type; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
