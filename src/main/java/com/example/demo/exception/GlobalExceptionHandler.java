package com.example.demo.exception;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> fields = new TreeMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed.", request, fields);
    }

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ApiError> handleRequestValidation(RequestValidationException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), request,
                exception.getFieldErrors());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception,
            HttpServletRequest request) {
        Map<String, String> fields = new TreeMap<>();
        exception.getConstraintViolations().forEach(violation ->
                fields.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed.", request, fields);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return error(exception.getStatus(), exception.getErrorCode(), exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler({CooldownException.class, InvalidDonationException.class})
    public ResponseEntity<ApiError> handleDonationRule(RuntimeException exception, HttpServletRequest request) {
        String code = exception instanceof CooldownException ? "COOLDOWN_ERROR" : "INVALID_DONATION";
        return error(HttpStatus.BAD_REQUEST, code, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(EntityInUseException.class)
    public ResponseEntity<ApiError> handleEntityInUse(EntityInUseException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "RESOURCE_IN_USE", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(UniqueConflictException.class)
    public ResponseEntity<ApiError> handleConflict(UniqueConflictException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "DUPLICATE_RECORD", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "DATA_CONFLICT", "The request conflicts with an existing record or relationship.", request, Map.of());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception exception, HttpServletRequest request) {
        String message = exception instanceof HttpMessageNotReadableException
                ? "Request body is invalid or malformed."
                : exception instanceof MethodArgumentTypeMismatchException
                        ? "Request parameter has an invalid value."
                        : exception.getMessage();
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request, Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleUnknownRoute(NoResourceFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "API route does not exist.", request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected server error occurred.", request, Map.of());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
            HttpServletRequest request, Map<String, String> fields) {
        ApiError body = new ApiError(LocalDateTime.now(), status.value(), code, message,
                request.getRequestURI(), fields);
        return ResponseEntity.status(status).body(body);
    }
}