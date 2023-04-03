package com.example.leaves.exceptions;

public class EntityCanNotBeDeletedException extends BaseCustomException {
    public EntityCanNotBeDeletedException(String typeName, String id) {
        super(String.format("%s with %s can not be deleted.", typeName, id));
    }
}
