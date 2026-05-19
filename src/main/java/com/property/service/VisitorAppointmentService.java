package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.VisitorAppointment;
import com.property.dto.request.VisitorAppointmentDTO;
import com.property.dto.request.VisitorAppointmentQueryDTO;
import com.property.dto.response.VisitorAppointmentVO;

public interface VisitorAppointmentService {

    PageResult<VisitorAppointmentVO> page(VisitorAppointmentQueryDTO query);

    VisitorAppointmentVO getById(String id);

    VisitorAppointmentVO create(VisitorAppointmentDTO dto);

    VisitorAppointmentVO approve(String id);

    VisitorAppointmentVO reject(String id);

    VisitorAppointmentVO checkIn(String id);

    VisitorAppointmentVO checkOut(String id);

    void cancel(String id);
}
