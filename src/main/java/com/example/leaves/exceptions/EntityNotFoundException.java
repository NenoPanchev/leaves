package com.example.leaves.exceptions;

public class EntityNotFoundException extends BaseCustomException {
    public EntityNotFoundException(String type, long id) {
        this(type, "id", String.valueOf(id));
    }

    public EntityNotFoundException(String type, String attribute, String value) {
        super(String.format("%s with %s %s not found.", type, attribute, value));
    }

    public EntityNotFoundException(String msg) {
        super(msg);
    }
}
