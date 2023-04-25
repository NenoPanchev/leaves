package com.example.leaves.exceptions;

public class PasswordsNotMatchingException extends BaseCustomException{
    public PasswordsNotMatchingException(String message, String type) {
        super(message, type);
    }

    public PasswordsNotMatchingException(String message) {
        super(message);
    }
}
