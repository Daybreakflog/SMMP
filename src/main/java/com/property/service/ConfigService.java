package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.dto.request.ConfigDTO;
import com.property.dto.response.ConfigVO;

public interface ConfigService {

    PageResult<ConfigVO> page(int pageNum, int pageSize);

    ConfigVO create(ConfigDTO dto);

    ConfigVO update(String id, ConfigDTO dto);

    void delete(String id);
}
