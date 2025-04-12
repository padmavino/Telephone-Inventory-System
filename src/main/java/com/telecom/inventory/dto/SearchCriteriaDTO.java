package com.telecom.inventory.dto;

import com.telecom.inventory.model.NumberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteriaDTO {

    private String number;
    private String countryCode;
    private String areaCode;
    private String numberType;
    private String category;
    private String features;
    private NumberStatus status;
    private Integer page;
    private Integer size;
}
