package com.example.leaves.model.entity;

import com.example.leaves.model.dto.DepartmentDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "departments")
public class DepartmentEntity extends BaseEntity{
    private String name;
    private UserEntity admin;
    private List<UserEntity> employees;

    public DepartmentEntity() {
        this.employees = new ArrayList<>();
    }

    public DepartmentEntity(String department) {
        this.name = department;
    }

    @Column(unique = true, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String department) {
        this.name = department;
    }

    @ManyToOne
    public UserEntity getAdmin() {
        return admin;
    }

    public void setAdmin(UserEntity admin) {
        this.admin = admin;
    }

    @OneToMany
    public List<UserEntity> getEmployees() {
        return employees;
    }

    public void setEmployees(List<UserEntity> employees) {
        this.employees = employees;
    }

    public void toDto(DepartmentDto dto) {
        if (dto == null) {
            return;
        }
        super.toDto(dto);
        dto.setName(this.name);
        dto.setAdminEmail(null);
        dto.setEmployeeEmails(null);

        if (admin != null) {
            dto.setAdminEmail(admin.getEmail());
        }
        if (employees.size() != 0) {
            dto.setEmployeeEmails(employees.stream()
                    .map(UserEntity::getEmail)
                    .collect(Collectors.toList()));
        }
    }

    public void toEntity(DepartmentDto dto) {
        if (dto == null) {
            return;
        }
        super.toEntity(dto);
        this.setName(dto.getName() == null ? this.getName() : dto.getName().toUpperCase());
    }
    public void addEmployee(UserEntity userEntity) {
        this.employees.add(userEntity);
    }

    public void removeEmployee(UserEntity userEntity) {
        this.employees.remove(userEntity);
    }
}
