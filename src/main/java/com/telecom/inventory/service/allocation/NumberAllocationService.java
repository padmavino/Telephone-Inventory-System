package com.telecom.inventory.service.allocation;

import com.telecom.inventory.dto.TelephoneNumberDTO;
import com.telecom.inventory.exception.ConcurrencyException;
import com.telecom.inventory.exception.ResourceNotFoundException;
import com.telecom.inventory.model.NumberStatus;
import com.telecom.inventory.model.TelephoneNumber;
import com.telecom.inventory.model.User;
import com.telecom.inventory.repository.TelephoneNumberRepository;
import com.telecom.inventory.repository.UserRepository;
import com.telecom.inventory.service.search.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NumberAllocationService {

    private final TelephoneNumberRepository telephoneNumberRepository;
    private final UserRepository userRepository;
    private final SearchIndexService searchIndexService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public TelephoneNumberDTO reserveNumber(Long numberId, String username) {
        try {
            // Use pessimistic locking to prevent concurrent reservations
            TelephoneNumber telephoneNumber = telephoneNumberRepository.findByIdWithLock(numberId)
                    .orElseThrow(() -> new ResourceNotFoundException("Telephone number not found with id: " + numberId));
            
            // Check if the number is available
            if (telephoneNumber.getStatus() != NumberStatus.AVAILABLE) {
                throw new IllegalStateException("Telephone number is not available for reservation");
            }
            
            // Get the user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
            
            // Set the number as reserved
            NumberStatus oldStatus = telephoneNumber.getStatus();
            telephoneNumber.setStatus(NumberStatus.RESERVED);
            telephoneNumber.setUser(user);
            telephoneNumber.setReservedUntil(LocalDateTime.now().plusHours(24)); // Reserve for 24 hours
            
            // Add status history
            telephoneNumber.addStatusHistory(oldStatus, NumberStatus.RESERVED, username, "Number reserved");
            
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

    @Transactional
    public TelephoneNumberDTO allocateNumber(Long numberId, String username) {
        try {
            // Use pessimistic locking to prevent concurrent allocations
            TelephoneNumber telephoneNumber = telephoneNumberRepository.findByIdWithLock(numberId)
                    .orElseThrow(() -> new ResourceNotFoundException("Telephone number not found with id: " + numberId));
            
            // Check if the number is reserved by the same user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
            
            if (telephoneNumber.getStatus() != NumberStatus.RESERVED || 
                    !user.getId().equals(telephoneNumber.getUser().getId())) {
                throw new IllegalStateException("Telephone number is not reserved by you");
            }
            
            // Set the number as allocated
            NumberStatus oldStatus = telephoneNumber.getStatus();
            telephoneNumber.setStatus(NumberStatus.ALLOCATED);
            telephoneNumber.setReservedUntil(null);
            
            // Add status history
            telephoneNumber.addStatusHistory(oldStatus, NumberStatus.ALLOCATED, username, "Number allocated");
            
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
}
