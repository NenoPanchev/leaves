package com.example.leaves.model.entity;

import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.PaidleaveNotEnoughException;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.util.EncryptionUtil;
import com.example.leaves.util.EntityListener;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

@EntityListeners(EntityListener.class)
@NamedEntityGraph(
        name = "fullInfo",
        attributeNodes = {
                @NamedAttributeNode("employeeType"),
                @NamedAttributeNode("leaveRequests")
        }
)
@Entity
@Getter
@Setter
@Table(name = "employee_info", schema = "public")
public class EmployeeInfo extends BaseEntity<EmployeeInfoDto> {

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "type_id")
    @JsonBackReference
    private TypeEmployee employeeType;

    @Column(name = "carryover_days_leave")
    private int carryoverDaysLeave;
    @Column(name = "current_year_days_leave")
    private int currentYearDaysLeave;
    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "ssn")
    private String ssn;
    //TODO CHANGE SSN TO CHAR ARR

    @Column(name = "address")
    private String address;

    @Column(name = "position")
    private String position;
    @OneToMany(mappedBy = "employee")
    private Set<LeaveRequest> leaveRequests;

    @OneToOne(mappedBy = "employeeInfo", cascade = CascadeType.ALL)
    private UserEntity userInfo;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employeeInfo", cascade = {CascadeType.ALL})
    private List<ContractEntity> contracts = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "employee_info_history", joinColumns = @JoinColumn(name = "employee_info_id"))
    @MapKeyColumn(name = "year")
    @Column(name = "days_used")
    private Map<Integer, Integer> history = new HashMap<>();

    public EmployeeInfo() {
        this.setContractStartDate(LocalDate.now());
    }

    public void subtractFromAnnualPaidLeave(int days) {
        if (this.getDaysLeave() - days < 0) {
            throw new PaidleaveNotEnoughException(days, this.getDaysLeave());
        } else {
            if (days >= this.carryoverDaysLeave) {
                setCurrentYearDaysLeave(this.getDaysLeave() - days);
                setCarryoverDaysLeave(0);
            } else {
                setCarryoverDaysLeave(this.carryoverDaysLeave - days);
            }
        }

    }

    public void subtractFromAnnualPaidLeaveWithoutThrowing(int days) {
        if (days >= this.carryoverDaysLeave) {
            setCurrentYearDaysLeave(this.getDaysLeave() - days);
            setCarryoverDaysLeave(0);
        } else {
            setCarryoverDaysLeave(this.carryoverDaysLeave - days);
        }
    }

    public UserEntity getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserEntity userInfo) {
        this.userInfo = userInfo;
    }

    public int getDaysLeave() {
        return this.currentYearDaysLeave + this.carryoverDaysLeave;
    }

    public int getCarryoverDaysLeave() {
        return carryoverDaysLeave;
    }

    public void setCarryoverDaysLeave(int carryoverDaysLeave) {
        this.carryoverDaysLeave = carryoverDaysLeave;
    }

    public int getCurrentYearDaysLeave() {
        return currentYearDaysLeave;
    }

    public void setCurrentYearDaysLeave(int currentYearDaysLeave) {
        this.currentYearDaysLeave = currentYearDaysLeave;
    }

    public void removeRequest(LeaveRequest leaveRequest) {
        if (leaveRequests.contains(leaveRequest)) {
            leaveRequests.remove(leaveRequest);
        } else {
            throw new EntityNotFoundException("request", leaveRequest.getId());
        }
    }

    public boolean checkIfPossibleToSubtractFromAnnualPaidLeave(int days) {
        return this.getDaysLeave() - days >= 0 && days > 0;
    }

    public Set<LeaveRequest> getRequests() {
        return leaveRequests;
    }

    public TypeEmployee getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(TypeEmployee employeeType) {
        //TODO reset annual leave when change or not ?
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

    public void resetAnnualLeave() {
        setCurrentYearDaysLeave(employeeType.getDaysLeave());
    }

    public EmployeeInfoDto toDto() {
        EmployeeInfoDto dto = new EmployeeInfoDto();
        dto.setTypeId(this.getEmployeeType().getId());
        dto.setTypeName(this.getEmployeeType().getTypeName());
        dto.setTypeDaysLeave(this.employeeType.getDaysLeave());
        dto.setDaysLeave(this.getDaysLeave());
        dto.setCarryoverDaysLeave(this.carryoverDaysLeave);
        dto.setCurrentYearDaysLeave(this.currentYearDaysLeave);
        dto.setName(userInfo.getName());
        dto.setId(userInfo.getId());
        dto.setAddress(this.address);
        dto.setSsn(EncryptionUtil.decrypt(this.ssn));
        dto.setContractStartDate(this.contractStartDate);
        return dto;
    }

    public void addContract(ContractEntity entity) {
        this.contracts.add(entity);
    }

    public void removeContract(ContractEntity entity) {
        this.contracts.remove(entity);
    }

    public void removeContract(int index) {
        this.contracts.remove(index);
    }
}
