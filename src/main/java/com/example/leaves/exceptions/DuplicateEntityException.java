package com.example.leaves.exceptions;

public class DuplicateEntityException extends BaseCustomException {
    public DuplicateEntityException(String type, String attribute, String value) {
        super(String.format("%s with %s %s already exists.", type, attribute, value));
    }

    public DuplicateEntityException(String formattedString) {
        super(formattedString);
    }

    public DuplicateEntityException(String formattedString, String type) {
        super(formattedString, type);
    }

    public DuplicateEntityException(String type, long id) {
        super(String.format("%s with %d already exists.", type, id));
    }

}
