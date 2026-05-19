package com.property.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class VisitorAppointmentDTO {
    @NotBlank
    private String visitorName;
    @NotBlank
    private String visitorPhone;
    private String visitorIdCard;
    private String purpose;
    @NotNull
    private LocalDate visitDate;
    private LocalDateTime expectedArrivalAt;
    private LocalDateTime expectedDepartureAt;
    private String remark;
}
