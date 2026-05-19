package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.Activity;
import com.property.dto.request.ActivityDTO;
import com.property.dto.request.ActivityQueryDTO;
import com.property.dto.response.ActivityVO;
import com.property.dto.response.ParticipantVO;

public interface ActivityService {

    PageResult<ActivityVO> page(ActivityQueryDTO query);

    ActivityVO getById(String id);

    ActivityVO create(ActivityDTO dto);

    ActivityVO update(String id, ActivityDTO dto);

    ActivityVO publish(String id);

    ActivityVO close(String id);

    void delete(String id);

    void register(String id);

    void cancelRegister(String id);

    PageResult<ParticipantVO> participants(String id, long page, long pageSize);
}
