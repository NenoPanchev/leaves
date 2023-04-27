package com.example.leaves.service.impl;

import com.example.leaves.exceptions.BaseCustomException;

public class PasswordChangeTokenDoesNotMatchException extends BaseCustomException {

    public PasswordChangeTokenDoesNotMatchException(String message) {
        super(message);
    }
}
