package com.example.leaves.exceptions;

public class BaseCustomException extends RuntimeException {
    String type;

    public BaseCustomException(String message, String type) {
        super(message);
        this.type = type;
    }

    public BaseCustomException(String message) {
        super(message);
    }

    public String getType() {
        return type;
    }
}
