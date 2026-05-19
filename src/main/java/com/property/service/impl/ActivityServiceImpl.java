package com.property.service.impl;

import com.property.service.ActivityService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.Activity;
import com.property.entity.ActivityRegistration;
import com.property.mapper.ActivityMapper;
import com.property.mapper.ActivityRegistrationMapper;
import com.property.dto.request.ActivityDTO;
import com.property.dto.request.ActivityQueryDTO;
import com.property.dto.response.ActivityVO;
import com.property.dto.response.ParticipantVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityRegistrationMapper registrationMapper;

    public PageResult<ActivityVO> page(ActivityQueryDTO query) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(StrUtil.isNotBlank(query.getStatus()), Activity::getStatus, query.getStatus())
                .orderByDesc(Activity::getCreatedAt);
        Page<Activity> raw = activityMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<ActivityVO> vos = raw.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    public ActivityVO getById(String id) {
        return toVO(requireExist(id));
    }

    @Transactional
    public ActivityVO create(ActivityDTO dto) {
        Activity a = new Activity();
        applyDTO(a, dto);
        a.setAuthorId(StpUtil.getLoginIdAsString());
        a.setStatus("DRAFT");
        activityMapper.insert(a);
        return toVO(a);
    }

    @Transactional
    public ActivityVO update(String id, ActivityDTO dto) {
        Activity a = requireExist(id);
        if (!"DRAFT".equals(a.getStatus())) {
            throw new BusinessException(ErrorCode.ACTIVITY_STATUS_ILLEGAL);
        }
        applyDTO(a, dto);
        activityMapper.updateById(a);
        return toVO(a);
    }

    @Transactional
    public ActivityVO publish(String id) {
        Activity a = requireExist(id);
        if (!"DRAFT".equals(a.getStatus())) {
            throw new BusinessException(ErrorCode.ACTIVITY_STATUS_ILLEGAL);
        }
        a.setStatus("PUBLISHED");
        activityMapper.updateById(a);
        return toVO(a);
    }

    @Transactional
    public ActivityVO close(String id) {
        Activity a = requireExist(id);
        if (!"PUBLISHED".equals(a.getStatus())) {
            throw new BusinessException(ErrorCode.ACTIVITY_STATUS_ILLEGAL);
        }
        a.setStatus("CLOSED");
        activityMapper.updateById(a);
        return toVO(a);
    }

    @Transactional
    public void delete(String id) {
        Activity a = requireExist(id);
        if (!"DRAFT".equals(a.getStatus())) {
            throw new BusinessException(ErrorCode.ACTIVITY_STATUS_ILLEGAL);
        }
        activityMapper.deleteById(a.getId());
    }

    @Transactional
    public void register(String id) {
        Activity a = requireExist(id);
        if (!"PUBLISHED".equals(a.getStatus())) {
            throw new BusinessException(ErrorCode.ACTIVITY_STATUS_ILLEGAL);
        }
        if (a.getRegisterDeadline() != null && LocalDateTime.now().isAfter(a.getRegisterDeadline())) {
            throw new BusinessException(ErrorCode.ACTIVITY_EXPIRED);
        }

        String userId = StpUtil.getLoginIdAsString();
        boolean already = registrationMapper.exists(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, id)
                        .eq(ActivityRegistration::getUserId, userId));
        if (already) {
            throw new BusinessException(ErrorCode.ACTIVITY_ALREADY_REGISTERED);
        }

        if (a.getMaxParticipants() != null && a.getMaxParticipants() > 0) {
            long count = registrationMapper.selectCount(
                    new LambdaQueryWrapper<ActivityRegistration>()
                            .eq(ActivityRegistration::getActivityId, id));
            if (count >= a.getMaxParticipants()) {
                throw new BusinessException(ErrorCode.ACTIVITY_FULL);
            }
        }

        ActivityRegistration reg = new ActivityRegistration();
        reg.setActivityId(id);
        reg.setUserId(userId);
        registrationMapper.insert(reg);
    }

    @Transactional
    public void cancelRegister(String id) {
        requireExist(id);
        String userId = StpUtil.getLoginIdAsString();
        ActivityRegistration reg = registrationMapper.selectOne(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, id)
                        .eq(ActivityRegistration::getUserId, userId));
        if (reg == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_REGISTERED);
        }
        registrationMapper.deleteById(reg.getId());
    }

    public PageResult<ParticipantVO> participants(String id, long page, long pageSize) {
        requireExist(id);
        Page<ActivityRegistration> raw = registrationMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, id)
                        .orderByDesc(ActivityRegistration::getCreatedAt));
        List<ParticipantVO> vos = raw.getRecords().stream().map(r -> {
            ParticipantVO vo = new ParticipantVO();
            vo.setUserId(r.getUserId());
            vo.setRegisteredAt(r.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Activity requireExist(String id) {
        Activity a = activityMapper.selectById(id);
        if (a == null) throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        return a;
    }

    private void applyDTO(Activity a, ActivityDTO dto) {
        if (dto.getTitle() != null) a.setTitle(dto.getTitle());
        if (dto.getDescription() != null) a.setDescription(dto.getDescription());
        if (dto.getLocation() != null) a.setLocation(dto.getLocation());
        if (dto.getMaxParticipants() != null) a.setMaxParticipants(dto.getMaxParticipants());
        if (dto.getRegisterDeadline() != null) a.setRegisterDeadline(dto.getRegisterDeadline());
        if (dto.getActivityStartAt() != null) a.setActivityStartAt(dto.getActivityStartAt());
        if (dto.getActivityEndAt() != null) a.setActivityEndAt(dto.getActivityEndAt());
    }

    private ActivityVO toVO(Activity a) {
        ActivityVO vo = new ActivityVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setDescription(a.getDescription());
        vo.setLocation(a.getLocation());
        vo.setStatus(a.getStatus());
        vo.setAuthorId(a.getAuthorId());
        vo.setMaxParticipants(a.getMaxParticipants());
        vo.setRegisterDeadline(a.getRegisterDeadline());
        vo.setActivityStartAt(a.getActivityStartAt());
        vo.setActivityEndAt(a.getActivityEndAt());
        vo.setCreatedAt(a.getCreatedAt());
        vo.setUpdatedAt(a.getUpdatedAt());
        vo.setRegisteredCount(Math.toIntExact(registrationMapper.selectCount(
                new LambdaQueryWrapper<ActivityRegistration>()
                        .eq(ActivityRegistration::getActivityId, a.getId()))));
        return vo;
    }
}
