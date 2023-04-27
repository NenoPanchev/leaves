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
import java.util.ArrayList;
import java.util.List;

@EntityListeners(EntityListener.class)
@Entity
@Getter
@Setter
@Table(name = "employee_info", schema = "public")
public class EmployeeInfo extends BaseEntity<EmployeeInfoDto> {

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST})
    @JoinColumn(name = "type_id")
    @JsonBackReference
    private TypeEmployee employeeType;
    @Column(name = "days_leave")
    private int paidLeave;

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
    private List<LeaveRequest> leaveRequests;

    @OneToOne(mappedBy = "employeeInfo", cascade = CascadeType.ALL)
    private UserEntity userInfo;

    @OneToMany(cascade = {CascadeType.ALL})
    private List<ContractEntity> contracts = new ArrayList<>();

    public EmployeeInfo() {
        this.setContractStartDate(LocalDate.now());
    }

    public void subtractFromAnnualPaidLeave(int days) {
        if (paidLeave - days < 0) {
            throw new PaidleaveNotEnoughException(days, this.paidLeave);
        } else {
            setPaidLeave(paidLeave - days);
        }

    }

    public UserEntity getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserEntity userInfo) {
        this.userInfo = userInfo;
    }

    public int getPaidLeave() {
        return paidLeave;
    }

    public void setPaidLeave(int paidLeave) {
        this.paidLeave = paidLeave;
    }

    public void removeRequest(LeaveRequest leaveRequest) {
        if (leaveRequests.contains(leaveRequest)) {
            leaveRequests.remove(leaveRequest);
        } else {
            throw new EntityNotFoundException("request", leaveRequest.getId());
        }
    }

    public boolean checkIfPossibleToSubtractFromAnnualPaidLeave(int days) {
        return paidLeave - days >= 0 && days > 0;
    }

    public List<LeaveRequest> getRequests() {
        return leaveRequests;
    }

    public TypeEmployee getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(TypeEmployee employeeType) {
        //TODO reset annual leave when change or not ?
        this.employeeType = employeeType;
        if (this.getId() == null) {
            setPaidLeave(employeeType.getDaysLeave());
        }
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public void resetAnnualLeave() {
        setPaidLeave(employeeType.getDaysLeave());
    }

    public EmployeeInfoDto toDto() {
        EmployeeInfoDto dto = new EmployeeInfoDto();
        dto.setTypeId(this.getEmployeeType().getId());
        dto.setTypeName(this.getEmployeeType().getTypeName());
        dto.setTypeDaysLeave(this.employeeType.getDaysLeave());
        dto.setDaysLeave(this.paidLeave);
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
