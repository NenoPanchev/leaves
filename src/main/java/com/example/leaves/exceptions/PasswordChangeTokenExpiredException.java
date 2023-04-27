package com.example.leaves.exceptions;

public class PasswordChangeTokenExpiredException extends BaseCustomException {

    public PasswordChangeTokenExpiredException(String message) {
        super(message);
    }
}
