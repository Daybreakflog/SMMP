package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("poll_votes")
public class PollVote {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String pollId;
    private String optionId;
    private String userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
