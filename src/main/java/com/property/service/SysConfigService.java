package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.SysConfig;
import com.property.dto.request.SysConfigDTO;
import com.property.dto.request.SysConfigQueryDTO;
import com.property.dto.response.SysConfigVO;

public interface SysConfigService {

    PageResult<SysConfigVO> page(SysConfigQueryDTO query);

    SysConfigVO getByKey(String key);

    SysConfigVO create(SysConfigDTO dto);

    SysConfigVO update(String key, SysConfigDTO dto);

    void delete(String key);
}
