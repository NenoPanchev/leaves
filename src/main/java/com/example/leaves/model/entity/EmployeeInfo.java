package com.example.leaves.model.entity;

import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.util.EncryptionUtil;
import com.example.leaves.util.EntityListener;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EntityListeners(EntityListener.class)
@NamedEntityGraph(
        name = "fullInfo",
        attributeNodes = {
                @NamedAttributeNode("employeeType"),
                @NamedAttributeNode("requests")
        }
)
@Entity
@Getter
@Setter
@Table(name = "employee_info", schema = "public")
public class EmployeeInfo extends BaseEntity<EmployeeInfoDto> {

    @ManyToOne(cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "type_id")
    @JsonBackReference
    private TypeEmployee employeeType;

//    @Column(name = "carryover_days_leave")
//    private int carryoverDaysLeave;
    @Column(name = "current_year_days_leave")
    private int currentYearDaysLeave;
    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "ssn")
    private String ssn;

    @Column(name = "address")
    private String address;

    @Column(name = "position")
    private String position;
    @OneToMany(mappedBy = "employee")
    private Set<RequestEntity> requests;

    @OneToOne(mappedBy = "employeeInfo", cascade = CascadeType.ALL)
    private UserEntity userInfo;

    @OneToMany(mappedBy = "employeeInfo", cascade = CascadeType.ALL)
    private List<HistoryEntity> historyList = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "employee_info_history", joinColumns = @JoinColumn(name = "employee_info_id"))
    @MapKeyColumn(name = "year")
    @Column(name = "days_used")
    private Map<Integer, Integer> history = new HashMap<>();

    public EmployeeInfo() {
        this.setContractStartDate(LocalDate.now());
    }

    public UserEntity getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserEntity userInfo) {
        this.userInfo = userInfo;
    }

    public int getDaysLeave() {
        return this.currentYearDaysLeave;
    }

//    public int getCarryoverDaysLeave() {
//        return carryoverDaysLeave;
//    }
//
//    public void setCarryoverDaysLeave(int carryoverDaysLeave) {
//        this.carryoverDaysLeave = carryoverDaysLeave;
//    }

    public int getCurrentYearDaysLeave() {
        return currentYearDaysLeave;
    }

    public void setCurrentYearDaysLeave(int currentYearDaysLeave) {
        this.currentYearDaysLeave = currentYearDaysLeave;
    }

    public Set<RequestEntity> getRequests() {
        return requests;
    }

    public TypeEmployee getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(TypeEmployee employeeType) {
        this.employeeType = employeeType;
        if (this.getId() == null) {
            setCurrentYearDaysLeave(employeeType.getDaysLeave());
        }
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public EmployeeInfoDto toDto() {
        EmployeeInfoDto dto = new EmployeeInfoDto();
        dto.setTypeId(this.getEmployeeType().getId());
        dto.setTypeName(this.getEmployeeType().getTypeName());
        dto.setTypeDaysLeave(this.employeeType.getDaysLeave());
        dto.setDaysLeave(this.getDaysLeave());
//        dto.setCarryoverDaysLeave(this.carryoverDaysLeave);
        dto.setCurrentYearDaysLeave(this.currentYearDaysLeave);
        dto.setName(userInfo.getName());
        dto.setId(userInfo.getId());
        dto.setAddress(this.address);
        dto.setSsn(EncryptionUtil.decrypt(this.ssn));
        dto.setContractStartDate(this.contractStartDate);
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "EmployeeInfo{" +
                ", currentYearDaysLeave=" + currentYearDaysLeave +
                ", contractStartDate=" + contractStartDate +
                '}';
    }
}
