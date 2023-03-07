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

    public DepartmentEntity(String department) {
        this.department = department;
    }

    @Column
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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
        dto.setId(this.getId());
        dto.setDepartment(this.department);
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

//    public DepartmentDto toDtoReturn() {
//        DepartmentDto dto = new DepartmentDto();
//                dto.setId(getId());
//                dto.setDepartment(department);
//                dto.setAdminEmail(null);
//                dto.setEmployeeEmails(null);
//        if (admin != null) {
//            dto.setAdminEmail(admin.getEmail());
//        }
//        if (employees.size() != 0) {
//            dto.setEmployeeEmails(employees.stream()
//                    .map(UserEntity::getEmail)
//                    .collect(Collectors.toList()));
//        }
//        return dto;
//    }
}
