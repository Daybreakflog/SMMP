package com.property.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.entity.Bill;
import com.property.dto.response.ArrearsVO;
import com.property.dto.response.BillStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

import java.util.List;

@Mapper
public interface BillMapper extends BaseMapper<Bill> {

    @Select("""
            SELECT
              COALESCE(SUM(total_amount), 0)              AS receivable,
              COALESCE(SUM(paid_amount), 0)               AS received,
              COALESCE(SUM(total_amount - paid_amount), 0) AS arrears
            FROM bills
            WHERE deleted = 0
              AND status != 'VOID'
            """)
    BillStatsVO stats();

    @Select("""
            SELECT b.tenant_id AS tenantId,
                   t.name      AS tenantName,
                   SUM(b.total_amount - b.paid_amount) AS arrears
            FROM bills b
            LEFT JOIN tenants t ON b.tenant_id = t.id AND t.deleted = 0
            WHERE b.status IN ('UNPAID','PARTIAL','OVERDUE')
              AND b.deleted = 0
            GROUP BY b.tenant_id, t.name
            ORDER BY arrears DESC
            LIMIT #{topN}
            """)
    List<ArrearsVO> topArrears(@Param("topN") int topN);

    @Select("""
            SELECT COALESCE(SUM(total_amount), 0) FROM bills
            WHERE deleted = 0 AND status != 'VOID'
            """)
    BigDecimal sumTotalAmount();

    @Select("""
            SELECT COALESCE(SUM(total_amount - paid_amount), 0) FROM bills
            WHERE deleted = 0 AND status IN ('UNPAID','PARTIAL','OVERDUE')
            """)
    BigDecimal sumArrears();

    @Select("""
            SELECT COUNT(*) FROM bills
            WHERE deleted = 0
              AND DATE_FORMAT(created_at, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')
            """)
    Long countMonthlyNew();
}
