package com.telecom.inventory.repository;

import com.telecom.inventory.model.NumberStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NumberStatusHistoryRepository extends JpaRepository<NumberStatusHistory, Long> {

    @Query("SELECT h FROM NumberStatusHistory h WHERE h.telephoneNumber.id = :telephoneNumberId ORDER BY h.createdAt DESC")
    List<NumberStatusHistory> findByTelephoneNumberIdOrderByCreatedAtDesc(@Param("telephoneNumberId") Long telephoneNumberId);
}
