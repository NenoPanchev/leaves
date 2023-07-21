package com.example.leaves.handlers;

import com.example.leaves.exceptions.*;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.awt.*;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(value = ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<Object> handleObjectNotFound(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn(bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = ResourceAlreadyExistsException.class)
    protected ResponseEntity<Object> handleExistingResources(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn(bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        // Get the principal (user) from the Authentication object
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = authentication != null ? authentication.getName() : "Unknown User";

        // Get the requested URL (page) from the WebRequest object
        String page = request != null ? request.getDescription(false) : "Unknown Page";

        LOGGER.warn("Access denied for user: {}, requested page: {}, Reason: {}", user, page, bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value
            = {IllegalArgumentException.class, IllegalStateException.class, IllegalContractStartDateException.class})
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn(bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(BaseCustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionRestResponse handleCustomException(BaseCustomException exception) {
        LOGGER.warn(exception.getMessage());
        if (exception.getType() == null) {
            return new ExceptionRestResponse(400, "default", exception.getMessage());
        } else {
            return new ExceptionRestResponse(400, exception.getType(), exception.getMessage());
        }

    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = ExpiredJwtException.class)
    protected ResponseEntity<Object> handleJwtExpired(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn(bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = PasswordsNotMatchingException.class)
    protected ResponseEntity<Object> handlePasswordsNotMatching(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn(bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = SameNewPasswordException.class)
    protected ResponseEntity<Object> handleSameNewPassword(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        LOGGER.warn(bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {IOException.class})
    protected ResponseEntity<Object> handleIORelatedExceptions(
            IOException ex, WebRequest request) {
        String requestedPath = request.getDescription(false); // Get the requested path from the WebRequest
        String bodyOfResponse = "Resource not found at path: " + requestedPath;
        LOGGER.warn(bodyOfResponse);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    // Catch-all exception handler for unhandled RuntimeExceptions
    @ExceptionHandler(value = {RuntimeException.class})
    protected ResponseEntity<Object> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "An unexpected error occurred.";
        LOGGER.error("Unhandled RuntimeException:", ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Value
    public static class ExceptionRestResponse {
        int code;
        String type;
        String message;

        public ExceptionRestResponse(int code, String type, String message) {
            this.code = code;
            this.type = type;
            this.message = message;
        }
    }

}
