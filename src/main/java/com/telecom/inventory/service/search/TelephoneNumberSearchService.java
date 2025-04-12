package com.telecom.inventory.service.search;

import com.telecom.inventory.dto.SearchCriteriaDTO;
import com.telecom.inventory.dto.TelephoneNumberDTO;
import com.telecom.inventory.model.NumberStatus;
import com.telecom.inventory.model.TelephoneNumber;
import com.telecom.inventory.model.TelephoneNumberDocument;
import com.telecom.inventory.repository.TelephoneNumberRepository;
import com.telecom.inventory.repository.TelephoneNumberSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelephoneNumberSearchService {

    private final TelephoneNumberSearchRepository searchRepository;
    private final TelephoneNumberRepository telephoneNumberRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public List<TelephoneNumberDTO> searchTelephoneNumbers(SearchCriteriaDTO criteria) {
        // Default to AVAILABLE status if not specified
        if (criteria.getStatus() == null) {
            criteria.setStatus(NumberStatus.AVAILABLE);
        }
        
        // Default pagination
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 20;
        
        // Build the query
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        
        // Add status filter (always required)
        queryBuilder.must(QueryBuilders.termQuery("status", criteria.getStatus().name()));
        
        // Add optional filters
        if (criteria.getNumber() != null && !criteria.getNumber().isEmpty()) {
            queryBuilder.must(QueryBuilders.wildcardQuery("number", "*" + criteria.getNumber() + "*"));
        }
        
        if (criteria.getCountryCode() != null && !criteria.getCountryCode().isEmpty()) {
            queryBuilder.must(QueryBuilders.termQuery("countryCode", criteria.getCountryCode()));
        }
        
        if (criteria.getAreaCode() != null && !criteria.getAreaCode().isEmpty()) {
            queryBuilder.must(QueryBuilders.termQuery("areaCode", criteria.getAreaCode()));
        }
        
        if (criteria.getNumberType() != null && !criteria.getNumberType().isEmpty()) {
            queryBuilder.must(QueryBuilders.termQuery("numberType", criteria.getNumberType()));
        }
        
        if (criteria.getCategory() != null && !criteria.getCategory().isEmpty()) {
            queryBuilder.must(QueryBuilders.termQuery("category", criteria.getCategory()));
        }
        
        if (criteria.getFeatures() != null && !criteria.getFeatures().isEmpty()) {
            queryBuilder.must(QueryBuilders.matchQuery("features", criteria.getFeatures()));
        }
        
        // Create the search query
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withPageable(PageRequest.of(page, size))
                .build();
        
        // Execute the search
        SearchHits<TelephoneNumberDocument> searchHits = elasticsearchOperations.search(searchQuery, TelephoneNumberDocument.class);
        
        // Extract IDs from search results
        List<Long> ids = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(doc -> Long.parseLong(doc.getId()))
                .collect(Collectors.toList());
        
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Fetch the actual entities from the database
        List<TelephoneNumber> telephoneNumbers = telephoneNumberRepository.findAllById(ids);
        
        // Map to DTOs
        return telephoneNumbers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TelephoneNumberDTO getTelephoneNumberById(Long id) {
        Optional<TelephoneNumber> telephoneNumberOpt = telephoneNumberRepository.findById(id);
        return telephoneNumberOpt.map(this::mapToDTO).orElse(null);
    }

    public TelephoneNumberDTO getTelephoneNumberByNumber(String number) {
        Optional<TelephoneNumber> telephoneNumberOpt = telephoneNumberRepository.findByNumber(number);
        return telephoneNumberOpt.map(this::mapToDTO).orElse(null);
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
