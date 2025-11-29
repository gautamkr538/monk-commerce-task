package com.monk.commerce.task.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "buy_product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bxgy_coupon_id", nullable = false)
    private BxGyCoupon bxgyCoupon;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "tier_level", nullable = false)
    private Integer tierLevel = 1;

    @PrePersist
    protected void onCreate() {
        if (tierLevel == null) tierLevel = 1;
    }
}
