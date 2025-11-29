package com.monk.commerce.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupon_usage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 1;

    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        if (usageCount == null) usageCount = 1;
        if (lastUsedAt == null) lastUsedAt = LocalDateTime.now();
    }
}
