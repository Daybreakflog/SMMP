package com.property.service.impl;

import com.property.service.ConfigService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.SysConfig;
import com.property.mapper.SysConfigMapper;
import com.property.dto.request.ConfigDTO;
import com.property.dto.response.ConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SysConfigMapper configMapper;

    public PageResult<ConfigVO> page(int pageNum, int pageSize) {
        return PageResult.of(configMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysConfig>().orderByAsc(SysConfig::getKey)
        ).convert(this::toVO));
    }

    @Transactional
    public ConfigVO create(ConfigDTO dto) {
        checkDuplicate(dto.getKey(), null);
        SysConfig config = new SysConfig();
        applyDTO(config, dto);
        configMapper.insert(config);
        return toVO(config);
    }

    @Transactional
    public ConfigVO update(String id, ConfigDTO dto) {
        SysConfig config = requireExist(id);
        String newKey = StrUtil.isNotBlank(dto.getKey()) ? dto.getKey() : config.getKey();
        if (!newKey.equals(config.getKey())) {
            checkDuplicate(newKey, id);
        }
        applyDTO(config, dto);
        configMapper.updateById(config);
        return toVO(config);
    }

    @Transactional
    public void delete(String id) {
        requireExist(id);
        configMapper.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void checkDuplicate(String key, String excludeId) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getKey, key);
        if (excludeId != null) wrapper.ne(SysConfig::getId, excludeId);
        if (configMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE);
        }
    }

    private SysConfig requireExist(String id) {
        SysConfig config = configMapper.selectById(id);
        if (config == null) throw new BusinessException(ErrorCode.CONFIG_NOT_FOUND);
        return config;
    }

    private void applyDTO(SysConfig config, ConfigDTO dto) {
        if (dto.getKey() != null) config.setKey(dto.getKey());
        if (dto.getValue() != null) config.setValue(dto.getValue());
        if (dto.getDescription() != null) config.setDescription(dto.getDescription());
    }

    private ConfigVO toVO(SysConfig config) {
        ConfigVO vo = new ConfigVO();
        vo.setId(config.getId());
        vo.setKey(config.getKey());
        vo.setValue(config.getValue());
        vo.setDescription(config.getDescription());
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }
}
