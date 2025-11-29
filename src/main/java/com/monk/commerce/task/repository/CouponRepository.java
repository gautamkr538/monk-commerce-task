package com.monk.commerce.task.repository;

import com.monk.commerce.task.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Coupon c WHERE c.couponCode = :couponCode AND c.isActive = true")
    boolean existsActiveByCouponCode(@Param("couponCode") String couponCode);

    @Query("SELECT c FROM Coupon c WHERE c.id = :id AND c.isActive = true")
    Optional<Coupon> findActiveById(@Param("id") UUID id);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    List<Coupon> findAllActiveCoupons();

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
            "AND (c.expirationDate IS NULL OR c.expirationDate > :currentDate) " +
            "AND (c.maxUsageLimit IS NULL OR c.usageCount < c.maxUsageLimit) " +
            "ORDER BY c.priority DESC, c.createdAt DESC")
    List<Coupon> findAllValidCoupons(@Param("currentDate") LocalDateTime currentDate);

    @Modifying
    @Query("UPDATE Coupon c SET c.isActive = false, c.updatedAt = :updatedAt WHERE c.id = :id AND c.isActive = true")
    int softDeleteById(@Param("id") UUID id, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query("UPDATE Coupon c SET c.usageCount = c.usageCount + 1, c.updatedAt = :updatedAt WHERE c.id = :id")
    void incrementUsageCount(@Param("id") UUID id, @Param("updatedAt") LocalDateTime updatedAt);
}