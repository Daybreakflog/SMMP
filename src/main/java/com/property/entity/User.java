package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {
    private String username;
    private String passwordHash;
    private String name;
    private String phone;
    private String avatar;
    /** ACTIVE | INACTIVE | LOCKED */
    private String status;
    private String projectId;
    private LocalDateTime lastLoginAt;
}
