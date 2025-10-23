package ru.practicum.stats.server.exception;

public class MissingRequiredParameterException extends RuntimeException {

    public MissingRequiredParameterException(String parameterName) {
        super("Required request parameter '%s' is not present".formatted(parameterName));
    }
}
