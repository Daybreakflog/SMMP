package com.property.service.impl;

import com.property.job.ExportProcessor;

import com.property.service.ExportService;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.ExportTask;
import com.property.mapper.ExportTaskMapper;
import com.property.dto.request.ExportRequestDTO;
import com.property.dto.response.ExportTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ExportTaskMapper exportTaskMapper;
    private final ExportProcessor exportProcessor;
    private final ObjectMapper objectMapper;

    public ExportTaskVO create(ExportRequestDTO dto) {
        ExportTask task = new ExportTask();
        task.setType(dto.getType());
        task.setStatus("PENDING");
        task.setOperatorId(currentUserId());
        if (dto.getParams() != null) {
            try {
                task.setParamsJson(objectMapper.writeValueAsString(dto.getParams()));
            } catch (JsonProcessingException e) {
                task.setParamsJson("{}");
            }
        }
        exportTaskMapper.insert(task);
        exportProcessor.process(task.getId());
        return toVO(task);
    }

    public ExportTaskVO getById(String id) {
        ExportTask task = requireExist(id);
        return toVO(task);
    }

    public String getDownloadUrl(String id) {
        ExportTask task = requireExist(id);
        if (!"DONE".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.EXPORT_NOT_READY);
        }
        return task.getFileUrl();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private ExportTask requireExist(String id) {
        ExportTask task = exportTaskMapper.selectById(id);
        if (task == null) throw new BusinessException(ErrorCode.EXPORT_TASK_NOT_FOUND);
        return task;
    }

    private String currentUserId() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private ExportTaskVO toVO(ExportTask task) {
        ExportTaskVO vo = new ExportTaskVO();
        vo.setId(task.getId());
        vo.setType(task.getType());
        vo.setStatus(task.getStatus());
        vo.setFileUrl(task.getFileUrl());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setFinishedAt(task.getFinishedAt());
        return vo;
    }
}
