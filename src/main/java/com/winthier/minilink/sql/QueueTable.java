package com.winthier.minilink.sql;

import java.sql.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import lombok.Getter;
import lombok.Setter;

/**
 * Queued games retrieve their configuration from the
 * GameConfigTable.
 */
@Entity @Getter @Setter @Table(name = "queues")
public class QueueTable {
    @Id
    private Integer id;

    @Version
    private Integer version;

    @Column(nullable = false)
    private String gameKey;
}
