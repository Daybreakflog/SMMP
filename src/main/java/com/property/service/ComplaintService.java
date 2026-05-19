package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.Appeal;
import com.property.entity.Complaint;
import com.property.dto.request.AppealDTO;
import com.property.dto.request.ComplaintDTO;
import com.property.dto.request.ComplaintQueryDTO;
import com.property.dto.response.ComplaintVO;

public interface ComplaintService {

    PageResult<ComplaintVO> page(ComplaintQueryDTO query);

    ComplaintVO create(ComplaintDTO dto);

    ComplaintVO getById(String id);

    ComplaintVO patch(String id, ComplaintDTO dto);

    ComplaintVO accept(String id);

    ComplaintVO resolve(String id);

    ComplaintVO close(String id);

    void appeal(String id, AppealDTO dto);
}
