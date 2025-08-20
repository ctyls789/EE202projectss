package com.project.HR.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.HR.model.LeaveRecord;

@Repository
public interface LeaveRecordRepository extends JpaRepository<LeaveRecord, Integer> {

    // Spring Data JPA 會根據方法名稱自動生成查詢
    // 這行程式碼等同於 HQL: "FROM LeaveRecord WHERE uuid = :uuid"
    Optional<LeaveRecord> findByUuid(String uuid);
}
