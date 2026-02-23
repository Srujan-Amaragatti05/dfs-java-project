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
