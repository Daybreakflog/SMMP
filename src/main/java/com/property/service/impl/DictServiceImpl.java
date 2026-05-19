package com.property.service.impl;

import com.property.service.DictService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.SysDictItem;
import com.property.mapper.SysDictItemMapper;
import com.property.dto.request.DictItemDTO;
import com.property.dto.response.DictItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictItemMapper dictItemMapper;

    public Map<String, List<DictItemVO>> listGrouped() {
        List<SysDictItem> all = dictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>().orderByAsc(SysDictItem::getType, SysDictItem::getSort)
        );
        return all.stream()
                .collect(Collectors.groupingBy(
                        SysDictItem::getType,
                        Collectors.mapping(this::toVO, Collectors.toList())
                ));
    }

    @Transactional
    public DictItemVO create(DictItemDTO dto) {
        checkDuplicate(dto.getType(), dto.getCode(), null);
        SysDictItem item = new SysDictItem();
        applyDTO(item, dto);
        if (item.getEnabled() == null) item.setEnabled(true);
        dictItemMapper.insert(item);
        return toVO(item);
    }

    @Transactional
    public DictItemVO update(String id, DictItemDTO dto) {
        SysDictItem item = requireExist(id);
        String newType = dto.getType() != null ? dto.getType() : item.getType();
        String newCode = dto.getCode() != null ? dto.getCode() : item.getCode();
        if (!newType.equals(item.getType()) || !newCode.equals(item.getCode())) {
            checkDuplicate(newType, newCode, id);
        }
        applyDTO(item, dto);
        dictItemMapper.updateById(item);
        return toVO(item);
    }

    @Transactional
    public void delete(String id) {
        requireExist(id);
        dictItemMapper.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void checkDuplicate(String type, String code, String excludeId) {
        LambdaQueryWrapper<SysDictItem> wrapper = new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getType, type)
                .eq(SysDictItem::getCode, code);
        if (excludeId != null) wrapper.ne(SysDictItem::getId, excludeId);
        if (dictItemMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.DATA_DUPLICATE);
        }
    }

    private SysDictItem requireExist(String id) {
        SysDictItem item = dictItemMapper.selectById(id);
        if (item == null) throw new BusinessException(ErrorCode.DICT_NOT_FOUND);
        return item;
    }

    private void applyDTO(SysDictItem item, DictItemDTO dto) {
        if (dto.getType() != null) item.setType(dto.getType());
        if (dto.getCode() != null) item.setCode(dto.getCode());
        if (dto.getLabel() != null) item.setLabel(dto.getLabel());
        if (dto.getSort() != null) item.setSort(dto.getSort());
        if (dto.getEnabled() != null) item.setEnabled(dto.getEnabled());
    }

    private DictItemVO toVO(SysDictItem item) {
        DictItemVO vo = new DictItemVO();
        vo.setId(item.getId());
        vo.setType(item.getType());
        vo.setCode(item.getCode());
        vo.setLabel(item.getLabel());
        vo.setSort(item.getSort());
        vo.setEnabled(item.getEnabled());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setUpdatedAt(item.getUpdatedAt());
        return vo;
    }
}
