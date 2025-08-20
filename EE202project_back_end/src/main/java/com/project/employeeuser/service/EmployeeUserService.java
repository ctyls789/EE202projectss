package com.project.employeeuser.service;

import com.project.employeeuser.dao.EmployeeUserDAO;
import com.project.employeeuser.model.EmployeeUser;
import com.project.employeeuser.model.EmployeeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeUserService {

    @Autowired
    private EmployeeUserDAO employeeUserDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<EmployeeUser> getAllEmployeeUsers() {
        return employeeUserDAO.findAll();
    }

    public Optional<EmployeeUser> getEmployeeUserById(Long id) {
        return employeeUserDAO.findById(id);
    }

    @Transactional
    public EmployeeUser createEmployeeUser(EmployeeUser employeeUser) {
        // Encode password before saving
        employeeUser.setPasswordHash(passwordEncoder.encode(employeeUser.getPasswordHash()));
        employeeUser.setIsActive(true); // Default to active
        employeeUser.setCreatedAt(LocalDateTime.now());
        employeeUser.setUpdatedAt(LocalDateTime.now());
        return employeeUserDAO.save(employeeUser);
    }

    @Transactional
    public EmployeeUser updateEmployeeUser(Long id, EmployeeUser employeeUserDetails) {
        EmployeeUser employeeUser = employeeUserDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeUser not found with id " + id));

        employeeUser.setEmployeeNumber(employeeUserDetails.getEmployeeNumber());
        employeeUser.setFirstName(employeeUserDetails.getFirstName());
        employeeUser.setLastName(employeeUserDetails.getLastName());
        employeeUser.setUsername(employeeUserDetails.getUsername());
        employeeUser.setEmployeeType(employeeUserDetails.getEmployeeType());
        employeeUser.setEmail(employeeUserDetails.getEmail());
        employeeUser.setPhone(employeeUserDetails.getPhone());
        employeeUser.setBirthDate(employeeUserDetails.getBirthDate());
        employeeUser.setHireDate(employeeUserDetails.getHireDate());
        employeeUser.setTerminationDate(employeeUserDetails.getTerminationDate());
        employeeUser.setPhotoPath(employeeUserDetails.getPhotoPath());
        employeeUser.setEmployeeDepartmentId(employeeUserDetails.getEmployeeDepartmentId());
        employeeUser.setEmployeePositionId(employeeUserDetails.getEmployeePositionId());
        employeeUser.setManagerEmployeeUserId(employeeUserDetails.getManagerEmployeeUserId());
        employeeUser.setIsActive(employeeUserDetails.getIsActive());
        employeeUser.setUpdatedAt(LocalDateTime.now());

        // Only update password if a new one is provided
        if (employeeUserDetails.getPasswordHash() != null && !employeeUserDetails.getPasswordHash().isEmpty()) {
            employeeUser.setPasswordHash(passwordEncoder.encode(employeeUserDetails.getPasswordHash()));
        }

        return employeeUserDAO.save(employeeUser);
    }

    @Transactional
    public void deleteEmployeeUser(Long id) {
        EmployeeUser employeeUser = employeeUserDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeUser not found with id " + id));
        employeeUserDAO.delete(employeeUser);
    }

    public boolean existsByUsername(String username) {
        return employeeUserDAO.existsByUsername(username);
    }

    public boolean existsByEmployeeNumber(String employeeNumber) {
        return employeeUserDAO.existsByEmployeeNumber(employeeNumber);
    }

    public boolean existsByEmail(String email) {
        return employeeUserDAO.existsByEmail(email);
    }
}
