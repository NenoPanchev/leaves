package com.example.leaves.model.payload.request;

import com.example.leaves.util.validators.FieldMatch;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.Size;

@FieldMatch(
        first = "newPassword",
        second = "newPasswordConfirm",
        message = "Passwords must match"
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PasswordChangeDto {
    private String oldPassword;
    private String newPassword;
    private String newPasswordConfirm;

    private String token;


    public PasswordChangeDto() {
    }

    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    public void setNewPasswordConfirm(String newPasswordConfirm) {
        this.newPasswordConfirm = newPasswordConfirm;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
