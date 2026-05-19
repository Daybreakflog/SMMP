package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.WoAttachment;
import com.property.entity.WoMessage;
import com.property.entity.WorkOrder;
import com.property.dto.request.WorkOrderAssignDTO;
import com.property.dto.request.AttachmentDTO;
import com.property.dto.request.MessageDTO;
import com.property.dto.request.WorkOrderDTO;
import com.property.dto.request.WorkOrderQueryDTO;
import com.property.dto.response.WoAttachmentVO;
import com.property.dto.response.WoMessageVO;
import com.property.dto.response.WorkOrderVO;
import java.util.List;

public interface WorkOrderService {

    PageResult<WorkOrderVO> page(WorkOrderQueryDTO query);

    WorkOrderVO create(WorkOrderDTO dto);

    WorkOrderVO getById(String id);

    WorkOrderVO patch(String id, WorkOrderDTO dto);

    WorkOrderVO assign(String id, WorkOrderAssignDTO dto);

    WorkOrderVO start(String id);

    WorkOrderVO complete(String id);

    WorkOrderVO close(String id);

    WorkOrderVO reopen(String id);

    WoMessageVO addMessage(String id, MessageDTO dto);

    List<WoMessageVO> listMessages(String id);

    WoAttachmentVO addAttachment(String id, AttachmentDTO dto);
}
