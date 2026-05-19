package com.property.service.impl;

import com.property.service.TemplateService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.MessageTemplate;
import com.property.mapper.MessageTemplateMapper;
import com.property.dto.request.TemplateDTO;
import com.property.dto.request.TemplateQueryDTO;
import com.property.dto.response.TemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final MessageTemplateMapper templateMapper;

    public PageResult<TemplateVO> page(TemplateQueryDTO query) {
        LambdaQueryWrapper<MessageTemplate> wrapper = new LambdaQueryWrapper<MessageTemplate>()
                .eq(StrUtil.isNotBlank(query.getType()), MessageTemplate::getType, query.getType())
                .like(StrUtil.isNotBlank(query.getName()), MessageTemplate::getName, query.getName())
                .eq(StrUtil.isNotBlank(query.getCode()), MessageTemplate::getCode, query.getCode())
                .orderByDesc(MessageTemplate::getCreatedAt);
        return PageResult.of(templateMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper
        ).convert(this::toVO));
    }

    @Transactional
    public TemplateVO create(TemplateDTO dto) {
        long count = templateMapper.selectCount(
                new LambdaQueryWrapper<MessageTemplate>()
                        .eq(MessageTemplate::getCode, dto.getCode())
        );
        if (count > 0) throw new BusinessException(ErrorCode.DATA_DUPLICATE);

        MessageTemplate tpl = new MessageTemplate();
        applyDTO(tpl, dto);
        templateMapper.insert(tpl);
        return toVO(tpl);
    }

    @Transactional
    public TemplateVO update(String id, TemplateDTO dto) {
        MessageTemplate tpl = requireExist(id);
        if (!tpl.getCode().equals(dto.getCode())) {
            long count = templateMapper.selectCount(
                    new LambdaQueryWrapper<MessageTemplate>()
                            .eq(MessageTemplate::getCode, dto.getCode())
            );
            if (count > 0) throw new BusinessException(ErrorCode.DATA_DUPLICATE);
        }
        applyDTO(tpl, dto);
        templateMapper.updateById(tpl);
        return toVO(tpl);
    }

    @Transactional
    public void delete(String id) {
        requireExist(id);
        templateMapper.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MessageTemplate requireExist(String id) {
        MessageTemplate tpl = templateMapper.selectById(id);
        if (tpl == null) throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        return tpl;
    }

    private void applyDTO(MessageTemplate tpl, TemplateDTO dto) {
        if (dto.getCode() != null) tpl.setCode(dto.getCode());
        if (dto.getType() != null) tpl.setType(dto.getType());
        if (dto.getName() != null) tpl.setName(dto.getName());
        if (dto.getTitle() != null) tpl.setTitle(dto.getTitle());
        if (dto.getContent() != null) tpl.setContent(dto.getContent());
        if (dto.getParamsJson() != null) tpl.setParamsJson(dto.getParamsJson());
        if (dto.getEnabled() != null) tpl.setEnabled(dto.getEnabled());
    }

    private TemplateVO toVO(MessageTemplate tpl) {
        TemplateVO vo = new TemplateVO();
        vo.setId(tpl.getId());
        vo.setCode(tpl.getCode());
        vo.setType(tpl.getType());
        vo.setName(tpl.getName());
        vo.setTitle(tpl.getTitle());
        vo.setContent(tpl.getContent());
        vo.setParamsJson(tpl.getParamsJson());
        vo.setEnabled(tpl.getEnabled());
        vo.setCreatedAt(tpl.getCreatedAt());
        vo.setUpdatedAt(tpl.getUpdatedAt());
        return vo;
    }
}
