package com.property.constant;

public final class RabbitMQConstants {

    private RabbitMQConstants() {}

    // ── Exchange ──────────────────────────────────────────
    public static final String EXCHANGE_BILL          = "property.bill.exchange";
    public static final String EXCHANGE_NOTIFICATION  = "property.notification.exchange";
    public static final String EXCHANGE_WORKORDER     = "property.workorder.exchange";
    public static final String EXCHANGE_REPORT        = "property.report.exchange";
    public static final String EXCHANGE_DLX           = "property.dlx";

    // ── Queue ─────────────────────────────────────────────
    public static final String QUEUE_BILL_GENERATE        = "property.bill.generate";
    public static final String QUEUE_BILL_OVERDUE         = "property.bill.overdue";
    public static final String QUEUE_NOTIFICATION_IN_APP  = "property.notification.in_app";
    public static final String QUEUE_NOTIFICATION_SMS     = "property.notification.sms";
    public static final String QUEUE_NOTIFICATION_PUSH    = "property.notification.push";
    public static final String QUEUE_NOTIFICATION_EMAIL   = "property.notification.email";
    public static final String QUEUE_WORKORDER_ASSIGNED   = "property.workorder.assigned";
    public static final String QUEUE_WORKORDER_COMPLETED  = "property.workorder.completed";
    public static final String QUEUE_REPORT_EXPORT        = "property.report.export";
    public static final String QUEUE_DLQ                  = "property.dlq";

    // ── Routing Key ───────────────────────────────────────
    public static final String RK_BILL_GENERATE       = "bill.generate";
    public static final String RK_BILL_OVERDUE        = "bill.overdue";
    public static final String RK_NOTIFICATION_IN_APP  = "notification.in_app";
    public static final String RK_NOTIFICATION_SMS    = "notification.sms";
    public static final String RK_NOTIFICATION_PUSH   = "notification.push";
    public static final String RK_NOTIFICATION_EMAIL  = "notification.email";
    public static final String RK_WORKORDER_ASSIGNED  = "workorder.assigned";
    public static final String RK_WORKORDER_COMPLETED = "workorder.completed";
    public static final String RK_REPORT_EXPORT       = "report.export";
    public static final String RK_DLQ                 = "dlq";
}
