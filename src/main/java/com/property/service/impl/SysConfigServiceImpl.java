package com.property.service.impl;

import com.property.service.SysConfigService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.SysConfig;
import com.property.mapper.SysConfigMapper;
import com.property.dto.request.SysConfigDTO;
import com.property.dto.request.SysConfigQueryDTO;
import com.property.dto.response.SysConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    public PageResult<SysConfigVO> page(SysConfigQueryDTO query) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .likeRight(StrUtil.isNotBlank(query.getGroupCode()), SysConfig::getKey, query.getGroupCode())
                .and(StrUtil.isNotBlank(query.getKeyword()), w -> w
                        .like(SysConfig::getKey, query.getKeyword())
                        .or().like(SysConfig::getValue, query.getKeyword())
                        .or().like(SysConfig::getDescription, query.getKeyword()))
                .orderByDesc(SysConfig::getCreatedAt);
        return PageResult.of(sysConfigMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper
        ).convert(this::toVO));
    }

    public SysConfigVO getByKey(String key) {
        return toVO(requireExistByKey(key));
    }

    @Transactional
    public SysConfigVO create(SysConfigDTO dto) {
        if (existsByKey(dto.getKey())) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE);
        }
        SysConfig entity = new SysConfig();
        entity.setKey(dto.getKey());
        entity.setValue(dto.getValue());
        entity.setDescription(dto.getDescription());
        sysConfigMapper.insert(entity);
        return toVO(entity);
    }

    @Transactional
    public SysConfigVO update(String key, SysConfigDTO dto) {
        SysConfig entity = requireExistByKey(key);
        if (dto.getValue() != null) entity.setValue(dto.getValue());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        sysConfigMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public void delete(String key) {
        SysConfig entity = requireExistByKey(key);
        sysConfigMapper.deleteById(entity.getId());
    }

    // ── private helpers ─────────────────────────────────────────────

    private SysConfig requireExistByKey(String key) {
        SysConfig entity = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getKey, key));
        if (entity == null) throw new BusinessException(ErrorCode.CONFIG_NOT_FOUND);
        return entity;
    }

    private boolean existsByKey(String key) {
        return sysConfigMapper.exists(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getKey, key));
    }

    private SysConfigVO toVO(SysConfig e) {
        SysConfigVO vo = new SysConfigVO();
        vo.setId(e.getId());
        vo.setKey(e.getKey());
        vo.setValue(e.getValue());
        vo.setDescription(e.getDescription());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
