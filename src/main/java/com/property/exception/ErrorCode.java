package com.property.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 通用
    BAD_REQUEST(40000, "请求参数错误"),
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    FORBIDDEN(40300, "无权限访问"),
    NOT_FOUND(40400, "资源不存在"),
    INTERNAL_ERROR(50000, "服务器内部错误"),

    // 用户 / 鉴权
    USER_NOT_FOUND(40401, "用户不存在"),
    PASSWORD_WRONG(40002, "密码错误"),
    USER_DISABLED(40003, "账号已被禁用"),
    TOKEN_INVALID(40101, "Token 无效或已过期"),

    // 租户
    TENANT_NOT_FOUND(40402, "租户不存在"),

    // 账单
    BILL_NOT_FOUND(40403, "账单不存在"),
    FEE_ITEM_NOT_FOUND(40404, "收费项目不存在"),

    // 工单 / 投诉
    WORK_ORDER_NOT_FOUND(40405, "工单不存在"),
    COMPLAINT_NOT_FOUND(40406, "投诉不存在"),

    // 通知 / 公告 / 上传
    NOTIFICATION_NOT_FOUND(40407, "通知不存在"),
    ANNOUNCEMENT_NOT_FOUND(40408, "公告不存在"),
    UPLOAD_NOT_FOUND(40409, "上传记录不存在"),
    EXPORT_TASK_NOT_FOUND(40410, "导出任务不存在"),

    // 消息模板
    TEMPLATE_NOT_FOUND(40411, "消息模板不存在"),

    // 字典 / 配置
    DICT_NOT_FOUND(40412, "字典项不存在"),
    CONFIG_NOT_FOUND(40413, "配置不存在"),

    // 导出
    EXPORT_NOT_READY(40910, "导出任务未完成，请稍后再试"),
    BILL_ALREADY_CLOSED(40901, "账期已关账"),
    BILL_ALREADY_PAID(40902, "账单已完成支付"),
    ANNOUNCEMENT_STATUS_ILLEGAL(40903, "公告状态不允许此操作"),

    // 投票
    POLL_NOT_FOUND(40414, "投票不存在"),
    POLL_STATUS_ILLEGAL(40904, "投票状态不允许此操作"),
    POLL_ALREADY_VOTED(40905, "已经投过票了"),
    POLL_EXPIRED(40906, "投票已截止"),

    // 活动
    ACTIVITY_NOT_FOUND(40415, "活动不存在"),
    ACTIVITY_STATUS_ILLEGAL(40907, "活动状态不允许此操作"),
    ACTIVITY_ALREADY_REGISTERED(40908, "已经报名了"),
    ACTIVITY_FULL(40909, "报名名额已满"),
    ACTIVITY_EXPIRED(40911, "报名已截止"),
    ACTIVITY_NOT_REGISTERED(40912, "未报名此活动"),

    // 访客预约
    VISITOR_NOT_FOUND(40416, "访客预约不存在"),
    VISITOR_STATUS_ILLEGAL(40913, "预约状态不允许此操作"),
    VISITOR_NOT_OWNER(40914, "只能操作自己的预约"),

    // 车位
    PARKING_NOT_FOUND(40417, "车位不存在"),
    PARKING_STATUS_ILLEGAL(40915, "车位状态不允许此操作"),
    PARKING_SPACE_DUPLICATE(40916, "同区域车位编号已存在"),

    // 设备设施
    FACILITY_NOT_FOUND(40418, "设备不存在"),
    FACILITY_STATUS_ILLEGAL(40917, "设备状态不允许此操作"),
    FACILITY_DUPLICATE(40918, "同类别设备名称已存在"),

    // 巡检
    PLAN_NOT_FOUND(40419, "巡检计划不存在"),
    TASK_NOT_FOUND(40420, "巡检任务不存在"),
    PLAN_STATUS_ILLEGAL(40919, "计划状态不允许此操作"),
    TASK_STATUS_ILLEGAL(40920, "任务状态不允许此操作"),
    PLAN_DUPLICATE(40921, "巡检计划名称已存在"),

    // 报修
    REPAIR_NOT_FOUND(40421, "报修单不存在"),
    REPAIR_STATUS_ILLEGAL(40922, "报修状态不允许此操作"),
    REPAIR_ALREADY_RATED(40923, "已经评价过了"),

    // 合同
    CONTRACT_NOT_FOUND(40422, "合同不存在"),
    CONTRACT_STATUS_ILLEGAL(40924, "合同状态不允许此操作"),
    CONTRACT_NO_DUPLICATE(40925, "合同编号已存在"),
    CONTRACT_END_DATE_ILLEGAL(40926, "续签到期日期必须晚于当前到期日期"),

    // 业务
    DATA_DUPLICATE(40004, "数据已存在"),
    STATUS_ILLEGAL(40005, "状态流转非法"),
    AMOUNT_ILLEGAL(40006, "金额不合法");

    private final int code;
    private final String message;
}
