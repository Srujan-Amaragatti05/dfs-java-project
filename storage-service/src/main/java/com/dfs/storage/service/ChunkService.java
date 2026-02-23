// ChunkService.java
package com.dfs.storage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

@Service
public class ChunkService {

    private static final Logger log = LoggerFactory.getLogger(ChunkService.class);

    private final Path storagePath;

    // configurable storage directory (e.g., storage1, storage2, storage3)
    public ChunkService(@Value("${dfs.storage.dir:storage}") String storageDir) {
        this.storagePath = Paths.get(storageDir).toAbsolutePath().normalize();
        log.info("📁 Storage directory: {}", this.storagePath);

        // ensure directory exists at startup
        try {
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                log.info("📁 Created storage directory at startup");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storage directory", e);
        }
    }

    public void saveChunk(MultipartFile file, String chunkName) {
        Path filePath = resolveChunkPath(chunkName);

        try {
            Files.write(filePath,
                    file.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            log.info("💾 [WRITE SUCCESS] Chunk={} | Size={} bytes | Path={}",
                    chunkName, file.getSize(), filePath);

        } catch (IOException e) {
            log.error("❌ [WRITE ERROR] Chunk={} | Path={} | Reason={}",
                    chunkName, filePath, e.getMessage());
            throw new RuntimeException("Failed to store chunk");
        }
    }

    public Resource getChunk(String chunkName) {
        Path path = resolveChunkPath(chunkName);

        try {
            if (!Files.exists(path)) {
                log.warn("❌ [MISSING] Chunk={} | Path={}", chunkName, path);
                throw new RuntimeException("Chunk not found");
            }

            if (!Files.isRegularFile(path)) {
                log.error("❌ [INVALID FILE] Chunk={} | Path={}", chunkName, path);
                throw new RuntimeException("Invalid chunk file");
            }

            log.info("✅ [READ SUCCESS] Chunk={} | Path={}", chunkName, path);
            return new FileSystemResource(path);

        } catch (RuntimeException e) {
            throw e; // already meaningful
        } catch (Exception e) {
            log.error("❌ [READ ERROR] Chunk={} | Path={} | Reason={}",
                    chunkName, path, e.getMessage());
            throw new RuntimeException("Failed to read chunk");
        }
    }

    private Path resolveChunkPath(String chunkName) {
        Path path = storagePath.resolve(chunkName).normalize();

        if (!path.startsWith(storagePath)) {
            throw new RuntimeException("Invalid chunk name");
        }

        return path;
    }
}
// update: 2026-05-14 23:30:33.008447
