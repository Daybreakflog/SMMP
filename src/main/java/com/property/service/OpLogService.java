package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.OpLog;
import com.property.dto.request.OpLogQueryDTO;
import com.property.dto.response.OpLogVO;

public interface OpLogService {

    PageResult<OpLogVO> page(OpLogQueryDTO query);

    OpLogVO getById(Long id);
}
