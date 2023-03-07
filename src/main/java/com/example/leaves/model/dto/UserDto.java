package com.example.leaves.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String password;
    private List<RoleDto> roles;
    private String department;

    public UserDto() {
    }

    public Long getId() {
        return id;
    }

    public UserDto setId(Long id) {
        this.id = id;
        return this;
    }
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotEmpty(message = "Field cannot be empty")
    @Email(message = "Enter valid email address")
    public String getEmail() {
        return email;
    }

    public UserDto setEmail(String email) {
        this.email = email;
        return this;
    }

    @Size(min = 4, max = 20, message = "Password must be between 4 and 20 characters")
    public String getPassword() {
        return password;
    }

    public UserDto setPassword(String password) {
        this.password = password;
        return this;
    }

    public List<@Valid RoleDto> getRoles() {
        return roles;
    }

    public UserDto setRoles(List<RoleDto> roles) {
        this.roles = roles;
        return this;
    }

    @Size(min = 2, max = 20, message = "Department must be between 2 and 20 characters")
    public String getDepartment() {
        return department;
    }

    public UserDto setDepartment(String department) {
        this.department = department;
        return this;
    }
}
