package com.project.HR.model;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee_users")
public class EmployeeHr implements Serializable {

	@Id
	@Column(name = "employee_user_id") // 建議明確指定欄位名稱
	private Integer employeeId; // 作為業務主鍵，不應由資料庫生成

	@Column(name = "last_name", length = 50, nullable = false)
	private String lastName;

	@Column(name = "first_name", length = 50, nullable = false)
	private String firstName;

	@Column(name = "department_id")
	private Integer departmentId;

	@Column(name = "position_id", length = 100)
	private Integer positionId;

	@Column(name = "hire_date")
	private LocalDate hireDate;

	public EmployeeHr() {
	}

	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public Integer getPositionId() {
		return positionId;
	}

	public void setPositionId(Integer positionId) {
		this.positionId = positionId;
	}

	public LocalDate getHireDate() {
		return hireDate;
	}

	public void setHireDate(LocalDate hireDate) {
		this.hireDate = hireDate;
	}
	
	//遵循DRY原則:直接用getter組合全名，也不必對應資料表欄位，好讚
	public String getFullName() {
		return this.firstName + " " + this.lastName;
	}
}
