package com.project.machine.Bean;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

@SuppressWarnings("serial")
@Entity
@Table(name = "machine_repair")
public class MachineRepairBean implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repair_id")
    private int repairId;

    @Column(name = "machine_id")
    private int machineId;

    @Column(name = "repair_description", columnDefinition = "TEXT")
    private String repairDescription;

    @Column(name = "repair_time")
    private LocalDateTime repairTime;

    @Column(name = "repair_status", length = 20)
    private String repairStatus;

    @Column(name = "employee_id")
    private Integer employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", referencedColumnName = "machine_id", insertable = false, updatable = false)
    private MachinesBean machine;

  

    // 無參數建構子
    public MachineRepairBean() {
    }

    // 完整建構子
    public MachineRepairBean(int repairId, int machineId, String repairDescription, LocalDateTime repairTime,
            String repairStatus, Integer employeeId, MachinesBean machine,
            String machineName, String serialNumber) {
        this.repairId = repairId;
        this.machineId = machineId;
        this.repairDescription = repairDescription;
        this.repairTime = repairTime;
        this.repairStatus = repairStatus;
        this.employeeId = employeeId;
        this.machine = machine;

    }

    // 原有的建構子
    public MachineRepairBean(int machineId, String repairDescription, LocalDateTime repairTime,
            String repairStatus, Integer employeeId) {
        this.machineId = machineId;
        this.repairDescription = repairDescription;
        this.repairTime = repairTime;
        this.repairStatus = repairStatus;
        this.employeeId = employeeId;
    }

    public MachineRepairBean(int repairId, int machineId, String repairDescription, LocalDateTime repairTime,
            String repairStatus, Integer employeeId) {
        this.repairId = repairId;
        this.machineId = machineId;
        this.repairDescription = repairDescription;
        this.repairTime = repairTime;
        this.repairStatus = repairStatus;
        this.employeeId = employeeId;
    }

    // Getter 和 Setter 方法
    public int getRepairId() {
        return repairId;
    }

    public void setRepairId(int repairId) {
        this.repairId = repairId;
    }

    public int getMachineId() {
        return machineId;
    }

    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    public String getRepairDescription() {
        return repairDescription;
    }

    public void setRepairDescription(String repairDescription) {
        this.repairDescription = repairDescription;
    }

    public LocalDateTime getRepairTime() {
        return repairTime;
    }

    public void setRepairTime(LocalDateTime repairTime) {
        this.repairTime = repairTime;
    }

    public String getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(String repairStatus) {
        this.repairStatus = repairStatus;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public MachinesBean getMachine() {
        return machine;
    }

    public void setMachine(MachinesBean machine) {
        this.machine = machine;
    }



    // toString 方法（排除 machine 以避免循環引用）
    @Override
    public String toString() {
        return "MachineRepairBean{" +
                "repairId=" + repairId +
                ", machineId=" + machineId +
                ", repairDescription='" + repairDescription + '\'' +
                ", repairTime=" + repairTime +
                ", repairStatus='" + repairStatus + '\'' +
                ", employeeId=" + employeeId +
               
                '}';
    }

    // equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MachineRepairBean that = (MachineRepairBean) o;
        return repairId == that.repairId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(repairId);
    }
}
