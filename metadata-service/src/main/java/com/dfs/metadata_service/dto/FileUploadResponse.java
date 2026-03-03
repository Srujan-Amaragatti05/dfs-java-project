// FileUploadResponse.java (updated)
package com.dfs.metadata_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private Long fileId;
    private String filename;
    private Integer totalChunks;
    private List<ChunkInfo> chunks;

    @Data
    @AllArgsConstructor
    public static class ChunkInfo {
        private Integer chunkIndex;
        private List<Long> storageNodeIds;
    }
}
// update: 2026-05-14 23:30:28.494999

// update: 2026-05-14 23:30:28.916332

// update: 2026-05-14 23:30:31.771875

// update: 2026-05-14 23:30:41.556800

// update: 2026-05-14 23:30:45.345711
