package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.*;
import com.property.mapper.*;
import com.property.dto.request.BillDTO;
import com.property.dto.request.BillQueryDTO;
import com.property.dto.request.PaymentDTO;
import com.property.dto.request.PushDTO;
import com.property.dto.response.ArrearsVO;
import com.property.dto.response.BillDetailVO;
import com.property.dto.response.BillStatsVO;
import com.property.dto.response.BillVO;
import java.util.List;

public interface BillService {

    PageResult<BillVO> page(BillQueryDTO query);

    BillVO create(BillDTO dto);

    BillStatsVO stats();

    List<ArrearsVO> arrears(int topN);

    BillVO getById(String id);

    BillDetailVO getDetail(String id);

    void push(PushDTO dto);

    BillVO collectPayment(String id, PaymentDTO dto, String operatorId);

    BillVO voidBill(String id, String operatorId);

    BillVO payCallback(String id);
}
