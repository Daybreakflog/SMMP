package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.SysDictItem;
import com.property.dto.request.SysDictItemDTO;
import com.property.dto.request.SysDictItemQueryDTO;
import com.property.dto.response.SysDictItemVO;
import java.util.List;

public interface SysDictItemService {

    PageResult<SysDictItemVO> page(SysDictItemQueryDTO query);

    List<SysDictItemVO> listByType(String type);

    SysDictItemVO create(SysDictItemDTO dto);

    SysDictItemVO update(String id, SysDictItemDTO dto);

    void delete(String id);
}
