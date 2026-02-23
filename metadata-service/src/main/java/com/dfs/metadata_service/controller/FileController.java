// FileController.java (modify return type)
package com.dfs.metadata_service.controller;

import com.dfs.metadata_service.dto.FileUploadResponse;
import com.dfs.metadata_service.dto.FileResponse;
import com.dfs.metadata_service.entity.File;
import com.dfs.metadata_service.service.FileService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService service;

    @PostMapping("/upload")
    public FileUploadResponse upload(@RequestParam("file") MultipartFile file) {
        return service.upload(file);
    }

    @Data
    static class UploadRequest {
        private String filename;
        private Long totalSize;
    }

    @GetMapping("/{fileId}")
    public FileResponse getFile(@PathVariable Long fileId) {
        return service.getFile(fileId);
    }

    @DeleteMapping("/{fileId}")
    public String deleteFile(@PathVariable Long fileId) {
        return service.deleteFile(fileId);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        return service.download(fileId);
    }

//    @GetMapping("/download/{fileId}")
//    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
//
//        File file = service.getFileEntity(fileId); // method that returns File entity
//        byte[] data = service.download(fileId).getBody(); // method that returns merged bytes
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + file.getFilename() + "\"")
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(data);
//    }
}