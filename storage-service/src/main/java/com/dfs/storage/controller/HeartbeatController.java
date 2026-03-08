package com.dfs.storage.controller;

import com.dfs.storage.service.HeartbeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/heartbeat")
@RequiredArgsConstructor
public class HeartbeatController {

    private final HeartbeatService service;

    @PostMapping("/stop")
    public String stop() {
        service.setHeartbeatEnabled(false);
        return "Heartbeat stopped";
    }

    @PostMapping("/start")
    public String start() {
        service.setHeartbeatEnabled(true);
        return "Heartbeat started";
    }
}
// update: 2026-05-14 23:30:36.046380

// update: 2026-05-14 23:30:51.213578
