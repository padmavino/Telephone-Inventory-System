package com.telecom.inventory.controller;

import com.telecom.inventory.dto.FileUploadDTO;
import com.telecom.inventory.service.file.FileProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/uploads")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "API for managing telephone number file uploads")
public class FileUploadController {

    private final FileProcessingService fileProcessingService;

    @PostMapping
    @Operation(summary = "Upload a file containing telephone numbers")
    public ResponseEntity<FileUploadDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Name") String username) {
        
        FileUploadDTO result = fileProcessingService.uploadFile(file, username);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{batchId}")
    @Operation(summary = "Get the status of a file upload")
    public ResponseEntity<FileUploadDTO> getFileUploadStatus(@PathVariable String batchId) {
        FileUploadDTO result = fileProcessingService.getFileUploadStatus(batchId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(summary = "Get all file uploads")
    public ResponseEntity<List<FileUploadDTO>> getAllFileUploads() {
        List<FileUploadDTO> result = fileProcessingService.getAllFileUploads();
        return ResponseEntity.ok(result);
    }
}
