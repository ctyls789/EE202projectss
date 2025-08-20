package com.project.HR.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.HR.model.CreateLeaveRecordRequest;
import com.project.HR.model.LeaveRecordDto;
import com.project.HR.model.UpdateLeaveRecordRequest;
import com.project.HR.model.EmployeeHr;
import com.project.HR.model.LeaveRecord;
import com.project.HR.model.LeaveType;
import com.project.HR.repository.HrEmployeeRepository;
import com.project.HR.repository.LeaveRecordRepository;
import com.project.HR.repository.LeaveTypeRepository;
import java.util.stream.Collectors;

@Service
public class LeaveRecordServiceImpl implements LeaveRecordService {

    private final LeaveRecordRepository leaveRecordRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final HrEmployeeRepository employeeRepository;

    @Autowired
    public LeaveRecordServiceImpl(LeaveRecordRepository leaveRecordRepository,
            LeaveTypeRepository leaveTypeRepository, HrEmployeeRepository employeeRepository) {
        this.leaveRecordRepository = leaveRecordRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public LeaveRecordDto createLeaveRecord(CreateLeaveRecordRequest request) {
        LeaveRecord newRecord = new LeaveRecord();

        EmployeeHr employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + request.getEmployeeId()));
        LeaveType leaveType = leaveTypeRepository.findById(request.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("LeaveType not found with id: " + request.getLeaveTypeId()));

        newRecord.setEmployee(employee);
        newRecord.setLeaveType(leaveType);

        if (request.getAgentId() != null) {
            EmployeeHr agent = employeeRepository.findById(request.getAgentId())
                    .orElseThrow(() -> new RuntimeException("Agent not found with id: " + request.getAgentId()));
            newRecord.setAgent(agent);
        }

        newRecord.setReason(request.getReason());
        newRecord.setStartDatetime(request.getStartDatetime());
        newRecord.setEndDatetime(request.getEndDatetime());
        newRecord.setHours(request.getHours());
        newRecord.setStatus("pending"); // 預設狀態
        newRecord.setUuid(UUID.randomUUID().toString());

        LeaveRecord savedRecord = leaveRecordRepository.save(newRecord);
        return convertToDto(savedRecord);
    }

    @Override
    @Transactional
    public Optional<LeaveRecordDto> updateLeaveRecord(String uuid, UpdateLeaveRecordRequest request) {
        return leaveRecordRepository.findByUuid(uuid)
                .map(existingRecord -> {
                    existingRecord.setReason(request.getReason());
                    existingRecord.setStartDatetime(request.getStartDatetime());
                    existingRecord.setEndDatetime(request.getEndDatetime());
                    existingRecord.setHours(request.getHours());
                    LeaveRecord updatedRecord = leaveRecordRepository.save(existingRecord);
                    return convertToDto(updatedRecord);
                });
    }

    @Override
    @Transactional
    public void deleteByUuid(String uuid) {
        leaveRecordRepository.findByUuid(uuid).ifPresent(leaveRecordRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeaveRecord> getByUuid(String uuid) {
        return leaveRecordRepository.findByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRecordDto> getAllLeaveRecordsAsDto() {
        return leaveRecordRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeaveRecordDto> getLeaveRecordDtoByUuid(String uuid) {
        return leaveRecordRepository.findByUuid(uuid)
                .map(this::convertToDto);
    }

    // Helper method to convert Entity to DTO
    private LeaveRecordDto convertToDto(LeaveRecord record) {
        LeaveRecordDto dto = new LeaveRecordDto();
        dto.setUuid(record.getUuid());
        dto.setReason(record.getReason());
        dto.setStartDatetime(record.getStartDatetime());
        dto.setEndDatetime(record.getEndDatetime());
        dto.setHours(record.getHours());
        dto.setStatus(record.getStatus());

        // Safely access lazy-loaded fields within the transaction
        EmployeeHr employee = record.getEmployee();
        if (employee != null) {
            dto.setEmployeeId(employee.getEmployeeId());
            dto.setEmployeeName(employee.getFullName());
        }
        EmployeeHr agent = record.getAgent();
        if (agent != null) {
            dto.setAgentId(agent.getEmployeeId());
            dto.setAgentName(agent.getFullName());
        }
        dto.setLeaveTypeName(record.getLeaveType().getName());

        return dto;
    }
}
