package com.monk.commerce.task.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bxgy_coupon")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "id")
public class BxGyCoupon extends Coupon {

    @OneToMany(mappedBy = "bxgyCoupon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BuyProduct> buyProducts = new ArrayList<>();

    @OneToMany(mappedBy = "bxgyCoupon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GetProduct> getProducts = new ArrayList<>();

    @Column(name = "repetition_limit", nullable = false)
    private Integer repetitionLimit;

    @Column(name = "is_tiered", nullable = false)
    private Boolean isTiered = false;

    protected void onCreate() {
        super.onCreate();
        if (isTiered == null) isTiered = false;
    }
}
