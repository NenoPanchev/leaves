package com.example.leaves.model.entity;

import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
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
public class UserEntity extends BaseEntity {
    private String name;
    private String email;
    private String password;
    private DepartmentEntity department;
    private List<RoleEntity> roles;

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinColumn(name = "employee_info_id")
    private EmployeeInfo employeeInfo;

    public UserEntity() {
        this.roles = new ArrayList<>();
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

    @Column
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void toDto(UserDto dto) {
        if (dto == null) {
            return;
        }

        super.toDto(dto);
        dto.setName(this.getName());
        dto.setPassword(this.getPassword());
        dto.setEmail(this.getEmail());

        if (this.getDepartment() != null) {
            dto.setDepartment(this.getDepartment().getName());
        }
        List<RoleDto> roleDtoList = new ArrayList<>();

        for (RoleEntity role : this.roles) {
            RoleDto roleDto = new RoleDto();
            role.toDto(roleDto);
            roleDtoList.add(roleDto);
        }
        dto.setRoles(roleDtoList);
    }

    public void toEntity(UserDto dto) {
        if (dto == null) {
            return;
        }
        super.toEntity(dto);
        this.setName(dto.getName() == null ? this.getName() : dto.getName());
        this.setPassword(dto.getPassword() == null ? this.getPassword() : dto.getPassword());
        this.setEmail(dto.getEmail() == null ? this.getEmail() : dto.getEmail());
    }

    public void removeRole(RoleEntity role) {
        this.roles.remove(role);
    }
}
