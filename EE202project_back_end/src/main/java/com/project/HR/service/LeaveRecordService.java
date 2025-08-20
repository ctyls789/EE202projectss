package com.project.HR.service;

import com.project.HR.model.CreateLeaveRecordRequest;
import com.project.HR.model.LeaveRecord;
import com.project.HR.model.LeaveRecordDto;
import com.project.HR.model.LeaveType;
import com.project.HR.model.UpdateLeaveRecordRequest;

import java.util.List;
import java.util.Optional;

public interface LeaveRecordService {

	LeaveRecordDto createLeaveRecord(CreateLeaveRecordRequest request);

	Optional<LeaveRecordDto> updateLeaveRecord(String uuid, UpdateLeaveRecordRequest request);

	void deleteByUuid(String uuid);

	List<LeaveType> getAllLeaveTypes();

	Optional<LeaveRecord> getByUuid(String uuid);

	List<LeaveRecordDto> getAllLeaveRecordsAsDto();

	Optional<LeaveRecordDto> getLeaveRecordDtoByUuid(String uuid);
}
