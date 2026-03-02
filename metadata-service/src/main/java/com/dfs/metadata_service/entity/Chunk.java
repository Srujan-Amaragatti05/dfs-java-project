// Chunk.java
package com.dfs.metadata_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    private Integer chunkIndex;

    private String storageNodeIds;

    private String checksum;

    public Long getStorageNodeId() {
        return null;
    }
}
// update: 2026-05-14 23:30:31.142257

// update: 2026-05-14 23:30:34.654436

// update: 2026-05-14 23:30:38.244800

// update: 2026-05-14 23:30:42.978861
