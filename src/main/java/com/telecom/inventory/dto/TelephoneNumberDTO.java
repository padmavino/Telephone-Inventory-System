package com.telecom.inventory.dto;

import com.telecom.inventory.model.NumberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelephoneNumberDTO {

    private Long id;
    
    @NotBlank(message = "Number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String number;
    
    @NotBlank(message = "Country code is required")
    private String countryCode;
    
    private String areaCode;
    
    private String numberType;
    
    private String category;
    
    private String features;
    
    private NumberStatus status;
    
    private UserDTO user;
    
    private LocalDateTime reservedUntil;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
