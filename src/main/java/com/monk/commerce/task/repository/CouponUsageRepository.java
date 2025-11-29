package com.monk.commerce.task.repository;

import com.monk.commerce.task.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

    @Query(value = "SELECT * FROM coupon_usage WHERE coupon_id = :couponId AND user_id = :userId",
            nativeQuery = true)
    Optional<CouponUsage> findByCouponIdAndUserId(@Param("couponId") UUID couponId,
                                                  @Param("userId") String userId);

    @Modifying
    @Query(value = "INSERT INTO coupon_usage (id, coupon_id, user_id, usage_count, last_used_at) " +
            "VALUES (:id, :couponId, :userId, 1, :lastUsedAt) " +
            "ON CONFLICT (coupon_id, user_id) " +
            "DO UPDATE SET usage_count = coupon_usage.usage_count + 1, last_used_at = :lastUsedAt",
            nativeQuery = true)
    void upsertUsage(@Param("id") UUID id,
                     @Param("couponId") UUID couponId,
                     @Param("userId") String userId,
                     @Param("lastUsedAt") LocalDateTime lastUsedAt);

    @Query(value = "SELECT COALESCE(SUM(usage_count), 0) FROM coupon_usage WHERE coupon_id = :couponId AND user_id = :userId",
            nativeQuery = true)
    Integer getTotalUsageByUserAndCoupon(@Param("couponId") UUID couponId,
                                         @Param("userId") String userId);
}
