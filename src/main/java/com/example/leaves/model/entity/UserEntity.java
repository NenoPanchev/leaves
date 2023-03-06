package com.example.leaves.model.entity;

import javax.persistence.*;

import java.util.List;

@NamedEntityGraph(
        name = "full",
        attributeNodes = {
                @NamedAttributeNode("roles"),
                @NamedAttributeNode("department")
        }
)

@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity{
    private String email;
    private String password;
    private List<RoleEntity> roles;
    private DepartmentEntity department;

    public UserEntity() {
    }

    @Column(nullable = false, unique = true)
    public String getEmail() {
        return email;
    }

    public UserEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    @Column(nullable = false)
    public String getPassword() {
        return password;
    }

    public UserEntity setPassword(String password) {
        this.password = password;
        return this;
    }

    @ManyToMany
    public List<RoleEntity> getRoles() {
        return roles;
    }

    public UserEntity setRoles(List<RoleEntity> roles) {
        this.roles = roles;
        return this;
    }


    @ManyToOne
    public DepartmentEntity getDepartment() {
        return department;
    }

    public UserEntity setDepartment(DepartmentEntity department) {
        this.department = department;
        return this;
    }
}
