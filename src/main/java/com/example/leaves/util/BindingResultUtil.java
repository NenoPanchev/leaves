package com.example.leaves.util;

import com.example.leaves.exceptions.ValidationException;
import org.springframework.validation.BindingResult;

import java.util.stream.Collectors;

public class BindingResultUtil {



    public static String getValidationMessage(BindingResult bindingResult) {
        return bindingResult.getAllErrors()
                .stream()
                .map(ValidationException::getValidationMessage)
                .collect(Collectors.toList()).toString().replace(",", "\n");
    }


}
