package com.property.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("uploads")
public class Upload {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String name;
    private Long size;
    private String mimeType;
    private String url;
    private String ossKey;
    private String creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
