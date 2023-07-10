package com.example.leaves.model.entity;

import com.example.leaves.model.dto.DepartmentDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@NamedEntityGraph(
        name = "fullDepartment",
        attributeNodes = {
                @NamedAttributeNode("admin"),
                @NamedAttributeNode("employees"),
        }
)
@Table(name = "departments", schema = "public")
public class DepartmentEntity extends BaseEntity<DepartmentDto> {
    @Column(unique = true, nullable = false, name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private UserEntity admin;
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "department")
    private List<UserEntity> employees = new ArrayList<>();

    public DepartmentEntity() {
        this.employees = new ArrayList<>();
    }

    public DepartmentEntity(String department) {
        this.name = department;
    }


    public String getName() {
        return name;
    }

    public void setName(String department) {
        this.name = department;
    }


    public UserEntity getAdmin() {
        return admin;
    }

    public void setAdmin(UserEntity admin) {
        this.admin = admin;
    }


    public List<UserEntity> getEmployees() {
        return employees;
    }

    public void setEmployees(List<UserEntity> employees) {
        this.employees = employees;
    }

    @Override
    public void toDto(DepartmentDto dto) {
        if (dto == null) {
            return;
        }

        super.toDto(dto);
        dto.setName(this.name);
        dto.setAdminEmail(null);
        dto.setEmployeeEmails(null);

        if (this.admin != null && !this.admin.isDeleted()) {
            dto.setAdminEmail(admin.getEmail());
        }
        if (this.employees != null && !this.employees.isEmpty()) {
            dto.setEmployeeEmails(employees.stream()
                    .filter(userEntity -> !userEntity.isDeleted())
                    .map(UserEntity::getEmail)
                    .collect(Collectors.toList()));
            if (dto.getEmployeeEmails().isEmpty()) {
                dto.setEmployeeEmails(null);
            }
        }
    }

    @Override
    public void toEntity(DepartmentDto dto) {
        if (dto == null) {
            return;
        }
        super.toEntity(dto);
        this.setName(dto.getName() == null ? this.getName() : dto.getName().toUpperCase());
    }

    public void addEmployee(UserEntity userEntity) {
        if (this.employees == null) {
            this.employees = new ArrayList<>();
        }
        this.employees.add(userEntity);
    }

    public void addAll(List<UserEntity> employees) {
        if (this.employees == null) {
            this.employees = new ArrayList<>();
        }
        this.employees.addAll(employees);
    }

    public void removeEmployee(UserEntity userEntity) {
        this.employees.remove(userEntity);
    }

    public void removeAll(List<UserEntity> entities) {
        this.employees.removeAll(entities);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DepartmentEntity that = (DepartmentEntity) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
