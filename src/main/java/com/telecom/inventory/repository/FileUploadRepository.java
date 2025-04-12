package com.telecom.inventory.repository;

import com.telecom.inventory.model.FileUpload;
import com.telecom.inventory.model.FileUploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {

    Optional<FileUpload> findByBatchId(String batchId);

    List<FileUpload> findByStatus(FileUploadStatus status);

    List<FileUpload> findByUploadedBy(String uploadedBy);
}
