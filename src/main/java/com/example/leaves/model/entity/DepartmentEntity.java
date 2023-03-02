package com.example.leaves.model.entity;

import javax.persistence.*;

@Entity
@Table(name = "departments")
public class DepartmentEntity extends BaseEntity{
    private String department;
    private UserEntity admin;

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
}
