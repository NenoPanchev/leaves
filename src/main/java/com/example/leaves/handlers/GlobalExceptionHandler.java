package com.example.leaves.handlers;

import com.example.leaves.exceptions.BaseCustomException;
import com.example.leaves.exceptions.IllegalContractStartDateException;
import com.example.leaves.exceptions.ObjectNotFoundException;
import com.example.leaves.exceptions.PasswordsNotMatchingException;
import com.example.leaves.exceptions.ResourceAlreadyExistsException;
import com.example.leaves.exceptions.SameNewPasswordException;
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

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    public static final String WARNING_LOG_TEMPLATE = "{} thrown from method: {} of a {} class. Message: {}";

    @ExceptionHandler(value = ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected ResponseEntity<Object> handleObjectNotFound(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        logException(ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = ResourceAlreadyExistsException.class)
    protected ResponseEntity<Object> handleExistingResources(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        logException(ex);
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
        logException(ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value
            = {IllegalArgumentException.class, IllegalStateException.class, IllegalContractStartDateException.class})
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        logException(ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(BaseCustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionRestResponse handleCustomException(BaseCustomException exception) {
        logException(exception);
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
        logException(ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = PasswordsNotMatchingException.class)
    protected ResponseEntity<Object> handlePasswordsNotMatching(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        logException(ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = SameNewPasswordException.class)
    protected ResponseEntity<Object> handleSameNewPassword(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage();
        logException(ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {IOException.class})
    protected ResponseEntity<Object> handleIORelatedExceptions(
            IOException ex, WebRequest request) {
        String requestedPath = request.getDescription(false); // Get the requested path from the WebRequest
        String bodyOfResponse = "Resource not found at path: " + requestedPath;
        logException(ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    // Catch-all exception handler for unhandled RuntimeExceptions
    @ExceptionHandler(value = {RuntimeException.class})
    protected ResponseEntity<Object> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "An unexpected error occurred.";
        logException(ex);
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

    private void logException(Exception ex) {
        LOGGER.warn(WARNING_LOG_TEMPLATE, ex.getClass().getSimpleName(), getMethodName(ex), ex.getStackTrace()[0].getClassName(), ex.getMessage());
    }

    private String getMethodName(Exception ex) {
        StackTraceElement[] stackTrace = ex.getStackTrace();
        if (stackTrace.length > 0) {
            return stackTrace[0].getMethodName();
        }
        return "Unknown Method";
    }

}
