.
├── .gitattributes
├── .gitignore
├── .mvn
│   └── wrapper
│       └── maven-wrapper.properties
├── HELP.md
├── README.md
├── mvnw
├── mvnw.cmd
├── pom.xml
├── project_structure.md
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── monk
    │   │           └── commerce
    │   │               └── task
    │   │                   ├── TaskApplication.java
    │   │                   ├── config
    │   │                   │   └── SwaggerConfig.java
    │   │                   ├── controller
    │   │                   │   ├── CartController.java
    │   │                   │   └── CouponController.java
    │   │                   ├── dto
    │   │                   │   ├── request
    │   │                   │   │   ├── BxGyDetailsDTO.java
    │   │                   │   │   ├── BxGyProductDTO.java
    │   │                   │   │   ├── CartItemDTO.java
    │   │                   │   │   ├── CartRequestDTO.java
    │   │                   │   │   ├── CartWiseDetailsDTO.java
    │   │                   │   │   ├── CouponRequestDTO.java
    │   │                   │   │   └── ProductWiseDetailsDTO.java
    │   │                   │   └── response
    │   │                   │       ├── ApiErrorResponseDTO.java
    │   │                   │       ├── ApplicableCouponResponseDTO.java
    │   │                   │       ├── AppliedCouponResponseDTO.java
    │   │                   │       ├── CartItemResponseDTO.java
    │   │                   │       ├── CouponResponseDTO.java
    │   │                   │       └── UpdatedCartDTO.java
    │   │                   ├── entity
    │   │                   │   ├── BuyProduct.java
    │   │                   │   ├── BxGyCoupon.java
    │   │                   │   ├── CartWiseCoupon.java
    │   │                   │   ├── Coupon.java
    │   │                   │   ├── CouponUsage.java
    │   │                   │   ├── ExcludedProduct.java
    │   │                   │   ├── GetProduct.java
    │   │                   │   └── ProductWiseCoupon.java
    │   │                   ├── enums
    │   │                   │   └── CouponType.java
    │   │                   ├── exception
    │   │                   │   ├── CouponNotApplicableException.java
    │   │                   │   ├── CouponNotFoundException.java
    │   │                   │   ├── GlobalExceptionHandler.java
    │   │                   │   ├── InvalidCartException.java
    │   │                   │   └── InvalidCouponException.java
    │   │                   ├── factory
    │   │                   │   └── CouponStrategyFactory.java
    │   │                   ├── mapper
    │   │                   │   └── CouponMapper.java
    │   │                   ├── repository
    │   │                   │   ├── CouponRepository.java
    │   │                   │   └── CouponUsageRepository.java
    │   │                   ├── service
    │   │                   │   ├── CartService.java
    │   │                   │   ├── CouponService.java
    │   │                   │   └── serviceImpl
    │   │                   │       ├── CartServiceImpl.java
    │   │                   │       └── CouponServiceImpl.java
    │   │                   ├── strategy
    │   │                   │   ├── BxGyCouponStrategy.java
    │   │                   │   ├── CartWiseCouponStrategy.java
    │   │                   │   ├── CouponStrategy.java
    │   │                   │   └── ProductWiseCouponStrategy.java
    │   │                   ├── util
    │   │                   │   ├── CartUtil.java
    │   │                   │   ├── Constants.java
    │   │                   │   ├── CouponUtil.java
    │   │                   │   └── DiscountCalculator.java
    │   │                   └── validator
    │   │                       ├── CartValidator.java
    │   │                       └── CouponValidator.java
    │   └── resources
    │       ├── application.properties
    │       ├── postman_collection.json
    │       ├── schema.sql
    │       ├── static
    │       ├── templates
    │       └── test_report.html
    └── test
        └── java
            └── com
                └── monk
                    └── commerce
                        └── task
                            ├── TaskApplicationTests.java
                            ├── factory
                            │   └── CouponStrategyFactoryTest.java
                            ├── mapper
                            │   └── CouponMapperTest.java
                            ├── service
                            │   ├── CartServiceImplTest.java
                            │   └── CouponServiceImplTest.java
                            ├── strategy
                            │   ├── BxGyCouponStrategyTest.java
                            │   ├── CartWiseCouponStrategyTest.java
                            │   └── ProductWiseCouponStrategyTest.java
                            ├── util
                            │   ├── CartUtilTest.java
                            │   ├── CouponUtilTest.java
                            │   └── DiscountCalculatorTest.java
                            └── validator
                                ├── CartValidatorTest.java
                                └── CouponValidatorTest.java

41 directories, 75 files
