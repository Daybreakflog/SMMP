package com.property.service;

import com.property.dto.request.DictItemDTO;
import com.property.dto.response.DictItemVO;
import java.util.List;
import java.util.Map;

public interface DictService {

    Map<String, List<DictItemVO>> listGrouped();

    DictItemVO create(DictItemDTO dto);

    DictItemVO update(String id, DictItemDTO dto);

    void delete(String id);
}
