package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("visitor_appointments")
public class VisitorAppointment extends BaseEntity {
    private String visitorName;
    private String visitorPhone;
    private String visitorIdCard;
    private String purpose;
    private LocalDate visitDate;
    private LocalDateTime expectedArrivalAt;
    private LocalDateTime expectedDepartureAt;
    /** PENDING | APPROVED | REJECTED | CHECKED_IN | CHECKED_OUT */
    private String status;
    private String applicantId;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private String remark;
}
