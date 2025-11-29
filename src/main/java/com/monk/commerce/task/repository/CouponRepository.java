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

    @Query(value = "SELECT EXISTS(SELECT 1 FROM coupon WHERE coupon_code = :couponCode AND is_active = true)", nativeQuery = true)
    boolean existsActiveByCouponCode(@Param("couponCode") String couponCode);

    @Query(value = "SELECT * FROM coupon WHERE id = :id AND is_active = true", nativeQuery = true)
    Optional<Coupon> findActiveById(@Param("id") UUID id);

    @Query(value = "SELECT * FROM coupon WHERE is_active = true ORDER BY created_at DESC", nativeQuery = true)
    List<Coupon> findAllActiveCoupons();

    @Query(value = "SELECT * FROM coupon " +
            "WHERE is_active = true " +
            "AND (expiration_date IS NULL OR expiration_date > :currentDate) " +
            "AND (max_usage_limit IS NULL OR usage_count < max_usage_limit) " +
            "ORDER BY priority DESC, created_at DESC", nativeQuery = true)
    List<Coupon> findAllValidCoupons(@Param("currentDate") LocalDateTime currentDate);

    @Modifying
    @Query(value = "UPDATE coupon SET is_active = false, updated_at = :updatedAt WHERE id = :id AND is_active = true", nativeQuery = true)
    int softDeleteById(@Param("id") UUID id, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query(value = "UPDATE coupon SET usage_count = usage_count + 1, updated_at = :updatedAt WHERE id = :id", nativeQuery = true)
    void incrementUsageCount(@Param("id") UUID id, @Param("updatedAt") LocalDateTime updatedAt);
}
