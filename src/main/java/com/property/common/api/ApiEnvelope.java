package com.property.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.slf4j.MDC;

/**
 * 统一响应包装体。Controller 无需手动调用，由 ApiResponseAdvice 自动包装。
 * 异常场景由 GlobalExceptionHandler 调用 fail() 手动包装。
 */
@Getter
@Schema(description = "统一响应体")
public class ApiEnvelope<T> {

    @Schema(description = "业务状态码，0 表示成功")
    private int code;

    @Schema(description = "描述信息")
    private String message;

    @Schema(description = "数据载荷")
    private T data;

    @Schema(description = "链路追踪 ID，与请求日志 MDC 一致")
    private String traceId;

    private ApiEnvelope() {}

    public static <T> ApiEnvelope<T> ok(T data) {
        ApiEnvelope<T> env = new ApiEnvelope<>();
        env.code = 0;
        env.message = "success";
        env.data = data;
        env.traceId = MDC.get("traceId");
        return env;
    }

    public static <T> ApiEnvelope<T> fail(int code, String message) {
        ApiEnvelope<T> env = new ApiEnvelope<>();
        env.code = code;
        env.message = message;
        env.traceId = MDC.get("traceId");
        return env;
    }
}
