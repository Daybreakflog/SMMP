package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.dto.request.TemplateDTO;
import com.property.dto.request.TemplateQueryDTO;
import com.property.dto.response.TemplateVO;

public interface TemplateService {

    PageResult<TemplateVO> page(TemplateQueryDTO query);

    TemplateVO create(TemplateDTO dto);

    TemplateVO update(String id, TemplateDTO dto);

    void delete(String id);
}
