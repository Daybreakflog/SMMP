package com.property.service.impl;

import com.property.service.AnnouncementService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.Announcement;
import com.property.mapper.AnnouncementMapper;
import com.property.dto.request.AnnouncementDTO;
import com.property.dto.request.AnnouncementQueryDTO;
import com.property.dto.response.AnnouncementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    public PageResult<AnnouncementVO> page(AnnouncementQueryDTO query) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(StrUtil.isNotBlank(query.getStatus()), Announcement::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getType()), Announcement::getType, query.getType())
                .like(StrUtil.isNotBlank(query.getKeyword()), Announcement::getTitle, query.getKeyword())
                .orderByDesc(Announcement::getPinned)
                .orderByDesc(Announcement::getCreatedAt);
        return PageResult.of(announcementMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper
        ).convert(this::toVO));
    }

    public List<AnnouncementVO> active() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, "PUBLISHED")
                .orderByDesc(Announcement::getPublishedAt)
                .last("LIMIT 20");
        return announcementMapper.selectList(wrapper)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementVO create(AnnouncementDTO dto) {
        Announcement ann = new Announcement();
        applyDTO(ann, dto);
        ann.setAuthorId(StpUtil.getLoginIdAsString());
        ann.setStatus("DRAFT");
        announcementMapper.insert(ann);
        return toVO(ann);
    }

    public AnnouncementVO getById(String id) {
        return toVO(requireExist(id));
    }

    @Transactional
    public AnnouncementVO patch(String id, AnnouncementDTO dto) {
        Announcement ann = requireExist(id);
        if (!"DRAFT".equals(ann.getStatus())) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_STATUS_ILLEGAL);
        }
        applyDTO(ann, dto);
        announcementMapper.updateById(ann);
        return toVO(ann);
    }

    @Transactional
    public AnnouncementVO publish(String id) {
        Announcement ann = requireExist(id);
        if (!"DRAFT".equals(ann.getStatus())) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_STATUS_ILLEGAL);
        }
        ann.setStatus("PUBLISHED");
        ann.setPublishedAt(LocalDateTime.now());
        announcementMapper.updateById(ann);
        return toVO(ann);
    }

    @Transactional
    public AnnouncementVO revoke(String id) {
        Announcement ann = requireExist(id);
        if (!"PUBLISHED".equals(ann.getStatus())) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_STATUS_ILLEGAL);
        }
        ann.setStatus("REVOKED");
        announcementMapper.updateById(ann);
        return toVO(ann);
    }

    @Transactional
    public void delete(String id) {
        Announcement ann = requireExist(id);
        if ("PUBLISHED".equals(ann.getStatus())) {
            throw new BusinessException(ErrorCode.ANNOUNCEMENT_STATUS_ILLEGAL);
        }
        announcementMapper.deleteById(ann.getId());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Announcement requireExist(String id) {
        Announcement ann = announcementMapper.selectById(id);
        if (ann == null) throw new BusinessException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
        return ann;
    }

    private void applyDTO(Announcement ann, AnnouncementDTO dto) {
        if (dto.getTitle() != null) ann.setTitle(dto.getTitle());
        if (dto.getContent() != null) ann.setContent(dto.getContent());
        if (dto.getType() != null) ann.setType(dto.getType());
        if (dto.getProjectId() != null) ann.setProjectId(dto.getProjectId());
        if (dto.getPinned() != null) ann.setPinned(dto.getPinned());
        if (dto.getExpiredAt() != null) ann.setExpiredAt(dto.getExpiredAt());
    }

    private AnnouncementVO toVO(Announcement ann) {
        AnnouncementVO vo = new AnnouncementVO();
        vo.setId(ann.getId());
        vo.setTitle(ann.getTitle());
        vo.setContent(ann.getContent());
        vo.setType(ann.getType());
        vo.setStatus(ann.getStatus());
        vo.setAuthorId(ann.getAuthorId());
        vo.setProjectId(ann.getProjectId());
        vo.setPinned(ann.getPinned());
        vo.setPublishedAt(ann.getPublishedAt());
        vo.setExpiredAt(ann.getExpiredAt());
        vo.setCreatedAt(ann.getCreatedAt());
        vo.setUpdatedAt(ann.getUpdatedAt());
        return vo;
    }
}
