package com.property.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipantVO {
    private String userId;
    private LocalDateTime registeredAt;
}
