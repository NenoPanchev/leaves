package com.example.leaves.exceptions;

public class RequestAlreadyProcessed extends BaseCustomException {
    public RequestAlreadyProcessed(long id) {
        super(String.format("Request with %d already processed", id));
    }

    public RequestAlreadyProcessed(long id, String type) {
        super(String.valueOf(id), type);
    }
}
