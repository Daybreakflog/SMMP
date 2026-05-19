package com.property.service.impl;

import com.property.service.FacilityService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.Facility;
import com.property.entity.FacilityMaintenanceRecord;
import com.property.mapper.FacilityMaintenanceRecordMapper;
import com.property.mapper.FacilityMapper;
import com.property.dto.request.FacilityDTO;
import com.property.dto.request.FacilityMaintenanceRecordDTO;
import com.property.dto.request.FacilityQueryDTO;
import com.property.dto.request.MaintenanceRecordQueryDTO;
import com.property.dto.response.FacilityMaintenanceRecordVO;
import com.property.dto.response.FacilityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityMapper facilityMapper;
    private final FacilityMaintenanceRecordMapper recordMapper;

    public PageResult<FacilityVO> page(FacilityQueryDTO query) {
        LambdaQueryWrapper<Facility> wrapper = new LambdaQueryWrapper<Facility>()
                .eq(StrUtil.isNotBlank(query.getStatus()), Facility::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getCategory()), Facility::getCategory, query.getCategory())
                .orderByDesc(Facility::getCreatedAt);
        Page<Facility> raw = facilityMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<FacilityVO> vos = raw.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    public FacilityVO getById(String id) {
        return toVO(requireExist(id));
    }

    @Transactional
    public FacilityVO create(FacilityDTO dto) {
        checkDuplicate(dto.getCategory(), dto.getName(), null);
        Facility entity = new Facility();
        entity.setName(dto.getName());
        entity.setCategory(dto.getCategory());
        entity.setLocation(dto.getLocation());
        entity.setStatus("NORMAL");
        entity.setInstallDate(dto.getInstallDate());
        entity.setRemark(dto.getRemark());
        facilityMapper.insert(entity);
        return toVO(entity);
    }

    @Transactional
    public FacilityVO update(String id, FacilityDTO dto) {
        Facility entity = requireExist(id);
        String newCategory = dto.getCategory() != null ? dto.getCategory() : entity.getCategory();
        String newName = dto.getName() != null ? dto.getName() : entity.getName();
        if (!newCategory.equals(entity.getCategory()) || !newName.equals(entity.getName())) {
            checkDuplicate(newCategory, newName, id);
        }
        entity.setName(newName);
        entity.setCategory(newCategory);
        if (dto.getLocation() != null) entity.setLocation(dto.getLocation());
        if (dto.getInstallDate() != null) entity.setInstallDate(dto.getInstallDate());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        facilityMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public void delete(String id) {
        Facility entity = requireExist(id);
        if (!"NORMAL".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.FACILITY_STATUS_ILLEGAL);
        }
        facilityMapper.deleteById(id);
    }

    @Transactional
    public FacilityVO maintenance(String id) {
        Facility entity = requireExist(id);
        if (!"NORMAL".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.FACILITY_STATUS_ILLEGAL);
        }
        entity.setStatus("MAINTENANCE");
        facilityMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public FacilityVO restore(String id) {
        Facility entity = requireExist(id);
        if (!"MAINTENANCE".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.FACILITY_STATUS_ILLEGAL);
        }
        entity.setStatus("NORMAL");
        facilityMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public FacilityVO scrap(String id) {
        Facility entity = requireExist(id);
        if ("SCRAPPED".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.FACILITY_STATUS_ILLEGAL);
        }
        entity.setStatus("SCRAPPED");
        facilityMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public FacilityMaintenanceRecordVO addRecord(String facilityId, FacilityMaintenanceRecordDTO dto) {
        Facility facility = requireExist(facilityId);

        FacilityMaintenanceRecord record = new FacilityMaintenanceRecord();
        record.setFacilityId(facilityId);
        record.setType(dto.getType());
        record.setDescription(dto.getDescription());
        record.setMaintainedBy(dto.getMaintainedBy());
        record.setMaintainedAt(dto.getMaintainedAt() != null ? dto.getMaintainedAt() : LocalDateTime.now());
        record.setCost(dto.getCost());
        record.setRemark(dto.getRemark());
        recordMapper.insert(record);

        facility.setLastMaintenanceAt(record.getMaintainedAt());
        facilityMapper.updateById(facility);

        return toRecordVO(record);
    }

    public PageResult<FacilityMaintenanceRecordVO> recordPage(String facilityId, MaintenanceRecordQueryDTO query) {
        requireExist(facilityId);
        LambdaQueryWrapper<FacilityMaintenanceRecord> wrapper = new LambdaQueryWrapper<FacilityMaintenanceRecord>()
                .eq(FacilityMaintenanceRecord::getFacilityId, facilityId)
                .orderByDesc(FacilityMaintenanceRecord::getMaintainedAt);
        Page<FacilityMaintenanceRecord> raw = recordMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<FacilityMaintenanceRecordVO> vos = raw.getRecords().stream()
                .map(this::toRecordVO).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    // ── private helpers ─────────────────────────────────────────────

    private Facility requireExist(String id) {
        Facility entity = facilityMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.FACILITY_NOT_FOUND);
        return entity;
    }

    private void checkDuplicate(String category, String name, String excludeId) {
        LambdaQueryWrapper<Facility> wrapper = new LambdaQueryWrapper<Facility>()
                .eq(Facility::getCategory, category)
                .eq(Facility::getName, name)
                .ne(excludeId != null, Facility::getId, excludeId);
        if (facilityMapper.exists(wrapper)) {
            throw new BusinessException(ErrorCode.FACILITY_DUPLICATE);
        }
    }

    private FacilityVO toVO(Facility e) {
        FacilityVO vo = new FacilityVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setCategory(e.getCategory());
        vo.setLocation(e.getLocation());
        vo.setStatus(e.getStatus());
        vo.setInstallDate(e.getInstallDate());
        vo.setLastMaintenanceAt(e.getLastMaintenanceAt());
        vo.setRemark(e.getRemark());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }

    private FacilityMaintenanceRecordVO toRecordVO(FacilityMaintenanceRecord e) {
        FacilityMaintenanceRecordVO vo = new FacilityMaintenanceRecordVO();
        vo.setId(e.getId());
        vo.setFacilityId(e.getFacilityId());
        vo.setType(e.getType());
        vo.setDescription(e.getDescription());
        vo.setMaintainedBy(e.getMaintainedBy());
        vo.setMaintainedAt(e.getMaintainedAt());
        vo.setCost(e.getCost());
        vo.setRemark(e.getRemark());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
