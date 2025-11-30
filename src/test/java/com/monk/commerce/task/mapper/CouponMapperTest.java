    package com.monk.commerce.task.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monk.commerce.task.dto.request.*;
import com.monk.commerce.task.dto.response.CouponResponseDTO;
import com.monk.commerce.task.entity.BxGyCoupon;
import com.monk.commerce.task.entity.CartWiseCoupon;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.entity.ExcludedProduct;
import com.monk.commerce.task.entity.ProductWiseCoupon;
import com.monk.commerce.task.enums.CouponType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CouponMapperTest {

    private CouponMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new CouponMapper(new ObjectMapper());
    }

    @Test
    void toEntity_cartWise_correctMapping() {
        CouponRequestDTO dto = new CouponRequestDTO();
        dto.setType("cart-wise");
        dto.setCouponCode("C1");

        CartWiseDetailsDTO details = new CartWiseDetailsDTO();
        details.setThreshold(BigDecimal.valueOf(100));
        details.setDiscountPercentage(BigDecimal.valueOf(10));
        details.setMaxDiscount(BigDecimal.valueOf(50));
        dto.setDetails(details);

        Coupon coupon = mapper.toEntity(dto);

        assertTrue(coupon instanceof CartWiseCoupon);
        CartWiseCoupon c = (CartWiseCoupon) coupon;
        assertEquals(BigDecimal.valueOf(100), c.getThresholdAmount());
    }

    @Test
    void toEntity_productWise_correctMapping() {
        CouponRequestDTO dto = new CouponRequestDTO();
        dto.setType("product-wise");

        ProductWiseDetailsDTO details = new ProductWiseDetailsDTO();
        details.setProductId(5L);
        details.setDiscount(BigDecimal.valueOf(20));
        dto.setDetails(details);

        Coupon coupon = mapper.toEntity(dto);

        assertTrue(coupon instanceof ProductWiseCoupon);
        ProductWiseCoupon c = (ProductWiseCoupon) coupon;
        assertEquals(5L, c.getProductId());
    }

    @Test
    void toEntity_bxgy_correctMapping() {
        CouponRequestDTO dto = new CouponRequestDTO();
        dto.setType("bxgy");

        BxGyDetailsDTO details = new BxGyDetailsDTO();
        details.setRepetitionLimit(2);

        details.setBuyProducts(List.of(
                new BxGyProductDTO(1L, 2, 1)
        ));
        details.setGetProducts(List.of(
                new BxGyProductDTO(2L, 1, 1)
        ));

        dto.setDetails(details);

        Coupon coupon = mapper.toEntity(dto);

        assertTrue(coupon instanceof BxGyCoupon);
        BxGyCoupon c = (BxGyCoupon) coupon;
        assertEquals(1, c.getBuyProducts().size());
        assertEquals(2, c.getGetProducts().size() - 1 + 1);
    }

    @Test
    void toResponseDTO_correctMapping() {
        CartWiseCoupon coupon = CartWiseCoupon.builder()
                .id(UUID.randomUUID())
                .couponCode("C10")
                .type(CouponType.CART_WISE)
                .thresholdAmount(BigDecimal.valueOf(100))
                .discountPercentage(BigDecimal.valueOf(10))
                .maxDiscountAmount(BigDecimal.valueOf(30))
                .isActive(true)
                .priority(1)
                .description("desc")
                .usageCount(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ExcludedProduct ep = ExcludedProduct.builder()
                .productId(5L)
                .coupon(coupon)
                .build();
        coupon.setExcludedProducts(List.of(ep));

        CouponResponseDTO dto = mapper.toResponseDTO(coupon);

        assertEquals("C10", dto.getCouponCode());
        assertEquals(1, dto.getExcludedProducts().size());
    }

    @Test
    void updateEntity_updatesFieldsCorrectly() {
        ProductWiseCoupon coupon = new ProductWiseCoupon();
        coupon.setProductId(10L);
        coupon.setDiscountPercentage(BigDecimal.valueOf(5));

        CouponRequestDTO dto = new CouponRequestDTO();
        ProductWiseDetailsDTO d = new ProductWiseDetailsDTO();
        d.setProductId(20L);
        d.setDiscount(BigDecimal.valueOf(15));
        dto.setDetails(d);

        dto.setCouponCode("NEW");
        dto.setDescription("New Desc");
        dto.setIsActive(false);
        dto.setMaxUsageLimit(50L);

        mapper.updateEntity(coupon, dto);

        assertEquals(20L, coupon.getProductId());
        assertEquals(BigDecimal.valueOf(15), coupon.getDiscountPercentage());
        assertEquals("NEW", coupon.getCouponCode());
        assertEquals("New Desc", coupon.getDescription());
        assertFalse(coupon.getIsActive());
    }
}
