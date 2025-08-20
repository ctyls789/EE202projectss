package com.project.HR.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "hr_leave_record")
public class LeaveRecord implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "leave_uuid", length = 36, nullable = false)
	private String uuid;

	// 外鍵參考 employee_users.employee_user_id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "emp_id", referencedColumnName = "employee_user_id", nullable = false)
	private EmployeeHr employee;

	// 外鍵參考 employee_users.employee_user_id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "agent_id", referencedColumnName = "employee_user_id")
	private EmployeeHr agent;

	// 外鍵參考 leave_type.id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type_id", nullable = false)
	private LeaveType leaveType;

	@Column(name = "reason", length = 200)
	private String reason;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy/MM/dd HH")
	@Column(name = "start_datetime", nullable = false)
	private LocalDateTime startDatetime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy/MM/dd HH")
	@Column(name = "end_datetime", nullable = false)
	private LocalDateTime endDatetime;

	@Column(name = "hours", precision = 5, scale = 2, nullable = false)
	private BigDecimal hours;

	@Column(name = "status", length = 10, nullable = false)
	private String status;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public LeaveRecord() {
	}

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public EmployeeHr getEmployee() {
		return employee;
	}

	public void setEmployee(EmployeeHr employee) {
		this.employee = employee;
	}

	public EmployeeHr getAgent() {
		return agent;
	}

	public void setAgent(EmployeeHr agent) {
		this.agent = agent;
	}

	public LeaveType getLeaveType() {
		return leaveType;
	}

	public void setLeaveType(LeaveType leaveType) {
		this.leaveType = leaveType;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public LocalDateTime getStartDatetime() {
		return startDatetime;
	}

	public void setStartDatetime(LocalDateTime startDatetime) {
		this.startDatetime = startDatetime;
	}

	public LocalDateTime getEndDatetime() {
		return endDatetime;
	}

	public void setEndDatetime(LocalDateTime endDatetime) {
		this.endDatetime = endDatetime;
	}

	public BigDecimal getHours() {
		return hours;
	}

	public void setHours(BigDecimal hours) {
		this.hours = hours;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
