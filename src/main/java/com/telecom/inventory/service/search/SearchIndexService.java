package com.telecom.inventory.service.search;

import com.telecom.inventory.model.TelephoneNumber;
import com.telecom.inventory.model.TelephoneNumberDocument;
import com.telecom.inventory.repository.TelephoneNumberSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIndexService {

    private final TelephoneNumberSearchRepository searchRepository;

    public void indexTelephoneNumber(TelephoneNumber telephoneNumber) {
        TelephoneNumberDocument document = mapToDocument(telephoneNumber);
        searchRepository.save(document);
    }

    public void indexTelephoneNumbers(List<TelephoneNumber> telephoneNumbers) {
        List<TelephoneNumberDocument> documents = telephoneNumbers.stream()
                .map(this::mapToDocument)
                .collect(Collectors.toList());
        
        searchRepository.saveAll(documents);
    }

    public void updateTelephoneNumberIndex(TelephoneNumber telephoneNumber) {
        TelephoneNumberDocument document = mapToDocument(telephoneNumber);
        searchRepository.save(document);
    }

    public void deleteTelephoneNumberIndex(String id) {
        searchRepository.deleteById(id);
    }

    private TelephoneNumberDocument mapToDocument(TelephoneNumber telephoneNumber) {
        return TelephoneNumberDocument.builder()
                .id(telephoneNumber.getId().toString())
                .number(telephoneNumber.getNumber())
                .countryCode(telephoneNumber.getCountryCode())
                .areaCode(telephoneNumber.getAreaCode())
                .numberType(telephoneNumber.getNumberType())
                .category(telephoneNumber.getCategory())
                .features(telephoneNumber.getFeatures())
                .status(telephoneNumber.getStatus().name())
                .build();
    }
}
