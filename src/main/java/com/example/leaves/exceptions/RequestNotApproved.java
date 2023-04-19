package com.example.leaves.exceptions;

public class RequestNotApproved extends BaseCustomException {
    public RequestNotApproved(long id) {
        super(String.format("Request with %d not approved", id));
    }

    public RequestNotApproved(long id, String type) {
        super(String.valueOf(id), type);
    }
}
