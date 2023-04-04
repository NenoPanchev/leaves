package com.example.leaves.model.payload.request;

public class RefreshRequest {
    private String jwt;

    public RefreshRequest() {
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
