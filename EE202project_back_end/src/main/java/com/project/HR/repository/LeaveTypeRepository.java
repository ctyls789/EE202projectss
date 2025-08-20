package com.project.HR.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.HR.model.LeaveType;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Integer> {
    // JpaRepository 已提供 findAll(), findById(), save(), delete() 等常用方法，不需額外撰寫。
}
