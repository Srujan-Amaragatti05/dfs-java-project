// StorageNodeService.java
package com.dfs.metadata_service.service;

import com.dfs.metadata_service.entity.Chunk;
import com.dfs.metadata_service.repository.ChunkRepository;
import com.dfs.metadata_service.entity.StorageNode;
import com.dfs.metadata_service.repository.StorageNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class StorageNodeService {

    private final StorageNodeRepository repository;
    private final StorageNodeRepository storageNodeRepository;
    private final ChunkRepository chunkRepository;

    @Autowired
    private RestTemplate restTemplate;


    public StorageNode registerNode(StorageNode node) {
        node.setStatus(StorageNode.Status.ACTIVE);
        node.setLastHeartbeat(LocalDateTime.now());
        return repository.save(node);
    }
    public Optional<StorageNode> heartbeat(String ipAddress, Integer port) {
        Optional<StorageNode> nodeOpt = repository.findByIpAddressAndPort(ipAddress, port);

        if (nodeOpt.isPresent()) {
            StorageNode node = nodeOpt.get();
            node.setLastHeartbeat(LocalDateTime.now());
            node.setStatus(StorageNode.Status.ACTIVE);
            return Optional.of(repository.save(node));
        }

        return Optional.empty();
    }

    @Scheduled(fixedRate = 10000)
    public void detectFailures() {
        List<StorageNode> nodes = repository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (StorageNode node : nodes) {
            if (node.getLastHeartbeat() != null &&
                    node.getLastHeartbeat().plusSeconds(30).isBefore(now)) {

                node.setStatus(StorageNode.Status.DOWN);
                repository.save(node);
            }
        }
    }

    private void uploadToNode(StorageNode node, String chunkName, byte[] data) {

        String url = "http://" + node.getIpAddress() + ":" + node.getPort() + "/chunks/upload";

        ByteArrayResource resource = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return chunkName;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("chunkName", chunkName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    @Scheduled(fixedRate = 15000)
    public void reReplicate() {

        System.out.println("Re-replication job running...");

        List<StorageNode> activeNodes =
                storageNodeRepository.findByStatus(StorageNode.Status.ACTIVE);

        if (activeNodes.size() < 2) return;

        List<Chunk> chunks = chunkRepository.findAll();

        for (Chunk chunk : chunks) {

            List<Long> nodeIds = Arrays.stream(chunk.getStorageNodeIds().split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            List<Long> validNodeIds = new ArrayList<>();

            // 🔍 CHECK WHICH NODES ACTUALLY HAVE CHUNK
            for (Long nodeId : nodeIds) {
                String chunkName = null;
                try {
                    StorageNode node = storageNodeRepository.findById(nodeId).orElse(null);

                    if (node == null) continue;

                    chunkName = chunk.getFile().getId() + "_" + chunk.getChunkIndex();

                    String url = "http://" + node.getIpAddress() + ":" + node.getPort()
                            + "/chunks/" + chunkName;

                    // try fetching chunk
                    restTemplate.getForEntity(url, byte[].class);

                    validNodeIds.add(nodeId); // chunk exists

                } catch (Exception e) {
                    System.out.println(
                            "❌ Missing chunk: " + chunkName +
                                    " on node: " + nodeId
                    );
                }
            }

            // 🔥 IF REPLICA < 2 → RE-REPLICATE
            if (validNodeIds.size() < 2) {

                // get source node (first valid)
                if (validNodeIds.isEmpty()) continue;

                Long sourceNodeId = validNodeIds.get(0);
                StorageNode sourceNode = storageNodeRepository.findById(sourceNodeId).get();

                String chunkName = chunk.getFile().getId() + "_" + chunk.getChunkIndex();

                try {
                    String sourceUrl = "http://" + sourceNode.getIpAddress() + ":" + sourceNode.getPort()
                            + "/chunks/" + chunkName;

                    byte[] data = restTemplate.getForObject(sourceUrl, byte[].class);

                    // replicate to other nodes
                    for (StorageNode node : activeNodes) {

                        if (validNodeIds.contains(node.getId())) continue;

                        try {
                            uploadToNode(node, chunkName, data);
                            validNodeIds.add(node.getId());

                        } catch (Exception e) {
                            System.out.println("❌ Failed replication to node: " + node.getId());
                        }

                        if (validNodeIds.size() == 2) break;
                    }

                } catch (Exception e) {
                    System.out.println("❌ Failed to fetch source chunk: " + chunkName);
                    continue;
                }
            }

            // update DB if changed
            String updated = validNodeIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            if (!updated.equals(chunk.getStorageNodeIds())) {
                chunk.setStorageNodeIds(updated);
                chunkRepository.save(chunk);
            }
        }
    }
}
// update: 2026-05-14 23:30:30.943618
