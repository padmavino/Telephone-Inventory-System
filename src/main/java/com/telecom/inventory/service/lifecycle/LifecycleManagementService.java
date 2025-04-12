package com.telecom.inventory.service.lifecycle;

import com.telecom.inventory.dto.NumberStatusHistoryDTO;
import com.telecom.inventory.dto.StatusChangeDTO;
import com.telecom.inventory.dto.TelephoneNumberDTO;
import com.telecom.inventory.exception.ConcurrencyException;
import com.telecom.inventory.exception.IllegalStatusTransitionException;
import com.telecom.inventory.exception.ResourceNotFoundException;
import com.telecom.inventory.model.NumberStatus;
import com.telecom.inventory.model.NumberStatusHistory;
import com.telecom.inventory.model.TelephoneNumber;
import com.telecom.inventory.repository.NumberStatusHistoryRepository;
import com.telecom.inventory.repository.TelephoneNumberRepository;
import com.telecom.inventory.service.search.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LifecycleManagementService {

    private final TelephoneNumberRepository telephoneNumberRepository;
    private final NumberStatusHistoryRepository statusHistoryRepository;
    private final SearchIndexService searchIndexService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public TelephoneNumberDTO changeStatus(Long numberId, StatusChangeDTO statusChangeDTO, String username) {
        try {
            // Use pessimistic locking to prevent concurrent status changes
            TelephoneNumber telephoneNumber = telephoneNumberRepository.findByIdWithLock(numberId)
                    .orElseThrow(() -> new ResourceNotFoundException("Telephone number not found with id: " + numberId));
            
            // Validate the status transition
            validateStatusTransition(telephoneNumber.getStatus(), statusChangeDTO.getNewStatus());
            
            // Change the status
            NumberStatus oldStatus = telephoneNumber.getStatus();
            telephoneNumber.setStatus(statusChangeDTO.getNewStatus());
            
            // Handle specific status transitions
            handleStatusTransition(telephoneNumber, oldStatus, statusChangeDTO.getNewStatus());
            
            // Add status history
            telephoneNumber.addStatusHistory(oldStatus, statusChangeDTO.getNewStatus(), username, 
                    statusChangeDTO.getReason() != null ? statusChangeDTO.getReason() : "Status changed");
            
            // Save the changes
            telephoneNumber = telephoneNumberRepository.save(telephoneNumber);
            
            // Update the search index
            searchIndexService.updateTelephoneNumberIndex(telephoneNumber);
            
            return mapToDTO(telephoneNumber);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.error("Concurrent modification detected", e);
            throw new ConcurrencyException("The number has been modified by another user. Please try again.");
        }
    }

    private void validateStatusTransition(NumberStatus currentStatus, NumberStatus newStatus) {
        // Define valid status transitions
        boolean isValid = switch (currentStatus) {
            case AVAILABLE -> newStatus == NumberStatus.RESERVED;
            case RESERVED -> newStatus == NumberStatus.AVAILABLE || newStatus == NumberStatus.ALLOCATED;
            case ALLOCATED -> newStatus == NumberStatus.ACTIVATED || newStatus == NumberStatus.AVAILABLE;
            case ACTIVATED -> newStatus == NumberStatus.DEACTIVATED;
            case DEACTIVATED -> newStatus == NumberStatus.AVAILABLE || newStatus == NumberStatus.ACTIVATED;
        };
        
        if (!isValid) {
            throw new IllegalStatusTransitionException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void handleStatusTransition(TelephoneNumber telephoneNumber, NumberStatus oldStatus, NumberStatus newStatus) {
        // Handle specific status transitions
        if (newStatus == NumberStatus.AVAILABLE) {
            // Clear user and reservation time when number becomes available
            telephoneNumber.setUser(null);
            telephoneNumber.setReservedUntil(null);
        } else if (oldStatus == NumberStatus.RESERVED && newStatus == NumberStatus.ALLOCATED) {
            // Clear reservation time when number is allocated
            telephoneNumber.setReservedUntil(null);
        }
    }

    public List<NumberStatusHistoryDTO> getNumberStatusHistory(Long numberId) {
        // Check if the number exists
        if (!telephoneNumberRepository.existsById(numberId)) {
            throw new ResourceNotFoundException("Telephone number not found with id: " + numberId);
        }
        
        // Get the status history
        List<NumberStatusHistory> history = statusHistoryRepository.findByTelephoneNumberIdOrderByCreatedAtDesc(numberId);
        
        // Map to DTOs
        return history.stream()
                .map(this::mapToHistoryDTO)
                .collect(Collectors.toList());
    }

    private TelephoneNumberDTO mapToDTO(TelephoneNumber telephoneNumber) {
        return TelephoneNumberDTO.builder()
                .id(telephoneNumber.getId())
                .number(telephoneNumber.getNumber())
                .countryCode(telephoneNumber.getCountryCode())
                .areaCode(telephoneNumber.getAreaCode())
                .numberType(telephoneNumber.getNumberType())
                .category(telephoneNumber.getCategory())
                .features(telephoneNumber.getFeatures())
                .status(telephoneNumber.getStatus())
                .reservedUntil(telephoneNumber.getReservedUntil())
                .createdAt(telephoneNumber.getCreatedAt())
                .updatedAt(telephoneNumber.getUpdatedAt())
                .build();
    }

    private NumberStatusHistoryDTO mapToHistoryDTO(NumberStatusHistory history) {
        return NumberStatusHistoryDTO.builder()
                .id(history.getId())
                .telephoneNumberId(history.getTelephoneNumber().getId())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .userId(history.getUserId())
                .reason(history.getReason())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
