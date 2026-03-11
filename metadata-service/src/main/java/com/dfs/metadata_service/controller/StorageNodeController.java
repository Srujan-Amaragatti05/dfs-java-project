// StorageNodeController.java
package com.dfs.metadata_service.controller;

import com.dfs.metadata_service.entity.StorageNode;
import com.dfs.metadata_service.service.StorageNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;


import java.util.Optional;

@RestController
@RequestMapping("/nodes")
@RequiredArgsConstructor
public class StorageNodeController {

    private final StorageNodeService service;

    @PostMapping("/register")
    public StorageNode registerNode(@RequestBody StorageNode node) {
        return service.registerNode(node);
    }
    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@RequestBody HeartbeatRequest request) {
        Optional<StorageNode> updated = service.heartbeat(
                request.getIpAddress(),
                request.getPort()
        );

        if (updated.isPresent()) {
            return ResponseEntity.ok(updated.get());
        } else {
            return ResponseEntity.badRequest().body("Storage node not found");
        }
    }

    @Data
    static class HeartbeatRequest {
        private String ipAddress;
        private Integer port;
    }
}

// update: 2026-05-14 23:30:36.902740

// update: 2026-05-14 23:30:38.847741

// update: 2026-05-14 23:30:44.313283

// update: 2026-05-14 23:30:44.724926

// update: 2026-05-14 23:30:48.789616

// update: 2026-05-14 23:30:50.595708

// update: 2026-05-14 23:30:53.695724

// update: 2026-05-14 23:30:55.397757
