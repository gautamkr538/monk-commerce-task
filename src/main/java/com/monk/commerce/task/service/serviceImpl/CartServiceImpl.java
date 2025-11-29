package com.monk.commerce.task.service.serviceImpl;

import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.ApplicableCouponResponseDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.exception.CouponNotApplicableException;
import com.monk.commerce.task.exception.CouponNotFoundException;
import com.monk.commerce.task.exception.InvalidCartException;
import com.monk.commerce.task.factory.CouponStrategyFactory;
import com.monk.commerce.task.repository.CouponRepository;
import com.monk.commerce.task.repository.CouponUsageRepository;
import com.monk.commerce.task.service.CartService;
import com.monk.commerce.task.strategy.CouponStrategy;
import com.monk.commerce.task.util.Constants;
import com.monk.commerce.task.util.CouponUtil;
import com.monk.commerce.task.validator.CartValidator;
import com.monk.commerce.task.validator.CouponValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CouponRepository couponRepository;
    private final CouponStrategyFactory strategyFactory;
    private final CartValidator cartValidator;
    private final CouponValidator couponValidator;
    private final CouponUsageRepository couponUsageRepository;

    public CartServiceImpl(
            CouponRepository couponRepository,
            CouponStrategyFactory strategyFactory,
            CartValidator cartValidator,
            CouponValidator couponValidator,
            CouponUsageRepository couponUsageRepository) {
        this.couponRepository = couponRepository;
        this.strategyFactory = strategyFactory;
        this.cartValidator = cartValidator;
        this.couponValidator = couponValidator;
        this.couponUsageRepository = couponUsageRepository;
    }

    @Override
    public Map<String, List<ApplicableCouponResponseDTO>> getApplicableCoupons(Map<String, CartRequestDTO> request) {
        log.debug("Fetching applicable coupons for cart");
        Objects.requireNonNull(request, "Request cannot be null");
        CartRequestDTO cart = request.get("cart");
        if (cart == null) {
            log.error("Cart data not found in request");
            throw new InvalidCartException("Cart data not found in request");
        }
        cartValidator.validateCartRequest(cart);
        log.debug("Cart validated with {} items", cart.getItems().size());
        List<Coupon> validCoupons = couponRepository.findAllValidCoupons(LocalDateTime.now());
        if (validCoupons == null || validCoupons.isEmpty()) {
            log.info("No valid coupons found");
            Map<String, List<ApplicableCouponResponseDTO>> emptyResponse = new HashMap<>();
            emptyResponse.put("applicable_coupons", new ArrayList<>());
            return emptyResponse;
        }
        log.debug("Found {} valid coupons to evaluate", validCoupons.size());
        List<ApplicableCouponResponseDTO> applicableCoupons = validCoupons.stream()
                .filter(coupon -> isApplicableToCoupon(coupon, cart))
                .map(coupon -> buildApplicableCouponResponse(coupon, cart))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ApplicableCouponResponseDTO::getDiscount).reversed())
                .collect(Collectors.toList());
        log.info("Found {} applicable coupons with discounts", applicableCoupons.size());
        Map<String, List<ApplicableCouponResponseDTO>> response = new HashMap<>();
        response.put("applicable_coupons", applicableCoupons);
        return response;
    }

    @Override
    @Transactional
    public AppliedCouponResponseDTO applyCoupon(UUID couponId, Map<String, CartRequestDTO> request) {
        log.info("Applying coupon: {}", couponId);
        couponValidator.validateCouponId(couponId);
        Objects.requireNonNull(request, "Request cannot be null");
        CartRequestDTO cart = request.get("cart");
        if (cart == null) {
            log.error("Cart data not found in request");
            throw new InvalidCartException("Cart data not found in request");
        }
        cartValidator.validateCartRequest(cart);
        log.debug("Validating cart with {} items for coupon: {}", cart.getItems().size(), couponId);
        Coupon coupon = couponRepository.findActiveById(couponId)
                .orElseThrow(() -> {log.error("Coupon not found: {}", couponId);
                    return new CouponNotFoundException(String.format(Constants.COUPON_NOT_FOUND, couponId));});
        log.debug("Found coupon: {} with type: {}", couponId, coupon.getType());
        couponValidator.validateCouponValid(coupon);
        if (cart.getUserId() != null && CouponUtil.hasUserReachedLimit(coupon, cart.getUserId())) {
            log.error("User {} has reached usage limit for coupon: {}", cart.getUserId(), couponId);
            throw new CouponNotApplicableException("User has reached usage limit for this coupon");
        }
        CouponStrategy strategy = strategyFactory.getStrategy(coupon.getType());
        log.debug("Using strategy: {} for coupon: {}", strategy.getClass().getSimpleName(), couponId);
        if (!strategy.isApplicable(coupon, cart)) {
            log.error("Coupon {} is not applicable to cart", couponId);
            throw new CouponNotApplicableException(Constants.COUPON_NOT_APPLICABLE);
        }
        AppliedCouponResponseDTO response = strategy.applyCoupon(coupon, cart);
        log.debug("Calculated discount: {} for coupon: {}", response.getUpdatedCart().getTotalDiscount(), couponId);
        updateCouponUsage(coupon, cart.getUserId());
        log.info("Successfully applied coupon: {} with discount: {}", couponId, response.getUpdatedCart().getTotalDiscount());
        return response;
    }

    private boolean isApplicableToCoupon(Coupon coupon, CartRequestDTO cart) {
        try {
            log.debug("Checking applicability for coupon: {}", coupon.getId());
            if (cart.getUserId() != null && CouponUtil.hasUserReachedLimit(coupon, cart.getUserId())) {
                log.debug("User {} reached limit for coupon: {}", cart.getUserId(), coupon.getId());
                return false;
            }
            CouponStrategy strategy = strategyFactory.getStrategy(coupon.getType());
            boolean applicable = strategy.isApplicable(coupon, cart);
            log.debug("Coupon {} applicability: {}", coupon.getId(), applicable);
            return applicable;
        } catch (Exception e) {
            log.error("Error checking applicability for coupon {}: {}", coupon.getId(), e.getMessage());
            return false;
        }
    }

    private ApplicableCouponResponseDTO buildApplicableCouponResponse(Coupon coupon, CartRequestDTO cart) {
        try {
            log.debug("Building response for coupon: {}", coupon.getId());
            CouponStrategy strategy = strategyFactory.getStrategy(coupon.getType());
            BigDecimal discount = strategy.calculateDiscount(coupon, cart);
            log.debug("Calculated discount: {} for coupon: {}", discount, coupon.getId());
            return ApplicableCouponResponseDTO.builder()
                    .couponId(coupon.getId())
                    .type(coupon.getType().getValue())
                    .discount(discount)
                    .isStackable(coupon.getAllowStacking())
                    .priority(coupon.getPriority())
                    .userUsageRemaining(cart.getUserId() != null ?
                            CouponUtil.getUserUsageRemaining(coupon, cart.getUserId()) : null)
                    .globalUsageRemaining(CouponUtil.getGlobalUsageRemaining(coupon))
                    .build();
        } catch (Exception e) {
            log.error("Error calculating discount for coupon {}: {}", coupon.getId(), e.getMessage());
            return null;
        }
    }

    private void updateCouponUsage(Coupon coupon, String userId) {
        log.debug("Updating usage count for coupon: {}", coupon.getId());
        couponRepository.incrementUsageCount(coupon.getId(), LocalDateTime.now());
        if (userId != null) {
            log.debug("Recording usage for user: {} on coupon: {}", userId, coupon.getId());
            couponUsageRepository.upsertUsage(UUID.randomUUID(), coupon.getId(), userId, LocalDateTime.now());
        }
    }
}
