// File.java
package com.dfs.metadata_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    private Long totalSize;

    private Integer totalChunks;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chunk> chunks;
}
// update: 2026-05-14 23:30:34.857525

// update: 2026-05-14 23:30:40.431342

// update: 2026-05-14 23:30:41.347689

// update: 2026-05-14 23:30:43.494790
