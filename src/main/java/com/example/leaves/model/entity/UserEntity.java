package com.example.leaves.model.entity;

import com.example.leaves.model.dto.RoleDto;
import com.example.leaves.model.dto.UserDto;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@NamedEntityGraph(
        name = "full",
        attributeNodes = {
                @NamedAttributeNode("roles"),
                @NamedAttributeNode("department"),
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
    @ManyToOne(cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;
    @ManyToMany
    private List<RoleEntity> roles;


    @OneToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_info_id")
    private EmployeeInfo employeeInfo;


    @OneToOne(fetch = FetchType.EAGER,mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private PasswordResetToken token;

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

    @Override
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

        dto.setEmployeeInfo(this.employeeInfo.toDto());
    }

    @Override
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

    public PasswordResetToken getToken() {
        return token;
    }

    public void setToken(PasswordResetToken token) {
        if (this.token != null) {
            this.getToken().dismissUser();
        }
        this.token = token;
    }


    public EmployeeInfo getEmployeeInfo() {
        return employeeInfo;
    }

    public void setEmployeeInfo(EmployeeInfo employeeInfo) {
        this.employeeInfo = employeeInfo;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
