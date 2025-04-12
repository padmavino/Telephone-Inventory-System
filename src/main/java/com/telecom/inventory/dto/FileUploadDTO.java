package com.telecom.inventory.dto;

import com.telecom.inventory.model.FileUploadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDTO {

    private Long id;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String batchId;
    private FileUploadStatus status;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer failedRecords;
    private String errorMessage;
    private String uploadedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
