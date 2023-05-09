package com.example.leaves.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IllegalContractStartDateException extends RuntimeException {
    public IllegalContractStartDateException() {
    }

    public IllegalContractStartDateException(String message) {
        super(message);
    }
}
