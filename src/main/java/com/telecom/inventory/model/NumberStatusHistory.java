package com.telecom.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "number_status_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "telephone_number_id", nullable = false)
    private TelephoneNumber telephoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private NumberStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private NumberStatus newStatus;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "reason")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
