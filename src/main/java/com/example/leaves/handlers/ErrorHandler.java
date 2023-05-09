package com.example.leaves.handlers;

import com.example.leaves.exceptions.BaseCustomException;
import com.example.leaves.exceptions.ValidationException;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionRestResponse handleCustomException(MethodArgumentNotValidException exception) {
        Map<String, Object> response = new HashMap<>();

        if (exception.hasFieldErrors()) {
            List<Map<String, String>> errors = new ArrayList<>();

            for (FieldError error : exception.getFieldErrors()) {
                Map<String, String> transformedError = new HashMap<>();
                transformedError.put("field", error.getField());
                transformedError.put("error", error.getDefaultMessage());

                errors.add(transformedError);
            }
            response.put("errors", errors);
        }
        
            return new ExceptionRestResponse(400, "default",response.toString());


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