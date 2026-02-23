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