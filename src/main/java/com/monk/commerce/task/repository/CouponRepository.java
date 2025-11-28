package com.monk.commerce.task.repository;

import com.monk.commerce.task.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    /**
     * Check if coupon code exists
     */
    boolean existsByCouponCode(String couponCode);

    /**
     * Find all valid coupons (active and not expired)
     */
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND (c.expirationDate IS NULL OR c.expirationDate > :currentDate)")
    List<Coupon> findAllValidCoupons(@Param("currentDate") LocalDateTime currentDate);

}
