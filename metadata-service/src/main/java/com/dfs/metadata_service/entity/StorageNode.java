// StorageNode.java
package com.dfs.metadata_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;

    private Integer port;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime lastHeartbeat;

    private Long availableSpace;

    public enum Status {
        ACTIVE,
        INACTIVE,
        DOWN
    }
}
// update: 2026-05-14 23:30:27.275819

// update: 2026-05-14 23:30:27.820270

// update: 2026-05-14 23:30:37.132837

// update: 2026-05-14 23:30:48.353055
