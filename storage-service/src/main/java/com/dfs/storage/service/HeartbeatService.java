package com.dfs.storage.service;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class HeartbeatService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    @Setter
    private volatile boolean heartbeatEnabled = true;

    private final String ip = "127.0.0.1";
    private int port;

    private final String metadataUrl = "http://localhost:8081";

    private volatile boolean registered = false;

    // ✅ INIT ON STARTUP
    @PostConstruct
    public void init() {
        this.port = Integer.parseInt(env.getProperty("server.port", "8080"));
        System.out.println("🚀 Node started on port: " + port);
        // small delay to ensure metadata is up
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {}
        registered = registerNode();
        if (!registered) {
            System.out.println("⛔ Registration failed, will retry automatically...");
        }
    }

    // ✅ SAFE REGISTER (NO DUPLICATES)
    private boolean registerNode() {
        try {
            String url = metadataUrl + "/nodes/register";
            Map<String, Object> request = new HashMap<>();
            request.put("ipAddress", ip);
            request.put("port", port);
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("✅ Node registered: " + ip + ":" + port);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
            return false;
        }
    }

    // ✅ HEARTBEAT
    @Scheduled(fixedRate = 5000)
    public void sendHeartbeat() {
        if (!heartbeatEnabled) {
            System.out.println("⏸️ Heartbeat paused: " + port);
            return;
        }
        if (!registered) return;
        try {
            String url = metadataUrl + "/nodes/heartbeat";
            Map<String, Object> request = new HashMap<>();
            request.put("ipAddress", ip);
            request.put("port", port);
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("💓 Heartbeat: " + port);
        } catch (Exception e) {
            System.out.println("❌ Heartbeat failed: " + e.getMessage());
            registered = false; // force re-registration
        }
    }

    // ✅ RETRY ONLY IF NOT REGISTERED
    @Scheduled(fixedRate = 10000)
    public void retryRegistration() {
        if (!heartbeatEnabled) return;
        if (registered) return;
        System.out.println("🔁 Retrying registration for: " + port);
        registered = registerNode();
    }

    // ✅ MANUAL CONTROL (for testing)
    public void stopHeartbeat() {
        this.heartbeatEnabled = false;
        System.out.println("⛔ Heartbeat stopped: " + port);
    }

    public void startHeartbeat() {
        this.heartbeatEnabled = true;
        System.out.println("▶️ Heartbeat resumed: " + port);
    }
}
// update: 2026-05-14 23:30:29.317942

// update: 2026-05-14 23:30:31.354439

// update: 2026-05-14 23:30:34.213519

// update: 2026-05-14 23:30:37.525785
