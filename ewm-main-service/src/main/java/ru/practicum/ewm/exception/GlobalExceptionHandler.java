package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.dto.ApiError;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(ValidationException.class)
    public ApiError handleValidationException(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), "Incorrectly made request");
    }

    @ExceptionHandler({ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class})
    public ApiError handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), "Incorrectly made request");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        log.warn("Validation failed: {}", errors);
        return buildError(HttpStatus.BAD_REQUEST, "Validation failed", "Incorrectly made request", errors);
    }

    @ExceptionHandler(NotFoundException.class)
    public ApiError handleNotFound(NotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), "The required object was not found");
    }

    @ExceptionHandler(ConflictException.class)
    public ApiError handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), "Integrity constraint has been violated");
    }

    @ExceptionHandler(Exception.class)
    public ApiError handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "Unexpected error");
    }

    private ApiError buildError(HttpStatus status, String message, String reason) {
        return buildError(status, message, reason, List.of());
    }

    private ApiError buildError(HttpStatus status, String message, String reason, List<String> errors) {
        return ApiError.builder()
                .status(status)
                .message(message)
                .reason(reason)
                .errors(errors)
                .timestamp(LocalDateTime.now(clock))
                .build();
    }
}

