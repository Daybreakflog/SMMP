package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.Facility;
import com.property.entity.FacilityMaintenanceRecord;
import com.property.dto.request.FacilityDTO;
import com.property.dto.request.FacilityMaintenanceRecordDTO;
import com.property.dto.request.FacilityQueryDTO;
import com.property.dto.request.MaintenanceRecordQueryDTO;
import com.property.dto.response.FacilityMaintenanceRecordVO;
import com.property.dto.response.FacilityVO;

public interface FacilityService {

    PageResult<FacilityVO> page(FacilityQueryDTO query);

    FacilityVO getById(String id);

    FacilityVO create(FacilityDTO dto);

    FacilityVO update(String id, FacilityDTO dto);

    void delete(String id);

    FacilityVO maintenance(String id);

    FacilityVO restore(String id);

    FacilityVO scrap(String id);

    FacilityMaintenanceRecordVO addRecord(String facilityId, FacilityMaintenanceRecordDTO dto);

    PageResult<FacilityMaintenanceRecordVO> recordPage(String facilityId, MaintenanceRecordQueryDTO query);
}
