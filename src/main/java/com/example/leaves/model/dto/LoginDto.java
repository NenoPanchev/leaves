package com.example.leaves.model.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class LoginDto {

    private String email;
    private String password;

    public LoginDto() {
    }

    @NotEmpty(message = "Field cannot be empty")
    @Email(message = "Enter valid email address")
    public String getEmail() {
        return email;
    }

    public LoginDto setEmail(String email) {
        this.email = email;
        return this;
    }

    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
    public String getPassword() {
        return password;
    }

    public LoginDto setPassword(String password) {
        this.password = password;
        return this;
    }
}
