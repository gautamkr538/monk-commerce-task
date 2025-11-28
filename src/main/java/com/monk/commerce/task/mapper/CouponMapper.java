package com.monk.commerce.task.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.commerce.task.dto.request.*;
import com.monk.commerce.task.dto.response.CouponResponseDTO;
import com.monk.commerce.task.entity.*;
import com.monk.commerce.task.enums.CouponType;
import com.monk.commerce.task.exception.InvalidCouponException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CouponMapper {

    private final ObjectMapper objectMapper;

    public CouponMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Map CouponRequestDTO to Coupon entity based on type
     */
    public Coupon toEntity(CouponRequestDTO dto) {
        Objects.requireNonNull(dto, "CouponRequestDTO cannot be null");
        Objects.requireNonNull(dto.getType(), "Coupon type cannot be null");

        CouponType couponType = CouponType.fromValue(dto.getType());

        switch (couponType) {
            case CART_WISE:
                return toCartWiseCoupon(dto);
            case PRODUCT_WISE:
                return toProductWiseCoupon(dto);
            case BXGY:
                return toBxGyCoupon(dto);
            default:
                throw new InvalidCouponException("Unsupported coupon type: " + dto.getType());
        }
    }

    /**
     * Map CartWiseDetailsDTO to CartWiseCoupon entity
     */
    private CartWiseCoupon toCartWiseCoupon(CouponRequestDTO dto) {
        CartWiseDetailsDTO details = objectMapper.convertValue(
            dto.getDetails(), 
            CartWiseDetailsDTO.class
        );

        Objects.requireNonNull(details, "Cart-wise details cannot be null");
        Objects.requireNonNull(details.getThreshold(), "Threshold cannot be null");
        Objects.requireNonNull(details.getDiscountPercentage(), "Discount percentage cannot be null");

        return CartWiseCoupon.builder()
                .couponCode(generateCouponCodeIfNull(dto.getCouponCode()))
                .type(CouponType.CART_WISE)
                .description(dto.getDescription())
                .isActive(Optional.ofNullable(dto.getIsActive()).orElse(true))
                .expirationDate(dto.getExpirationDate())
                .thresholdAmount(details.getThreshold())
                .discountPercentage(details.getDiscountPercentage())
                .maxDiscountAmount(details.getMaxDiscount())
                .build();
    }

    /**
     * Map ProductWiseDetailsDTO to ProductWiseCoupon entity
     */
    private ProductWiseCoupon toProductWiseCoupon(CouponRequestDTO dto) {
        ProductWiseDetailsDTO details = objectMapper.convertValue(
            dto.getDetails(), 
            ProductWiseDetailsDTO.class
        );

        Objects.requireNonNull(details, "Product-wise details cannot be null");
        Objects.requireNonNull(details.getProductId(), "Product ID cannot be null");
        Objects.requireNonNull(details.getDiscount(), "Discount cannot be null");

        return ProductWiseCoupon.builder()
                .couponCode(generateCouponCodeIfNull(dto.getCouponCode()))
                .type(CouponType.PRODUCT_WISE)
                .description(dto.getDescription())
                .isActive(Optional.ofNullable(dto.getIsActive()).orElse(true))
                .expirationDate(dto.getExpirationDate())
                .productId(details.getProductId())
                .discountPercentage(details.getDiscount())
                .build();
    }

    /**
     * Map BxGyDetailsDTO to BxGyCoupon entity
     */
    private BxGyCoupon toBxGyCoupon(CouponRequestDTO dto) {
        BxGyDetailsDTO details = objectMapper.convertValue(
            dto.getDetails(), 
            BxGyDetailsDTO.class
        );

        Objects.requireNonNull(details, "BxGy details cannot be null");
        Objects.requireNonNull(details.getBuyProducts(), "Buy products cannot be null");
        Objects.requireNonNull(details.getGetProducts(), "Get products cannot be null");
        Objects.requireNonNull(details.getRepetitionLimit(), "Repetition limit cannot be null");

        BxGyCoupon bxGyCoupon = BxGyCoupon.builder()
                .couponCode(generateCouponCodeIfNull(dto.getCouponCode()))
                .type(CouponType.BXGY)
                .description(dto.getDescription())
                .isActive(Optional.ofNullable(dto.getIsActive()).orElse(true))
                .expirationDate(dto.getExpirationDate())
                .repetitionLimit(details.getRepetitionLimit())
                .buyProducts(new ArrayList<>())
                .getProducts(new ArrayList<>())
                .build();

        // Map buy products
        details.getBuyProducts().forEach(buyProductDTO -> {
            BuyProduct buyProduct = BuyProduct.builder()
                    .productId(buyProductDTO.getProductId())
                    .quantity(buyProductDTO.getQuantity())
                    .bxgyCoupon(bxGyCoupon)
                    .build();
            bxGyCoupon.getBuyProducts().add(buyProduct);
        });

        // Map get products
        details.getGetProducts().forEach(getProductDTO -> {
            GetProduct getProduct = GetProduct.builder()
                    .productId(getProductDTO.getProductId())
                    .quantity(getProductDTO.getQuantity())
                    .bxgyCoupon(bxGyCoupon)
                    .build();
            bxGyCoupon.getGetProducts().add(getProduct);
        });

        return bxGyCoupon;
    }

    /**
     * Map Coupon entity to CouponResponseDTO
     */
    public CouponResponseDTO toResponseDTO(Coupon coupon) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");

        return CouponResponseDTO.builder()
                .id(coupon.getId())
                .couponCode(coupon.getCouponCode())
                .type(coupon.getType().getValue())
                .description(coupon.getDescription())
                .isActive(coupon.getIsActive())
                .expirationDate(coupon.getExpirationDate())
                .details(mapCouponDetails(coupon))
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

    /**
     * Map specific coupon details based on type
     */
    private Object mapCouponDetails(Coupon coupon) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");

        if (coupon instanceof CartWiseCoupon) {
            CartWiseCoupon cartWiseCoupon = (CartWiseCoupon) coupon;
            return CartWiseDetailsDTO.builder()
                    .threshold(cartWiseCoupon.getThresholdAmount())
                    .discountPercentage(cartWiseCoupon.getDiscountPercentage())
                    .maxDiscount(cartWiseCoupon.getMaxDiscountAmount())
                    .build();
        } else if (coupon instanceof ProductWiseCoupon) {
            ProductWiseCoupon productWiseCoupon = (ProductWiseCoupon) coupon;
            return ProductWiseDetailsDTO.builder()
                    .productId(productWiseCoupon.getProductId())
                    .discount(productWiseCoupon.getDiscountPercentage())
                    .build();
        } else if (coupon instanceof BxGyCoupon) {
            BxGyCoupon bxGyCoupon = (BxGyCoupon) coupon;
            
            List<BxGyProductDTO> buyProducts = bxGyCoupon.getBuyProducts().stream()
                    .map(bp -> BxGyProductDTO.builder()
                            .productId(bp.getProductId())
                            .quantity(bp.getQuantity())
                            .build())
                    .collect(Collectors.toList());

            List<BxGyProductDTO> getProducts = bxGyCoupon.getGetProducts().stream()
                    .map(gp -> BxGyProductDTO.builder()
                            .productId(gp.getProductId())
                            .quantity(gp.getQuantity())
                            .build())
                    .collect(Collectors.toList());

            return BxGyDetailsDTO.builder()
                    .buyProducts(buyProducts)
                    .getProducts(getProducts)
                    .repetitionLimit(bxGyCoupon.getRepetitionLimit())
                    .build();
        }

        return null;
    }

    /**
     * Update existing coupon entity from DTO
     */
    public void updateEntity(Coupon existingCoupon, CouponRequestDTO dto) {
        Objects.requireNonNull(existingCoupon, "Existing coupon cannot be null");
        Objects.requireNonNull(dto, "CouponRequestDTO cannot be null");

        // Update common fields
        if (Objects.nonNull(dto.getCouponCode())) {
            existingCoupon.setCouponCode(dto.getCouponCode());
        }
        if (Objects.nonNull(dto.getDescription())) {
            existingCoupon.setDescription(dto.getDescription());
        }
        if (Objects.nonNull(dto.getIsActive())) {
            existingCoupon.setIsActive(dto.getIsActive());
        }
        if (Objects.nonNull(dto.getExpirationDate())) {
            existingCoupon.setExpirationDate(dto.getExpirationDate());
        }

        // Update type-specific fields
        if (existingCoupon instanceof CartWiseCoupon && Objects.nonNull(dto.getDetails())) {
            updateCartWiseCoupon((CartWiseCoupon) existingCoupon, dto);
        } else if (existingCoupon instanceof ProductWiseCoupon && Objects.nonNull(dto.getDetails())) {
            updateProductWiseCoupon((ProductWiseCoupon) existingCoupon, dto);
        } else if (existingCoupon instanceof BxGyCoupon && Objects.nonNull(dto.getDetails())) {
            updateBxGyCoupon((BxGyCoupon) existingCoupon, dto);
        }
    }

    /**
     * Update CartWiseCoupon specific fields
     */
    private void updateCartWiseCoupon(CartWiseCoupon coupon, CouponRequestDTO dto) {
        CartWiseDetailsDTO details = objectMapper.convertValue(
            dto.getDetails(), 
            CartWiseDetailsDTO.class
        );

        if (Objects.nonNull(details.getThreshold())) {
            coupon.setThresholdAmount(details.getThreshold());
        }
        if (Objects.nonNull(details.getDiscountPercentage())) {
            coupon.setDiscountPercentage(details.getDiscountPercentage());
        }
        if (Objects.nonNull(details.getMaxDiscount())) {
            coupon.setMaxDiscountAmount(details.getMaxDiscount());
        }
    }

    /**
     * Update ProductWiseCoupon specific fields
     */
    private void updateProductWiseCoupon(ProductWiseCoupon coupon, CouponRequestDTO dto) {
        ProductWiseDetailsDTO details = objectMapper.convertValue(
            dto.getDetails(), 
            ProductWiseDetailsDTO.class
        );

        if (Objects.nonNull(details.getProductId())) {
            coupon.setProductId(details.getProductId());
        }
        if (Objects.nonNull(details.getDiscount())) {
            coupon.setDiscountPercentage(details.getDiscount());
        }
    }

    /**
     * Update BxGyCoupon specific fields
     */
    private void updateBxGyCoupon(BxGyCoupon coupon, CouponRequestDTO dto) {
        BxGyDetailsDTO details = objectMapper.convertValue(
            dto.getDetails(), 
            BxGyDetailsDTO.class
        );

        if (Objects.nonNull(details.getRepetitionLimit())) {
            coupon.setRepetitionLimit(details.getRepetitionLimit());
        }

        // Clear and update buy products
        if (Objects.nonNull(details.getBuyProducts()) && !details.getBuyProducts().isEmpty()) {
            coupon.getBuyProducts().clear();
            details.getBuyProducts().forEach(buyProductDTO -> {
                BuyProduct buyProduct = BuyProduct.builder()
                        .productId(buyProductDTO.getProductId())
                        .quantity(buyProductDTO.getQuantity())
                        .bxgyCoupon(coupon)
                        .build();
                coupon.getBuyProducts().add(buyProduct);
            });
        }

        // Clear and update get products
        if (Objects.nonNull(details.getGetProducts()) && !details.getGetProducts().isEmpty()) {
            coupon.getGetProducts().clear();
            details.getGetProducts().forEach(getProductDTO -> {
                GetProduct getProduct = GetProduct.builder()
                        .productId(getProductDTO.getProductId())
                        .quantity(getProductDTO.getQuantity())
                        .bxgyCoupon(coupon)
                        .build();
                coupon.getGetProducts().add(getProduct);
            });
        }
    }

    /**
     * Generate coupon code if null
     */
    private String generateCouponCodeIfNull(String couponCode) {
        if (Objects.isNull(couponCode) || couponCode.trim().isEmpty()) {
            return "COUPON-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        return couponCode;
    }
}
