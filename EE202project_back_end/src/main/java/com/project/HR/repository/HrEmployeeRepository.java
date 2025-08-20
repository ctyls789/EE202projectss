package com.project.HR.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.HR.model.EmployeeHr;

@Repository
public interface HrEmployeeRepository extends JpaRepository<EmployeeHr, Integer> {
    // JpaRepository 已提供基礎的 CRUD 方法
}
