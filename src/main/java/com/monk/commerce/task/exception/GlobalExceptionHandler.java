package com.monk.commerce.task.exception;

import com.monk.commerce.task.dto.response.ApiErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle CouponNotFoundException
     */
    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleCouponNotFoundException(CouponNotFoundException ex) {
        log.error("Coupon not found: {}", ex.getMessage());
        ApiErrorResponseDTO error = ApiErrorResponseDTO.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle InvalidCouponException
     */
    @ExceptionHandler(InvalidCouponException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidCouponException(InvalidCouponException ex) {
        log.error("Invalid coupon: {}", ex.getMessage());
        ApiErrorResponseDTO error = ApiErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle CouponNotApplicableException
     */
    @ExceptionHandler(CouponNotApplicableException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleCouponNotApplicableException(CouponNotApplicableException ex) {
        log.warn("Coupon not applicable: {}", ex.getMessage());
        ApiErrorResponseDTO error = ApiErrorResponseDTO.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Unprocessable Entity")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handle InvalidCartException
     */
    @ExceptionHandler(InvalidCartException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleInvalidCartException(InvalidCartException ex) {
        log.error("Invalid cart: {}", ex.getMessage());
        ApiErrorResponseDTO error = ApiErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation errors (MethodArgumentNotValidException)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation failed: {}", ex.getMessage());
        ApiErrorResponseDTO error = ApiErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ConstraintViolationException
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());
        ApiErrorResponseDTO error = ApiErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Constraint violation")
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        ApiErrorResponseDTO error = ApiErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleNullPointerException(NullPointerException ex) {
        log.error("Null pointer exception: {}", ex.getMessage(), ex);
        ApiErrorResponseDTO error = ApiErrorResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("A null value was encountered. Please check your input data.")
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions (Generic handler)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleGlobalException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        ApiErrorResponseDTO error = ApiErrorResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred. Please try again later.")
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
