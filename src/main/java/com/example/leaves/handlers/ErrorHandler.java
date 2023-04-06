package com.example.leaves.handlers;

import com.example.leaves.exceptions.BaseCustomException;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(BaseCustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionRestResponse handleCustomException(BaseCustomException exception) {
        if (exception.getType() == null) {
            return new ExceptionRestResponse(400, "default", exception.getMessage());
        } else {
            return new ExceptionRestResponse(400, exception.getType(), exception.getMessage());
        }

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