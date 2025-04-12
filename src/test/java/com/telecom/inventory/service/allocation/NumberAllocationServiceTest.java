package com.telecom.inventory.service.allocation;

import com.telecom.inventory.dto.TelephoneNumberDTO;
import com.telecom.inventory.exception.ResourceNotFoundException;
import com.telecom.inventory.model.NumberStatus;
import com.telecom.inventory.model.TelephoneNumber;
import com.telecom.inventory.model.User;
import com.telecom.inventory.repository.TelephoneNumberRepository;
import com.telecom.inventory.repository.UserRepository;
import com.telecom.inventory.service.search.SearchIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NumberAllocationServiceTest {

    @Mock
    private TelephoneNumberRepository telephoneNumberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SearchIndexService searchIndexService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private NumberAllocationService allocationService;

    private TelephoneNumber mockTelephoneNumber;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        mockTelephoneNumber = TelephoneNumber.builder()
                .id(1L)
                .number("+12025550001")
                .countryCode("1")
                .areaCode("202")
                .status(NumberStatus.AVAILABLE)
                .build();
    }

    @Test
    void reserveNumber_shouldReserveAvailableNumber() {
        // Arrange
        when(telephoneNumberRepository.findByIdWithLock(1L)).thenReturn(Optional.of(mockTelephoneNumber));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(telephoneNumberRepository.save(any(TelephoneNumber.class))).thenReturn(mockTelephoneNumber);

        // Act
        TelephoneNumberDTO result = allocationService.reserveNumber(1L, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(NumberStatus.RESERVED, mockTelephoneNumber.getStatus());
        assertEquals(mockUser, mockTelephoneNumber.getUser());
        assertNotNull(mockTelephoneNumber.getReservedUntil());

        verify(telephoneNumberRepository).findByIdWithLock(1L);
        verify(userRepository).findByUsername("testuser");
        verify(telephoneNumberRepository).save(mockTelephoneNumber);
        verify(searchIndexService).updateTelephoneNumberIndex(mockTelephoneNumber);
    }

    @Test
    void reserveNumber_shouldThrowExceptionWhenNumberNotFound() {
        // Arrange
        when(telephoneNumberRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            allocationService.reserveNumber(1L, "testuser");
        });

        verify(telephoneNumberRepository).findByIdWithLock(1L);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(searchIndexService);
    }

    @Test
    void reserveNumber_shouldThrowExceptionWhenNumberNotAvailable() {
        // Arrange
        mockTelephoneNumber.setStatus(NumberStatus.RESERVED);
        when(telephoneNumberRepository.findByIdWithLock(1L)).thenReturn(Optional.of(mockTelephoneNumber));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            allocationService.reserveNumber(1L, "testuser");
        });

        verify(telephoneNumberRepository).findByIdWithLock(1L);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(searchIndexService);
    }
}
