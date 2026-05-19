package com.property.service;

import com.property.entity.FeeItem;
import com.property.dto.request.FeeItemDTO;
import com.property.dto.response.FeeItemVO;
import java.util.List;

public interface FeeItemService {

    List<FeeItemVO> list();

    FeeItemVO create(FeeItemDTO dto);

    FeeItemVO patch(String id, FeeItemDTO dto);

    void delete(String id);
}
