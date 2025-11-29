package com.monk.commerce.task.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.commerce.task.dto.request.BxGyDetailsDTO;
import com.monk.commerce.task.dto.request.BxGyProductDTO;
import com.monk.commerce.task.dto.request.CartWiseDetailsDTO;
import com.monk.commerce.task.dto.request.CouponRequestDTO;
import com.monk.commerce.task.dto.request.ProductWiseDetailsDTO;
import com.monk.commerce.task.dto.response.CouponResponseDTO;
import com.monk.commerce.task.entity.*;
import com.monk.commerce.task.enums.CouponType;
import com.monk.commerce.task.exception.InvalidCouponException;
import com.monk.commerce.task.util.CouponUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CouponMapper {

    private final ObjectMapper objectMapper;

    public CouponMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Coupon toEntity(CouponRequestDTO dto) {
        Objects.requireNonNull(dto, "CouponRequestDTO cannot be null");
        Objects.requireNonNull(dto.getType(), "Coupon type cannot be null");

        CouponType couponType = CouponType.fromValue(dto.getType());
        Coupon coupon = switch (couponType) {
            case CART_WISE -> toCartWiseCoupon(dto);
            case PRODUCT_WISE -> toProductWiseCoupon(dto);
            case BXGY -> toBxGyCoupon(dto);
            default -> throw new InvalidCouponException("Unsupported coupon type: " + dto.getType());
        };

        if (dto.getExcludedProducts() != null && !dto.getExcludedProducts().isEmpty()) {
            List<ExcludedProduct> excludedProducts = dto.getExcludedProducts().stream()
                    .map(productId -> ExcludedProduct.builder()
                            .coupon(coupon)
                            .productId(productId)
                            .build())
                    .collect(Collectors.toList());
            coupon.setExcludedProducts(excludedProducts);
        }
        return coupon;
    }

    private CartWiseCoupon toCartWiseCoupon(CouponRequestDTO dto) {
        CartWiseDetailsDTO details = objectMapper.convertValue(dto.getDetails(), CartWiseDetailsDTO.class);

        Objects.requireNonNull(details, "Cart-wise details cannot be null");
        Objects.requireNonNull(details.getThreshold(), "Threshold cannot be null");
        Objects.requireNonNull(details.getDiscountPercentage(), "Discount percentage cannot be null");

        return CartWiseCoupon.builder()
                .couponCode(dto.getCouponCode() != null ? dto.getCouponCode() : CouponUtil.generateCouponCode())
                .type(CouponType.CART_WISE)
                .description(dto.getDescription())
                .isActive(Optional.ofNullable(dto.getIsActive()).orElse(true))
                .expirationDate(dto.getExpirationDate())
                .thresholdAmount(details.getThreshold())
                .discountPercentage(details.getDiscountPercentage())
                .maxDiscountAmount(details.getMaxDiscount())
                .build();
    }

    private ProductWiseCoupon toProductWiseCoupon(CouponRequestDTO dto) {
        ProductWiseDetailsDTO details = objectMapper.convertValue(dto.getDetails(), ProductWiseDetailsDTO.class);

        Objects.requireNonNull(details, "Product-wise details cannot be null");
        Objects.requireNonNull(details.getProductId(), "Product ID cannot be null");
        Objects.requireNonNull(details.getDiscount(), "Discount cannot be null");

        return ProductWiseCoupon.builder()
                .couponCode(dto.getCouponCode() != null ? dto.getCouponCode() : CouponUtil.generateCouponCode())
                .type(CouponType.PRODUCT_WISE)
                .description(dto.getDescription())
                .isActive(Optional.ofNullable(dto.getIsActive()).orElse(true))
                .expirationDate(dto.getExpirationDate())
                .productId(details.getProductId())
                .discountPercentage(details.getDiscount())
                .build();
    }

    private BxGyCoupon toBxGyCoupon(CouponRequestDTO dto) {
        BxGyDetailsDTO details = objectMapper.convertValue(dto.getDetails(), BxGyDetailsDTO.class);

        Objects.requireNonNull(details, "BxGy details cannot be null");
        Objects.requireNonNull(details.getBuyProducts(), "Buy products cannot be null");
        Objects.requireNonNull(details.getGetProducts(), "Get products cannot be null");
        Objects.requireNonNull(details.getRepetitionLimit(), "Repetition limit cannot be null");

        BxGyCoupon bxGyCoupon = BxGyCoupon.builder()
                .couponCode(dto.getCouponCode() != null ? dto.getCouponCode() : CouponUtil.generateCouponCode())
                .type(CouponType.BXGY)
                .description(dto.getDescription())
                .isActive(Optional.ofNullable(dto.getIsActive()).orElse(true))
                .expirationDate(dto.getExpirationDate())
                .repetitionLimit(details.getRepetitionLimit())
                .buyProducts(new ArrayList<>())
                .getProducts(new ArrayList<>())
                .build();

        details.getBuyProducts().forEach(buyProductDTO -> {
            BuyProduct buyProduct = BuyProduct.builder()
                    .productId(buyProductDTO.getProductId())
                    .quantity(buyProductDTO.getQuantity())
                    .tierLevel(Optional.ofNullable(buyProductDTO.getTierLevel()).orElse(1))
                    .bxgyCoupon(bxGyCoupon)
                    .build();
            bxGyCoupon.getBuyProducts().add(buyProduct);
        });

        details.getGetProducts().forEach(getProductDTO -> {
            GetProduct getProduct = GetProduct.builder()
                    .productId(getProductDTO.getProductId())
                    .quantity(getProductDTO.getQuantity())
                    .tierLevel(Optional.ofNullable(getProductDTO.getTierLevel()).orElse(1))
                    .bxgyCoupon(bxGyCoupon)
                    .build();
            bxGyCoupon.getGetProducts().add(getProduct);
        });

        boolean isTiered = bxGyCoupon.getBuyProducts().stream().anyMatch(bp -> bp.getTierLevel() > 1) ||
                bxGyCoupon.getGetProducts().stream().anyMatch(gp -> gp.getTierLevel() > 1);
        bxGyCoupon.setIsTiered(isTiered);
        return bxGyCoupon;
    }

    public CouponResponseDTO toResponseDTO(Coupon coupon) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");

        List<Long> excludedProductIds = null;
        if (coupon.getExcludedProducts() != null && !coupon.getExcludedProducts().isEmpty()) {
            excludedProductIds = coupon.getExcludedProducts().stream()
                    .map(ExcludedProduct::getProductId)
                    .collect(Collectors.toList());
        }

        return CouponResponseDTO.builder()
                .id(coupon.getId())
                .couponCode(coupon.getCouponCode())
                .type(coupon.getType().getValue())
                .description(coupon.getDescription())
                .isActive(coupon.getIsActive())
                .expirationDate(coupon.getExpirationDate())
                .usageCount(coupon.getUsageCount())
                .maxUsageLimit(coupon.getMaxUsageLimit())
                .usageLimitPerUser(coupon.getUsageLimitPerUser())
                .allowStacking(coupon.getAllowStacking())
                .priority(coupon.getPriority())
                .details(mapCouponDetails(coupon))
                .excludedProducts(excludedProductIds) // ADD THIS
                .createdAt(coupon.getCreatedAt())
                .updatedAt(coupon.getUpdatedAt())
                .build();
    }

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
                            .tierLevel(bp.getTierLevel())
                            .build())
                    .collect(Collectors.toList());

            List<BxGyProductDTO> getProducts = bxGyCoupon.getGetProducts().stream()
                    .map(gp -> BxGyProductDTO.builder()
                            .productId(gp.getProductId())
                            .quantity(gp.getQuantity())
                            .tierLevel(gp.getTierLevel())
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

    public void updateEntity(Coupon existingCoupon, CouponRequestDTO dto) {
        Objects.requireNonNull(existingCoupon, "Existing coupon cannot be null");
        Objects.requireNonNull(dto, "CouponRequestDTO cannot be null");

        if (dto.getCouponCode() != null) {
            existingCoupon.setCouponCode(dto.getCouponCode());
        }
        if (dto.getDescription() != null) {
            existingCoupon.setDescription(dto.getDescription());
        }
        if (dto.getIsActive() != null) {
            existingCoupon.setIsActive(dto.getIsActive());
        }
        if (dto.getExpirationDate() != null) {
            existingCoupon.setExpirationDate(dto.getExpirationDate());
        }

        if (dto.getExcludedProducts() != null) {
            existingCoupon.getExcludedProducts().clear();
            if (!dto.getExcludedProducts().isEmpty()) {
                List<ExcludedProduct> excludedProducts = dto.getExcludedProducts().stream()
                        .map(productId -> ExcludedProduct.builder()
                                .coupon(existingCoupon)
                                .productId(productId)
                                .build())
                        .toList();
                existingCoupon.getExcludedProducts().addAll(excludedProducts);
            }
        }

        if (dto.getDetails() != null) {
            if (existingCoupon instanceof CartWiseCoupon) {
                updateCartWiseCoupon((CartWiseCoupon) existingCoupon, dto);
            } else if (existingCoupon instanceof ProductWiseCoupon) {
                updateProductWiseCoupon((ProductWiseCoupon) existingCoupon, dto);
            } else if (existingCoupon instanceof BxGyCoupon) {
                updateBxGyCoupon((BxGyCoupon) existingCoupon, dto);
            }
        }
    }

    private void updateCartWiseCoupon(CartWiseCoupon coupon, CouponRequestDTO dto) {
        CartWiseDetailsDTO details = objectMapper.convertValue(dto.getDetails(), CartWiseDetailsDTO.class);

        if (details.getThreshold() != null) {
            coupon.setThresholdAmount(details.getThreshold());
        }
        if (details.getDiscountPercentage() != null) {
            coupon.setDiscountPercentage(details.getDiscountPercentage());
        }
        if (details.getMaxDiscount() != null) {
            coupon.setMaxDiscountAmount(details.getMaxDiscount());
        }
    }

    private void updateProductWiseCoupon(ProductWiseCoupon coupon, CouponRequestDTO dto) {
        ProductWiseDetailsDTO details = objectMapper.convertValue(dto.getDetails(), ProductWiseDetailsDTO.class);

        if (details.getProductId() != null) {
            coupon.setProductId(details.getProductId());
        }
        if (details.getDiscount() != null) {
            coupon.setDiscountPercentage(details.getDiscount());
        }
    }

    private void updateBxGyCoupon(BxGyCoupon coupon, CouponRequestDTO dto) {
        BxGyDetailsDTO details = objectMapper.convertValue(dto.getDetails(), BxGyDetailsDTO.class);

        if (details.getRepetitionLimit() != null) {
            coupon.setRepetitionLimit(details.getRepetitionLimit());
        }

        if (details.getBuyProducts() != null && !details.getBuyProducts().isEmpty()) {
            coupon.getBuyProducts().clear();
            details.getBuyProducts().forEach(buyProductDTO -> {
                BuyProduct buyProduct = BuyProduct.builder()
                        .productId(buyProductDTO.getProductId())
                        .quantity(buyProductDTO.getQuantity())
                        .tierLevel(Optional.ofNullable(buyProductDTO.getTierLevel()).orElse(1))
                        .bxgyCoupon(coupon)
                        .build();
                coupon.getBuyProducts().add(buyProduct);
            });
        }

        if (details.getGetProducts() != null && !details.getGetProducts().isEmpty()) {
            coupon.getGetProducts().clear();
            details.getGetProducts().forEach(getProductDTO -> {
                GetProduct getProduct = GetProduct.builder()
                        .productId(getProductDTO.getProductId())
                        .quantity(getProductDTO.getQuantity())
                        .tierLevel(Optional.ofNullable(getProductDTO.getTierLevel()).orElse(1))
                        .bxgyCoupon(coupon)
                        .build();
                coupon.getGetProducts().add(getProduct);
            });
        }

        boolean isTiered = coupon.getBuyProducts().stream().anyMatch(bp -> bp.getTierLevel() > 1) ||
                coupon.getGetProducts().stream().anyMatch(gp -> gp.getTierLevel() > 1);
        coupon.setIsTiered(isTiered);
    }
}
