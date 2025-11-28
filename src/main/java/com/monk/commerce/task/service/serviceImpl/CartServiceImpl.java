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
import com.monk.commerce.task.service.CartService;
import com.monk.commerce.task.strategy.CouponStrategy;
import com.monk.commerce.task.util.Constants;
import com.monk.commerce.task.validator.CartValidator;
import com.monk.commerce.task.validator.CouponValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CouponRepository couponRepository;
    private final CouponStrategyFactory strategyFactory;
    private final CartValidator cartValidator;
    private final CouponValidator couponValidator;

    public CartServiceImpl(
            CouponRepository couponRepository,
            CouponStrategyFactory strategyFactory,
            CartValidator cartValidator,
            CouponValidator couponValidator) {
        this.couponRepository = couponRepository;
        this.strategyFactory = strategyFactory;
        this.cartValidator = cartValidator;
        this.couponValidator = couponValidator;
    }

    @Override
    public Map<String, List<ApplicableCouponResponseDTO>> getApplicableCoupons(Map<String, CartRequestDTO> request) {
        log.info("Received request to fetch applicable coupons");
        Objects.requireNonNull(request, "Request cannot be null");
        CartRequestDTO cart = request.get("cart");
        if (Objects.isNull(cart)) {
            throw new InvalidCartException("Cart data not found in request");
        }

        cartValidator.validateCartRequest(cart);
        log.info("Finding applicable coupons for cart with {} items", cart.getItems().size());
        try {
            List<Coupon> validCoupons = couponRepository.findAllValidCoupons(LocalDateTime.now());
            if (Objects.isNull(validCoupons)) {
                log.warn("Repository returned null for valid coupons");
                Map<String, List<ApplicableCouponResponseDTO>> emptyResponse = new HashMap<>();
                emptyResponse.put("applicable_coupons", new ArrayList<>());
                return emptyResponse;
            }
            log.info("Found {} valid coupons", validCoupons.size());
            List<ApplicableCouponResponseDTO> applicableCoupons = validCoupons.stream()
                    .filter(coupon -> {
                        try {
                            CouponStrategy strategy = strategyFactory.getStrategy(coupon.getType());
                            return strategy.isApplicable(coupon, cart);
                        } catch (Exception e) {
                            log.error("Error checking applicability for coupon {}: {}", coupon.getId(), e.getMessage());
                            return false;
                        }
                    })
                    .map(coupon -> {
                        try {
                            CouponStrategy strategy = strategyFactory.getStrategy(coupon.getType());
                            BigDecimal discount = strategy.calculateDiscount(coupon, cart);
                            return ApplicableCouponResponseDTO.builder()
                                    .couponId(coupon.getId())
                                    .type(coupon.getType().getValue())
                                    .discount(discount)
                                    .build();
                        } catch (Exception e) {
                            log.error("Error calculating discount for coupon {}: {}", coupon.getId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(ApplicableCouponResponseDTO::getDiscount).reversed())
                    .collect(Collectors.toList());
            log.info("Found {} applicable coupons", applicableCoupons.size());
            Map<String, List<ApplicableCouponResponseDTO>> response = new HashMap<>();
            response.put("applicable_coupons", applicableCoupons);
            return response;
        } catch (Exception e) {
            log.error("Error while finding applicable coupons", e);
            throw new RuntimeException("Failed to fetch applicable coupons: " + e.getMessage(), e);
        }
    }

    @Override
    public AppliedCouponResponseDTO applyCoupon(Long couponId, Map<String, CartRequestDTO> request) {
        log.info("Received request to apply coupon {} to cart", couponId);
        couponValidator.validateCouponId(couponId);
        Objects.requireNonNull(request, "Request cannot be null");
        CartRequestDTO cart = request.get("cart");
        if (Objects.isNull(cart)) {
            throw new InvalidCartException("Cart data not found in request");
        }

        cartValidator.validateCartRequest(cart);
        log.info("Applying coupon {} to cart with {} items", couponId, cart.getItems().size());
        try {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new CouponNotFoundException(String.format(Constants.COUPON_NOT_FOUND, couponId)));
            couponValidator.validateCouponValid(coupon);
            CouponStrategy strategy = strategyFactory.getStrategy(coupon.getType());
            Objects.requireNonNull(strategy, "Coupon strategy cannot be null");
            if (!strategy.isApplicable(coupon, cart)) {
                throw new CouponNotApplicableException(Constants.COUPON_NOT_APPLICABLE + " (Coupon ID: " + couponId + ")");
            }
            AppliedCouponResponseDTO response = strategy.applyCoupon(coupon, cart);
            Objects.requireNonNull(response, "Applied coupon response cannot be null");
            log.info("Successfully applied coupon {} with discount: {}", couponId, response.getUpdatedCart().getTotalDiscount());
            return response;
        } catch (CouponNotFoundException | CouponNotApplicableException e) {
            log.error("Failed to apply coupon {}: {}", couponId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while applying coupon {}", couponId, e);
            throw new RuntimeException("Failed to apply coupon: " + e.getMessage(), e);
        }
    }
}