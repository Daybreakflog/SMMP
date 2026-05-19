package com.property.common.api;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 自动将 Controller 返回值包装进 ApiEnvelope。
 * 排除：已是 ApiEnvelope、actuator、springdoc/knife4j 路径。
 * String 返回类型跳过（StringHttpMessageConverter 不兼容对象替换）。
 */
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        // ResponseEntity 自带完整状态码和 body，不做二次包装（如 logout 返回 204 No Content）
        return !returnType.getParameterType().equals(String.class)
                && !ResponseEntity.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof ApiEnvelope) {
            return body;
        }
        String path = request.getURI().getPath();
        if (isExcludedPath(path)) {
            return body;
        }
        return ApiEnvelope.ok(body);
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/actuator")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars")
                || path.startsWith("/doc.html")
                || path.startsWith("/favicon");
    }
}
