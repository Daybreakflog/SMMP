package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.Contract;
import com.property.dto.request.*;
import com.property.dto.response.ContractVO;

public interface ContractService {

    PageResult<ContractVO> page(ContractQueryDTO query);

    ContractVO getById(String id);

    ContractVO create(ContractDTO dto);

    ContractVO update(String id, ContractDTO dto);

    void delete(String id);

    ContractVO submit(String id);

    ContractVO approve(String id);

    ContractVO reject(String id, ContractRejectDTO dto);

    ContractVO terminate(String id, ContractTerminateDTO dto);

    ContractVO renew(String id, ContractRenewDTO dto);
}
