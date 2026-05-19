package com.property.service.impl;

import com.property.service.ParkingSpaceService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.ParkingSpace;
import com.property.mapper.ParkingSpaceMapper;
import com.property.dto.request.ParkingSpaceAssignDTO;
import com.property.dto.request.ParkingSpaceDTO;
import com.property.dto.request.ParkingSpaceQueryDTO;
import com.property.dto.response.ParkingSpaceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingSpaceServiceImpl implements ParkingSpaceService {

    private final ParkingSpaceMapper parkingSpaceMapper;

    public PageResult<ParkingSpaceVO> page(ParkingSpaceQueryDTO query) {
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<ParkingSpace>()
                .eq(StrUtil.isNotBlank(query.getStatus()), ParkingSpace::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getZone()), ParkingSpace::getZone, query.getZone())
                .orderByAsc(ParkingSpace::getZone)
                .orderByAsc(ParkingSpace::getSpaceNo);
        Page<ParkingSpace> raw = parkingSpaceMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<ParkingSpaceVO> vos = raw.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    public ParkingSpaceVO getById(String id) {
        return toVO(requireExist(id));
    }

    @Transactional
    public ParkingSpaceVO create(ParkingSpaceDTO dto) {
        checkDuplicate(dto.getZone(), dto.getSpaceNo(), null);
        ParkingSpace entity = new ParkingSpace();
        entity.setSpaceNo(dto.getSpaceNo());
        entity.setZone(dto.getZone());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "AVAILABLE");
        entity.setRemark(dto.getRemark());
        parkingSpaceMapper.insert(entity);
        return toVO(entity);
    }

    @Transactional
    public ParkingSpaceVO update(String id, ParkingSpaceDTO dto) {
        ParkingSpace entity = requireExist(id);
        if (dto.getSpaceNo() != null || dto.getZone() != null) {
            String newZone = dto.getZone() != null ? dto.getZone() : entity.getZone();
            String newSpaceNo = dto.getSpaceNo() != null ? dto.getSpaceNo() : entity.getSpaceNo();
            checkDuplicate(newZone, newSpaceNo, id);
            entity.setZone(newZone);
            entity.setSpaceNo(newSpaceNo);
        }
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getRemark() != null) entity.setRemark(dto.getRemark());
        parkingSpaceMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public void delete(String id) {
        ParkingSpace entity = requireExist(id);
        if (!"AVAILABLE".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.PARKING_STATUS_ILLEGAL);
        }
        parkingSpaceMapper.deleteById(id);
    }

    @Transactional
    public ParkingSpaceVO assign(String id, ParkingSpaceAssignDTO dto) {
        ParkingSpace entity = requireExist(id);
        if (!"AVAILABLE".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.PARKING_STATUS_ILLEGAL);
        }
        entity.setStatus("OCCUPIED");
        entity.setOwnerId(dto.getOwnerId());
        entity.setOwnerName(dto.getOwnerName());
        entity.setVehiclePlate(dto.getVehiclePlate());
        parkingSpaceMapper.updateById(entity);
        return toVO(entity);
    }

    @Transactional
    public ParkingSpaceVO release(String id) {
        ParkingSpace entity = requireExist(id);
        if (!"OCCUPIED".equals(entity.getStatus())) {
            throw new BusinessException(ErrorCode.PARKING_STATUS_ILLEGAL);
        }
        LambdaUpdateWrapper<ParkingSpace> wrapper = new LambdaUpdateWrapper<ParkingSpace>()
                .eq(ParkingSpace::getId, id)
                .set(ParkingSpace::getStatus, "AVAILABLE")
                .set(ParkingSpace::getOwnerId, null)
                .set(ParkingSpace::getOwnerName, null)
                .set(ParkingSpace::getVehiclePlate, null);
        parkingSpaceMapper.update(null, wrapper);
        entity.setStatus("AVAILABLE");
        entity.setOwnerId(null);
        entity.setOwnerName(null);
        entity.setVehiclePlate(null);
        return toVO(entity);
    }

    public List<ParkingSpaceVO> my() {
        String currentUserId = StpUtil.getLoginIdAsString();
        List<ParkingSpace> list = parkingSpaceMapper.selectList(
                new LambdaQueryWrapper<ParkingSpace>()
                        .eq(ParkingSpace::getOwnerId, currentUserId)
                        .orderByAsc(ParkingSpace::getZone)
                        .orderByAsc(ParkingSpace::getSpaceNo));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    // ── private helpers ─────────────────────────────────────────────

    private ParkingSpace requireExist(String id) {
        ParkingSpace entity = parkingSpaceMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.PARKING_NOT_FOUND);
        return entity;
    }

    private void checkDuplicate(String zone, String spaceNo, String excludeId) {
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<ParkingSpace>()
                .eq(ParkingSpace::getZone, zone)
                .eq(ParkingSpace::getSpaceNo, spaceNo)
                .ne(excludeId != null, ParkingSpace::getId, excludeId);
        if (parkingSpaceMapper.exists(wrapper)) {
            throw new BusinessException(ErrorCode.PARKING_SPACE_DUPLICATE);
        }
    }

    private ParkingSpaceVO toVO(ParkingSpace e) {
        ParkingSpaceVO vo = new ParkingSpaceVO();
        vo.setId(e.getId());
        vo.setSpaceNo(e.getSpaceNo());
        vo.setZone(e.getZone());
        vo.setStatus(e.getStatus());
        vo.setOwnerId(e.getOwnerId());
        vo.setOwnerName(e.getOwnerName());
        vo.setVehiclePlate(e.getVehiclePlate());
        vo.setRemark(e.getRemark());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
