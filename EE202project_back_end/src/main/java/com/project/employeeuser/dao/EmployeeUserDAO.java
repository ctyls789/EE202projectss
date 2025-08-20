package com.project.employeeuser.dao;

import com.project.employeeuser.model.EmployeeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeUserDAO extends JpaRepository<EmployeeUser, Long> {
    Optional<EmployeeUser> findByUsername(String username);
    Optional<EmployeeUser> findByEmployeeNumber(String employeeNumber);
    Optional<EmployeeUser> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmployeeNumber(String employeeNumber);
    boolean existsByEmail(String email);
}
