package com.property.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.property.entity.Complaint;
import com.property.dto.response.CategoryCountVO;
import com.property.dto.response.StatusCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ComplaintMapper extends BaseMapper<Complaint> {

    @Select("SELECT status, COUNT(*) AS count FROM complaints WHERE deleted = 0 GROUP BY status")
    List<StatusCountVO> countByStatus();

    @Select("SELECT category, COUNT(*) AS count FROM complaints WHERE deleted = 0 GROUP BY category")
    List<CategoryCountVO> countByCategory();
}
