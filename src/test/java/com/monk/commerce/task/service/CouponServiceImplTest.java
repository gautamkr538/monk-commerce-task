package com.monk.commerce.task.service;

import com.monk.commerce.task.dto.request.CouponRequestDTO;
import com.monk.commerce.task.dto.response.CouponResponseDTO;
import com.monk.commerce.task.entity.CartWiseCoupon;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.enums.CouponType;
import com.monk.commerce.task.exception.CouponNotFoundException;
import com.monk.commerce.task.exception.InvalidCouponException;
import com.monk.commerce.task.mapper.CouponMapper;
import com.monk.commerce.task.repository.CouponRepository;
import com.monk.commerce.task.service.serviceImpl.CouponServiceImpl;
import com.monk.commerce.task.util.CouponUtil;
import com.monk.commerce.task.validator.CouponValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    @Mock
    private CouponValidator couponValidator;

    @InjectMocks
    private CouponServiceImpl couponService;

    private CouponRequestDTO request;
    private Coupon coupon;
    private CouponResponseDTO response;

    @BeforeEach
    void setup() {
        request = new CouponRequestDTO();
        request.setType(CouponType.CART_WISE.getValue());
        request.setDescription("Test coupon");

        coupon = CartWiseCoupon.builder()
                .id(UUID.randomUUID())
                .couponCode("TEST123")
                .type(CouponType.CART_WISE)
                .description("Test coupon")
                .isActive(true)
                .expirationDate(null)
                .usageCount(0L)
                .maxUsageLimit(null)
                .usageLimitPerUser(null)
                .allowStacking(false)
                .priority(0)
                .excludedProducts(new ArrayList<>())
                .usageHistory(new ArrayList<>())
                .thresholdAmount(BigDecimal.valueOf(100))
                .discountPercentage(BigDecimal.valueOf(10))
                .maxDiscountAmount(BigDecimal.valueOf(50))
                .build();

        response = CouponResponseDTO.builder()
                .id(coupon.getId())
                .couponCode("TEST123")
                .type("cart-wise")
                .description("Test")
                .build();
    }

    @Test
    void createCoupon_nullRequest_throwsException() {
        assertThrows(NullPointerException.class, () -> couponService.createCoupon(null));
    }

    @Test
    void createCoupon_codeAlreadyExists_throwsException() {
        request.setCouponCode("TEST123");
        when(couponRepository.existsActiveByCouponCode("TEST123")).thenReturn(true);

        assertThrows(InvalidCouponException.class, () -> couponService.createCoupon(request));
    }

    @Test
    void createCoupon_generatesCode_savesAndReturnsDTO() {
        MockedStatic<CouponUtil> mock = Mockito.mockStatic(CouponUtil.class);
        mock.when(CouponUtil::generateCouponCode).thenReturn("AUTO100");

        when(couponMapper.toEntity(any())).thenReturn(coupon);
        when(couponRepository.save(any())).thenReturn(coupon);
        when(couponMapper.toResponseDTO(any())).thenReturn(response);

        CouponResponseDTO result = couponService.createCoupon(request);

        mock.close();

        assertNotNull(result);
        verify(couponRepository).save(any());
        verify(couponMapper).toResponseDTO(any());
    }

    @Test
    void getAllCoupons_repoReturnsNull_throwsException() {
        when(couponRepository.findAllActiveCoupons()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> couponService.getAllCoupons());
    }

    @Test
    void getAllCoupons_success_returnsMappedDTOs() {
        when(couponRepository.findAllActiveCoupons()).thenReturn(List.of(coupon));
        when(couponMapper.toResponseDTO(any())).thenReturn(response);

        List<CouponResponseDTO> result = couponService.getAllCoupons();

        assertEquals(1, result.size());
    }

    @Test
    void getCouponById_notFound_throwsException() {
        UUID id = UUID.randomUUID();

        when(couponRepository.findActiveById(id)).thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class, () -> couponService.getCouponById(id));
    }

    @Test
    void getCouponById_success_returnsDTO() {
        UUID id = coupon.getId();

        when(couponRepository.findActiveById(id)).thenReturn(Optional.of(coupon));
        when(couponMapper.toResponseDTO(coupon)).thenReturn(response);

        CouponResponseDTO result = couponService.getCouponById(id);

        assertNotNull(result);
        assertEquals("TEST123", result.getCouponCode());
    }

    @Test
    void updateCoupon_nullRequest_throwsException() {
        UUID id = UUID.randomUUID();
        assertThrows(NullPointerException.class, () -> couponService.updateCoupon(id, null));
    }

    @Test
    void updateCoupon_notFound_throwsException() {
        UUID id = UUID.randomUUID();

        when(couponRepository.findActiveById(id)).thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class, () -> couponService.updateCoupon(id, request));
    }

    @Test
    void updateCoupon_duplicateCode_throwsException() {
        UUID id = coupon.getId();
        request.setCouponCode("NEWCODE");

        when(couponRepository.findActiveById(id)).thenReturn(Optional.of(coupon));
        when(couponRepository.existsActiveByCouponCode("NEWCODE")).thenReturn(true);

        assertThrows(InvalidCouponException.class, () -> couponService.updateCoupon(id, request));
    }

    @Test
    void updateCoupon_success_updatesAndReturnsDTO() {
        UUID id = coupon.getId();

        when(couponRepository.findActiveById(id)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any())).thenReturn(coupon);
        when(couponMapper.toResponseDTO(coupon)).thenReturn(response);

        CouponResponseDTO result = couponService.updateCoupon(id, request);

        assertNotNull(result);
        verify(couponMapper).updateEntity(eq(coupon), eq(request));
        verify(couponRepository).save(coupon);
    }

    @Test
    void deleteCoupon_notFound_throwsException() {
        UUID id = UUID.randomUUID();

        when(couponRepository.softDeleteById(eq(id), any())).thenReturn(0);

        assertThrows(CouponNotFoundException.class, () -> couponService.deleteCoupon(id));
    }

    @Test
    void deleteCoupon_success_callsRepo() {
        UUID id = UUID.randomUUID();

        when(couponRepository.softDeleteById(eq(id), any())).thenReturn(1);

        assertDoesNotThrow(() -> couponService.deleteCoupon(id));
        verify(couponRepository).softDeleteById(eq(id), any());
    }
}
