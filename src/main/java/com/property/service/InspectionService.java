package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.InspectionPlan;
import com.property.entity.InspectionTask;
import com.property.dto.request.*;
import com.property.dto.response.InspectionPlanVO;
import com.property.dto.response.InspectionTaskVO;

public interface InspectionService {

    PageResult<InspectionPlanVO> planPage(InspectionPlanQueryDTO query);

    InspectionPlanVO getPlanById(String id);

    InspectionPlanVO createPlan(InspectionPlanDTO dto);

    InspectionPlanVO updatePlan(String id, InspectionPlanDTO dto);

    void deletePlan(String id);

    InspectionPlanVO activatePlan(String id);

    InspectionPlanVO disablePlan(String id);

    InspectionTaskVO createTask(String planId, InspectionTaskCreateDTO dto);

    PageResult<InspectionTaskVO> taskPage(InspectionTaskQueryDTO query);

    InspectionTaskVO getTaskById(String id);

    InspectionTaskVO startTask(String id);

    InspectionTaskVO completeTask(String id, InspectionTaskCompleteDTO dto);
}
