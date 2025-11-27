package com.monk.commerce.task.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Monk Commerce - Coupon Management API")
                        .version("1.0.0")
                        .description("RESTful API for managing and applying discount coupons for an monk-commerce platform"));
    }
}
