package com.telecom.inventory.repository;

import com.telecom.inventory.model.NumberStatus;
import com.telecom.inventory.model.TelephoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface TelephoneNumberRepository extends JpaRepository<TelephoneNumber, Long> {

    boolean existsByNumber(String number);

    Optional<TelephoneNumber> findByNumber(String number);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TelephoneNumber t WHERE t.id = :id")
    Optional<TelephoneNumber> findByIdWithLock(@Param("id") Long id);

    List<TelephoneNumber> findByBatchId(String batchId);

    List<TelephoneNumber> findByStatus(NumberStatus status);

    @Query("SELECT t FROM TelephoneNumber t WHERE t.status = :status AND t.countryCode = :countryCode")
    List<TelephoneNumber> findByStatusAndCountryCode(@Param("status") NumberStatus status, @Param("countryCode") String countryCode);

    @Query("SELECT t FROM TelephoneNumber t WHERE t.status = :status AND t.areaCode = :areaCode")
    List<TelephoneNumber> findByStatusAndAreaCode(@Param("status") NumberStatus status, @Param("areaCode") String areaCode);

    @Query("SELECT t FROM TelephoneNumber t WHERE t.status = :status AND t.numberType = :numberType")
    List<TelephoneNumber> findByStatusAndNumberType(@Param("status") NumberStatus status, @Param("numberType") String numberType);

    @Query("SELECT t FROM TelephoneNumber t WHERE t.status = :status AND t.category = :category")
    List<TelephoneNumber> findByStatusAndCategory(@Param("status") NumberStatus status, @Param("category") String category);
}
