package com.telecom.inventory.service.file;

import com.telecom.inventory.dto.FileUploadDTO;
import com.telecom.inventory.model.FileUpload;
import com.telecom.inventory.model.FileUploadStatus;
import com.telecom.inventory.repository.FileUploadRepository;
import com.telecom.inventory.repository.TelephoneNumberRepository;
import com.telecom.inventory.service.search.SearchIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileProcessingServiceTest {

    @Mock
    private FileUploadRepository fileUploadRepository;

    @Mock
    private TelephoneNumberRepository telephoneNumberRepository;

    @Mock
    private SearchIndexService searchIndexService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private FileProcessingService fileProcessingService;

    private MultipartFile mockFile;
    private FileUpload mockFileUpload;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "file",
                "numbers.csv",
                "text/csv",
                "number,countryCode,areaCode\n+12025550001,1,202".getBytes()
        );

        mockFileUpload = FileUpload.builder()
                .id(1L)
                .fileName("batch-id_numbers.csv")
                .originalFileName("numbers.csv")
                .fileSize(100L)
                .contentType("text/csv")
                .batchId("batch-id")
                .status(FileUploadStatus.PENDING)
                .uploadedBy("testuser")
                .build();
    }

    @Test
    void uploadFile_shouldCreateFileUploadAndSendKafkaMessage() throws IOException {
        // Arrange
        when(fileUploadRepository.save(any(FileUpload.class))).thenReturn(mockFileUpload);

        // Act
        FileUploadDTO result = fileProcessingService.uploadFile(mockFile, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("numbers.csv", result.getOriginalFileName());
        assertEquals(FileUploadStatus.PENDING, result.getStatus());
        assertEquals("testuser", result.getUploadedBy());

        verify(fileUploadRepository).save(any(FileUpload.class));
        verify(kafkaTemplate).send(anyString(), anyString());
    }

    @Test
    void getFileUploadStatus_shouldReturnFileUploadDTO() {
        // Arrange
        when(fileUploadRepository.findByBatchId("batch-id")).thenReturn(Optional.of(mockFileUpload));

        // Act
        FileUploadDTO result = fileProcessingService.getFileUploadStatus("batch-id");

        // Assert
        assertNotNull(result);
        assertEquals("numbers.csv", result.getOriginalFileName());
        assertEquals(FileUploadStatus.PENDING, result.getStatus());

        verify(fileUploadRepository).findByBatchId("batch-id");
    }
}
