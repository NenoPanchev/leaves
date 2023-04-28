package com.example.leaves.exceptions;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationException extends BaseCustomException {
    private final BindingResult errors;

    public ValidationException(BindingResult errors) {
        super(getValidationMessage(errors).toString().replace(",", "\n"));
        this.errors = errors;
    }

    //demonstrate how to extract a message from the binging result
    private static List<String> getValidationMessage(BindingResult bindingResult) {
        return bindingResult.getAllErrors()
                .stream()
                .map(ValidationException::getValidationMessage)
                .collect(Collectors.toList());
    }

    public static String getValidationMessage(ObjectError error) {
        if (error instanceof FieldError) {
            FieldError fieldError = (FieldError) error;
            String className = fieldError.getObjectName();
            String property = fieldError.getField();
            Object invalidValue = fieldError.getRejectedValue();
            String message = fieldError.getDefaultMessage();
            return String.format("%s.%s %s but it was %s", className, property, message, invalidValue);
        }
        return String.format("%s: %s", error.getObjectName(), error.getDefaultMessage());
    }
    public List<String> getMessages() {
        return getValidationMessage(this.errors);
    }

    @Override
    public String getMessage() {
        return this.getMessages().toString().replace(",", "\n");
    }

}
