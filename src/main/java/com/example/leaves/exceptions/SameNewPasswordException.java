package com.example.leaves.exceptions;

public class SameNewPasswordException extends BaseCustomException{
    public SameNewPasswordException(String message, String type) {
        super(message, type);
    }

    public SameNewPasswordException(String message) {
        super(message);
    }
}
