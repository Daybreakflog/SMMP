package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.RepairOrder;
import com.property.dto.request.*;
import com.property.dto.response.RepairOrderVO;

public interface RepairOrderService {

    RepairOrderVO create(RepairOrderDTO dto);

    PageResult<RepairOrderVO> page(RepairOrderQueryDTO query);

    RepairOrderVO getById(String id);

    RepairOrderVO update(String id, RepairOrderDTO dto);

    void delete(String id);

    RepairOrderVO assign(String id, RepairAssignDTO dto);

    RepairOrderVO start(String id);

    RepairOrderVO complete(String id, RepairCompleteDTO dto);

    RepairOrderVO confirm(String id);

    RepairOrderVO reject(String id, RepairRejectDTO dto);

    RepairOrderVO cancel(String id);

    RepairOrderVO rate(String id, RateDTO dto);
}
