package com.example.leaves.model.entity;

import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.dto.UserDto;
import com.example.leaves.model.service.UserServiceModel;

import javax.persistence.*;

import java.util.List;
import java.util.stream.Collectors;

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

    public UserDto toDto() {
        return null;
//        List<RoleDto> roleDtos = roles.stream()
//                .map(RoleEntity::toDto)
//                .collect(Collectors.toList());
//
//        return new UserDto()
//                .setId(getId())
//                .setEmail(email)
//                .setPassword(password)
//                .setRoles(roleDtos)
//                .setDepartment(department.getDepartment());
    }
}
