package com.property.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VisitorAppointmentVO {
    private String id;
    private String visitorName;
    private String visitorPhone;
    private String visitorIdCard;
    private String purpose;
    private LocalDate visitDate;
    private LocalDateTime expectedArrivalAt;
    private LocalDateTime expectedDepartureAt;
    private String status;
    private String applicantId;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
