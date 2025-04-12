package com.telecom.inventory.repository;

import com.telecom.inventory.model.TelephoneNumberDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelephoneNumberSearchRepository extends ElasticsearchRepository<TelephoneNumberDocument, String> {

    List<TelephoneNumberDocument> findByStatus(String status);

    List<TelephoneNumberDocument> findByStatusAndCountryCode(String status, String countryCode);

    List<TelephoneNumberDocument> findByStatusAndAreaCode(String status, String areaCode);

    List<TelephoneNumberDocument> findByStatusAndNumberType(String status, String numberType);

    List<TelephoneNumberDocument> findByStatusAndCategory(String status, String category);

    List<TelephoneNumberDocument> findByStatusAndCountryCodeAndAreaCode(String status, String countryCode, String areaCode);
}
