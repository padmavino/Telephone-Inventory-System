package com.telecom.inventory.dto;

import com.telecom.inventory.model.NumberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberStatusHistoryDTO {

    private Long id;
    private Long telephoneNumberId;
    private NumberStatus oldStatus;
    private NumberStatus newStatus;
    private String userId;
    private String reason;
    private LocalDateTime createdAt;
}
