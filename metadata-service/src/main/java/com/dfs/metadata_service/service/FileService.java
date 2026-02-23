package com.dfs.metadata_service.service;

import com.dfs.metadata_service.dto.FileUploadResponse;
import com.dfs.metadata_service.dto.FileResponse;
import com.dfs.metadata_service.entity.Chunk;
import com.dfs.metadata_service.entity.File;
import com.dfs.metadata_service.entity.StorageNode;
import com.dfs.metadata_service.repository.ChunkRepository;
import com.dfs.metadata_service.repository.FileRepository;
import com.dfs.metadata_service.repository.StorageNodeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final ChunkRepository chunkRepository;
    private final StorageNodeRepository storageNodeRepository;
    private final RestTemplate restTemplate;

    @Value("${dfs.chunk.size}")
    private long chunkSize;

    // ================= CHECKSUM =================
    private String generateChecksum(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("Checksum generation failed", e);
        }
    }

    // ================= UPLOAD =================
    public FileUploadResponse upload(MultipartFile file) {

        try {
            byte[] fileBytes = file.getBytes();
            long totalSize = fileBytes.length;

            int totalChunks = (int) Math.ceil((double) totalSize / chunkSize);

            List<StorageNode> activeNodes =
                    storageNodeRepository.findByStatus(StorageNode.Status.ACTIVE);

            if (activeNodes.size() < 2) {
                throw new RuntimeException("Not enough active nodes");
            }

            File fileEntity = File.builder()
                    .filename(file.getOriginalFilename())
                    .totalSize(totalSize)
                    .totalChunks(totalChunks)
                    .createdAt(LocalDateTime.now())
                    .build();

            fileEntity = fileRepository.save(fileEntity);

            List<Chunk> chunks = new ArrayList<>();

            for (int i = 0; i < totalChunks; i++) {

                int start = (int) (i * chunkSize);
                int end = (int) Math.min(start + chunkSize, fileBytes.length);

                byte[] chunkData = new byte[end - start];
                System.arraycopy(fileBytes, start, chunkData, 0, end - start);

                String checksum = generateChecksum(chunkData);

                StorageNode node1 = activeNodes.get(i % activeNodes.size());
                StorageNode node2 = activeNodes.get((i + 1) % activeNodes.size());

                String nodeIds = node1.getId() + "," + node2.getId();
                String chunkName = fileEntity.getId() + "_" + i;

                // send to storage-service
                ByteArrayResource resource = new ByteArrayResource(chunkData) {
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

                String url1 = "http://" + node1.getIpAddress() + ":" + node1.getPort() + "/chunks/upload";
                String url2 = "http://" + node2.getIpAddress() + ":" + node2.getPort() + "/chunks/upload";

                // send to node1
                restTemplate.postForEntity(
                        url1,
                        new HttpEntity<>(body, headers),
                        String.class
                );

                // send to node2
                restTemplate.postForEntity(
                        url2,
                        new HttpEntity<>(body, headers),
                        String.class
                );

                Chunk chunk = Chunk.builder()
                        .file(fileEntity)
                        .chunkIndex(i)
                        .storageNodeIds(nodeIds)
                        .checksum(checksum)
                        .build();

                chunks.add(chunk);
            }

            chunkRepository.saveAll(chunks);

            List<FileUploadResponse.ChunkInfo> chunkInfos = chunks.stream()
                    .map(c -> new FileUploadResponse.ChunkInfo(
                            c.getChunkIndex(),
                            Arrays.stream(c.getStorageNodeIds().split(","))
                                    .map(Long::parseLong)
                                    .toList()
                    ))
                    .toList();

            return new FileUploadResponse(
                    fileEntity.getId(),
                    fileEntity.getFilename(),
                    fileEntity.getTotalChunks(),
                    chunkInfos
            );

        } catch (IOException e) {
            throw new RuntimeException("File processing failed", e);
        }
    }

    // ================= DOWNLOAD =================
    public ResponseEntity<byte[]> download(Long fileId) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        List<Chunk> chunks = chunkRepository.findByFileId(fileId);
        chunks.sort(Comparator.comparingInt(Chunk::getChunkIndex));

        List<CompletableFuture<byte[]>> futures = chunks.stream()
                .map(chunk -> CompletableFuture.supplyAsync(() -> {

                    String chunkName = fileId + "_" + chunk.getChunkIndex();

                    List<Long> nodeIds = Arrays.stream(chunk.getStorageNodeIds().split(","))
                            .map(Long::parseLong)
                            .toList();

                    List<Long> activeNodeIds = nodeIds.stream()
                            .filter(id -> storageNodeRepository.findById(id)
                                    .map(n -> n.getStatus() == StorageNode.Status.ACTIVE)
                                    .orElse(false))
                            .toList();

                    if (activeNodeIds.isEmpty()) {
                        throw new RuntimeException("File not available (all replicas down)");
                    }

                    for (Long nodeId : activeNodeIds) {
                        try {
                            StorageNode node = storageNodeRepository.findById(nodeId)
                                    .orElseThrow(() -> new RuntimeException("Node not found"));
                            
                            String url = "http://" + node.getIpAddress() + ":" + node.getPort() + "/chunks/" + chunkName;

                            ResponseEntity<byte[]> response =
                                    restTemplate.getForEntity(url, byte[].class);

                            byte[] data = response.getBody();

                            if (data != null) {
                                String newChecksum = generateChecksum(data);

                                if (newChecksum.equals(chunk.getChecksum())) {
                                    return data;
                                } else{
                                    return ("Data is Corrupted".getBytes());
                                }
                            }

                        } catch (Exception e) {
                            // try next node
                        }
                    }

                    throw new RuntimeException("Data corrupted");

                }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (CompletableFuture<byte[]> future : futures) {
            outputStream.writeBytes(future.join());
        }

        byte[] finalFile = outputStream.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(finalFile);
    }

    // ================= OTHER METHODS =================

    public FileResponse getFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        List<Chunk> chunks = chunkRepository.findByFileId(fileId);

        List<FileResponse.ChunkInfo> chunkInfos = chunks.stream()
                .map(c -> new FileResponse.ChunkInfo(
                        c.getChunkIndex(),
                        Arrays.stream(c.getStorageNodeIds().split(","))
                                .map(Long::parseLong)
                                .collect(Collectors.toList())
                ))
                .toList();

        return new FileResponse(
                file.getId(),
                file.getFilename(),
                file.getTotalChunks(),
                chunkInfos
        );
    }

    @Transactional
    public String deleteFile(Long fileId) {
        if (!fileRepository.existsById(fileId)) {
            throw new RuntimeException("File not found");
        }

        chunkRepository.deleteByFileId(fileId);
        fileRepository.deleteById(fileId);

        return "File deleted successfully";
    }
}