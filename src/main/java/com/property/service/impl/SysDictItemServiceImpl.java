package com.property.service.impl;

import com.property.service.SysDictItemService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.SysDictItem;
import com.property.mapper.SysDictItemMapper;
import com.property.dto.request.SysDictItemDTO;
import com.property.dto.request.SysDictItemQueryDTO;
import com.property.dto.response.SysDictItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl implements SysDictItemService {

    private final SysDictItemMapper sysDictItemMapper;

    public PageResult<SysDictItemVO> page(SysDictItemQueryDTO query) {
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<SysDictItem>()
                .eq(StrUtil.isNotBlank(query.getType()), SysDictItem::getType, query.getType())
                .orderByAsc(SysDictItem::getType)
                .orderByAsc(SysDictItem::getSort);
        return PageResult.of(sysDictItemMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper
        ).convert(this::toVO));
    }

    public List<SysDictItemVO> listByType(String type) {
        return sysDictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getType, type)
                        .orderByAsc(SysDictItem::getSort)
        ).stream().map(this::toVO).toList();
    }

    @Transactional
    public SysDictItemVO create(SysDictItemDTO dto) {
        if (existsByTypeAndCode(dto.getType(), dto.getCode())) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE);
        }
        SysDictItem entity = new SysDictItem();
        entity.setType(dto.getType());
        entity.setCode(dto.getCode());
        entity.setLabel(dto.getLabel());
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        sysDictItemMapper.insert(entity);
        return toVO(entity);
    }

    @Transactional
    public SysDictItemVO update(String id, SysDictItemDTO dto) {
        SysDictItem entity = requireExist(id);
        if (dto.getLabel() != null) entity.setLabel(dto.getLabel());
        if (dto.getCode() != null) entity.setCode(dto.getCode());
        if (dto.getSort() != null) entity.setSort(dto.getSort());
        if (dto.getEnabled() != null) entity.setEnabled(dto.getEnabled());
        sysDictItemMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public void delete(String id) {
        requireExist(id);
        sysDictItemMapper.deleteById(id);
    }

    // ── private helpers ─────────────────────────────────────────────

    private SysDictItem requireExist(String id) {
        SysDictItem entity = sysDictItemMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.DICT_NOT_FOUND);
        return entity;
    }

    private boolean existsByTypeAndCode(String type, String code) {
        return sysDictItemMapper.exists(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getType, type)
                        .eq(SysDictItem::getCode, code));
    }

    private SysDictItemVO toVO(SysDictItem e) {
        SysDictItemVO vo = new SysDictItemVO();
        vo.setId(e.getId());
        vo.setType(e.getType());
        vo.setCode(e.getCode());
        vo.setLabel(e.getLabel());
        vo.setSort(e.getSort());
        vo.setEnabled(e.getEnabled());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
