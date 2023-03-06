package com.example.leaves.model.entity;

import com.example.leaves.model.dto.DepartmentDto;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "departments")
public class DepartmentEntity extends BaseEntity{
    private String department;
    private UserEntity admin;
    private List<UserEntity> employees;

    public DepartmentEntity() {
    }

    @Column
    public String getDepartment() {
        return department;
    }

    public DepartmentEntity setDepartment(String department) {
        this.department = department;
        return this;
    }

    @ManyToOne
    public UserEntity getAdmin() {
        return admin;
    }

    public DepartmentEntity setAdmin(UserEntity admin) {
        this.admin = admin;
        return this;
    }

    @OneToMany
    public List<UserEntity> getEmployees() {
        return employees;
    }

    public DepartmentEntity setEmployees(List<UserEntity> employees) {
        this.employees = employees;
        return this;
    }

    public DepartmentDto toDto() {
        DepartmentDto dto = new DepartmentDto()
                .setId(getId())
                .setDepartment(department)
                .setAdminEmail(null)
                .setEmployeeEmails(null);
        if (admin != null) {
            dto.setAdminEmail(admin.getEmail());
        }
        if (employees.size() != 0) {
            dto.setEmployeeEmails(employees.stream()
                    .map(UserEntity::getEmail)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
