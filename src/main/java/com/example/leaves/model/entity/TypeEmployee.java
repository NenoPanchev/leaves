package com.example.leaves.model.entity;

import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.model.dto.TypeEmployeeDto;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "types", schema = "public")
@NamedEntityGraph(
        name = "type",
        attributeNodes = {
                @NamedAttributeNode("employeeWithType")
        }
)
@AttributeOverride(name = "id", column = @Column(name = "id"))
public class TypeEmployee extends BaseEntity<TypeEmployeeDto> {

    @Column(name = "type_name")
    private String typeName;

    @Column(name = "type_days")
    private int daysLeave;

    @OneToMany(mappedBy = "employeeType")
    private List<EmployeeInfo> employeeWithType;

    public List<EmployeeInfo> getEmployeesWithType() {
        return employeeWithType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getDaysLeave() {
        return daysLeave;
    }

    public void setDaysLeave(int daysLeave) {
        this.daysLeave = daysLeave;
    }

    public void addEmployee(EmployeeInfo employeeWithType) {
        if (this.employeeWithType == null) {
            this.employeeWithType = new ArrayList<>();
        }
        this.employeeWithType.add(employeeWithType);
    }

    public void setEmployeeWithType(List<EmployeeInfo> employeeWithType) {
        this.employeeWithType = employeeWithType;
    }

    public TypeEmployeeDto toDto() {
        TypeEmployeeDto typeEmployeeDto = new TypeEmployeeDto();
        super.toDto(typeEmployeeDto);
        typeEmployeeDto.setTypeName(this.typeName);
        typeEmployeeDto.setDaysLeave(this.daysLeave);
        typeEmployeeDto.setDeleted(isDeleted());
        List<EmployeeInfoDto> list = new ArrayList<>();

        if (this.employeeWithType != null && !getEmployeesWithType().isEmpty()) {
            this.getEmployeesWithType().forEach(e -> list.add(e.toDto()));
            typeEmployeeDto.setEmployeeWithType(list);
        } else {
            typeEmployeeDto.setEmployeeWithType(list);
        }

        return typeEmployeeDto;
    }

    @Override
    public void toEntity(TypeEmployeeDto typeEmployeeDto) {
        super.toEntity(typeEmployeeDto);
        this.setTypeName(typeEmployeeDto.getTypeName());
        this.setDaysLeave(typeEmployeeDto.getDaysLeave());

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
