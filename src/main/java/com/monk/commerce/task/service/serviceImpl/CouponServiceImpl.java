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
import com.monk.commerce.task.validator.CouponValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
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
        log.debug("Received request to create coupon of type: {}", request.getType());
        Objects.requireNonNull(request, "Coupon request cannot be null");
        
        log.info("Creating new coupon of type: {}", request.getType());
        try {
            // Check if coupon code already exists
            if (Objects.nonNull(request.getCouponCode()) && 
                couponRepository.existsByCouponCode(request.getCouponCode())) {
                throw new InvalidCouponException("Coupon code already exists: " + request.getCouponCode());
            }
            // Map DTO to entity
            Coupon coupon = couponMapper.toEntity(request);
            Objects.requireNonNull(coupon, "Mapped coupon entity cannot be null");
            // Save coupon
            Coupon savedCoupon = couponRepository.save(coupon);
            log.info("Successfully created coupon with ID: {}", savedCoupon.getId());
            // Map to response DTO
            return couponMapper.toResponseDTO(savedCoupon);
        } catch (InvalidCouponException e) {
            log.error("Failed to create coupon: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating coupon", e);
            throw new InvalidCouponException("Failed to create coupon: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponseDTO> getAllCoupons() {
        log.debug("Received request to fetch all coupons");

        log.info("Fetching all coupons");
        try {
            List<Coupon> coupons = couponRepository.findAll();
            if (Objects.isNull(coupons)) {
                log.error("Repository returned null for findAll()");
                throw new IllegalStateException("Failed to fetch coupons");
            }
            log.info("Found {} coupons", coupons.size());
            return coupons.stream().map(couponMapper::toResponseDTO).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error while fetching all coupons", e);
            throw new RuntimeException("Failed to fetch coupons: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponseDTO getCouponById(Long id) {
        log.debug("Received request to fetch coupon with ID: {}", id);
        couponValidator.validateCouponId(id);
        
        log.info("Fetching coupon with ID: {}", id);
        try {
            Coupon coupon = couponRepository.findById(id)
                    .orElseThrow(() -> new CouponNotFoundException(String.format(Constants.COUPON_NOT_FOUND, id)));
            log.info("Found coupon with ID: {}", id);
            return couponMapper.toResponseDTO(coupon);
        } catch (CouponNotFoundException e) {
            log.error("Coupon not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error while fetching coupon with ID: {}", id, e);
            throw new RuntimeException("Failed to fetch coupon: " + e.getMessage(), e);
        }
    }

    @Override
    public CouponResponseDTO updateCoupon(Long id, CouponRequestDTO request) {
        log.debug("Received request to update coupon with ID: {}", id);
        couponValidator.validateCouponId(id);
        Objects.requireNonNull(request, "Coupon request cannot be null");

        log.info("Updating coupon with ID: {}", id);
        try {
            // Find existing coupon
            Coupon existingCoupon = couponRepository.findById(id)
                    .orElseThrow(() -> new CouponNotFoundException(String.format(Constants.COUPON_NOT_FOUND, id)));

            // Check if updating coupon code and it already exists
            if (Objects.nonNull(request.getCouponCode()) && 
                !request.getCouponCode().equals(existingCoupon.getCouponCode()) &&
                couponRepository.existsByCouponCode(request.getCouponCode())) {
                throw new InvalidCouponException("Coupon code already exists: " + request.getCouponCode());
            }

            // Update entity
            couponMapper.updateEntity(existingCoupon, request);

            // Save updated coupon
            Coupon updatedCoupon = couponRepository.save(existingCoupon);
            log.info("Successfully updated coupon with ID: {}", id);
            return couponMapper.toResponseDTO(updatedCoupon);
        } catch (CouponNotFoundException | InvalidCouponException e) {
            log.error("Failed to update coupon with ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while updating coupon with ID: {}", id, e);
            throw new RuntimeException("Failed to update coupon: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCoupon(Long id) {
        log.debug("Received request to delete coupon with ID: {}", id);
        couponValidator.validateCouponId(id);

        log.info("Deleting coupon with ID: {}", id);
        try {
            // Check if coupon exists
            if (!couponRepository.existsById(id)) {
                throw new CouponNotFoundException(String.format(Constants.COUPON_NOT_FOUND, id));
            }

            // Delete coupon
            couponRepository.deleteById(id);
            log.info("Successfully deleted coupon with ID: {}", id);
        } catch (CouponNotFoundException e) {
            log.error("Coupon not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error while deleting coupon with ID: {}", id, e);
            throw new RuntimeException("Failed to delete coupon: " + e.getMessage(), e);
        }
    }
}
