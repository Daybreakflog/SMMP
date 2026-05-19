package com.property.aop;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.mapper.OpLogMapper;
import com.property.annotation.OpLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OpLogAspect {

    private static final int MAX_BODY_LEN = 2000;

    private final OpLogMapper opLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OpLog opLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long durationMs = System.currentTimeMillis() - start;

        try {
            save(opLog, pjp.getArgs(), result, durationMs);
        } catch (Exception e) {
            log.warn("[OpLog] 写入操作日志失败: {}", e.getMessage());
        }
        return result;
    }

    private void save(OpLog opLog, Object[] args, Object result, long durationMs) {
        HttpServletRequest req = getRequest();

        String actorId = null;
        String actorName = null;
        try {
            if (StpUtil.isLogin()) {
                actorId = StpUtil.getLoginIdAsString();
                Object extra = StpUtil.getExtra("name");
                actorName = extra != null ? extra.toString() : actorId;
            }
        } catch (Exception ignored) {
        }

        String requestBody = truncate(toJson(args));
        String responseBody = truncate(toJson(result));

        Map<String, Object> diff = new LinkedHashMap<>();
        diff.put("requestBody", requestBody);
        diff.put("responseBody", responseBody);
        diff.put("durationMs", durationMs);

        com.property.entity.OpLog entity = new com.property.entity.OpLog();
        entity.setActorId(actorId);
        entity.setActorName(actorName);
        entity.setAction(opLog.action());
        entity.setTarget(opLog.module());
        entity.setDiff(toJson(diff));
        entity.setIp(resolveIp(req));
        entity.setUserAgent(req != null ? req.getHeader("User-Agent") : null);
        entity.setAt(LocalDateTime.now());

        opLogMapper.insert(entity);
    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveIp(HttpServletRequest req) {
        if (req == null) return null;
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        // X-Forwarded-For 可能是逗号分隔列表，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > MAX_BODY_LEN ? s.substring(0, MAX_BODY_LEN) : s;
    }
}
