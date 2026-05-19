package com.property.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("parking_spaces")
public class ParkingSpace extends BaseEntity {

    private String spaceNo;
    private String zone;
    /** AVAILABLE | OCCUPIED | MAINTENANCE */
    private String status;
    private String ownerId;
    private String ownerName;
    private String vehiclePlate;
    private String remark;
}
