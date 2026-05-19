package com.property.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志（分区表，主键为 (id, at)）。
 * 不继承 BaseEntity：使用 BIGINT 自增 id，无逻辑删除，无 updated_at。
 */
@Data
@TableName("op_logs")
public class OpLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String actorId;
    private String actorName;
    private String action;
    private String target;
    private String diff;
    private String ip;
    private String userAgent;
    private String traceId;
    private LocalDateTime at;
}
