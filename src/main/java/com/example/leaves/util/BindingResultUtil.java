package com.example.leaves.util;

import com.example.leaves.exceptions.ValidationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.stream.Collectors;

public class BindingResultUtil {



    public static String getValidationMessage(BindingResult bindingResult) {
        return bindingResult.getAllErrors()
                .stream()
                .map(ValidationException::getValidationMessage)
                .collect(Collectors.toList()).toString().replace(",", "\n");
    }


}
