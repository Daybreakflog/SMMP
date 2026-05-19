package com.property.service.impl;

import com.property.service.FeeItemService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.FeeItem;
import com.property.mapper.FeeItemMapper;
import com.property.dto.request.FeeItemDTO;
import com.property.dto.response.FeeItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeItemServiceImpl implements FeeItemService {

    private final FeeItemMapper feeItemMapper;

    public List<FeeItemVO> list() {
        return feeItemMapper.selectList(
                        new LambdaQueryWrapper<FeeItem>()
                                .eq(FeeItem::getStatus, "ACTIVE")
                                .orderByAsc(FeeItem::getName))
                .stream().map(this::toVO).toList();
    }

    public FeeItemVO create(FeeItemDTO dto) {
        FeeItem item = new FeeItem();
        applyDTO(item, dto);
        if (item.getStatus() == null) item.setStatus("ACTIVE");
        feeItemMapper.insert(item);
        return toVO(item);
    }

    public FeeItemVO patch(String id, FeeItemDTO dto) {
        FeeItem item = feeItemMapper.selectById(id);
        if (item == null) throw new BusinessException(ErrorCode.FEE_ITEM_NOT_FOUND);
        applyDTO(item, dto);
        feeItemMapper.updateById(item);
        return toVO(item);
    }

    public void delete(String id) {
        FeeItem item = feeItemMapper.selectById(id);
        if (item == null) throw new BusinessException(ErrorCode.FEE_ITEM_NOT_FOUND);
        feeItemMapper.deleteById(id);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void applyDTO(FeeItem item, FeeItemDTO dto) {
        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getType() != null) item.setType(dto.getType());
        if (dto.getFixedAmount() != null) item.setFixedAmount(dto.getFixedAmount());
        if (dto.getUnitPrice() != null) item.setUnitPrice(dto.getUnitPrice());
        if (dto.getProjectId() != null) item.setProjectId(dto.getProjectId());
        if (dto.getStatus() != null) item.setStatus(dto.getStatus());
    }

    private FeeItemVO toVO(FeeItem f) {
        FeeItemVO vo = new FeeItemVO();
        vo.setId(f.getId());
        vo.setName(f.getName());
        vo.setType(f.getType());
        vo.setFixedAmount(f.getFixedAmount());
        vo.setUnitPrice(f.getUnitPrice());
        vo.setProjectId(f.getProjectId());
        vo.setStatus(f.getStatus());
        vo.setCreatedAt(f.getCreatedAt());
        return vo;
    }
}
