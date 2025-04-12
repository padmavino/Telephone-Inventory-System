package com.telecom.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "telephone_numbers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelephoneNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false, unique = true)
    private String number;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "area_code")
    private String areaCode;

    @Column(name = "number_type")
    private String numberType;

    @Column(name = "category")
    private String category;

    @Column(name = "features")
    private String features;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NumberStatus status;

    @Column(name = "batch_id")
    private String batchId;

    @OneToMany(mappedBy = "telephoneNumber", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<NumberStatusHistory> statusHistory = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addStatusHistory(NumberStatus oldStatus, NumberStatus newStatus, String userId, String reason) {
        NumberStatusHistory history = NumberStatusHistory.builder()
                .telephoneNumber(this)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .userId(userId)
                .reason(reason)
                .build();
        
        this.statusHistory.add(history);
    }
}
