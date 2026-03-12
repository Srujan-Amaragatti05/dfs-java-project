// ChunkRepository.java
package com.dfs.metadata_service.repository;

import com.dfs.metadata_service.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {
    List<Chunk> findByFileId(Long fileId);
    void deleteByFileId(Long fileId);

}
// update: 2026-05-14 23:30:28.068408

// update: 2026-05-14 23:30:29.958267

// update: 2026-05-14 23:30:32.181329

// update: 2026-05-14 23:30:46.645045

// update: 2026-05-14 23:30:48.990541

// update: 2026-05-14 23:30:54.303404

// update: 2026-05-14 23:30:55.897749
