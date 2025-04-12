package com.telecom.inventory.controller;

import com.telecom.inventory.dto.NumberStatusHistoryDTO;
import com.telecom.inventory.dto.SearchCriteriaDTO;
import com.telecom.inventory.dto.StatusChangeDTO;
import com.telecom.inventory.dto.TelephoneNumberDTO;
import com.telecom.inventory.service.allocation.NumberAllocationService;
import com.telecom.inventory.service.lifecycle.LifecycleManagementService;
import com.telecom.inventory.service.search.TelephoneNumberSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/numbers")
@RequiredArgsConstructor
@Tag(name = "Telephone Numbers", description = "API for managing telephone numbers")
public class TelephoneNumberController {

    private final TelephoneNumberSearchService searchService;
    private final NumberAllocationService allocationService;
    private final LifecycleManagementService lifecycleService;

    @GetMapping("/search")
    @Operation(summary = "Search for telephone numbers based on criteria")
    public ResponseEntity<List<TelephoneNumberDTO>> searchNumbers(SearchCriteriaDTO criteria) {
        List<TelephoneNumberDTO> results = searchService.searchTelephoneNumbers(criteria);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a telephone number by ID")
    public ResponseEntity<TelephoneNumberDTO> getNumberById(@PathVariable Long id) {
        TelephoneNumberDTO result = searchService.getTelephoneNumberById(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Reserve a telephone number")
    public ResponseEntity<TelephoneNumberDTO> reserveNumber(
            @PathVariable Long id,
            @RequestHeader("X-User-Name") String username) {
        
        TelephoneNumberDTO result = allocationService.reserveNumber(id, username);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/allocate")
    @Operation(summary = "Allocate a telephone number")
    public ResponseEntity<TelephoneNumberDTO> allocateNumber(
            @PathVariable Long id,
            @RequestHeader("X-User-Name") String username) {
        
        TelephoneNumberDTO result = allocationService.allocateNumber(id, username);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Change the status of a telephone number")
    public ResponseEntity<TelephoneNumberDTO> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusChangeDTO statusChangeDTO,
            @RequestHeader("X-User-Name") String username) {
        
        TelephoneNumberDTO result = lifecycleService.changeStatus(id, statusChangeDTO, username);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get the status history of a telephone number")
    public ResponseEntity<List<NumberStatusHistoryDTO>> getStatusHistory(@PathVariable Long id) {
        List<NumberStatusHistoryDTO> result = lifecycleService.getNumberStatusHistory(id);
        return ResponseEntity.ok(result);
    }
}
