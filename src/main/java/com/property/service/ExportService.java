package com.property.service;

import com.property.entity.ExportTask;
import com.property.dto.request.ExportRequestDTO;
import com.property.dto.response.ExportTaskVO;

public interface ExportService {

    ExportTaskVO create(ExportRequestDTO dto);

    ExportTaskVO getById(String id);

    String getDownloadUrl(String id);
}
