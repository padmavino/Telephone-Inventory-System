package com.telecom.inventory.dto;

import com.telecom.inventory.model.NumberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangeDTO {

    @NotNull(message = "New status is required")
    private NumberStatus newStatus;
    
    private String reason;
}
