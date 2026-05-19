package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.ParkingSpace;
import com.property.dto.request.ParkingSpaceAssignDTO;
import com.property.dto.request.ParkingSpaceDTO;
import com.property.dto.request.ParkingSpaceQueryDTO;
import com.property.dto.response.ParkingSpaceVO;
import java.util.List;

public interface ParkingSpaceService {

    PageResult<ParkingSpaceVO> page(ParkingSpaceQueryDTO query);

    ParkingSpaceVO getById(String id);

    ParkingSpaceVO create(ParkingSpaceDTO dto);

    ParkingSpaceVO update(String id, ParkingSpaceDTO dto);

    void delete(String id);

    ParkingSpaceVO assign(String id, ParkingSpaceAssignDTO dto);

    ParkingSpaceVO release(String id);

    List<ParkingSpaceVO> my();
}
