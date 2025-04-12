package com.telecom.inventory.service.file;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.telecom.inventory.dto.FileUploadDTO;
import com.telecom.inventory.exception.FileProcessingException;
import com.telecom.inventory.model.FileUpload;
import com.telecom.inventory.model.FileUploadStatus;
import com.telecom.inventory.model.NumberStatus;
import com.telecom.inventory.model.TelephoneNumber;
import com.telecom.inventory.repository.FileUploadRepository;
import com.telecom.inventory.repository.TelephoneNumberRepository;
import com.telecom.inventory.service.search.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private final FileUploadRepository fileUploadRepository;
    private final TelephoneNumberRepository telephoneNumberRepository;
    private final SearchIndexService searchIndexService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.file-processing.chunk-size}")
    private int chunkSize;

    @Value("${app.file-processing.max-threads}")
    private int maxThreads;

    @Value("${app.file-processing.temp-directory}")
    private String tempDirectory;

    @Value("${app.kafka.topics.file-processing}")
    private String fileProcessingTopic;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public FileUploadDTO uploadFile(MultipartFile file, String username) {
        try {
            // Create a unique batch ID for this upload
            String batchId = UUID.randomUUID().toString();
            
            // Save the file to a temporary location
            Path tempDir = Paths.get(tempDirectory);
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            
            String fileName = batchId + "_" + file.getOriginalFilename();
            Path filePath = tempDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            
            // Create a file upload record
            FileUpload fileUpload = FileUpload.builder()
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .batchId(batchId)
                    .status(FileUploadStatus.PENDING)
                    .uploadedBy(username)
                    .build();
            
            fileUpload = fileUploadRepository.save(fileUpload);
            
            // Send a message to Kafka to process the file asynchronously
            kafkaTemplate.send(fileProcessingTopic, batchId);
            
            return mapToDTO(fileUpload);
        } catch (IOException e) {
            log.error("Error uploading file", e);
            throw new FileProcessingException("Failed to upload file: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.file-processing}", groupId = "${spring.kafka.consumer.group-id}")
    public void processFile(String batchId) {
        log.info("Processing file with batch ID: {}", batchId);
        
        FileUpload fileUpload = fileUploadRepository.findByBatchId(batchId)
                .orElseThrow(() -> new FileProcessingException("File upload not found for batch ID: " + batchId));
        
        try {
            // Update status to PROCESSING
            fileUpload.setStatus(FileUploadStatus.PROCESSING);
            fileUploadRepository.save(fileUpload);
            
            // Process the file
            Path filePath = Paths.get(tempDirectory, fileUpload.getFileName());
            File file = filePath.toFile();
            
            processCSVFile(file, batchId, fileUpload);
            
            // Update status to COMPLETED
            fileUpload.setStatus(FileUploadStatus.COMPLETED);
            fileUploadRepository.save(fileUpload);
            
            // Clean up the temporary file
            Files.deleteIfExists(filePath);
            
        } catch (Exception e) {
            log.error("Error processing file", e);
            fileUpload.setStatus(FileUploadStatus.FAILED);
            fileUpload.setErrorMessage(e.getMessage());
            fileUploadRepository.save(fileUpload);
        }
    }

    private void processCSVFile(File file, String batchId, FileUpload fileUpload) throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            // Read header
            String[] header = reader.readNext();
            if (header == null) {
                throw new FileProcessingException("Empty file");
            }
            
            // Validate header
            validateHeader(header);
            
            // Read and process data in chunks
            List<String[]> chunk = new ArrayList<>(chunkSize);
            String[] line;
            int totalRecords = 0;
            int processedRecords = 0;
            int failedRecords = 0;
            
            while ((line = reader.readNext()) != null) {
                totalRecords++;
                chunk.add(line);
                
                if (chunk.size() >= chunkSize) {
                    int[] results = processChunk(chunk, batchId);
                    processedRecords += results[0];
                    failedRecords += results[1];
                    chunk.clear();
                    
                    // Update progress
                    updateProgress(fileUpload, totalRecords, processedRecords, failedRecords);
                }
            }
            
            // Process remaining records
            if (!chunk.isEmpty()) {
                int[] results = processChunk(chunk, batchId);
                processedRecords += results[0];
                failedRecords += results[1];
                
                // Update final progress
                updateProgress(fileUpload, totalRecords, processedRecords, failedRecords);
            }
        }
    }

    private void validateHeader(String[] header) {
        // Validate that the header contains the required columns
        boolean hasNumber = false;
        boolean hasCountryCode = false;
        
        for (String column : header) {
            if ("number".equalsIgnoreCase(column)) {
                hasNumber = true;
            } else if ("countryCode".equalsIgnoreCase(column)) {
                hasCountryCode = true;
            }
        }
        
        if (!hasNumber || !hasCountryCode) {
            throw new FileProcessingException("CSV file must contain 'number' and 'countryCode' columns");
        }
    }

    @Transactional
    public int[] processChunk(List<String[]> chunk, String batchId) {
        int processed = 0;
        int failed = 0;
        
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        
        // Split the chunk into smaller batches for parallel processing
        int batchSize = Math.max(1, chunk.size() / maxThreads);
        for (int i = 0; i < chunk.size(); i += batchSize) {
            int end = Math.min(chunk.size(), i + batchSize);
            List<String[]> batch = chunk.subList(i, end);
            
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                try {
                    processBatch(batch, batchId);
                    return true;
                } catch (Exception e) {
                    log.error("Error processing batch", e);
                    return false;
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // Count successes and failures
        for (CompletableFuture<Boolean> future : futures) {
            if (future.join()) {
                processed += batchSize;
            } else {
                failed += batchSize;
            }
        }
        
        return new int[]{processed, failed};
    }

    @Transactional
    public void processBatch(List<String[]> batch, String batchId) {
        List<TelephoneNumber> numbers = new ArrayList<>();
        
        for (String[] line : batch) {
            try {
                String number = line[0];
                String countryCode = line[1];
                
                // Skip if the number already exists
                if (telephoneNumberRepository.existsByNumber(number)) {
                    continue;
                }
                
                TelephoneNumber telephoneNumber = TelephoneNumber.builder()
                        .number(number)
                        .countryCode(countryCode)
                        .areaCode(line.length > 2 ? line[2] : null)
                        .numberType(line.length > 3 ? line[3] : null)
                        .category(line.length > 4 ? line[4] : null)
                        .features(line.length > 5 ? line[5] : null)
                        .status(NumberStatus.AVAILABLE)
                        .batchId(batchId)
                        .build();
                
                numbers.add(telephoneNumber);
            } catch (Exception e) {
                log.error("Error processing line", e);
            }
        }
        
        // Save all numbers in a batch
        if (!numbers.isEmpty()) {
            List<TelephoneNumber> savedNumbers = telephoneNumberRepository.saveAll(numbers);
            
            // Index the saved numbers in Elasticsearch
            searchIndexService.indexTelephoneNumbers(savedNumbers);
        }
    }

    private void updateProgress(FileUpload fileUpload, int totalRecords, int processedRecords, int failedRecords) {
        fileUpload.setTotalRecords(totalRecords);
        fileUpload.setProcessedRecords(processedRecords);
        fileUpload.setFailedRecords(failedRecords);
        fileUploadRepository.save(fileUpload);
    }

    public FileUploadDTO getFileUploadStatus(String batchId) {
        FileUpload fileUpload = fileUploadRepository.findByBatchId(batchId)
                .orElseThrow(() -> new FileProcessingException("File upload not found for batch ID: " + batchId));
        
        return mapToDTO(fileUpload);
    }

    public List<FileUploadDTO> getAllFileUploads() {
        return fileUploadRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private FileUploadDTO mapToDTO(FileUpload fileUpload) {
        return FileUploadDTO.builder()
                .id(fileUpload.getId())
                .fileName(fileUpload.getFileName())
                .originalFileName(fileUpload.getOriginalFileName())
                .fileSize(fileUpload.getFileSize())
                .contentType(fileUpload.getContentType())
                .batchId(fileUpload.getBatchId())
                .status(fileUpload.getStatus())
                .totalRecords(fileUpload.getTotalRecords())
                .processedRecords(fileUpload.getProcessedRecords())
                .failedRecords(fileUpload.getFailedRecords())
                .errorMessage(fileUpload.getErrorMessage())
                .uploadedBy(fileUpload.getUploadedBy())
                .createdAt(fileUpload.getCreatedAt())
                .updatedAt(fileUpload.getUpdatedAt())
                .build();
    }
}
