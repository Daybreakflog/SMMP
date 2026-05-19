package com.property.service.impl;

import com.property.service.InspectionService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.InspectionPlan;
import com.property.entity.InspectionTask;
import com.property.mapper.InspectionPlanMapper;
import com.property.mapper.InspectionTaskMapper;
import com.property.dto.request.*;
import com.property.dto.response.InspectionPlanVO;
import com.property.dto.response.InspectionTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionServiceImpl implements InspectionService {

    private final InspectionPlanMapper planMapper;
    private final InspectionTaskMapper taskMapper;

    // ── Plan ────────────────────────────────────────────────────────

    public PageResult<InspectionPlanVO> planPage(InspectionPlanQueryDTO query) {
        LambdaQueryWrapper<InspectionPlan> wrapper = new LambdaQueryWrapper<InspectionPlan>()
                .eq(StrUtil.isNotBlank(query.getStatus()), InspectionPlan::getStatus, query.getStatus())
                .orderByDesc(InspectionPlan::getCreatedAt);
        Page<InspectionPlan> raw = planMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<InspectionPlanVO> vos = raw.getRecords().stream().map(this::toPlanVO).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    public InspectionPlanVO getPlanById(String id) {
        return toPlanVO(requirePlanExist(id));
    }

    @Transactional
    public InspectionPlanVO createPlan(InspectionPlanDTO dto) {
        checkPlanDuplicate(dto.getName(), null);
        InspectionPlan entity = new InspectionPlan();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setRoute(dto.getRoute());
        entity.setFrequency(dto.getFrequency());
        entity.setStatus("DRAFT");
        planMapper.insert(entity);
        return toPlanVO(entity);
    }

    @Transactional
    public InspectionPlanVO updatePlan(String id, InspectionPlanDTO dto) {
        InspectionPlan entity = requirePlanExist(id);
        String newName = dto.getName() != null ? dto.getName() : entity.getName();
        if (!newName.equals(entity.getName())) {
            checkPlanDuplicate(newName, id);
        }
        entity.setName(newName);
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getRoute() != null) entity.setRoute(dto.getRoute());
        if (dto.getFrequency() != null) entity.setFrequency(dto.getFrequency());
        planMapper.updateById(entity);
        return toPlanVO(entity);
    }

    @Transactional
    public void deletePlan(String id) {
        InspectionPlan entity = requirePlanExist(id);
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_ILLEGAL);
        }
        planMapper.deleteById(id);
    }

    @Transactional
    public InspectionPlanVO activatePlan(String id) {
        InspectionPlan entity = requirePlanExist(id);
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_ILLEGAL);
        }
        entity.setStatus("ACTIVE");
        planMapper.updateById(entity);
        return toPlanVO(entity);
    }

    @Transactional
    public InspectionPlanVO disablePlan(String id) {
        InspectionPlan entity = requirePlanExist(id);
        if (!"ACTIVE".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_ILLEGAL);
        }
        entity.setStatus("DISABLED");
        planMapper.updateById(entity);
        return toPlanVO(entity);
    }

    // ── Task ────────────────────────────────────────────────────────

    @Transactional
    public InspectionTaskVO createTask(String planId, InspectionTaskCreateDTO dto) {
        InspectionPlan plan = requirePlanExist(planId);
        if (!"ACTIVE".equals(plan.getStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_ILLEGAL);
        }
        InspectionTask task = new InspectionTask();
        task.setPlanId(planId);
        task.setAssigneeId(dto.getAssigneeId());
        task.setAssigneeName(dto.getAssigneeName());
        task.setScheduledAt(dto.getScheduledAt());
        task.setStatus("PENDING");
        taskMapper.insert(task);
        return toTaskVO(task);
    }

    public PageResult<InspectionTaskVO> taskPage(InspectionTaskQueryDTO query) {
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<InspectionTask>()
                .eq(StrUtil.isNotBlank(query.getStatus()), InspectionTask::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getAssigneeId()), InspectionTask::getAssigneeId, query.getAssigneeId())
                .orderByDesc(InspectionTask::getCreatedAt);
        Page<InspectionTask> raw = taskMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<InspectionTaskVO> vos = raw.getRecords().stream().map(this::toTaskVO).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    public InspectionTaskVO getTaskById(String id) {
        return toTaskVO(requireTaskExist(id));
    }

    @Transactional
    public InspectionTaskVO startTask(String id) {
        InspectionTask task = requireTaskExist(id);
        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.TASK_STATUS_ILLEGAL);
        }
        task.setStatus("IN_PROGRESS");
        task.setStartedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return toTaskVO(task);
    }

    @Transactional
    public InspectionTaskVO completeTask(String id, InspectionTaskCompleteDTO dto) {
        InspectionTask task = requireTaskExist(id);
        if (!"IN_PROGRESS".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.TASK_STATUS_ILLEGAL);
        }
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        task.setResult(dto.getResult());
        if (dto.getRemark() != null) task.setRemark(dto.getRemark());
        taskMapper.updateById(task);
        return toTaskVO(task);
    }

    // ── private helpers ─────────────────────────────────────────────

    private InspectionPlan requirePlanExist(String id) {
        InspectionPlan entity = planMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        return entity;
    }

    private InspectionTask requireTaskExist(String id) {
        InspectionTask entity = taskMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        return entity;
    }

    private void checkPlanDuplicate(String name, String excludeId) {
        LambdaQueryWrapper<InspectionPlan> wrapper = new LambdaQueryWrapper<InspectionPlan>()
                .eq(InspectionPlan::getName, name)
                .ne(excludeId != null, InspectionPlan::getId, excludeId);
        if (planMapper.exists(wrapper)) {
            throw new BusinessException(ErrorCode.PLAN_DUPLICATE);
        }
    }

    private InspectionPlanVO toPlanVO(InspectionPlan e) {
        InspectionPlanVO vo = new InspectionPlanVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setDescription(e.getDescription());
        vo.setRoute(e.getRoute());
        vo.setFrequency(e.getFrequency());
        vo.setStatus(e.getStatus());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }

    private InspectionTaskVO toTaskVO(InspectionTask e) {
        InspectionTaskVO vo = new InspectionTaskVO();
        vo.setId(e.getId());
        vo.setPlanId(e.getPlanId());
        vo.setAssigneeId(e.getAssigneeId());
        vo.setAssigneeName(e.getAssigneeName());
        vo.setStatus(e.getStatus());
        vo.setScheduledAt(e.getScheduledAt());
        vo.setStartedAt(e.getStartedAt());
        vo.setCompletedAt(e.getCompletedAt());
        vo.setResult(e.getResult());
        vo.setRemark(e.getRemark());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
