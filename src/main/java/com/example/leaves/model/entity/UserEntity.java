package com.example.leaves.model.entity;

import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.dto.UserDto;

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
@AttributeOverrides(
        {
                @AttributeOverride(name = "id", column = @Column(name = "id"))
        }
)
@Entity
@Table(name = "users", schema = "public")
public class UserEntity extends BaseEntity<UserDto> {
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @Column(nullable = false, name = "password")
    private String password;
    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;
    @ManyToMany
    private List<RoleEntity> roles;

    @OneToOne(mappedBy = "userInfo")
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


    public String getPassword() {
        return password;
    }

    public UserEntity setPassword(String password) {
        this.password = password;
        return this;
    }


    public List<RoleEntity> getRoles() {
        return roles;
    }

    public UserEntity setRoles(List<RoleEntity> roles) {
        this.roles = roles;
        return this;
    }


    public DepartmentEntity getDepartment() {
        return department;
    }

    public UserEntity setDepartment(DepartmentEntity department) {
        this.department = department;
        return this;
    }


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

    public EmployeeInfo getEmployeeInfo() {
        return employeeInfo;
    }

    public void setEmployeeInfo(EmployeeInfo employeeInfo) {
        this.employeeInfo = employeeInfo;
    }
}
