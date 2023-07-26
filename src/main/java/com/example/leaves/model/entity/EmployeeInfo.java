package com.example.leaves.model.entity;

import com.example.leaves.model.dto.EmployeeInfoDto;
import com.example.leaves.util.EncryptionUtil;
import com.example.leaves.util.EntityListener;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    public EmployeeInfo() {
        this.setContractStartDate(LocalDate.now());
    }

    public UserEntity getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserEntity userInfo) {
        this.userInfo = userInfo;
    }

    public Set<RequestEntity> getRequests() {
        return requests;
    }

    public TypeEmployee getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(TypeEmployee employeeType) {
        this.employeeType = employeeType;
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
        dto.setName(userInfo.getName());
        dto.setId(userInfo.getId());
        dto.setAddress(this.address);
        dto.setSsn(EncryptionUtil.decrypt(this.ssn));
        dto.setContractStartDate(this.contractStartDate);
        dto.setDaysLeave(getCurrentYearDaysLeave());
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
                "id: " + this.getId() +
                '}';
    }

    private int getCurrentYearDaysLeave() {
        int currentYear = LocalDate.now().getYear();
        HistoryEntity historyEntity = getHistoryList()
                .stream()
                .filter(entity -> entity.getCalendarYear() == currentYear)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No history for year: " + currentYear));
        return historyEntity.getDaysLeft();
    }
}
