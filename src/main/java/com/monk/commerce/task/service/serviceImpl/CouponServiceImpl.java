package com.monk.commerce.task.service.serviceImpl;

import com.monk.commerce.task.dto.request.CouponRequestDTO;
import com.monk.commerce.task.dto.response.CouponResponseDTO;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.exception.CouponNotFoundException;
import com.monk.commerce.task.exception.InvalidCouponException;
import com.monk.commerce.task.mapper.CouponMapper;
import com.monk.commerce.task.repository.CouponRepository;
import com.monk.commerce.task.service.CouponService;
import com.monk.commerce.task.util.Constants;
import com.monk.commerce.task.util.CouponUtil;
import com.monk.commerce.task.validator.CouponValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CouponServiceImpl implements CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponServiceImpl.class);

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;
    private final CouponValidator couponValidator;

    public CouponServiceImpl(
            CouponRepository couponRepository,
            CouponMapper couponMapper,
            CouponValidator couponValidator) {
        this.couponRepository = couponRepository;
        this.couponMapper = couponMapper;
        this.couponValidator = couponValidator;
    }

    @Override
    public CouponResponseDTO createCoupon(CouponRequestDTO request) {
        log.debug("Creating coupon with type: {}", request.getType());
        Objects.requireNonNull(request, "Coupon request cannot be null");
        String couponCode = request.getCouponCode() != null ?
                request.getCouponCode() : CouponUtil.generateCouponCode();
        log.debug("Using coupon code: {}", couponCode);
        if (couponRepository.existsActiveByCouponCode(couponCode)) {
            log.warn("Coupon code already exists: {}", couponCode);
            throw new InvalidCouponException("Coupon code already exists: " + couponCode);
        }
        Coupon coupon = couponMapper.toEntity(request);
        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("Created coupon: {} with code: {}", savedCoupon.getId(), savedCoupon.getCouponCode());
        return couponMapper.toResponseDTO(savedCoupon);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponseDTO> getAllCoupons() {
        log.debug("Fetching all active coupons");
        List<Coupon> coupons = couponRepository.findAllActiveCoupons();
        if (coupons == null) {
            log.error("Repository returned null for active coupons");
            throw new IllegalStateException("Failed to fetch coupons");
        }
        log.info("Found {} active coupons", coupons.size());
        return coupons.stream()
                .map(couponMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponseDTO getCouponById(UUID id) {
        log.debug("Fetching coupon by ID: {}", id);
        couponValidator.validateCouponId(id);
        Coupon coupon = couponRepository.findActiveById(id)
                .orElseThrow(() -> {
                    log.warn("Coupon not found with ID: {}", id);
                    return new CouponNotFoundException(String.format(Constants.COUPON_NOT_FOUND, id));
                });
        log.debug("Found coupon: {} with code: {}", id, coupon.getCouponCode());
        return couponMapper.toResponseDTO(coupon);
    }

    @Override
    public CouponResponseDTO updateCoupon(UUID id, CouponRequestDTO request) {
        log.debug("Updating coupon with ID: {}", id);
        couponValidator.validateCouponId(id);
        Objects.requireNonNull(request, "Coupon request cannot be null");
        Coupon existingCoupon = couponRepository.findActiveById(id)
                .orElseThrow(() -> {
                    log.warn("Coupon not found for update: {}", id);
                    return new CouponNotFoundException(String.format(Constants.COUPON_NOT_FOUND, id));
                });
        if (request.getCouponCode() != null &&
                !request.getCouponCode().equals(existingCoupon.getCouponCode()) &&
                couponRepository.existsActiveByCouponCode(request.getCouponCode())) {
            log.warn("Duplicate coupon code during update: {}", request.getCouponCode());
            throw new InvalidCouponException("Coupon code already exists: " + request.getCouponCode());
        }
        log.debug("Applying updates to coupon: {}", id);
        couponMapper.updateEntity(existingCoupon, request);
        Coupon updatedCoupon = couponRepository.save(existingCoupon);
        log.info("Updated coupon: {} with code: {}", id, updatedCoupon.getCouponCode());
        return couponMapper.toResponseDTO(updatedCoupon);
    }

    @Override
    public void deleteCoupon(UUID id) {
        log.debug("Soft deleting coupon with ID: {}", id);
        couponValidator.validateCouponId(id);
        int rowsAffected = couponRepository.softDeleteById(id, LocalDateTime.now());
        if (rowsAffected == 0) {
            log.warn("Coupon not found for deletion: {}", id);
            throw new CouponNotFoundException(String.format(Constants.COUPON_NOT_FOUND, id));
        }
        log.info("Soft deleted coupon: {}", id);
    }
}
