package com.property.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.entity.WorkOrder;
import com.property.dto.response.MonthCountVO;
import com.property.dto.response.StatusCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    @Select("SELECT status, COUNT(*) AS count FROM work_orders WHERE deleted = 0 GROUP BY status")
    List<StatusCountVO> countByStatus();

    @Select("""
            SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS count
            FROM work_orders
            WHERE deleted = 0
            GROUP BY month
            ORDER BY month DESC
            LIMIT 12
            """)
    List<MonthCountVO> countByMonth();
}
