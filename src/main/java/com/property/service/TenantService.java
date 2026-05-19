package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.Tenant;
import com.property.dto.request.TenantDTO;
import com.property.dto.request.TenantQueryDTO;
import com.property.dto.response.ImportPreviewVO;
import com.property.dto.response.TenantVO;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface TenantService {

    TenantVO create(TenantDTO dto);

    TenantVO getById(String id);

    TenantVO update(String id, TenantDTO dto);

    void delete(String id);

    PageResult<TenantVO> page(TenantQueryDTO query);

    List<Object> getContracts(String id);

    List<Object> getWorkOrders(String id);

    ImportPreviewVO importPreview(MultipartFile file) throws IOException;

    ImportPreviewVO importCommit(MultipartFile file) throws IOException;
}
