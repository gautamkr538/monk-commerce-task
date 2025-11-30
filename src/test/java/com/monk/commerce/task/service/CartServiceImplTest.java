package com.monk.commerce.task.service;

import com.monk.commerce.task.dto.request.CartItemDTO;
import com.monk.commerce.task.dto.request.CartRequestDTO;
import com.monk.commerce.task.dto.response.ApplicableCouponResponseDTO;
import com.monk.commerce.task.dto.response.AppliedCouponResponseDTO;
import com.monk.commerce.task.dto.response.UpdatedCartDTO;
import com.monk.commerce.task.entity.CartWiseCoupon;
import com.monk.commerce.task.entity.Coupon;
import com.monk.commerce.task.enums.CouponType;
import com.monk.commerce.task.exception.CouponNotApplicableException;
import com.monk.commerce.task.exception.CouponNotFoundException;
import com.monk.commerce.task.exception.InvalidCartException;
import com.monk.commerce.task.factory.CouponStrategyFactory;
import com.monk.commerce.task.repository.CouponRepository;
import com.monk.commerce.task.repository.CouponUsageRepository;
import com.monk.commerce.task.service.serviceImpl.CartServiceImpl;
import com.monk.commerce.task.strategy.CouponStrategy;
import com.monk.commerce.task.util.CouponUtil;
import com.monk.commerce.task.validator.CartValidator;
import com.monk.commerce.task.validator.CouponValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Mock
    private CouponStrategyFactory strategyFactory;

    @Mock
    private CartValidator cartValidator;

    @Mock
    private CouponValidator couponValidator;

    @Mock
    private CouponStrategy strategy;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartRequestDTO cart;
    private Map<String, CartRequestDTO> request;

    @BeforeEach
    void setup() {
        cart = new CartRequestDTO();
        List<CartItemDTO> items = new ArrayList<>();
        items.add(new CartItemDTO(1L, 2, BigDecimal.valueOf(100)));
        cart.setItems(items);
        cart.setUserId("user123");

        request = new HashMap<>();
        request.put("cart", cart);
    }

    private Coupon createCoupon(UUID id, CouponType type, BigDecimal threshold) {
        CartWiseCoupon coupon = new CartWiseCoupon();
        coupon.setId(id);
        coupon.setType(type);
        coupon.setIsActive(true);
        coupon.setPriority(1);
        coupon.setThresholdAmount(threshold);
        coupon.setDiscountPercentage(BigDecimal.valueOf(10));
        coupon.setMaxDiscountAmount(BigDecimal.valueOf(50));
        coupon.setExcludedProducts(new ArrayList<>());
        coupon.setUsageHistory(new ArrayList<>());
        return coupon;
    }

    @Test
    void getApplicableCoupons_nullRequest_throwsException() {
        assertThrows(NullPointerException.class, () -> cartService.getApplicableCoupons(null));
    }

    @Test
    void getApplicableCoupons_missingCart_throwsException() {
        Map<String, CartRequestDTO> req = new HashMap<>();
        assertThrows(InvalidCartException.class, () -> cartService.getApplicableCoupons(req));
    }

    @Test
    void getApplicableCoupons_noValidCoupons_returnsEmptyList() {
        when(couponRepository.findAllValidCoupons(any())).thenReturn(Collections.emptyList());

        Map<String, List<ApplicableCouponResponseDTO>> result = cartService.getApplicableCoupons(request);

        assertNotNull(result);
        assertTrue(result.get("applicable_coupons").isEmpty());
    }

    @Test
    void getApplicableCoupons_filtersAndMapsApplicableCoupons() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Coupon c1 = createCoupon(id1, CouponType.CART_WISE, BigDecimal.valueOf(100));
        Coupon c2 = createCoupon(id2, CouponType.CART_WISE, BigDecimal.valueOf(50));

        when(couponRepository.findAllValidCoupons(any())).thenReturn(List.of(c1, c2));
        when(strategyFactory.getStrategy(any())).thenReturn(strategy);
        when(strategy.isApplicable(any(), any())).thenReturn(true);
        when(strategy.calculateDiscount(eq(c1), any())).thenReturn(BigDecimal.valueOf(20));
        when(strategy.calculateDiscount(eq(c2), any())).thenReturn(BigDecimal.valueOf(10));

        MockedStatic<CouponUtil> mock = Mockito.mockStatic(CouponUtil.class);
        mock.when(() -> CouponUtil.getUserUsageRemaining(any(), anyString())).thenReturn(5);
        mock.when(() -> CouponUtil.getGlobalUsageRemaining(any())).thenReturn(100L);

        Map<String, List<ApplicableCouponResponseDTO>> result = cartService.getApplicableCoupons(request);

        mock.close();

        assertEquals(2, result.get("applicable_coupons").size());
        assertEquals(BigDecimal.valueOf(20), result.get("applicable_coupons").get(0).getDiscount());
    }

    @Test
    void applyCoupon_nullRequest_throwsException() {
        assertThrows(NullPointerException.class, () -> cartService.applyCoupon(UUID.randomUUID(), null));
    }

    @Test
    void applyCoupon_missingCart_throwsException() {
        Map<String, CartRequestDTO> req = new HashMap<>();
        assertThrows(InvalidCartException.class, () -> cartService.applyCoupon(UUID.randomUUID(), req));
    }

    @Test
    void applyCoupon_couponNotFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(couponRepository.findActiveById(id)).thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class, () -> cartService.applyCoupon(id, request));
    }

    @Test
    void applyCoupon_userReachedLimit_throwsException() {
        UUID id = UUID.randomUUID();
        Coupon coupon = createCoupon(id, CouponType.CART_WISE, BigDecimal.valueOf(100));

        when(couponRepository.findActiveById(id)).thenReturn(Optional.of(coupon));

        MockedStatic<CouponUtil> mock = Mockito.mockStatic(CouponUtil.class);
        mock.when(() -> CouponUtil.hasUserReachedLimit(eq(coupon), anyString())).thenReturn(true);

        assertThrows(CouponNotApplicableException.class, () -> cartService.applyCoupon(id, request));

        mock.close();
    }

    @Test
    void applyCoupon_strategyNotApplicable_throwsException() {
        UUID id = UUID.randomUUID();
        Coupon coupon = createCoupon(id, CouponType.CART_WISE, BigDecimal.valueOf(100));

        when(couponRepository.findActiveById(id)).thenReturn(Optional.of(coupon));
        when(strategyFactory.getStrategy(CouponType.CART_WISE)).thenReturn(strategy);
        when(strategy.isApplicable(any(), any())).thenReturn(false);

        MockedStatic<CouponUtil> mock = Mockito.mockStatic(CouponUtil.class);
        mock.when(() -> CouponUtil.hasUserReachedLimit(any(), anyString())).thenReturn(false);

        assertThrows(CouponNotApplicableException.class, () -> cartService.applyCoupon(id, request));

        mock.close();
    }

    @Test
    void applyCoupon_success_updatesUsageAndReturnsResponse() {
        UUID id = UUID.randomUUID();
        Coupon coupon = createCoupon(id, CouponType.CART_WISE, BigDecimal.valueOf(50));

        UpdatedCartDTO updatedCart = UpdatedCartDTO.builder()
                .totalDiscount(BigDecimal.valueOf(30))
                .finalPrice(BigDecimal.valueOf(170))
                .build();

        AppliedCouponResponseDTO responseDTO = AppliedCouponResponseDTO.builder()
                .updatedCart(updatedCart)
                .build();

        when(couponRepository.findActiveById(id)).thenReturn(Optional.of(coupon));
        when(strategyFactory.getStrategy(CouponType.CART_WISE)).thenReturn(strategy);
        when(strategy.isApplicable(any(), any())).thenReturn(true);
        when(strategy.applyCoupon(any(), any())).thenReturn(responseDTO);

        MockedStatic<CouponUtil> mock = Mockito.mockStatic(CouponUtil.class);
        mock.when(() -> CouponUtil.hasUserReachedLimit(any(), anyString())).thenReturn(false);

        AppliedCouponResponseDTO result = cartService.applyCoupon(id, request);

        mock.close();

        assertNotNull(result);
        verify(couponUsageRepository, times(1))
                .upsertUsage(any(), eq(id), eq("user123"), any());
        verify(couponRepository, times(1))
                .incrementUsageCount(eq(id), any());
    }
}
