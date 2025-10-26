package ru.practicum.stats.server.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static String formatFieldError(FieldError fieldError) {
        return String.format("Field '%s': %s", fieldError.getField(), fieldError.getDefaultMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(Exception ex) {
        String message;
        if (ex instanceof MethodArgumentNotValidException validException) {
            message = validException.getBindingResult().getFieldErrors().stream()
                    .map(GlobalExceptionHandler::formatFieldError)
                    .reduce((left, right) -> left + "; " + right)
                    .orElse(validException.getMessage());
        } else {
            message = ex.getMessage();
        }

        log.warn("Validation failed: {}", message);
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request.")
                .message(message)
                .build();
    }

    @ExceptionHandler({InvalidDateRangeException.class, MissingRequiredParameterException.class, DateTimeParseException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(RuntimeException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request.")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Throwable ex) {
        log.error("Unexpected error", ex);
        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .reason("Unexpected error.")
                .message(ex.getMessage())
                .build();
    }
}
