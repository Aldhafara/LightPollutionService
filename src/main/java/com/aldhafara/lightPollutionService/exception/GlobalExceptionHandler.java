package com.aldhafara.lightPollutionService.exception;

import com.aldhafara.lightPollutionService.model.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
                .badRequest().body(new ApiErrorResponse(
                        Instant.now().toString(),
                        400,
                        "Bad Request",
                        "Invalid parameter: " + ex.getName()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
                        Instant.now().toString(),
                        500,
                        "Internal Server Error",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity
                .badRequest().body(new ApiErrorResponse(
                        Instant.now().toString(),
                        400,
                        "Bad Request",
                        "Missing required request parameter: " + ex.getParameterName()
                ));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitException ex) {
        return ResponseEntity
                .status(429).body(new ApiErrorResponse(
                        Instant.now().toString(),
                        429,
                        "Too Many Requests",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleValidationException(ConstraintViolationException ex) {
        return ResponseEntity
                .badRequest().body(new ApiErrorResponse(
                        Instant.now().toString(),
                        400,
                        "Invalid request parameters",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler({CoordinatesOutOfRasterBoundsException.class})
    public ResponseEntity<ApiErrorResponse> handleCoordinatesOutOfRasterBoundsException(CoordinatesOutOfRasterBoundsException ex) {
        return ResponseEntity
                .badRequest().body(new ApiErrorResponse(
                        Instant.now().toString(),
                        400,
                        "Coordinates outside raster bounds",
                        ex.getMessage()
                ));
    }
}
