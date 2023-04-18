package com.example.leaves.model.entity;

import com.example.leaves.exceptions.EntityNotFoundException;
import com.example.leaves.exceptions.PaidleaveNotEnoughException;
import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.util.EntityListener;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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
        setPaidLeave(employeeType.getDaysLeave());
    }

    public void resetAnnualLeave() {
        setPaidLeave(employeeType.getDaysLeave());
    }

    public EmployeeInfoDto toDto() {
        EmployeeInfoDto dto = new EmployeeInfoDto();
        dto.setTypeId(this.getEmployeeType().getId());
        dto.setName(userInfo.getName());
        return dto;
    }


}
