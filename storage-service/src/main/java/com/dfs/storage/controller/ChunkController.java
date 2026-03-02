// ChunkController.java
package com.dfs.storage.controller;

import com.dfs.storage.service.ChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

// logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/chunks")
@RequiredArgsConstructor
public class ChunkController {

    private static final Logger log = LoggerFactory.getLogger(ChunkController.class);

    private final ChunkService service;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chunkName") String chunkName) {

        log.info("📥 Upload request received for chunk: {}", chunkName);

        service.saveChunk(file, chunkName);

        return ResponseEntity.ok("Chunk uploaded successfully");
    }

    @GetMapping("/{chunkName}")
    public ResponseEntity<Resource> getChunk(@PathVariable String chunkName) {

        log.info("📤 Download request for chunk: {}", chunkName);

        Resource resource = service.getChunk(chunkName);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + chunkName + "\"")
                .body(resource);
    }
}
// update: 2026-05-14 23:30:30.740495

// update: 2026-05-14 23:30:42.357974
