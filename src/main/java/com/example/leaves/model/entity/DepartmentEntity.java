package com.example.leaves.model.entity;

import com.example.leaves.model.entity.enums.DepartmentEnum;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "departments")
public class DepartmentEntity extends BaseEntity{
    private DepartmentEnum department;

    public DepartmentEntity() {
    }

    @Enumerated(EnumType.STRING)
    public DepartmentEnum getDepartment() {
        return department;
    }

    public DepartmentEntity setDepartment(DepartmentEnum department) {
        this.department = department;
        return this;
    }

}
